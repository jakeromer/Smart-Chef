package com.example.recipefinder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
public class ImageService {

    @Value("${spoonacular.api.key}")
    private String apiKey;

    private Map<String, String> imageCache = new HashMap<>();

    public String getImageForRecipe(String recipeName) {
        if (imageCache.containsKey(recipeName)) {
            return imageCache.get(recipeName);
        }

        try {
            OkHttpClient client = new OkHttpClient();
            String url = "https://api.spoonacular.com/recipes/search?query=" +
                recipeName.replace(" ", "+") + "&number=1&apiKey=" + apiKey;

            Request request = new Request.Builder().url(url).build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    JSONObject json = new JSONObject(response.body().string());
                    JSONArray results = json.getJSONArray("results");
                    if (results.length() > 0) {
                        JSONObject first = results.getJSONObject(0);
                        if (!first.isNull("image")) {
                            String baseUrl = json.getString("baseUri");
                            String image = first.getString("image");
                            String fullUrl = baseUrl + image;
                            imageCache.put(recipeName, fullUrl);
                            return fullUrl;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String fallback = "https://placehold.co/400x300/e07b39/white?text=" + recipeName.replace(" ", "+");
        imageCache.put(recipeName, fallback);
        return fallback;
    }
}