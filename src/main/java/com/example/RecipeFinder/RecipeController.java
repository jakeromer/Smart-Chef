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

    @GetMapping("/recipes")
    public List<RecipeResult> getRecipes(@RequestParam String ingredients) throws IOException {
        // Use AI to extract clean ingredient list from natural language
        String cleanedIngredients = IngredientExtractor.extractIngredients(ingredients);
        
        // Search the Excel spreadsheet
        return recipeService.searchRecipes(cleanedIngredients);
    }
}