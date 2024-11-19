package com.example.demo;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;

public class MovieQuery {
    private static final String API_KEY = "0b4e744f6953e2f4d1ef85a812a7f2cd"; // 替換成你的 TMDB API Key
    private static final String TMDB_SEARCH_URL = "https://api.themoviedb.org/3/search/movie";

    private String searchKeyword;


    public MovieQuery(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }


    public HashMap<String, String> query() {
        HashMap<String, String> results = new HashMap<>();
        try {

            String url = UriComponentsBuilder.fromHttpUrl(TMDB_SEARCH_URL)
                    .queryParam("api_key", API_KEY)
                    .queryParam("query", searchKeyword)
                    .queryParam("language", "zh-TW")
                    .queryParam("page", "1")
                    .toUriString();

            RestTemplate restTemplate = new RestTemplate();
            TMDBResponse response = restTemplate.getForObject(url, TMDBResponse.class);

            if (response != null && response.getResults() != null) {

                for (Movie movie : response.getResults()) {
                    results.put(movie.getTitle(), "");
                }
            }
        } catch (Exception e) {
            System.err.println("Error querying TMDB API: " + e.getMessage());
        }

        return results;
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

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}





