package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
@SpringBootApplication
@Controller

public class Main {

	public static void main(String[] args) {
	    SpringApplication.run(Main.class, args);
	}
    // 根頁面顯示 search.html
    @GetMapping("/")
    public String index() {
        return "search"; // 返回 search.html
    }

    // 排序頁面，顯示 ranking.html 並傳遞電影名稱參數
    @GetMapping("/ranking")
    public String ranking(@RequestParam("movie") String movie, Model model) {
        model.addAttribute("movie", movie); // 將電影名稱傳遞到 ranking.html
        return "ranking"; // 返回 ranking.html
    }

    // 返回 TMDB 查詢結果
    @PostMapping("/search")
    public ResponseEntity<HashMap<String, String>> search(@RequestBody HashMap<String, String> request) {
        String keyword = request.get("keyword");
        int page = Integer.parseInt(request.getOrDefault("page", "1"));

        MovieQuery query = new MovieQuery(keyword, page);
        HashMap<String, String> results = query.query();

        return ResponseEntity.ok(results);
    }

    // 返回基於電影名稱的網頁排序結果
    @PostMapping("/ranked-urls")
    public ResponseEntity<HashMap<String, Integer>> rankedUrls(@RequestBody HashMap<String, String> request) {
        String keyword = request.get("keyword");
        MovieQuery query = new MovieQuery(keyword);
        HashMap<String, Integer> rankedResults = query.fetchAndRankWebsites();

        return ResponseEntity.ok(rankedResults);
    }
}












