package com.example.recipefinder;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private IngredientExtractor ingredientExtractor;

    @GetMapping("/recipes")
    public List<RecipeResult> getRecipes(@RequestParam String ingredients) throws IOException {
        System.out.println("Raw input: " + ingredients);
        String cleanedIngredients = ingredientExtractor.extractIngredients(ingredients);
        System.out.println("Claude cleaned: " + cleanedIngredients);
        return recipeService.searchRecipes(cleanedIngredients);
    }
}