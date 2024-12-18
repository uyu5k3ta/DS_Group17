package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;



import org.springframework.web.bind.annotation.PostMapping;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@Controller
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @GetMapping("/")
    public String index() {
        return "search"; // 返回搜索頁面
    }

    // 搜索電影名稱並返回 TMDB 查詢結果
    @PostMapping("/search")
    public ResponseEntity<HashMap<String, String>> search(@RequestBody Map<String, String> request) {
        String keyword = request.get("keyword");
        int page = Integer.parseInt(request.getOrDefault("page", "1"));

        System.out.println("Search Keyword: " + keyword + " Page: " + page);

        MovieQuery query = new MovieQuery(keyword, page);
        HashMap<String, String> results = query.query();

        return ResponseEntity.ok(results);
    }

    @GetMapping("/ranking")
    public String ranking(@RequestParam("movie") String movie, Model model) {
        model.addAttribute("movie", movie); // 將電影名稱傳遞到模板
        return "ranking"; // 返回 ranking.html（位於 templates 目錄下）
    }
    // 基於電影名稱關鍵字爬取並返回排序後的網站結果
    @PostMapping("/ranked-urls")
    public ResponseEntity<HashMap<String, Integer>> rankedUrls(@RequestBody Map<String, String> request) {
        String keyword = request.get("keyword");
        System.out.println("Ranking URLs for Keyword: " + keyword);

        MovieQuery query = new MovieQuery(keyword);
        HashMap<String, Integer> rankedResults = query.fetchAndRankWebsites();

        HashMap<String, Integer> sortedResults = rankedResults.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), Map::putAll);

        return ResponseEntity.ok(sortedResults);
    }
}











