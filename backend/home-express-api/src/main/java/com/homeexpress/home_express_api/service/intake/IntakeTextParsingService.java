package com.homeexpress.home_express_api.service.intake;

import com.homeexpress.home_express_api.dto.intake.IntakeParseTextResponse;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple heuristic-based parser that converts free-form text into item candidates.
 * This is meant as a lightweight fallback when AI services are not available.
 */
@Slf4j
@Service
public class IntakeTextParsingService {

    private static final Pattern BULLET_PREFIX = Pattern.compile("^(?:[\\-\\*\\+â€¢Â·\\u2022\\s]+|\\d+[\\).\\-\\s]+)+");
    private static final Pattern QUANTITY_SUFFIX = Pattern.compile(
            "(?iu)(?:x|Ã—)?\\s*(\\d{1,4})\\s*(?:cÃ¡i|cai|chiáº¿c|chiec|bá»™|bo|thÃ¹ng|thung|há»™p|hop|kiá»‡n|kien|pcs|piece|pieces|items?)?\\s*$");
    private static final Pattern QUANTITY_INLINE = Pattern.compile("(?iu)(\\d{1,4})\\s*(?:cÃ¡i|cai|chiáº¿c|chiec|bá»™|bo|thÃ¹ng|thung|há»™p|hop|kiá»‡n|kien|pcs|piece|pieces|items?)\\b");
    private static final Pattern SIZE_TOKEN = Pattern.compile("(?iu)\\b(size|cá»¡|co)\\s*([sml])\\b");
    private static final Pattern MULTIPLY_TOKEN = Pattern.compile("(?iu)(?:x|Ã—)\\s*(\\d{1,4})\\b");
    private static final Pattern NUMBER_AT_END = Pattern.compile("(\\d{1,4})\\s*$");
    private static final Pattern CLASSROOM_PATTERN = Pattern.compile("(?iu)(\\d{1,4})?\\s*phong\\s*hoc[^\\d]*(\\d{1,4})\\s*(?:bo\\s*)?ban\\s*ghe");
    private static final Pattern DINING_SET_PATTERN = Pattern.compile("(?iu)(\\d{1,4})\\s*(?:bo)\\s*ban\\s*an[^\\d]*(\\d{1,4})\\s*ghe");
    private static final Pattern GENERIC_SET_PATTERN = Pattern.compile("(?iu)(\\d{1,4})\\s*(?:bo)\\s*ban\\s*ghe(?:\\s+([a-z0-9\\s]{1,40}))?");
    private static final Pattern ASCII_CONNECTOR_PATTERN = Pattern.compile("(?iu)\\s*(?:\\+|&|\\b(?:va|voi|kem(?:\\s+theo)?|cung)\\b)\\s*");

    private static final String CATEGORY_ELECTRONICS = "Äiá»‡n tá»­";
    private static final String CATEGORY_FURNITURE = "Ná»™i tháº¥t";
    private static final String CATEGORY_HOME = "Äá»“ gia dá»¥ng";
    private static final String CATEGORY_CLOTHING = "Quáº§n Ã¡o";
    private static final String CATEGORY_OTHER = "KhÃ¡c";

    private static final String[] ELECTRONICS_KEYWORDS = {
            "bo pc", "pc", "pc gaming", "may tinh", "may tinh ban", "may tinh de ban", "may tinh xach tay",
            "may tinh bang", "may vi tinh", "laptop", "desktop", "computer", "macbook", "imac", "man hinh",
            "monitor", "tv", "tivi", "smart tv", "ban phim", "chuot", "may in", "may scan", "may photocopy",
            "may chieu", "playstation", "xbox", "may game", "may console"
    };

    private static final String[] FURNITURE_KEYWORDS = {
            "ban an", "ghe an", "ban hoc", "ghe hoc", "ban lam viec", "ghe xoay", "ban trang diem", "sofa",
            "bo sofa", "giuong", "giuong tang", "tu quan ao", "tu giay", "tu tivi", "ke sach", "ke tv",
            "ban tra", "ban cafe", "ban tiep tan", "ban van phong", "ghe van phong", "ghe gaming", "ban bar",
            "ban", "ghe"
    };

    private static final String[] HOME_KEYWORDS = {
            "tu lanh", "tu dong", "may giat", "may say", "may say chen", "noi com", "noi chien", "noi ap suat",
            "lo vi song", "lo nuong", "bep dien", "bep tu", "may hut mui", "may loc nuoc", "may loc khong khi",
            "binh nuoc nong", "may suoi", "may say toc"
    };

    private static final String[] CLOTHING_KEYWORDS = {
            "ao", "quan", "dam", "vay", "ao khoac", "ao so mi", "ao thun", "giay", "dep", "tui xach"
    };

    /**
     * Parse free-form text into a structured list of candidates.
     * Supports both line-separated and comma-separated items.
     */
    public ParseResult parse(String rawText) {
        if (!StringUtils.hasText(rawText)) {
            return ParseResult.builder()
                    .candidates(List.of())
                    .warnings(List.of("KhÃ´ng cÃ³ ná»™i dung Ä‘á»ƒ phÃ¢n tÃ­ch"))
                    .metadata(Map.of("lines_processed", 0, "lines_skipped", 0))
                    .build();
        }

        String normalizedText = rawText.replace("\r\n", "\n");

        // Pre-process: Split comma-separated items into separate lines
        List<String> allLines = new ArrayList<>();
        for (String line : normalizedText.split("\n")) {
            if (line.contains(",")) {
                // Split by comma and add each part as separate line
                String[] parts = line.split(",");
                for (String part : parts) {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty()) {
                        allLines.add(trimmed);
                    }
                }
            } else {
                allLines.add(line);
            }
        }

        String[] lines = allLines.toArray(new String[0]);

        List<IntakeParseTextResponse.ParsedItem> candidates = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        int processed = 0;
        int skipped = 0;
        int splitGeneratedItems = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                skipped++;
                continue;
            }

            processed++;

            String cleaned = cleanLine(line);
            if (cleaned.isEmpty()) {
                skipped++;
                warnings.add("Dòng " + (i + 1) + " không chứa thông tin hợp lệ.");
                continue;
            }

            List<IntakeParseTextResponse.ParsedItem> splitItems = splitCompositeDescription(cleaned);
            if (!splitItems.isEmpty()) {
                candidates.addAll(splitItems);
                splitGeneratedItems += splitItems.size();
                continue;
            }

            List<String> connectorSegments = splitByConnectors(cleaned);
            if (connectorSegments.size() > 1) {
                int emitted = 0;
                for (String segment : connectorSegments) {
                    Optional<IntakeParseTextResponse.ParsedItem> parsed =
                            buildCandidateFromCleanInput(segment, i, warnings);
                    if (parsed.isPresent()) {
                        candidates.add(parsed.get());
                        emitted++;
                    }
                }
                if (emitted == 0) {
                    skipped++;
                } else {
                    splitGeneratedItems += emitted;
                }
                continue;
            }

            Optional<IntakeParseTextResponse.ParsedItem> candidateOpt =
                    buildCandidateFromCleanInput(cleaned, i, warnings);
            if (candidateOpt.isPresent()) {
                candidates.add(candidateOpt.get());
            } else {
                skipped++;
            }
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("lines_processed", processed);
        metadata.put("lines_skipped", skipped);
        metadata.put("items_detected", candidates.size());
        metadata.put("comma_splitting_enabled", true);
        metadata.put("smart_split_items", splitGeneratedItems);

        return ParseResult.builder()
                .candidates(candidates)
                .warnings(warnings)
                .metadata(metadata)
                .build();
    }

    private List<IntakeParseTextResponse.ParsedItem> splitCompositeDescription(String input) {
        if (!StringUtils.hasText(input)) {
            return List.of();
        }

        String normalized = normalizeVietnamese(input).toLowerCase(Locale.ROOT);

        Matcher classroomMatcher = CLASSROOM_PATTERN.matcher(normalized);
        if (classroomMatcher.find()) {
            int roomCount = positiveOrDefault(classroomMatcher.group(1), 1);
            int setPerRoom = positiveOrDefault(classroomMatcher.group(2), 1);
            int totalSets = Math.max(1, roomCount) * Math.max(1, setPerRoom);
            return List.of(
                    buildCandidate("BÃ n há»c", totalSets, CATEGORY_FURNITURE, null, false, true, 0.85),
                    buildCandidate("Gháº¿ há»c", totalSets, CATEGORY_FURNITURE, null, false, false, 0.85)
            );
        }

        Matcher diningMatcher = DINING_SET_PATTERN.matcher(normalized);
        if (diningMatcher.find()) {
            int setCount = positiveOrDefault(diningMatcher.group(1), 1);
            int chairsPerSet = positiveOrDefault(diningMatcher.group(2), 1);
            int totalChairs = Math.max(1, setCount) * Math.max(1, chairsPerSet);
            return List.of(
                    buildCandidate("BÃ n Äƒn", Math.max(1, setCount), CATEGORY_FURNITURE, null, false, true, 0.9),
                    buildCandidate("Gháº¿ Äƒn", totalChairs, CATEGORY_FURNITURE, null, false, false, 0.9)
            );
        }

        Matcher genericMatcher = GENERIC_SET_PATTERN.matcher(normalized);
        if (genericMatcher.find()) {
            int setCount = positiveOrDefault(genericMatcher.group(1), 1);
            String descriptor = Optional.ofNullable(genericMatcher.group(2))
                    .map(String::trim)
                    .orElse("");

            String tableName = "BÃ n";
            String chairName = "Gháº¿";
            if (!descriptor.isEmpty()) {
                if (descriptor.contains("hoc")) {
                    tableName = "BÃ n há»c";
                    chairName = "Gháº¿ há»c";
                } else if (descriptor.contains("van phong")) {
                    tableName = "BÃ n vÄƒn phÃ²ng";
                    chairName = "Gháº¿ vÄƒn phÃ²ng";
                } else if (descriptor.contains("ngoai troi")) {
                    tableName = "BÃ n ngoÃ i trá»i";
                    chairName = "Gháº¿ ngoÃ i trá»i";
                } else if (descriptor.contains("an")) {
                    tableName = "BÃ n Äƒn";
                    chairName = "Gháº¿ Äƒn";
                } else {
                    String pretty = capitalize(descriptor);
                    tableName = "BÃ n " + pretty;
                    chairName = "Gháº¿ " + pretty;
                }
            }

            int quantity = Math.max(1, setCount);
            return List.of(
                    buildCandidate(tableName, quantity, CATEGORY_FURNITURE, null, false, true, 0.83),
                    buildCandidate(chairName, quantity, CATEGORY_FURNITURE, null, false, false, 0.83)
            );
        }

        return List.of();
    }

    private List<String> splitByConnectors(String input) {
        if (!StringUtils.hasText(input)) {
            return List.of();
        }
        String normalized = normalizeWhitespace(input);
        String ascii = normalizeVietnamese(normalized).toLowerCase(Locale.ROOT);
        Matcher matcher = ASCII_CONNECTOR_PATTERN.matcher(ascii);
        if (!matcher.find()) {
            return List.of(normalized);
        }
        List<String> parts = new ArrayList<>();
        int lastIndex = 0;
        matcher.reset();
        while (matcher.find()) {
            int start = matcher.start();
            if (start > lastIndex) {
                parts.add(normalized.substring(lastIndex, start).trim());
            }
            lastIndex = matcher.end();
        }
        if (lastIndex < normalized.length()) {
            parts.add(normalized.substring(lastIndex).trim());
        }
        List<String> filtered = new ArrayList<>();
        for (String part : parts) {
            if (StringUtils.hasText(part)) {
                filtered.add(part.trim());
            }
        }
        return filtered.isEmpty() ? List.of(normalized) : filtered;
    }

    private Optional<IntakeParseTextResponse.ParsedItem> buildCandidateFromCleanInput(String cleanedInput,
                                                                                     int lineIndex,
                                                                                     List<String> warnings) {
        if (!StringUtils.hasText(cleanedInput)) {
            return Optional.empty();
        }

        String working = cleanedInput.trim();
        if (working.isEmpty()) {
            return Optional.empty();
        }

        QuantityExtraction quantityExtraction = extractQuantity(working);
        String nameWithoutQuantity = quantityExtraction.cleanedName();
        int quantity = quantityExtraction.quantity();
        double confidence = quantityExtraction.confidence();

        SizeExtraction sizeExtraction = extractSize(nameWithoutQuantity);
        String nameWithoutSize = sizeExtraction.cleanedName();
        String detectedSize = sizeExtraction.size().orElse(null);
        if (sizeExtraction.confidenceBoost() > 0) {
            confidence = Math.min(1.0, confidence + sizeExtraction.confidenceBoost());
        }

        String normalizedName = normalizeWhitespace(nameWithoutSize);
        if (normalizedName.isEmpty()) {
            warnings.add("KhA'ng xA�c �`��<nh �`�����c tA�n v��-t ph��cm ��Y dA�ng " + (lineIndex + 1) + ".");
            return Optional.empty();
        }

        String category = detectCategory(normalizedName);
        if (category != null) {
            confidence = Math.min(1.0, confidence + 0.05);
        }

        boolean fragile = detectFragile(normalizedName);
        boolean requiresDisassembly = detectRequiresDisassembly(normalizedName);

        IntakeParseTextResponse.ParsedItem item = buildCandidate(
                capitalize(normalizedName),
                quantity,
                category,
                detectedSize,
                fragile,
                requiresDisassembly,
                confidence
        );
        return Optional.of(item);
    }

    private static String cleanLine(String input) {
        String withoutLeadingBullets = BULLET_PREFIX.matcher(input).replaceFirst("");
        String trimmed = withoutLeadingBullets.trim();
        // Remove leading separators like ":" or "-"
        trimmed = trimmed.replaceAll("^[\\-:]+\\s*", "");
        return normalizeWhitespace(trimmed);
    }

    private static QuantityExtraction extractQuantity(String input) {
        String working = input;

        Matcher multiplyMatcher = MULTIPLY_TOKEN.matcher(working);
        if (multiplyMatcher.find()) {
            int quantity = parseQuantity(multiplyMatcher.group(1));
            if (quantity > 0) {
                String cleaned = working.substring(0, multiplyMatcher.start()).trim();
                cleaned = cleaned.replaceAll("[,;]+$", "").trim();
                return new QuantityExtraction(cleaned, quantity, 0.78);
            }
        }

        Matcher suffixMatcher = QUANTITY_SUFFIX.matcher(working);
        if (suffixMatcher.find()) {
            int quantity = parseQuantity(suffixMatcher.group(1));
            if (quantity > 0) {
                String cleaned = working.substring(0, suffixMatcher.start()).trim();
                cleaned = cleaned.replaceAll("[,;]+$", "").trim();
                return new QuantityExtraction(cleaned, Math.max(quantity, 1), 0.82);
            }
        }

        Matcher inlineMatcher = QUANTITY_INLINE.matcher(working);
        if (inlineMatcher.find()) {
            int quantity = parseQuantity(inlineMatcher.group(1));
            if (quantity > 0) {
                String cleaned = (working.substring(0, inlineMatcher.start()) + " " + working.substring(inlineMatcher.end()))
                        .replaceAll("\\s+", " ")
                        .trim();
                return new QuantityExtraction(cleaned, Math.max(quantity, 1), 0.78);
            }
        }

        Matcher terminalNumber = NUMBER_AT_END.matcher(working);
        if (terminalNumber.find()) {
            int quantity = parseQuantity(terminalNumber.group(1));
            if (quantity > 0) {
                String cleaned = working.substring(0, terminalNumber.start()).trim();
                cleaned = cleaned.replaceAll("[,;]+$", "").trim();
                return new QuantityExtraction(cleaned, Math.max(quantity, 1), 0.75);
            }
        }

        return new QuantityExtraction(working.trim(), 1, 0.6);
    }

    private static SizeExtraction extractSize(String input) {
        Matcher sizeMatcher = SIZE_TOKEN.matcher(input);
        if (sizeMatcher.find()) {
            String size = sizeMatcher.group(2).toUpperCase(Locale.ROOT);
            String cleaned = (input.substring(0, sizeMatcher.start()) + " " + input.substring(sizeMatcher.end()))
                    .replaceAll("\\s+", " ")
                    .trim();
            return new SizeExtraction(cleaned, Optional.of(size), 0.05);
        }
        return new SizeExtraction(input.trim(), Optional.empty(), 0);
    }

    private static String detectCategory(String name) {
        String normalized = normalizeVietnamese(name).toLowerCase(Locale.ROOT);
        String tokenized = normalizeTokens(normalized);
        if (tokenized.isEmpty()) {
            return null;
        }

        if (containsAnyToken(tokenized, ELECTRONICS_KEYWORDS)) {
            return CATEGORY_ELECTRONICS;
        }
        if (containsAnyToken(tokenized, FURNITURE_KEYWORDS)) {
            return CATEGORY_FURNITURE;
        }
        if (containsAnyToken(tokenized, HOME_KEYWORDS)) {
            return CATEGORY_HOME;
        }
        if (containsAnyToken(tokenized, CLOTHING_KEYWORDS)) {
            return CATEGORY_CLOTHING;
        }
        return null;
    }

    private static boolean detectFragile(String name) {
        String normalized = normalizeVietnamese(name).toLowerCase(Locale.ROOT);
        String tokenized = normalizeTokens(normalized);
        if (tokenized.isEmpty()) {
            return false;
        }
        if (containsAnyToken(tokenized, ELECTRONICS_KEYWORDS)) {
            return true;
        }
        return containsAnyToken(tokenized, "kinh", "guong", "gom", "su", "thuy tinh", "pha le", "ceramic");
    }

    private static boolean detectRequiresDisassembly(String name) {
        String normalized = normalizeVietnamese(name).toLowerCase(Locale.ROOT);
        String tokenized = normalizeTokens(normalized);
        if (tokenized.isEmpty()) {
            return false;
        }
        if (containsAnyToken(tokenized, ELECTRONICS_KEYWORDS)) {
            return false;
        }
        if (containsAnyToken(tokenized, "giuong", "giuong tang", "bo sofa", "sofa", "tu", "tu quan ao", "tu giay",
                "tu sach", "tu tivi", "ke tv", "ke sach")) {
            return true;
        }
        return containsAnyToken(tokenized, "ban an", "ban lam viec", "ban hoc", "ban trang diem", "ban van phong",
                "ban tiep tan", "ban tra", "ban cafe");
    }

    private static IntakeParseTextResponse.ParsedItem buildCandidate(String name,
                                                                     int quantity,
                                                                     String category,
                                                                     String size,
                                                                     boolean fragile,
                                                                     boolean requiresDisassembly,
                                                                     double confidence) {
        return IntakeParseTextResponse.ParsedItem.builder()
                .name(name)
                .quantity(Math.max(1, quantity))
                .categoryName(category != null ? category : CATEGORY_OTHER)
                .size(size)
                .isFragile(fragile)
                .requiresDisassembly(requiresDisassembly)
                .confidence(round(confidence))
                .build();
    }

    private static String normalizeTokens(String normalized) {
        if (normalized == null || normalized.isBlank()) {
            return "";
        }
        String collapsed = normalized.replaceAll("[^a-z0-9]+", " ").trim();
        if (collapsed.isEmpty()) {
            return "";
        }
        return " " + collapsed.replaceAll("\\s+", " ") + " ";
    }

    private static boolean containsAnyToken(String tokenized, String... keywords) {
        if (tokenized == null || tokenized.isBlank()) {
            return false;
        }
        for (String keyword : keywords) {
            if (!StringUtils.hasText(keyword)) {
                continue;
            }
            String target = " " + keyword.trim() + " ";
            if (tokenized.contains(target)) {
                return true;
            }
        }
        return false;
    }

    private static String capitalize(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        String[] tokens = lower.split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(token.substring(0, 1).toUpperCase(Locale.ROOT));
            if (token.length() > 1) {
                builder.append(token.substring(1));
            }
        }
        return builder.toString();
    }

    private static int positiveOrDefault(String value, int fallback) {
        if (!StringUtils.hasText(value)) {
            return fallback;
        }
        int parsed = parseQuantity(value.trim());
        return parsed > 0 ? parsed : fallback;
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static int parseQuantity(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            log.debug("KhÃ´ng thá»ƒ phÃ¢n tÃ­ch sá»‘ lÆ°á»£ng tá»« '{}'", value);
            return 0;
        }
    }

    private static String normalizeWhitespace(String input) {
        return input.replaceAll("\\s+", " ").trim();
    }

    private static String normalizeVietnamese(String text) {
        if (text == null) {
            return "";
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }

    @Value
    @Builder
    public static class ParseResult {
        List<IntakeParseTextResponse.ParsedItem> candidates;
        List<String> warnings;
        Map<String, Object> metadata;
    }

    private record QuantityExtraction(String cleanedName, int quantity, double confidence) {}

    private record SizeExtraction(String cleanedName, Optional<String> size, double confidenceBoost) {}
}
