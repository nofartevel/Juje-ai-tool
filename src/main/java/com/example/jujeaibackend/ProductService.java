package com.example.jujeaibackend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
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