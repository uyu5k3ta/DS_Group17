package com.example.demo;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import java.nio.charset.StandardCharsets;

import java.util.HashMap;

public class MovieQuery {
    private static final String API_KEY = "0b4e744f6953e2f4d1ef85a812a7f2cd";
    private static final String TMDB_SEARCH_URL = "https://api.themoviedb.org/3/search/movie";

    private String searchKeyword;
    private String language;
    private int page;
    private int totalPages;

    public MovieQuery(String searchKeyword, String language, int page) {
        this.searchKeyword = searchKeyword;
        this.language = language;
        this.page = page;
    }

    public HashMap<String, String> query() {
        HashMap<String, String> results = new HashMap<>();
        try {
            int page = 1;
            while (true) {
                String url = UriComponentsBuilder.fromHttpUrl(TMDB_SEARCH_URL)
                        .queryParam("api_key", API_KEY)
                        .queryParam("query", searchKeyword)
                        .queryParam("language", language)
                        .queryParam("page", String.valueOf(page))
                        .toUriString();

                RestTemplate restTemplate = new RestTemplate();
                TMDBResponse response = restTemplate.getForObject(url, TMDBResponse.class);

                if (response != null && response.getResults() != null && response.getResults().length > 0) {
                    for (Movie movie : response.getResults()) {
                        String googleSearchUrl = "https://www.google.com/search?q=" + UriUtils.encodeQueryParam(movie.getTitle(), StandardCharsets.UTF_8);
                        results.put(movie.getTitle(), googleSearchUrl);
                    }
                    page++;
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error querying TMDB API: " + e.getMessage());
        }

        return results;
    }
    
    public int getTotalPages() {
        return totalPages;
    }

    public static class TMDBResponse {
        private Movie[] results;
        private int total_pages;

        public Movie[] getResults() {
            return results;
        }

        public void setResults(Movie[] results) {
            this.results = results;
        }

        public int getTotalPages() {
            return total_pages;
        }

        public void setTotalPages(int total_pages) {
            this.total_pages = total_pages;
        }
    }

    public static class Movie {
        private String title;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}






