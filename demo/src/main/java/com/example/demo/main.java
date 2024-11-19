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

@SpringBootApplication
@Controller
public class main {

    public static void main(String[] args) {
        SpringApplication.run(main.class, args);
    }

    @GetMapping("/")
    public String index() {
        return "search"; // 回傳前端頁面
    }

    @PostMapping("/search")
    public ResponseEntity<HashMap<String, String>> search(@RequestBody Map<String, String> request) {
        String keyword = request.get("keyword");
        System.out.println("Search Keyword: " + keyword);
        
        // 使用 TMDB API 查詢電影名稱
        MovieQuery query = new MovieQuery(keyword);
        HashMap<String, String> results = query.query();

        return ResponseEntity.ok(results);
    }
}




