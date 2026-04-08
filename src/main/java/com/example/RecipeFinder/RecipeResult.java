package com.example.recipefinder;

public class RecipeResult {
    public int id;
    public String title;
    public int usedIngredients;
    public int missingIngredients;
    public String link;
    public String calories;
    public String prepTime;
    public String cookTime;

    public RecipeResult(int id, String title, int used, int missed, String link, String calories, String prepTime, String cookTime) {
        this.id = id;
        this.title = title;
        this.usedIngredients = used;
        this.missingIngredients = missed;
        this.link = link;
        this.calories = calories;
        this.prepTime = prepTime;
        this.cookTime = cookTime;
    }
}