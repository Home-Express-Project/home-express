package com.homeexpress.home_express_api.constants;

/**
 * AI Prompts Factory for Vision and Language Models in HomeExpress.
 *
 * Provides optimized prompts and builders for item detection, estimation,
 * categorization, and logistics planning, focusing on structured JSON output.
 */
public final class AIPrompts {

    // Common instruction appended to prompts to ensure strict JSON output format without markdown wrappers.
    private static final String STRICT_JSON_OUTPUT =
            "OUTPUT FORMAT: Return ONLY a valid JSON object or array as specified. No explanations, no preamble text, and no markdown wrappers (like ```json).";

    // Common Categories used across detection prompts.
    private static final String DETECTION_CATEGORIES =
            "[furniture, appliance, electronics, box, household_item, sports_equipment, other]";

    // ========================================================================
    // SECTION 1: VISION PROMPTS (Image Input, Static)
    // These prompts take an image as input and do not require additional text context.
    // ========================================================================

    /**
     * BASIC DETECTION PROMPT
     * Used for: Quick item identification in images.
     * Output: Simple JSON array with name, category, confidence.
     */
    public static final String DETECTION_PROMPT = """
        ROLE: You are a logistics assistant.
        TASK: Analyze the image and identify all items relevant for house moving (furniture, appliances, electronics, boxes).
        
        For each item, provide:
        1. name: Specific, descriptive name (e.g., "Samsung 55-inch TV", "Three-Seat Leather Sofa").
        2. category: One of %s.
        3. confidence: Your confidence score (0.0 to 1.0).
        
        EXAMPLE OUTPUT:
        [
          {"name": "Three-Seat Sofa", "category": "furniture", "confidence": 0.95},
          {"name": "Coffee Table", "category": "furniture", "confidence": 0.92}
        ]
        
        %s
        """.formatted(DETECTION_CATEGORIES, STRICT_JSON_OUTPUT);

    /**
     * ENHANCED DETECTION PROMPT (Optimized for GPT-5-mini)
     * Used for: Detailed detection including quantity and Vietnamese name recognition.
     * Output: JSON array with key logistics fields.
     */
    public static final String ENHANCED_DETECTION_PROMPT = """
        ROLE: You are an expert logistics assistant for the Vietnamese market.
        TASK: Analyze the image to identify all items for house moving.

        DETAILS REQUIRED PER ITEM:
        - name: Specific name.
        - category: One of %s.
        - quantity: Count of identical items (default 1).
        - confidence: 0.0 to 1.0.

        RULES:
        1. Recognize and translate common Vietnamese names if detected in context:
           - "Tủ lạnh" → Refrigerator
           - "Máy giặt" → Washing Machine
           - "Tivi" → Television
           - "Sofa", "Ghế sofa" → Sofa
           - "Bàn ăn" → Dining Table
           - "Giường" → Bed
           - "Tủ quần áo" → Wardrobe
        2. Break down sets: "Bộ bàn ghế ăn" (Dining set) must be listed as separate "Dining Table" (1) and "Dining Chair" (e.g., 4).

        EXAMPLE OUTPUT:
        [
          {"name": "Three-Seat Sofa", "category": "furniture", "quantity": 1, "confidence": 0.95},
          {"name": "Dining Chair", "category": "furniture", "quantity": 4, "confidence": 0.90}
        ]

        %s
        """.formatted(DETECTION_CATEGORIES, STRICT_JSON_OUTPUT);

    /**
     * DIMENSION ESTIMATION PROMPT (Vision Task) - Uses Chain-of-Thought (CoT)
     * Used for: Estimating size measurement using visual cues and reference objects.
     * Output: Dimensions estimate including reasoning.
     */
    public static final String DIMENSION_ESTIMATION_PROMPT = """
        ROLE: You are an expert in estimating dimensions from images for logistics.
        TASK: Estimate the dimensions (in cm) of the primary object(s) in the image.

        ESTIMATION PROCESS (Use Chain-of-Thought):
        1. Identify Reference Objects: Look for objects with known standard sizes (e.g., Door frame ~200cm H; Dining table ~75cm H; Chair seat ~45cm H; Floor tiles 30x30cm or 60x60cm).
        2. Scale Comparison: Compare the target item's size to the reference object, considering perspective.
        3. Category Prior: If no reference is clear, use typical category sizes (e.g., Washing machine: 60x60x85cm).
        4. Final Estimate: Provide the final estimate, rounding up slightly for moving safety.

        EXAMPLE OUTPUT:
        {
          "reasoning": "Step 1: Identified a standard door frame (200cm H) behind the sofa. Step 2: Sofa height is approx 40%% of the door frame. Step 3: Width is slightly wider than the door frame. Step 4: Final estimate based on visual comparison.",
          "dims_cm": {"length": 210, "width": 90, "height": 85},
          "dims_confidence": 0.75,
          "dimensions_basis": "scale-from-reference",
          "reference_used": "door frame",
          "volume_m3": 1.607
        }
        
        %s
        """.formatted(STRICT_JSON_OUTPUT);

    /**
     * ROOM DETECTION PROMPT
     * Used for: Identifying the room context from an image.
     * Output: Room type and list of visible items.
     */
    public static final String ROOM_DETECTION_PROMPT = """
        ROLE: You are a room classification expert.
        TASK: Analyze the image context to determine the room type and identify major items.

        ROOM TYPES & INDICATORS:
        - living_room: sofas, TV, coffee tables.
        - bedroom: bed, wardrobe, dresser.
        - kitchen: appliances, counters.
        - office: desk, computer, chair.
        - garage/storage: tools, boxes, equipment.

        EXAMPLE OUTPUT:
        {
          "room_type": "living_room",
          "room_confidence": 0.92,
          "items_in_room": [
            {"name": "Three-Seat Sofa", "category": "furniture"},
            {"name": "TV", "category": "electronics"}
          ],
          "room_size_estimate": "medium"
        }
        
        %s
        """.formatted(STRICT_JSON_OUTPUT);

    // ========================================================================
    // SECTION 2: TEXT/DATA PROMPTS (Dynamic Input, Builders)
    // These methods inject dynamic data into prompt templates.
    // ========================================================================

    /**
     * WEIGHT ESTIMATION PROMPT BUILDER - Uses Chain-of-Thought (CoT)
     * Used for: Specialized weight calculation based on item details.
     * Input: Item details JSON string (name, category, dims_cm, material).
     * Output: Weight estimate including reasoning.
     */
    public static String buildWeightEstimationPrompt(String itemDetailsJson) {
        return """
            ROLE: You are an expert in estimating the weight of household items for logistics.
            TASK: Estimate the weight (in kg) based on the INPUT ITEM DETAILS provided below.

            ESTIMATION PROCESS (Use Chain-of-Thought):
            1. Base Weight: Determine the standard weight range for the category.
               - Examples: Sofas (3-seat): 50-80 kg; Refrigerators: 70-120 kg; Washing machines: 60-90 kg.
            2. Material Adjustment: Adjust the base weight based on material (e.g., Solid wood +30%%, Particle board -20%%).
            3. Dimension Adjustment: Scale the weight based on the provided dimensions compared to typical sizes.
            4. Final Estimate: Provide the final estimate and confidence.

            INPUT ITEM DETAILS:
            %s

            EXAMPLE OUTPUT:
            {
              "reasoning": "Step 1: Base weight for a 3-seat sofa is 50-80kg. Step 2: Material is fabric/wood, standard weight applies. Step 3: Dimensions are typical. Step 4: Final estimate is centered.",
              "weight_kg": 65.0,
              "weight_confidence": 0.85,
              "weight_range_min": 50.0,
              "weight_range_max": 80.0,
              "weight_basis": "category+size+material"
            }
            
            %s
            """.formatted(itemDetailsJson, STRICT_JSON_OUTPUT);
    }

    /**
     * CATEGORY CLASSIFICATION PROMPT BUILDER
     * Used for: Detailed categorization and determining handling requirements.
     * Input: Item name and optional description.
     * Output: Detailed category, subcategory, and logistics handling flags.
     */
    public static String buildCategoryClassificationPrompt(String itemName, String description) {
        String desc = (description == null || description.isBlank()) ? "N/A" : description;
        return """
            ROLE: You are a logistics categorization expert.
            TASK: Classify the INPUT item and determine handling requirements.

            CATEGORIES & SUBCATEGORIES:
            1. furniture: sofa, dining_table, desk, bed_frame, mattress, wardrobe, bookshelf, chair...
            2. appliance: refrigerator, washing_machine, dryer, oven, microwave, air_conditioner...
            3. electronics: tv, monitor, computer, printer, speaker...
            4. box: box_standard, box_heavy, box_fragile...
            5. other: artwork, mirror, lamp, rug, plant, sports_equipment...

            HANDLING RULES:
            - fragile: True for glass, electronics, mirrors, delicate artwork.
            - two_person_lift: True if weight typically > 25 kg or bulky.
            - stackable: True for sturdy boxes/crates; False otherwise.
            - disassembly_required: True for large items that won't fit through standard doors (beds, large wardrobes, large tables).

            INPUT:
            Item Name: %s
            Description: %s

            EXAMPLE OUTPUT:
            {
              "category": "furniture",
              "subcategory": "sofa",
              "fragile": false,
              "two_person_lift": true,
              "stackable": false,
              "disassembly_required": false,
              "handling_notes": "Remove cushions separately, protect fabric."
            }
            
            %s
            """.formatted(itemName, desc, STRICT_JSON_OUTPUT);
    }

    /**
     * VIETNAMESE ITEM RECOGNITION PROMPT BUILDER
     * Used for: Translating Vietnamese item names to English.
     * Input: Vietnamese term.
     * Output: English name and category.
     */
    public static String buildVietnameseRecognitionPrompt(String vietnameseTerm) {
        return """
            ROLE: You are a Vietnamese-English translation expert for logistics.
            TASK: Translate the INPUT Vietnamese item name to standard English and classify it.

            TRANSLATION MAP:
            - Tủ lạnh → Refrigerator (appliance)
            - Máy giặt / Máy giặc → Washing Machine (appliance)
            - Ghế sofa → Sofa (furniture)
            - Bàn ăn → Dining Table (furniture)
            - Giường → Bed (furniture)
            - Tủ quần áo → Wardrobe (furniture)
            - Ti vi / TV → Television (electronics)
            - Lò vi sóng / Lò viba → Microwave (appliance)
            - Điều hòa / Máy lạnh → Air Conditioner (appliance)
            - Thùng carton → Cardboard Box (box)

            INPUT:
            %s

            EXAMPLE OUTPUT:
            {
              "vietnamese_name": "Tủ lạnh Samsung",
              "english_name": "Samsung Refrigerator",
              "category": "appliance",
              "subcategory": "refrigerator",
              "confidence": 0.95
            }
            
            %s
            """.formatted(vietnameseTerm, STRICT_JSON_OUTPUT);
    }

    /**
     * PACKING RECOMMENDATION PROMPT BUILDER
     * Used for: Suggesting packing strategies based on a list of items.
     * Input: List of detected items (JSON array string).
     * Output: Packing recommendations, box requirements, and materials needed.
     */
    public static String buildPackingRecommendationPrompt(String itemListJson) {
        return """
            ROLE: You are a packing optimization expert.
            TASK: Given the INPUT ITEM LIST, recommend a packing strategy.

            REQUIREMENTS:
            1. Box Estimation: Calculate types needed.
               - Small (40x30x30cm): heavy items (books, tools).
               - Medium (50x40x40cm): general items (clothes, kitchenware).
               - Large (60x50x50cm): lightweight bulky items (bedding).
               - Specialized: Wardrobe boxes, Dish packs.
            2. Materials Estimation: Calculate required materials (Bubble wrap, Moving blankets, Stretch wrap).
            3. Strategy: Recommend packing order and special handling notes.

            INPUT ITEM LIST:
            %s

            EXAMPLE OUTPUT:
            {
              "boxes_needed": {
                "small": 5,
                "medium": 10,
                "large": 3,
                "wardrobe": 2
              },
              "materials_needed": {
                "bubble_wrap_meters": 20,
                "moving_blankets": 8
              },
              "packing_strategy": [
                "Pack heavy items in small boxes first.",
                "Wrap all electronics in bubble wrap.",
                "Disassemble large furniture and secure parts."
              ],
              "special_notes": [
                "TV requires specialized protection or original box.",
                "Defrost refrigerator 24h before moving."
              ]
            }
            
            %s
            """.formatted(itemListJson, STRICT_JSON_OUTPUT);
    }

    /**
     * QUALITY CHECK PROMPT BUILDER
     * Used for: Verifying AI detection results and flagging anomalies (AI Validator pattern).
     * Input: Detection results from primary AI (JSON array string).
     * Output: Quality assessment and recommendations.
     */
    public static String buildQualityCheckPrompt(String detectionResultsJson) {
        return """
            ROLE: You are a quality assurance expert for AI detection results in logistics.
            TASK: Review the INPUT DETECTION RESULTS and identify anomalies.

            VALIDATION CHECKS:
            1. Duplicates: Identify if the same physical item is listed multiple times.
            2. Category Errors: Check for misclassifications (e.g., TV as furniture).
            3. Unrealistic Estimations: Flag improbable dimensions or weights (e.g., 10m sofa, 500kg chair).
            4. Low Confidence: Flag items with confidence < 0.6 for manual review.

            INPUT DETECTION RESULTS:
            %s

            EXAMPLE OUTPUT:
            {
              "overall_quality": "good",
              "quality_score": 0.85,
              "issues_found": [
                {
                  "type": "duplicate",
                  "item_refs": ["Sofa-1", "Sofa-2"],
                  "description": "Same sofa detected twice.",
                  "recommendation": "Merge entries."
                },
                {
                  "type": "unrealistic_dimension",
                  "item_refs": ["Chair-1"],
                  "description": "Chair height estimated at 300cm.",
                  "recommendation": "Manually verify dimensions."
                }
              ]
            }
            
            %s
            """.formatted(detectionResultsJson, STRICT_JSON_OUTPUT);
    }

    private AIPrompts() {
        // Private constructor to prevent instantiation of this utility class
        throw new AssertionError("Cannot instantiate utility class AIPrompts");
    }
}