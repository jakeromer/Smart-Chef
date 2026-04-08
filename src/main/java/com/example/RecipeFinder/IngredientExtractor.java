package com.example.recipefinder;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Component
public class IngredientExtractor {
    
    private static String CLAUDE_API_KEY;
    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    
    @Value("${claude.api.key}")
    public void setApiKey(String apiKey) {
        CLAUDE_API_KEY = apiKey;
    }
    
    public static String extractIngredients(String userInput) {
        OkHttpClient client = new OkHttpClient();
        
        String prompt = "You are an ingredient parser. The user will describe what ingredients they have, " +
               "possibly in natural language (e.g. 'I have some potatoes and eggs') or with typos " +
               "(e.g. 'pottaoes', 'chiken', 'tomatoe'). " +
               "Your job is to: " +
               "1. Understand what ingredients they mean, even with spelling mistakes or casual phrasing. " +
               "2. Correct any typos to the proper ingredient name. " +
               "3. Return ONLY a comma-separated list of clean, correctly spelled ingredient names. " +
               "4. Do not include quantities, measurements, filler words, or any explanation. " +
               "Examples: " +
               "'I have pottaoes and some chiken' → 'potatoes, chicken' " +
               "'2 cups of flowr, 3 egs, and buttr' → 'flour, eggs, butter' " +
               "'tomatoe, garic, onoin' → 'tomato, garlic, onion' " +
               "Now parse this: " + userInput;
        
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "claude-sonnet-4-20250514");
        requestBody.put("max_tokens", 100);
        
        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);
        messages.put(message);
        requestBody.put("messages", messages);
        
        RequestBody body = RequestBody.create(
            requestBody.toString(),
            MediaType.parse("application/json")
        );
        
        Request request = new Request.Builder()
            .url(CLAUDE_API_URL)
            .addHeader("x-api-key", CLAUDE_API_KEY)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("content-type", "application/json")
            .post(body)
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                JSONObject responseJson = new JSONObject(response.body().string());
                JSONArray content = responseJson.getJSONArray("content");
                if (content.length() > 0) {
                    return content.getJSONObject(0).getString("text").trim();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return userInput; // Fallback if extraction fails
    }
}