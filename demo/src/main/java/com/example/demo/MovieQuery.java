package com.example.demo;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;

public class MovieQuery {
    private static final String API_KEY = "0b4e744f6953e2f4d1ef85a812a7f2cd"; // 替換成你的 TMDB API Key
    private static final String TMDB_SEARCH_URL = "https://api.themoviedb.org/3/search/movie";

    private String searchKeyword;

    // Constructor 初始化搜尋關鍵字
    public MovieQuery(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    // 使用 TMDB API 搜尋電影名稱
    public HashMap<String, String> query() {
        HashMap<String, String> results = new HashMap<>();
        try {
            // 建立請求 URL
            String url = UriComponentsBuilder.fromHttpUrl(TMDB_SEARCH_URL)
                    .queryParam("api_key", API_KEY)
                    .queryParam("query", searchKeyword)
                    .queryParam("language", "zh-TW") // 設定語言為繁體中文
                    .queryParam("page", "1")
                    .toUriString();

            // 發送 GET 請求到 TMDB API
            RestTemplate restTemplate = new RestTemplate();
            TMDBResponse response = restTemplate.getForObject(url, TMDBResponse.class);

            // 處理 API 回應，提取電影名稱
            if (response != null && response.getResults() != null) {
                // 使用 for 迴圈遍歷陣列
                for (Movie movie : response.getResults()) {
                    results.put(movie.getTitle(), ""); // 只添加電影名稱
                }
            }
        } catch (Exception e) {
            System.err.println("Error querying TMDB API: " + e.getMessage());
        }

        return results; // 回傳只包含電影名稱的結果
    }

    // 對應 TMDB API 回應的內部類別
    private static class TMDBResponse {
        private Movie[] results;

        public Movie[] getResults() {
            return results;
        }

        public void setResults(Movie[] results) {
            this.results = results;
        }
    }

    private static class Movie {
        private String title;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}





