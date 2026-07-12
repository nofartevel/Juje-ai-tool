package com.example.jujeaibackend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class JujeAiBackendApplicationTests {

    @Autowired
    private OpenAiSelectorService openAiSelectorService;

    @Autowired
    private ProductService productService;

    @Test
    void contextLoads() {
    }

}
