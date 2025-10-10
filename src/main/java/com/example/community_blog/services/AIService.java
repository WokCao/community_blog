package com.example.community_blog.services;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AIService {
    @Value("${huggingface.url}")
    private String HF_URL;
    @Value("${huggingface.token}")
    private String HF_TOKEN;

    public Set<String> extractTags(String title, String content) {
        Set<String> tags = new HashSet<>();
        RestTemplate rest = new RestTemplate();

        try {
            String inputText = title + ". " + content;

            JSONObject body = new JSONObject();
            body.put("inputs", inputText);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.setBearerAuth(HF_TOKEN);

            HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

            ResponseEntity<String> response = rest.exchange(HF_URL, HttpMethod.POST, request, String.class);


            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JSONArray arr = new JSONArray(response.getBody());

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    if (obj.has("word")) {
                        String tag = obj.getString("word").trim();
                        if (!tag.isEmpty()) tags.add(capitalize(tag));
                    }
                }
            } else {
                System.err.println("HF API failed: " + response.getStatusCode());
            }

        } catch (Exception e) {
            tags.add("Programming");
        }

        if (tags.isEmpty()) tags.add("Programming");
        return tags;
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
