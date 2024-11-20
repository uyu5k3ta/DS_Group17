package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@SpringBootApplication
@Controller
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @GetMapping("/")
    public String index() {
        return "search";
    }

    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestBody Map<String, String> request) {
        String keyword = request.get("keyword");
        String language = request.get("language");

        System.out.println("Search Keyword: " + keyword + " | Language: " + language);

        MovieQuery query = new MovieQuery(keyword, language);
        HashMap<String, String> results = query.query();

        List<Map<String, String>> movieResults = new ArrayList<>();
        results.forEach((title, googleSearchUrl) -> {
            Map<String, String> movie = new HashMap<>();
            movie.put("title", title);
            movie.put("googleSearchUrl", googleSearchUrl);
            movieResults.add(movie);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("results", movieResults);

        return ResponseEntity.ok(response);
    }
}




