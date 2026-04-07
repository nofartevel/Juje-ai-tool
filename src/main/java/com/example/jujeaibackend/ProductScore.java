package com.example.jujeaibackend;

public class ProductScore {
    private Product product;
    private int score;
    private int strongMatches;
    private int weakMatches;
    private boolean sectionMatch;

    public ProductScore(Product product, int score, int strongMatches, int weakMatches, boolean sectionMatch) {
        this.product = product;
        this.score = score;
        this.strongMatches = strongMatches;
        this.weakMatches = weakMatches;
        this.sectionMatch = sectionMatch;
    }

    public Product getProduct() {
        return product;
    }

    public int getScore() {
        return score;
    }

    public int getStrongMatches() {
        return strongMatches;
    }

    public int getWeakMatches() {
        return weakMatches;
    }

    public boolean isSectionMatch() {
        return sectionMatch;
    }
}