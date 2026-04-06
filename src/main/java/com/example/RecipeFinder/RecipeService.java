package com.example.recipefinder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class RecipeService {

    private List<RecipeData> allRecipes = new ArrayList<>();

    @PostConstruct
    public void init() throws IOException {
        loadRecipes();
    }

    private void loadRecipes() throws IOException {
        try (InputStream inputStream = new ClassPathResource("AI_Updated_Recipes.xlsx").getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    String name = getCellValue(row.getCell(0));
                    String ingredients = getCellValue(row.getCell(1));
                    String recipeLink = getCellValue(row.getCell(3));
                    allRecipes.add(new RecipeData(name, ingredients, recipeLink));
                }
            }
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> "";
        };
    }

    public List<RecipeResult> searchRecipes(String userIngredients) {
        String[] searchIngredients = userIngredients.toLowerCase().split(",");
        List<RecipeResult> results = new ArrayList<>();

        for (RecipeData recipe : allRecipes) {
            String[] recipeIngredients = recipe.ingredients.toLowerCase().split(",");
            int matchCount = 0;

            for (String recipeIng : recipeIngredients) {
                recipeIng = recipeIng.trim();
                for (String searchIng : searchIngredients) {
                    if (recipeIng.contains(searchIng.trim())) {
                        matchCount++;
                        break;
                    }
                }
            }

            if (matchCount > 0) {
                results.add(new RecipeResult(
                    results.size() + 1,
                    recipe.name,
                    matchCount,
                    recipeIngredients.length - matchCount,
                    recipe.recipeLink
                ));
            }
        }

        results.sort((a, b) -> Integer.compare(b.usedIngredients, a.usedIngredients));
        return results.subList(0, Math.min(25, results.size()));
    }

    private static class RecipeData {
        String name;
        String ingredients;
        String recipeLink;

        RecipeData(String name, String ingredients, String recipeLink) {
            this.name = name;
            this.ingredients = ingredients;
            this.recipeLink = recipeLink;
        }
    }
}