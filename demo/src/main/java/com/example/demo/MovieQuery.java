package com.example.demo;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class MovieQuery {
    private static final String API_KEY = "0b4e744f6953e2f4d1ef85a812a7f2cd";
    private static final String TMDB_SEARCH_URL = "https://api.themoviedb.org/3/search/movie";
    private static final String TMDB_MOVIE_DETAIL_URL = "https://api.themoviedb.org/3/movie/";

    private String searchKeyword;
    private String language;
    private int page = 1;

    public MovieQuery(String searchKeyword, String language) {
        this.searchKeyword = searchKeyword;
        this.language = language;
    }

    public LinkedHashMap<String, String> query() {
        LinkedHashMap<String, String> results = new LinkedHashMap<>();
        try {
            List<Movie> movies = new ArrayList<>();

            while (true) {
                String url = UriComponentsBuilder.fromHttpUrl(TMDB_SEARCH_URL)
                        .queryParam("api_key", API_KEY)
                        .queryParam("query", searchKeyword)
                        .queryParam("language", language)
                        .queryParam("page", page)
                        .toUriString();

                RestTemplate restTemplate = new RestTemplate();
                TMDBResponse response = restTemplate.getForObject(url, TMDBResponse.class);

                if (response != null && response.getResults() != null && response.getResults().length > 0) {
                    for (Movie movie : response.getResults()) {
                        if (movie.getTitle().toLowerCase().contains(searchKeyword.toLowerCase())) {
                            movies.add(movie);
                        }
                    }
                    this.page++;
                    if (page > response.getTotalPages()) {
                        break;
                    }
                } else {
                    break;
                }
            }

            ExecutorService executor = Executors.newFixedThreadPool(10);
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (Movie movie : movies) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        String detailUrl = TMDB_MOVIE_DETAIL_URL + movie.getId() +
                                "?api_key=" + API_KEY + "&language=" + language;

                        RestTemplate restTemplate = new RestTemplate();
                        MovieDetail detail = restTemplate.getForObject(detailUrl, MovieDetail.class);

                        if (detail != null) {
                            movie.setRevenue(detail.getRevenue());
                        }
                    } catch (Exception e) {
                        System.err.println("Error fetching movie details: " + e.getMessage());
                    }
                }, executor);
                futures.add(future);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            executor.shutdown();

            movies.sort((m1, m2) -> Long.compare(m2.getRevenue(), m1.getRevenue()));

            for (Movie movie : movies) {
                String googleSearchUrl = "https://www.google.com/search?q=" +
                        UriUtils.encodeQueryParam(movie.getTitle(), StandardCharsets.UTF_8);
                results.put(movie.getTitle(), googleSearchUrl);
            }

        } catch (Exception e) {
            System.err.println("Error querying TMDB API: " + e.getMessage());
            e.printStackTrace();
        }

        return results;
    }

    public static class TMDBResponse {
        private Movie[] results;

        @JsonProperty("total_pages")
        private int totalPages;

        public Movie[] getResults() {
            return results;
        }

        public void setResults(Movie[] results) {
            this.results = results;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        @Override
        public String toString() {
            return "TMDBResponse{" +
                    "results=" + Arrays.toString(results) +
                    ", totalPages=" + totalPages +
                    '}';
        }
    }

    public static class Movie {
        private String title;
        private int id;
        private long revenue;

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

        public long getRevenue() {
            return revenue;
        }

        public void setRevenue(long revenue) {
            this.revenue = revenue;
        }
    }

    public static class MovieDetail {
        private long revenue;

        public long getRevenue() {
            return revenue;
        }

        public void setRevenue(long revenue) {
            this.revenue = revenue;
        }
    }
}










