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
 * Bộ phân tích văn bản thủ công (Heuristic Parser).
 * Class này đóng vai trò dự phòng khi AI không khả dụng.
 * Nó dùng Regex và từ khóa để bóc tách thông tin từ văn bản nhập tay của khách.
 */
@Slf4j
@Service
public class IntakeTextParsingService {

    // Regex bắt các ký tự đầu dòng như gạch ngang, dấu sao, chấm tròn hoặc số thứ tự (1., 2.)
    private static final Pattern BULLET_PREFIX = Pattern.compile("^(?:[\\-\\*\\+•·\\s]+|\\d+[\\).\\-\\s]+)+");
    
    // Regex bắt số lượng ở cuối câu (VD: "Ghế x5", "Bàn 2 cái")
    private static final Pattern QUANTITY_SUFFIX = Pattern.compile(
            "(?iu)(?:x|×)?\\s*(\\d{1,4})\\s*(?:cái|cai|chiếc|chiec|bộ|bo|thùng|thung|hộp|hop|kiện|kien|pcs|piece|pieces|items?)?\\s*$");
    
    // Regex bắt số lượng nằm giữa câu (VD: "5 cái ghế")
    private static final Pattern QUANTITY_INLINE = Pattern.compile("(?iu)(\\d{1,4})\\s*(?:cái|cai|chiếc|chiec|bộ|bo|thùng|thung|hộp|hop|kiện|kien|pcs|piece|pieces|items?)\\b");
    
    // Regex bắt kích thước (VD: "Size L", "Cỡ M")
    private static final Pattern SIZE_TOKEN = Pattern.compile("(?iu)\\b(size|cỡ|co)\\s*([sml])\\b");
    
    // Regex bắt dấu nhân số lượng (VD: "x5", "×10")
    private static final Pattern MULTIPLY_TOKEN = Pattern.compile("(?iu)(?:x|×)\\s*(\\d{1,4})\\b");
    
    // Regex bắt số đứng một mình ở cuối câu
    private static final Pattern NUMBER_AT_END = Pattern.compile("(\\d{1,4})\\s*$");
    
    // Các mẫu câu phức tạp (Combo trường học, quán ăn)
    private static final Pattern CLASSROOM_PATTERN = Pattern.compile("(?iu)(\\d{1,4})?\\s*phong\\s*hoc[^\\d]*(\\d{1,4})\\s*(?:bo\\s*)?ban\\s*ghe");
    private static final Pattern DINING_SET_PATTERN = Pattern.compile("(?iu)(\\d{1,4})\\s*(?:bo)\\s*ban\\s*an[^\\d]*(\\d{1,4})\\s*ghe");
    private static final Pattern GENERIC_SET_PATTERN = Pattern.compile("(?iu)(\\d{1,4})\\s*(?:bo)\\s*ban\\s*ghe(?:\\s+([a-z0-9\\s]{1,40}))?");
    
    // Các từ nối để tách câu (và, với, kèm, +)
    private static final Pattern ASCII_CONNECTOR_PATTERN = Pattern.compile("(?iu)\\s*(?:\\+|&|\\b(?:va|voi|kem(?:\\s+theo)?|cung)\\b)\\s*");

    // Tên danh mục chuẩn
    private static final String CATEGORY_ELECTRONICS = "Điện tử";
    private static final String CATEGORY_FURNITURE = "Nội thất";
    private static final String CATEGORY_HOME = "Đồ gia dụng";
    private static final String CATEGORY_CLOTHING = "Quần áo";
    private static final String CATEGORY_OTHER = "Khác";

    // Từ khóa nhận diện Điện tử (không dấu để so sánh cho dễ)
    private static final String[] ELECTRONICS_KEYWORDS = {
            "bo pc", "pc", "pc gaming", "may tinh", "may tinh ban", "may tinh de ban", "may tinh xach tay",
            "may tinh bang", "may vi tinh", "laptop", "desktop", "computer", "macbook", "imac", "man hinh",
            "monitor", "tv", "tivi", "smart tv", "ban phim", "chuot", "may in", "may scan", "may photocopy",
            "may chieu", "playstation", "xbox", "may game", "may console"
    };

    // Từ khóa nhận diện Nội thất
    private static final String[] FURNITURE_KEYWORDS = {
            "ban an", "ghe an", "ban hoc", "ghe hoc", "ban lam viec", "ghe xoay", "ban trang diem", "sofa",
            "bo sofa", "giuong", "giuong tang", "tu quan ao", "tu giay", "tu tivi", "ke sach", "ke tv",
            "ban tra", "ban cafe", "ban tiep tan", "ban van phong", "ghe van phong", "ghe gaming", "ban bar",
            "ban", "ghe"
    };

    // Từ khóa nhận diện Đồ gia dụng
    private static final String[] HOME_KEYWORDS = {
            "tu lanh", "tu dong", "may giat", "may say", "may say chen", "noi com", "noi chien", "noi ap suat",
            "lo vi song", "lo nuong", "bep dien", "bep tu", "may hut mui", "may loc nuoc", "may loc khong khi",
            "binh nuoc nong", "may suoi", "may say toc"
    };

    // Từ khóa nhận diện Quần áo
    private static final String[] CLOTHING_KEYWORDS = {
            "ao", "quan", "dam", "vay", "ao khoac", "ao so mi", "ao thun", "giay", "dep", "tui xach"
    };

    /**
     * Hàm xử lý chính: Phân tích văn bản thô thành danh sách đồ vật.
     * Hỗ trợ cả định dạng xuống dòng và định dạng ngăn cách bằng dấu phẩy.
     */
    public ParseResult parse(String rawText) {
        if (!StringUtils.hasText(rawText)) {
            return ParseResult.builder()
                    .candidates(List.of())
                    .warnings(List.of("Không có nội dung để phân tích"))
                    .metadata(Map.of("lines_processed", 0, "lines_skipped", 0))
                    .build();
        }

        // Chuẩn hóa xuống dòng
        String normalizedText = rawText.replace("\r\n", "\n");

        // Bước 1: Chia nhỏ văn bản thành từng dòng xử lý
        List<String> allLines = new ArrayList<>();
        for (String line : normalizedText.split("\n")) {
            if (line.contains(",")) {
                // Nếu có dấu phẩy thì tách tiếp thành nhiều dòng con (VD: "Bàn, ghế, tủ" -> 3 dòng)
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

        // Bước 2: Duyệt qua từng dòng và phân tích
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                skipped++;
                continue;
            }

            processed++;

            // Làm sạch dòng (bỏ gạch đầu dòng, ký tự lạ)
            String cleaned = cleanLine(line);
            if (cleaned.isEmpty()) {
                skipped++;
                warnings.add("Dòng " + (i + 1) + " không chứa thông tin hợp lệ.");
                continue;
            }

            // Thử tách các mẫu câu phức hợp (VD: "10 bộ bàn ghế")
            List<IntakeParseTextResponse.ParsedItem> splitItems = splitCompositeDescription(cleaned);
            if (!splitItems.isEmpty()) {
                candidates.addAll(splitItems);
                splitGeneratedItems += splitItems.size();
                continue;
            }

            // Thử tách theo từ nối (VD: "Bàn và ghế")
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

            // Phân tích đơn lẻ bình thường
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

    // Xử lý các mẫu câu gộp (Composite) như "Bộ bàn ghế", "Phòng học"
    private List<IntakeParseTextResponse.ParsedItem> splitCompositeDescription(String input) {
        if (!StringUtils.hasText(input)) {
            return List.of();
        }

        String normalized = normalizeVietnamese(input).toLowerCase(Locale.ROOT);

        // Mẫu: Phòng học (VD: "5 phòng học 30 bộ bàn ghế")
        Matcher classroomMatcher = CLASSROOM_PATTERN.matcher(normalized);
        if (classroomMatcher.find()) {
            int roomCount = positiveOrDefault(classroomMatcher.group(1), 1);
            int setPerRoom = positiveOrDefault(classroomMatcher.group(2), 1);
            int totalSets = Math.max(1, roomCount) * Math.max(1, setPerRoom);
            return List.of(
                    buildCandidate("Bàn học", totalSets, CATEGORY_FURNITURE, null, false, true, 0.85),
                    buildCandidate("Ghế học", totalSets, CATEGORY_FURNITURE, null, false, false, 0.85)
            );
        }

        // Mẫu: Bộ bàn ăn (VD: "1 bộ bàn ăn 6 ghế")
        Matcher diningMatcher = DINING_SET_PATTERN.matcher(normalized);
        if (diningMatcher.find()) {
            int setCount = positiveOrDefault(diningMatcher.group(1), 1);
            int chairsPerSet = positiveOrDefault(diningMatcher.group(2), 1);
            int totalChairs = Math.max(1, setCount) * Math.max(1, chairsPerSet);
            return List.of(
                    buildCandidate("Bàn ăn", Math.max(1, setCount), CATEGORY_FURNITURE, null, false, true, 0.9),
                    buildCandidate("Ghế ăn", totalChairs, CATEGORY_FURNITURE, null, false, false, 0.9)
            );
        }

        // Mẫu: Bộ bàn ghế chung chung
        Matcher genericMatcher = GENERIC_SET_PATTERN.matcher(normalized);
        if (genericMatcher.find()) {
            int setCount = positiveOrDefault(genericMatcher.group(1), 1);
            String descriptor = Optional.ofNullable(genericMatcher.group(2))
                    .map(String::trim)
                    .orElse("");

            String tableName = "Bàn";
            String chairName = "Ghế";
            
            // Đoán loại bàn ghế dựa trên mô tả đi kèm
            if (!descriptor.isEmpty()) {
                if (descriptor.contains("hoc")) {
                    tableName = "Bàn học";
                    chairName = "Ghế học";
                } else if (descriptor.contains("van phong")) {
                    tableName = "Bàn văn phòng";
                    chairName = "Ghế văn phòng";
                } else if (descriptor.contains("ngoai troi")) {
                    tableName = "Bàn ngoài trời";
                    chairName = "Ghế ngoài trời";
                } else if (descriptor.contains("an")) {
                    tableName = "Bàn ăn";
                    chairName = "Ghế ăn";
                } else {
                    String pretty = capitalize(descriptor);
                    tableName = "Bàn " + pretty;
                    chairName = "Ghế " + pretty;
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

    // Tách câu dựa trên từ nối (và, với, kèm)
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

    // Hàm dựng đối tượng Item từ chuỗi đã làm sạch
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

        // Bóc tách số lượng
        QuantityExtraction quantityExtraction = extractQuantity(working);
        String nameWithoutQuantity = quantityExtraction.cleanedName();
        int quantity = quantityExtraction.quantity();
        double confidence = quantityExtraction.confidence();

        // Bóc tách kích thước (S, M, L)
        SizeExtraction sizeExtraction = extractSize(nameWithoutQuantity);
        String nameWithoutSize = sizeExtraction.cleanedName();
        String detectedSize = sizeExtraction.size().orElse(null);
        if (sizeExtraction.confidenceBoost() > 0) {
            confidence = Math.min(1.0, confidence + sizeExtraction.confidenceBoost());
        }

        String normalizedName = normalizeWhitespace(nameWithoutSize);
        if (normalizedName.isEmpty()) {
            warnings.add("Không xác định được tên vật phẩm ở dòng " + (lineIndex + 1) + ".");
            return Optional.empty();
        }

        // Tự động đoán danh mục
        String category = detectCategory(normalizedName);
        if (category != null) {
            confidence = Math.min(1.0, confidence + 0.05);
        }

        // Tự động phát hiện đồ dễ vỡ hoặc cần tháo lắp
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

    // Làm sạch dòng text (bỏ bullet points)
    private static String cleanLine(String input) {
        String withoutLeadingBullets = BULLET_PREFIX.matcher(input).replaceFirst("");
        String trimmed = withoutLeadingBullets.trim();
        // Bỏ các ký tự ngăn cách đầu dòng như : hoặc -
        trimmed = trimmed.replaceAll("^[\\-:]+\\s*", "");
        return normalizeWhitespace(trimmed);
    }

    // Logic trích xuất số lượng từ chuỗi
    private static QuantityExtraction extractQuantity(String input) {
        String working = input;

        // Trường hợp "x5"
        Matcher multiplyMatcher = MULTIPLY_TOKEN.matcher(working);
        if (multiplyMatcher.find()) {
            int quantity = parseQuantity(multiplyMatcher.group(1));
            if (quantity > 0) {
                String cleaned = working.substring(0, multiplyMatcher.start()).trim();
                cleaned = cleaned.replaceAll("[,;]+$", "").trim();
                return new QuantityExtraction(cleaned, quantity, 0.78);
            }
        }

        // Trường hợp số ở cuối (kèm đơn vị): "5 cái"
        Matcher suffixMatcher = QUANTITY_SUFFIX.matcher(working);
        if (suffixMatcher.find()) {
            int quantity = parseQuantity(suffixMatcher.group(1));
            if (quantity > 0) {
                String cleaned = working.substring(0, suffixMatcher.start()).trim();
                cleaned = cleaned.replaceAll("[,;]+$", "").trim();
                return new QuantityExtraction(cleaned, Math.max(quantity, 1), 0.82);
            }
        }

        // Trường hợp số ở giữa: "Bàn 5 cái to"
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

        // Trường hợp số đứng cuối cùng: "Ghế 5"
        Matcher terminalNumber = NUMBER_AT_END.matcher(working);
        if (terminalNumber.find()) {
            int quantity = parseQuantity(terminalNumber.group(1));
            if (quantity > 0) {
                String cleaned = working.substring(0, terminalNumber.start()).trim();
                cleaned = cleaned.replaceAll("[,;]+$", "").trim();
                return new QuantityExtraction(cleaned, Math.max(quantity, 1), 0.75);
            }
        }

        // Mặc định là 1 nếu không tìm thấy số
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

    // Logic đoán danh mục dựa trên từ khóa
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

    // Logic đoán đồ dễ vỡ
    private static boolean detectFragile(String name) {
        String normalized = normalizeVietnamese(name).toLowerCase(Locale.ROOT);
        String tokenized = normalizeTokens(normalized);
        if (tokenized.isEmpty()) {
            return false;
        }
        if (containsAnyToken(tokenized, ELECTRONICS_KEYWORDS)) {
            return true;
        }
        // Từ khóa vật liệu dễ vỡ
        return containsAnyToken(tokenized, "kinh", "guong", "gom", "su", "thuy tinh", "pha le", "ceramic");
    }

    // Logic đoán đồ cần tháo lắp
    private static boolean detectRequiresDisassembly(String name) {
        String normalized = normalizeVietnamese(name).toLowerCase(Locale.ROOT);
        String tokenized = normalizeTokens(normalized);
        if (tokenized.isEmpty()) {
            return false;
        }
        if (containsAnyToken(tokenized, ELECTRONICS_KEYWORDS)) {
            return false;
        }
        // Các đồ nội thất lớn thường phải tháo rời
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
            log.debug("Không thể phân tích số lượng từ '{}'", value);
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
