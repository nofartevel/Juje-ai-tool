package com.example.jujeaibackend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Service
public class ProductService {

    private final List<Product> products;
    public ProductService() {
        this.products = loadProducts();
    }

    private List<Product> loadProducts() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = new ClassPathResource("products.json").getInputStream();
            return mapper.readValue(inputStream, new TypeReference<List<Product>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Product> getAllProducts() {
        return products;
    }

    /*public List<Product> filterProductsByTags(List<String> inputTags) {

        List<Product> allProducts = getAllProducts();

        return allProducts.stream()
                .filter(product -> product.getTags() != null &&
                        product.getTags().stream()
                                .anyMatch(tag -> inputTags.stream()
                                        .anyMatch(input -> input.equalsIgnoreCase(tag)))
                )
                .toList();
    }*/

    /*public java.util.List<ProductScore> scoreProducts(java.util.List<String> inputTags, String intent) {
        java.util.List<String> normalizedInputTags = inputTags.stream()
                .map(String::toLowerCase)
                .toList();

        java.util.List<ProductScore> scored = new java.util.ArrayList<>();

        for (Product product : getAllProducts()) {
            int score = 0;
            int strongMatches = 0;
            int weakMatches = 0;

            boolean sectionMatch = sectionMatchesIntent(product.getSection(), intent);

            if (product.getTags() != null) {
                for (String tag : product.getTags()) {
                    String normalizedTag = tag.toLowerCase();

                    if (normalizedInputTags.contains(normalizedTag)) {
                        if (isStrongTag(normalizedTag)) {
                            score += 3;
                            strongMatches++;
                        } else {
                            score += 1;
                            weakMatches++;
                        }
                    }
                }
            }

            if (sectionMatch) {
                score += 2;
            }

            if (score > 0) {
                scored.add(new ProductScore(product, score, strongMatches, weakMatches, sectionMatch));
            }
        }

        scored.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
        return scored;
    }*/

    private boolean isStrongTag(String tag) {
        return java.util.Set.of(
                "travel", "flight", "car", "beach", "pool", "outdoor",
                "feeding", "snacks", "diaper", "changing", "bath",
                "sleep", "play", "activity", "organization", "storage", "meal_prep"
        ).contains(tag);
    }

    private boolean sectionMatchesIntent(String section, String intent) {
        if (section == null || intent == null) return false;

        return switch (intent) {
            case "travel" -> java.util.Set.of("travel", "feeding", "play", "sleep", "diapering", "organization").contains(section);
            case "beach_pool" -> java.util.Set.of("feeding", "organization", "diapering", "outdoor", "play").contains(section);
            case "outdoor_day" -> java.util.Set.of("feeding", "organization", "diapering", "outdoor", "play").contains(section);
            case "feeding" -> java.util.Set.of("feeding", "organization").contains(section);
            case "diapering" -> java.util.Set.of("diapering", "organization").contains(section);
            case "bath" -> java.util.Set.of("bath", "play").contains(section);
            case "play_development" -> java.util.Set.of("play").contains(section);
            case "sleep" -> java.util.Set.of("sleep", "feeding", "organization").contains(section);
            default -> false;
        };
    }

    public Product getProductById(String id) {
        return getAllProducts().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public java.util.List<Product> getProductsByIds(java.util.List<String> ids) {
        java.util.List<Product> result = new java.util.ArrayList<>();

        if (ids == null) {
            return result;
        }

        for (String id : ids) {
            Product product = getProductById(id);
            if (product != null) {
                result.add(product);
            }
        }

        return result;
    }
}