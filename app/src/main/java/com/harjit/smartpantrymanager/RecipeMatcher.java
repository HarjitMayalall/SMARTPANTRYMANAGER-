package com.harjit.smartpantrymanager;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecipeMatcher {

    private RecipeMatcher() {
    }

    public static boolean canMake(
            List<PantryItem> pantry,
            List<RecipeIngredient> requirements) {

        if (requirements.isEmpty()) {
            return false;
        }

        Map<String, BigDecimal> available = new HashMap<>();
        Map<String, BigDecimal> needed = new HashMap<>();

        // Combine duplicate pantry entries using compatible base units.
        for (PantryItem item : pantry) {
            addQuantity(available, item.getName(),
                    item.getQuantity(), item.getUnit());
        }

        for (RecipeIngredient ingredient : requirements) {
            addQuantity(needed, ingredient.getName(),
                    ingredient.getQuantity(), ingredient.getUnit());
        }

        // Every requirement must be satisfied.
        for (Map.Entry<String, BigDecimal> entry : needed.entrySet()) {
            BigDecimal stock = available.get(entry.getKey());

            if (stock == null || stock.compareTo(entry.getValue()) < 0) {
                return false;
            }
        }

        return true;
    }

    private static void addQuantity(
            Map<String, BigDecimal> totals,
            String name, double quantity, String unit) {

        String normalizedUnit = unit.trim().toLowerCase(Locale.ROOT);
        BigDecimal amount = BigDecimal.valueOf(quantity);

        // Convert kilograms to grams and litres to millilitres.
        if (normalizedUnit.equals("kg")) {
            normalizedUnit = "g";
            amount = amount.multiply(BigDecimal.valueOf(1000));
        } else if (normalizedUnit.equals("l")) {
            normalizedUnit = "ml";
            amount = amount.multiply(BigDecimal.valueOf(1000));
        }

        String key = normalizeName(name) + "|" + normalizedUnit;
        BigDecimal current = totals.get(key);

        totals.put(key,
                current == null ? amount : current.add(amount));
    }

    private static String normalizeName(String name) {
        String value = name.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");

        // Explicit aliases avoid changing words such as "peas" or "oats".
        switch (value) {
            case "eggs":
                return "egg";
            case "tomatoes":
                return "tomato";
            case "potatoes":
                return "potato";
            case "onions":
                return "onion";
            case "bananas":
                return "banana";
            case "apples":
                return "apple";
            case "carrots":
                return "carrot";
            case "pea":
                return "peas";
            case "oat":
                return "oats";
            case "bean":
                return "beans";
            case "yoghurt":
                return "yogurt";
            default:
                return value;
        }
    }
}