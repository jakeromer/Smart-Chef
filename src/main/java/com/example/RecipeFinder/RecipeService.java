package com.example.recipefinder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class RecipeService {

    private List<RecipeData> allRecipes = new ArrayList<>();

    public RecipeService() throws IOException {
        loadRecipes();
    }

    private void loadRecipes() throws IOException {
        InputStream inputStream = new ClassPathResource("AI_Updated_Recipes.xlsx").getInputStream();
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        // Skip header row, start from row 1
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                String name = getCellValue(row.getCell(0));
                String ingredients = getCellValue(row.getCell(1));
                String imageUrl = getCellValue(row.getCell(2));
                String recipeLink = getCellValue(row.getCell(3));
                
                allRecipes.add(new RecipeData(name, ingredients, imageUrl, recipeLink));
            }
        }
        workbook.close();
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : "";
    }

    public List<RecipeResult> searchRecipes(String userIngredients) {
        String[] searchIngredients = userIngredients.toLowerCase().split(",");
        List<RecipeResult> results = new ArrayList<>();

        for (RecipeData recipe : allRecipes) {
            String[] recipeIngredients = recipe.ingredients.toLowerCase().split(",");
            
            int matchCount = 0;
            List<String> matchedRecipeIngredients = new ArrayList<>();
            
            for (String recipeIng : recipeIngredients) {
                recipeIng = recipeIng.trim();
                boolean found = false;
                
                for (String searchIng : searchIngredients) {
                    searchIng = searchIng.trim();
                    // Check if either ingredient contains the other
                    if (recipeIng.contains(searchIng) || searchIng.contains(recipeIng)) {
                        found = true;
                        break;
                    }
                }
                
                if (found) {
                    matchCount++;
                    matchedRecipeIngredients.add(recipeIng);
                }
            }

            if (matchCount > 0) {
                int missingCount = recipeIngredients.length - matchCount;
                results.add(new RecipeResult(
                    results.size() + 1,
                    recipe.name,
                    recipe.imageUrl.isEmpty() ? "https://via.placeholder.com/312x231?text=No+Image" : recipe.imageUrl,
                    matchCount,
                    missingCount,
                    recipe.recipeLink
                ));
            }
        }

        // Sort by most matching ingredients
        results.sort((a, b) -> Integer.compare(b.usedIngredients, a.usedIngredients));
        
        // Return top 25
        return results.subList(0, Math.min(25, results.size()));
    }

    private static class RecipeData {
        String name;
        String ingredients;
        String imageUrl;
        String recipeLink;

        RecipeData(String name, String ingredients, String imageUrl, String recipeLink) {
            this.name = name;
            this.ingredients = ingredients;
            this.imageUrl = imageUrl;
            this.recipeLink = recipeLink;
        }
    }
}