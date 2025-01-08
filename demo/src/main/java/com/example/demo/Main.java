package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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

    @GetMapping("/ranking")
    public String ranking(@RequestParam("movie") String movie, Model model) {
        model.addAttribute("movie", movie);
        return "ranking";
    }

    @PostMapping("/search")
    public Object search(@RequestBody HashMap<String, String> request) {
        String keyword = request.get("keyword");
        int page = Integer.parseInt(request.getOrDefault("page", "1"));

        if (keyword == null || keyword.isEmpty()) {
            return ResponseEntity.badRequest().body("Keyword cannot be empty.");
        }

        MovieQuery query = new MovieQuery(keyword, page);
        HashMap<String, String> results = query.query();

        if (results.isEmpty()) {
            return new RedirectView("/ranking?movie=" + keyword);
        }

        return ResponseEntity.ok(results);
    }

    @PostMapping("/ranked-urls")
    public ResponseEntity<HashMap<String, Object>> rankedUrls(@RequestBody HashMap<String, String> request) {

        String keyword = request.get("keyword");
        if (keyword == null || keyword.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        MovieQuery query = new MovieQuery(keyword);
        HashMap<String, Object> rankedResults = query.fetchWebsitesWithKeywordsAndSubpagesSorted();

        HashMap<String, Object> formattedResults = new HashMap<>();
        Map<String, Map<String, Object>> siteData = (Map<String, Map<String, Object>>) rankedResults.get("siteData");

        Map<String, Map<String, String>> formattedSiteData = new LinkedHashMap<>();
        if (siteData != null) {
            for (Map.Entry<String, Map<String, Object>> entry : siteData.entrySet()) {
                String url = entry.getKey();
                Map<String, Object> pageInfo = entry.getValue();
                String title = (String) pageInfo.getOrDefault("title", "No Title");

                Map<String, String> siteInfo = new HashMap<>();
                siteInfo.put("title", title);
                siteInfo.put("url", url);

                formattedSiteData.put(url, siteInfo);
            }
        }

        formattedResults.put("siteData", formattedSiteData);
        formattedResults.put("relatedKeywords", rankedResults.get("relatedKeywords"));

        return ResponseEntity.ok(formattedResults);
    }


}



























