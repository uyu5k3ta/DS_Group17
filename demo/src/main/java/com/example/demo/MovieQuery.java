package com.example.demo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;



public class MovieQuery {
    private static final String API_KEY = "0b4e744f6953e2f4d1ef85a812a7f2cd"; // 替換為您的 TMDB API Key
    private static final String TMDB_SEARCH_URL = "https://api.themoviedb.org/3/search/movie";

    private String searchKeyword;
    private int page;

    public MovieQuery(String searchKeyword, int page) {
        this.searchKeyword = searchKeyword;
        this.page = page;
    }

    public MovieQuery(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public HashMap<String, String> query() {
        HashMap<String, String> results = new HashMap<>();
        try {
            String url = TMDB_SEARCH_URL + "?api_key=" + API_KEY +
                         "&query=" + searchKeyword +
                         "&language=en-US&page=" + page;

            Document response = Jsoup.connect(url).ignoreContentType(true).get();
            String json = response.text();

            // 使用 Jackson 解析 JSON
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(json).get("results");

            if (root != null) {
                for (com.fasterxml.jackson.databind.JsonNode movie : root) {
                    String title = movie.get("title").asText();
                    String id = movie.get("id").asText();
                    results.put(title, "https://www.themoviedb.org/movie/" + id);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    public HashMap<String, Integer> fetchAndRankWebsites() {
        HashMap<String, Integer> siteRankings = new HashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(10); // 限制執行緒數量

        try {
            // Google 搜尋 URL
            String searchUrl = "https://www.google.com/search?q=" + searchKeyword + "&num=10";

            // 抓取 Google 搜尋結果
            Document document = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(10000)
                    .get();

            // 提取有效 URL
            Elements links = document.select("a[href]");
            List<Callable<Void>> tasks = new ArrayList<>();

            for (Element link : links) {
                String url = link.attr("href");
                if (!url.startsWith("http") || url.contains("google.com")) {
                    continue; // 過濾無效 URL 和 Google 自身鏈接
                }

                tasks.add(() -> {
                    try {
                        Document siteDoc = Jsoup.connect(url)
                                .userAgent("Mozilla/5.0")
                                .timeout(5000)
                                .get();
                        String text = siteDoc.body().text();
                        int count = countKeywordOccurrences(text, searchKeyword);
                        synchronized (siteRankings) {
                            if (count > 0) {
                                siteRankings.put(url, count);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error fetching site: " + url);
                    }
                    return null;
                });
            }

            // 執行多執行緒任務
            executor.invokeAll(tasks);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }

        // 按出現次數排序結果
        return siteRankings.entrySet()
                .stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        HashMap::new
                ));
    }

    // 計算關鍵字在文本中出現的次數
    private int countKeywordOccurrences(String text, String keyword) {
        int count = 0, index = 0;
        while ((index = text.toLowerCase().indexOf(keyword.toLowerCase(), index)) != -1) {
            count++;
            index += keyword.length();
        }
        return count;
    }
}
















