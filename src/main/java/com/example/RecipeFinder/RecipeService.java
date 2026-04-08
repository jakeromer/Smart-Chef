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

@Service
public class RecipeService {

    private List<RecipeData> allRecipes = new ArrayList<>();

    public RecipeService() throws IOException {
        loadRecipes();
    }

    private void loadRecipes() throws IOException {
        InputStream inputStream = new ClassPathResource("AI_Recipes_Test.xlsx").getInputStream();
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                String name = getCellValue(row.getCell(0));
                String ingredients = getCellValue(row.getCell(1));
                String calories = getCellValue(row.getCell(2));
                String recipeLink = getCellValue(row.getCell(3));
                String prepTime = getCellValue(row.getCell(4));
                String cookTime = getCellValue(row.getCell(5));

                allRecipes.add(new RecipeData(name, ingredients, calories, recipeLink, prepTime, cookTime));
            }
        }
        workbook.close();
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return cell.getCellFormula();
                }
            default:
                return "";
        }
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
                    searchIng = searchIng.trim();
                    if (recipeIng.contains(searchIng) || searchIng.contains(recipeIng)) {
                        matchCount++;
                        break;
                    }
                }
            }

            if (matchCount > 0) {
                int missingCount = recipeIngredients.length - matchCount;
                results.add(new RecipeResult(
                    results.size() + 1,
                    recipe.name,
                    matchCount,
                    missingCount,
                    recipe.recipeLink,
                    recipe.calories,
                    recipe.prepTime,
                    recipe.cookTime
                ));
            }
        }

        results.sort((a, b) -> Integer.compare(b.usedIngredients, a.usedIngredients));
        return results.subList(0, Math.min(25, results.size()));
    }

    private static class RecipeData {
        String name, ingredients, calories, recipeLink, prepTime, cookTime;

        RecipeData(String name, String ingredients, String calories, String recipeLink, String prepTime, String cookTime) {
            this.name = name;
            this.ingredients = ingredients;
            this.calories = calories;
            this.recipeLink = recipeLink;
            this.prepTime = prepTime;
            this.cookTime = cookTime;
        }
    }
}