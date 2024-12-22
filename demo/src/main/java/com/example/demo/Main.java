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
import java.util.Map;

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

    @PostMapping("/search")
    public Object search(@RequestBody HashMap<String, String> request) {
        String keyword = request.get("keyword");
        int page = Integer.parseInt(request.getOrDefault("page", "1"));

        if (keyword == null || keyword.isEmpty()) {
            return ResponseEntity.badRequest().body("Keyword cannot be empty.");
        }

        MovieQuery query = new MovieQuery(keyword, page);
        HashMap<String, String> results = query.query();

        // 如果結果為空，直接返回 RedirectView 進行跳轉
        if (results.isEmpty()) {
            return new RedirectView("/ranking?movie=" + keyword);
        }

        return ResponseEntity.ok(results);
    }





    // 返回基於電影名稱的網頁排序結果
    @PostMapping("/ranked-urls")
    public ResponseEntity<HashMap<String, Object>> rankedUrls(@RequestBody HashMap<String, String> request) {
        // 獲取請求中的關鍵字
        String keyword = request.get("keyword");
        if (keyword == null || keyword.isEmpty()) {
            return ResponseEntity.badRequest().build(); // 如果關鍵字為空，返回 400 錯誤
        }

        // 創建 MovieQuery 對象並執行抓取
        MovieQuery query = new MovieQuery(keyword);
        HashMap<String, Object> rankedResults = query.fetchWebsitesWithKeywordsAndSubpagesSorted();

        // 返回抓取結果
        return ResponseEntity.ok(rankedResults);
    }
}



























