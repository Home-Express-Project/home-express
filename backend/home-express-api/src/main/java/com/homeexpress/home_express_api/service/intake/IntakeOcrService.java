package com.homeexpress.home_express_api.service.intake;

import com.homeexpress.home_express_api.dto.intake.IntakeOcrResponse;
import com.homeexpress.home_express_api.dto.intake.ItemCandidateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Stub OCR processing service that generates intake candidates from uploaded images.
 *
 * <p>This implementation is intentionally lightweight and deterministic so that the
 * frontend can integrate with the backend without depending on third-party OCR
 * providers during development. Once the actual OCR provider is ready, replace the
 * placeholder logic inside {@link #processOcrImages(List)} with real OCR parsing.</p>
 */
@Slf4j
@Service
public class IntakeOcrService {

    public IntakeOcrResponse processOcrImages(List<MultipartFile> images) {
        long startedAt = System.currentTimeMillis();

        List<ItemCandidateDto> candidates = new ArrayList<>();
        StringBuilder extractedTextBuilder = new StringBuilder();

        for (int index = 0; index < images.size(); index++) {
            MultipartFile image = images.get(index);
            String originalName = image.getOriginalFilename();
            String normalizedName = normalizeName(originalName, index);

            extractedTextBuilder
                .append("Image ")
                .append(index + 1)
                .append(": ")
                .append(normalizedName)
                .append(System.lineSeparator());

            ItemCandidateDto candidate = ItemCandidateDto.builder()
                .id("ocr-" + UUID.randomUUID())
                .name(normalizedName)
                .quantity(1)
                .source("ocr")
                .confidence(0.75)
                .metadata(buildMetadata(image, normalizedName, index))
                .build();

            candidates.add(candidate);
        }

        String extractedText = extractedTextBuilder.length() > 0
            ? extractedTextBuilder.toString().trim()
            : "No textual content detected in uploaded images.";

        log.info("OCR stub processed {} image(s) in {} ms", images.size(), System.currentTimeMillis() - startedAt);

        return IntakeOcrResponse.builder()
            .success(true)
            .data(IntakeOcrResponse.OcrData.builder()
                .extractedText(extractedText)
                .candidates(candidates)
                .build())
            .build();
    }

    private String normalizeName(String fileName, int index) {
        if (!StringUtils.hasText(fileName)) {
            return "Uploaded item " + (index + 1);
        }

        String baseName = fileName;
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            baseName = fileName.substring(0, lastDot);
        }

        baseName = baseName.replaceAll("[^A-Za-z0-9\\s-]", " ");
        baseName = baseName.replaceAll("\\s+", " ").trim();

        if (!StringUtils.hasText(baseName)) {
            baseName = "Uploaded item " + (index + 1);
        }

        return StringUtils.capitalize(baseName.toLowerCase(Locale.ENGLISH));
    }

    private Object buildMetadata(MultipartFile file, String normalizedName, int index) {
        return new Metadata(
            normalizedName,
            file.getOriginalFilename(),
            file.getSize(),
            file.getContentType(),
            index + 1,
            Instant.now().toString()
        );
    }

    private record Metadata(
        String label,
        String originalFileName,
        long sizeBytes,
        String contentType,
        int order,
        String processedAt
    ) {
    }
}
