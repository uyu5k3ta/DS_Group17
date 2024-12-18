package com.example.demo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;

public class MovieQuery {
    private static final String API_KEY = "0b4e744f6953e2f4d1ef85a812a7f2cd";
    private static final String TMDB_SEARCH_URL = "https://api.themoviedb.org/3/search/movie";

    private String searchKeyword;
    private int page;

    // 構造函數支持分頁
    public MovieQuery(String searchKeyword, int page) {
        this.searchKeyword = searchKeyword;
        this.page = page;
    }
    public MovieQuery(String searchKeyword) {
        this.searchKeyword = searchKeyword;
        this.page = 1; // 默認頁碼
    }

    public HashMap<String, String> query() {
        HashMap<String, String> results = new HashMap<>();
        try {
            String url = UriComponentsBuilder.fromHttpUrl(TMDB_SEARCH_URL)
                    .queryParam("api_key", API_KEY)
                    .queryParam("query", searchKeyword)
                    .queryParam("language", "zh-TW")
                    .queryParam("page", page)
                    .toUriString();

            RestTemplate restTemplate = new RestTemplate();
            TMDBResponse response = restTemplate.getForObject(url, TMDBResponse.class);

            if (response != null && response.getResults() != null) {
                for (Movie movie : response.getResults()) {
                    results.put(movie.getTitle(), "https://www.themoviedb.org/movie/" + movie.getId());
                }
            }
        } catch (Exception e) {
            System.err.println("Error querying TMDB API: " + e.getMessage());
        }
        return results;
    }
    public HashMap<String, Integer> fetchAndRankWebsites() {
        HashMap<String, Integer> siteRankings = new HashMap<>();
        try {
            String searchUrl = "https://www.google.com/search?q=" + searchKeyword;
            Document document = Jsoup.connect(searchUrl).userAgent("Mozilla/5.0").get();
            Elements links = document.select("a[href]");

            for (var link : links) {
                String url = link.attr("href");
                if (url.startsWith("http")) {
                    try {
                        Document siteDoc = Jsoup.connect(url).userAgent("Mozilla/5.0").get();
                        String html = siteDoc.body().text();
                        int count = html.split(searchKeyword, -1).length - 1;
                        siteRankings.put(url, count);
                    } catch (Exception e) {
                        System.err.println("Error fetching site: " + url);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching or parsing data: " + e.getMessage());
        }
        return siteRankings;
    }

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
        private int id;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
}















