package com.example.recipefinder;

public class RecipeResult {
    public int id;
    public String title;
    public int usedIngredients;
    public int missingIngredients;
    public String link;

    public RecipeResult(int id, String title, int used, int missed, String link) {
        this.id = id;
        this.title = title;
        this.usedIngredients = used;
        this.missingIngredients = missed;
        this.link = link;
    }
}