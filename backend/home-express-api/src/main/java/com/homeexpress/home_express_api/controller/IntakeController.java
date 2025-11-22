package com.homeexpress.home_express_api.controller;

import com.homeexpress.home_express_api.dto.ai.DetectionResult;
import com.homeexpress.home_express_api.dto.ai.DetectedItem;
import com.homeexpress.home_express_api.dto.ai.EnhancedDetectedItem;
import com.homeexpress.home_express_api.dto.intake.IntakeImageAnalysisResponse;
import com.homeexpress.home_express_api.dto.intake.IntakeMergeRequest;
import com.homeexpress.home_express_api.dto.intake.IntakeMergeResponse;
import com.homeexpress.home_express_api.dto.intake.IntakeOcrResponse;
import com.homeexpress.home_express_api.dto.intake.IntakeParseTextRequest;
import com.homeexpress.home_express_api.dto.intake.IntakeParseTextResponse;
import com.homeexpress.home_express_api.dto.intake.ItemCandidateDto;
import com.homeexpress.home_express_api.dto.intake.ItemCandidateDto.DimensionsDto;
import com.homeexpress.home_express_api.service.ai.HybridAIDetectionOrchestrator;
import com.homeexpress.home_express_api.service.intake.IntakeOcrService;
import com.homeexpress.home_express_api.service.intake.IntakeSessionService;
import com.homeexpress.home_express_api.service.intake.IntakeTextParsingService;
import com.homeexpress.home_express_api.service.intake.IntakeAIParsingService;
import com.homeexpress.home_express_api.service.intake.ItemDetectionPostProcessor;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Controller for handling item intake operations
 */
@RestController
@RequestMapping("/api/v1/intake")
@RequiredArgsConstructor
public class IntakeController {

    private static final Logger logger = LoggerFactory.getLogger(IntakeController.class);

    private final HybridAIDetectionOrchestrator detectionOrchestrator;
    private final IntakeOcrService intakeOcrService;
    private final IntakeTextParsingService textParsingService;
    private final IntakeAIParsingService aiParsingService;
    private final IntakeSessionService sessionService;
    private final ItemDetectionPostProcessor itemDetectionPostProcessor;
    private static final List<IntakeController.DocumentItemTemplate> DOCUMENT_TEMPLATES = List.of(
        new DocumentItemTemplate("sofa", "Sofa", "Living Room Furniture", 0.82, 1),
        new DocumentItemTemplate("bed", "Queen Bed", "Bedroom Furniture", 0.8, 1),
        new DocumentItemTemplate("table", "Dining Table", "Dining Furniture", 0.78, 1),
        new DocumentItemTemplate("chair", "Dining Chair", "Dining Furniture", 0.72, 4),
        new DocumentItemTemplate("dresser", "Dresser", "Bedroom Storage", 0.7, 1),
        new DocumentItemTemplate("box", "Moving Boxes", "Packing Supplies", 0.65, 10),
        new DocumentItemTemplate("tv", "Television", "Electronics", 0.68, 1)
    );

    /**
     * Merge item candidates into an intake session
     * 
     * @param sessionId The session ID (timestamp from frontend)
     * @param request The intake merge request containing item candidates
     * @return Response with session info
     */
    @PostMapping("/merge")
    public ResponseEntity<?> mergeItems(
            @RequestParam String sessionId,
            @Valid @RequestBody IntakeMergeRequest request) {
        
        try {
            logger.info("Merging {} items into session {}", 
                request.getCandidates().size(), sessionId);
            
            sessionService.createOrGetSession(sessionId, null);
            sessionService.saveItems(sessionId, request.getCandidates(), null, null);
            
            IntakeMergeResponse response = IntakeMergeResponse.builder()
                .sessionId(sessionId)
                .itemCount(request.getCandidates().size())
                .message("Items saved successfully to database")
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error merging items for session {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to save items: " + e.getMessage()));
        }
    }

    /**
     * Parse free-form text into potential item candidates.
     *
     * @param request Body containing the text to parse
     * @return Structured candidates detected in the text
     */
    @PostMapping("/parse-text")
    public ResponseEntity<?> parseText(@Valid @RequestBody IntakeParseTextRequest request) {
        try {
            // Try AI parsing first
            List<IntakeParseTextResponse.ParsedItem> aiCandidates = aiParsingService.parseWithAI(request.getText());
            
            if (!aiCandidates.isEmpty()) {
                IntakeParseTextResponse response = IntakeParseTextResponse.builder()
                    .success(true)
                    .data(IntakeParseTextResponse.ParseTextData.builder()
                        .candidates(aiCandidates)
                        .warnings(List.of())
                        .metadata(Map.of("parser", "ai", "items_detected", aiCandidates.size()))
                        .build())
                    .build();
                return ResponseEntity.ok(response);
            }
            
            // Fallback to heuristic parsing
            var result = textParsingService.parse(request.getText());
            IntakeParseTextResponse response = IntakeParseTextResponse.builder()
                .success(true)
                .data(IntakeParseTextResponse.ParseTextData.builder()
                    .candidates(result.getCandidates())
                    .warnings(result.getWarnings())
                    .metadata(Map.of("parser", "heuristic", "items_detected", result.getCandidates().size()))
                    .build())
                .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error parsing intake text: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to parse text: " + e.getMessage()));
        }
    }

    /**
     * Analyze uploaded images and return item candidates inferred by AI services.
     *
     * @param images List of uploaded image files
     * @return AI generated intake candidates
     */
    @PostMapping(value = "/analyze-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analyzeImages(@RequestParam("images") List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", "At least one image is required"
                ));
        }

        // Validate image count
        if (images.size() > 10) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", "Maximum 10 images allowed per request"
                ));
        }

        // Validate each image
        for (int i = 0; i < images.size(); i++) {
            MultipartFile image = images.get(i);
            if (image == null || image.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", String.format("Image %d is empty or invalid", i + 1)
                    ));
            }

            // Validate file size (max 10MB)
            long maxSize = 10 * 1024 * 1024; // 10MB
            if (image.getSize() > maxSize) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", String.format("Image %s exceeds maximum size of 10MB (current: %.2fMB)", 
                            image.getOriginalFilename(), image.getSize() / (1024.0 * 1024.0))
                    ));
            }

            // Validate content type
            String contentType = image.getContentType();
            if (contentType == null || 
                (!contentType.startsWith("image/jpeg") && 
                 !contentType.startsWith("image/png") && 
                 !contentType.startsWith("image/webp"))) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", String.format("Image %s has invalid format. Only JPG, PNG, and WebP are allowed.", 
                            image.getOriginalFilename())
                    ));
            }
        }

        try {
            logger.info("Analyzing {} intake images", images.size());

            // Convert MultipartFile images to base64 data URIs for AI analysis
            List<String> imageDataUris = IntStream.range(0, images.size())
                .mapToObj(index -> convertToDataUri(images.get(index)))
                .collect(Collectors.toList());

            DetectionResult detectionResult = detectionOrchestrator.detectItemsHybrid(imageDataUris);
            List<ItemCandidateDto> candidates = mapDetectionResult(detectionResult);
            
            // Post-process: aggregate similar items and normalize names
            candidates = itemDetectionPostProcessor.processAndAggregate(candidates);

            IntakeImageAnalysisResponse response = IntakeImageAnalysisResponse.builder()
                .success(true)
                .data(IntakeImageAnalysisResponse.AnalysisData.builder()
                    .candidates(candidates)
                    .metadata(buildMetadata(detectionResult))
                    .build())
                .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to analyze intake images: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Failed to analyze images: " + e.getMessage()
                ));
        }
    }
    
    /**
     * Parse structured documents (PDF, DOCX, XLSX) and return inferred item candidates.
     *
     * @param document Multipart file containing the document to parse
     * @return Candidates extracted from the document content or filename
     */
    @PostMapping(value = "/parse-document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> parseDocument(@RequestParam("document") MultipartFile document) {
        if (document == null || document.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", "A document file is required"
                ));
        }

        long startTime = System.currentTimeMillis();

        try {
            logger.info("Parsing intake document {} ({} bytes)", document.getOriginalFilename(), document.getSize());
            DocumentParseResult parseResult = parseDocumentCandidates(document);
            List<ItemCandidateDto> candidates = parseResult.getCandidates();
            boolean fallbackUsed = parseResult.isFallbackUsed();

            double averageConfidence = candidates.stream()
                .map(ItemCandidateDto::getConfidence)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(Double.NaN);

            IntakeImageAnalysisResponse response = IntakeImageAnalysisResponse.builder()
                .success(true)
                .data(IntakeImageAnalysisResponse.AnalysisData.builder()
                    .candidates(candidates)
                    .metadata(IntakeImageAnalysisResponse.AnalysisMetadata.builder()
                        .serviceUsed("DOCUMENT_KEYWORD_STUB")
                        .confidence(Double.isNaN(averageConfidence) ? null : averageConfidence)
                        .fallbackUsed(fallbackUsed)
                        .manualReviewRequired(fallbackUsed)
                        .manualInputRequired(fallbackUsed)
                        .imageCount(0)
                        .processingTimeMs(System.currentTimeMillis() - startTime)
                        .fromCache(false)
                        .detectedItemCount(candidates.size())
                        .enhancedItemCount(candidates.size())
                        .build())
                    .build())
                .build();

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            logger.error("Failed to parse intake document {}: {}", document.getOriginalFilename(), ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Failed to parse document: " + ex.getMessage()
                ));
        }
    }
    
    /**
     * Perform OCR on uploaded images and generate item candidates.
     *
     * @param images Array of uploaded image files bound to the "images" part
     * @return OCR extraction result with synthesized candidates
     */
    @PostMapping(value = "/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> processOcr(@RequestParam("images") MultipartFile[] images) {
        if (images == null || images.length == 0) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "error", "At least one image is required for OCR processing"
                ));
        }

        try {
            List<MultipartFile> validImages = Arrays.stream(images)
                .filter(Objects::nonNull)
                .filter(image -> !image.isEmpty())
                .toList();

            if (validImages.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "error", "Uploaded images are empty or invalid"
                    ));
            }

            logger.info("Processing OCR for {} intake images", validImages.size());

            IntakeOcrResponse response = intakeOcrService.processOcrImages(validImages);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            logger.error("Failed to process OCR images: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "error", "Failed to process OCR images"
                ));
        }
    }
    
    private DocumentParseResult parseDocumentCandidates(MultipartFile document) {
        String sourceFile = document.getOriginalFilename();
        String documentText = readDocumentAsText(document).toLowerCase(Locale.ENGLISH);

        StringBuilder searchSpaceBuilder = new StringBuilder();
        if (sourceFile != null) {
            searchSpaceBuilder.append(sourceFile.toLowerCase(Locale.ENGLISH)).append(' ');
        }
        searchSpaceBuilder.append(documentText);
        String searchSpace = searchSpaceBuilder.toString();

        List<ItemCandidateDto> candidates = new ArrayList<>();
        for (DocumentItemTemplate template : DOCUMENT_TEMPLATES) {
            if (searchSpace.contains(template.getKeyword())) {
                candidates.add(createCandidateFromTemplate(template, sourceFile));
            }
        }

        boolean fallbackUsed = candidates.isEmpty();
        if (fallbackUsed) {
            candidates.add(buildFallbackCandidate(sourceFile));
        }

        return new DocumentParseResult(candidates, fallbackUsed);
    }

    private ItemCandidateDto createCandidateFromTemplate(DocumentItemTemplate template, String sourceFile) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("matchedKeyword", template.getKeyword());
        if (sourceFile != null && !sourceFile.isBlank()) {
            metadata.put("sourceFile", sourceFile);
        }

        return ItemCandidateDto.builder()
            .id(UUID.randomUUID().toString())
            .name(template.getName())
            .categoryName(template.getCategory())
            .quantity(template.getDefaultQuantity())
            .source("document")
            .confidence(template.getConfidence())
            .notes("Parsed from document keyword: " + template.getKeyword())
            .metadata(metadata)
            .build();
    }

    private ItemCandidateDto buildFallbackCandidate(String sourceFile) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("fallbackReason", "No matching keywords found");
        if (sourceFile != null && !sourceFile.isBlank()) {
            metadata.put("sourceFile", sourceFile);
        }

        return ItemCandidateDto.builder()
            .id(UUID.randomUUID().toString())
            .name("Document Review Needed")
            .categoryName("Unclassified")
            .quantity(1)
            .source("document")
            .confidence(0.4)
            .notes("Document parsed but no known keywords matched. Please review manually.")
            .metadata(metadata)
            .build();
    }

    private String readDocumentAsText(MultipartFile document) {
        try {
            byte[] bytes = document.getBytes();
            if (bytes.length == 0) {
                return "";
            }
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            logger.warn("Unable to read document {} contents: {}", document.getOriginalFilename(), ex.getMessage());
            return "";
        }
    }
    
    /**
     * Get items from an intake session
     * 
     * @param sessionId The session ID
     * @return The stored items
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<?> getSessionItems(@PathVariable String sessionId) {
        
        var sessionOpt = sessionService.getSession(sessionId);
        
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Session not found"));
        }
        
        var items = sessionService.getSessionItems(sessionId);
        
        return ResponseEntity.ok(Map.of(
            "sessionId", sessionId,
            "items", items,
            "count", items.size()
        ));
    }

    /**
     * Convert MultipartFile to base64 data URI for AI analysis
     * Format: data:image/jpeg;base64,<base64-encoded-data>
     */
    private String convertToDataUri(MultipartFile file) {
        try {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                contentType = "image/jpeg"; // Default fallback
            }
            
            byte[] imageBytes = file.getBytes();
            String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);
            
            return "data:" + contentType + ";base64," + base64Image;
        } catch (IOException e) {
            logger.error("Failed to convert image to data URI: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process image: " + e.getMessage(), e);
        }
    }

    private List<ItemCandidateDto> mapDetectionResult(DetectionResult detectionResult) {
        if (detectionResult == null) {
            return List.of();
        }

        List<EnhancedDetectedItem> enhancedItems = detectionResult.getEnhancedItems();
        if (enhancedItems != null && !enhancedItems.isEmpty()) {
            return enhancedItems.stream()
                .map(item -> fromEnhancedItem(item, detectionResult))
                .collect(Collectors.toCollection(ArrayList::new));
        }

        List<DetectedItem> basicItems = detectionResult.getItems();
        if (basicItems != null && !basicItems.isEmpty()) {
            return basicItems.stream()
                .map(item -> fromDetectedItem(item, detectionResult))
                .collect(Collectors.toCollection(ArrayList::new));
        }

        return List.of();
    }

    private ItemCandidateDto fromEnhancedItem(EnhancedDetectedItem item, DetectionResult detectionResult) {
        if (item == null) {
            return ItemCandidateDto.builder().build();
        }

        DimensionsDto dimensions = null;
        if (item.getDimsCm() != null) {
            dimensions = DimensionsDto.builder()
                .widthCm(toDouble(item.getDimsCm().getWidth()))
                .heightCm(toDouble(item.getDimsCm().getHeight()))
                .depthCm(toDouble(item.getDimsCm().getLength()))
                .build();
        }

        Map<String, Object> metadata = new HashMap<>();
        putIfNotNull(metadata, "subcategory", item.getSubcategory());
        putIfNotNull(metadata, "roomHint", item.getRoomHint());
        putIfNotNull(metadata, "materials", item.getMaterial());
        putIfNotNull(metadata, "bbox", item.getBboxNorm());
        putIfNotNull(metadata, "weightBasis", item.getWeightBasis());
        putIfNotNull(metadata, "dimensionsBasis", item.getDimensionsBasis());
        putIfNotNull(metadata, "imageIndex", item.getImageIndex());
        putIfNotNull(metadata, "sourceService", detectionResult.getServiceUsed());

        return ItemCandidateDto.builder()
            .id(item.getId() != null ? item.getId() : UUID.randomUUID().toString())
            .name(item.getName())
            .categoryName(item.getCategory())
            .quantity(item.getQuantity() != null && item.getQuantity() > 0 ? item.getQuantity() : 1)
            .isFragile(item.getFragile())
            .requiresDisassembly(item.getDisassemblyRequired())
            .requiresPackaging(Boolean.TRUE.equals(item.getFragile()))
            .source("image")
            .confidence(item.getConfidence())
            .weightKg(item.getWeightKg())
            .dimensions(dimensions)
            .notes(item.getNotes())
            .metadata(metadata)
            .build();
    }

    private ItemCandidateDto fromDetectedItem(DetectedItem item, DetectionResult detectionResult) {
        if (item == null) {
            return ItemCandidateDto.builder().build();
        }

        DimensionsDto dimensions = null;
        if (item.getDimensions() != null) {
            dimensions = DimensionsDto.builder()
                .widthCm(toDouble(item.getDimensions().getWidth()))
                .heightCm(toDouble(item.getDimensions().getHeight()))
                .depthCm(toDouble(item.getDimensions().getDepth()))
                .build();
        }

        Map<String, Object> metadata = new HashMap<>();
        putIfNotNull(metadata, "rawLabel", item.getRawLabel());
        putIfNotNull(metadata, "imageIndex", item.getImageIndex());
        putIfNotNull(metadata, "sourceService", detectionResult.getServiceUsed());

        return ItemCandidateDto.builder()
            .id(UUID.randomUUID().toString())
            .name(item.getName())
            .categoryName(item.getCategory())
            .quantity(1)
            .source("image")
            .confidence(item.getConfidence())
            .dimensions(dimensions)
            .metadata(metadata)
            .build();
    }

    private IntakeImageAnalysisResponse.AnalysisMetadata buildMetadata(DetectionResult detectionResult) {
        if (detectionResult == null) {
            return IntakeImageAnalysisResponse.AnalysisMetadata.builder()
                .detectedItemCount(0)
                .enhancedItemCount(0)
                .build();
        }

        return IntakeImageAnalysisResponse.AnalysisMetadata.builder()
            .serviceUsed(detectionResult.getServiceUsed())
            .confidence(detectionResult.getConfidence())
            .fallbackUsed(detectionResult.getFallbackUsed())
            .manualReviewRequired(detectionResult.getManualReviewRequired())
            .manualInputRequired(detectionResult.getManualInputRequired())
            .imageCount(detectionResult.getImageCount())
            .processingTimeMs(detectionResult.getProcessingTimeMs())
            .fromCache(detectionResult.getFromCache())
            .detectedItemCount(detectionResult.getItems() != null ? detectionResult.getItems().size() : 0)
            .enhancedItemCount(detectionResult.getEnhancedItems() != null ? detectionResult.getEnhancedItems().size() : 0)
            .build();
    }

    private void putIfNotNull(Map<String, Object> metadata, String key, Object value) {
        if (value != null) {
            metadata.put(key, value);
        }
    }

    private Double toDouble(Number value) {
        return value != null ? value.doubleValue() : null;
    }

    private static class DocumentParseResult {
        private final List<ItemCandidateDto> candidates;
        private final boolean fallbackUsed;

        DocumentParseResult(List<ItemCandidateDto> candidates, boolean fallbackUsed) {
            this.candidates = candidates;
            this.fallbackUsed = fallbackUsed;
        }

        List<ItemCandidateDto> getCandidates() {
            return candidates;
        }

        boolean isFallbackUsed() {
            return fallbackUsed;
        }
    }

    private static class DocumentItemTemplate {
        private final String keyword;
        private final String name;
        private final String category;
        private final double confidence;
        private final int defaultQuantity;

        DocumentItemTemplate(String keyword, String name, String category, double confidence, int defaultQuantity) {
            this.keyword = keyword;
            this.name = name;
            this.category = category;
            this.confidence = confidence;
            this.defaultQuantity = defaultQuantity;
        }

        String getKeyword() {
            return keyword;
        }

        String getName() {
            return name;
        }

        String getCategory() {
            return category;
        }

        double getConfidence() {
            return confidence;
        }

        int getDefaultQuantity() {
            return defaultQuantity;
        }
    }
}
