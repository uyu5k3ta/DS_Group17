package com.example.demo;

import edu.stanford.nlp.simple.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class MovieQuery {
    private static final String API_KEY = "0b4e744f6953e2f4d1ef85a812a7f2cd";
    private static final String TMDB_SEARCH_URL = "https://api.themoviedb.org/3/search/movie";
    private static final int THREAD_POOL_SIZE = 50;

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
    public HashMap<String, Object> fetchWebsitesWithKeywordsAndSubpagesSorted() {
        HashMap<String, Map<String, Object>> siteData = new HashMap<>();
        Map<String, Integer> globalKeywordFrequency = new HashMap<>();
        List<String> urls = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        int currentPage = 1;

        try {
            while (urls.size() < 10) {
                String searchUrl = "https://www.google.com/search?q=" + searchKeyword + "+movie" + "&start=" + ((currentPage - 1) * 10);
                Document document = Jsoup.connect(searchUrl)
                        .userAgent(getRandomUserAgent())
                        .timeout(5000)
                        .get();

                Elements links = document.select("a[href]");
                for (Element link : links) {
                    String url = link.attr("href").replace("/url?q=", "").split("&")[0];
                    if (!url.startsWith("http") || url.contains("google.com")) {
                        continue;
                    }
                    if (!urls.contains(url)) {
                        urls.add(url);
                    }
                    if (urls.size() >= 10) {
                        break;
                    }
                }

                currentPage++;
            }

            List<Callable<Void>> tasks = new ArrayList<>();
            for (String url : urls) {
                tasks.add(() -> {
                    try {
                        Map<String, Object> pageData = fetchPageData(url);
                        String title = fetchTitle(url);
                        synchronized (siteData) {
                            Map<String, Object> pageInfo = new HashMap<>();
                            pageInfo.put("title", title);
                            pageInfo.put("url", url);
                            pageInfo.putAll(pageData);
                            siteData.put(url, pageInfo);
                        }
                        synchronized (globalKeywordFrequency) {
                            Map<String, Integer> localFrequency = (Map<String, Integer>) pageData.get("keywordFrequency");
                            localFrequency.forEach((word, count) ->
                                    globalKeywordFrequency.put(word, globalKeywordFrequency.getOrDefault(word, 0) + count)
                            );
                        }
                    } catch (Exception e) {
                        System.err.println("Error fetching data for: " + url);
                    }
                    return null;
                });
            }

            executor.invokeAll(tasks);
            System.out.println("Completed crawling all pages.");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }

        List<Map.Entry<String, Map<String, Object>>> sortedSites = siteData.entrySet().stream()
                .sorted((entry1, entry2) -> {
                    int count1 = (int) entry1.getValue().getOrDefault("keywordCount", 0);
                    int count2 = (int) entry2.getValue().getOrDefault("keywordCount", 0);
                    return Integer.compare(count2, count1);
                })
                .toList();

        LinkedHashMap<String, Map<String, Object>> sortedSiteData = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Object>> entry : sortedSites) {
            sortedSiteData.put(entry.getKey(), entry.getValue());
        }

        HashMap<String, Object> result = new HashMap<>();
        result.put("siteData", sortedSiteData);
        result.put("relatedKeywords", getTopNounKeywords(globalKeywordFrequency));
        return result;
    }

    private String fetchTitle(String url) {
        try {
            Document document = Jsoup.connect(url)
                    .userAgent(getRandomUserAgent())
                    .timeout(5000)
                    .get();
            return document.title();
        } catch (Exception e) {
            System.err.println("Error fetching title for: " + url);
            return "Unknown Title";
        }
    }



    private List<Map<String, Object>> getTopNounKeywords(Map<String, Integer> keywordFrequency) {
        List<Map<String, Object>> nounKeywords = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : keywordFrequency.entrySet()) {
            String word = entry.getKey();
            int count = entry.getValue();

            Sentence sentence = new Sentence(word);
            String posTag = sentence.posTag(0);

            if (posTag.startsWith("NN")) {
                Map<String, Object> keywordData = new HashMap<>();
                keywordData.put("key", word);
                keywordData.put("value", count);
                nounKeywords.add(keywordData);
            }
        }

        return nounKeywords.stream()
                .sorted((e1, e2) -> ((int) e2.get("value")) - ((int) e1.get("value")))
                .limit(10)
                .collect(Collectors.toList());
    }

    private Map<String, Object> fetchPageData(String url) {
        Map<String, Object> pageData = new HashMap<>();
        List<Map<String, Object>> subpages = new ArrayList<>();
        final int[] totalKeywordCount = {0};
        Map<String, Integer> keywordFrequency = new HashMap<>();

        try {
        	randomDelay(2000, 5000); 
            Document document = Jsoup.connect(url)
                    .userAgent(getRandomUserAgent())
                    .timeout(5000)
                    .get();

            String text = document.body().text();
            int motherKeywordCount = countKeywordOccurrences(text, searchKeyword);
            totalKeywordCount[0] += motherKeywordCount;

            updateKeywordFrequency(text, keywordFrequency);

            Elements links = document.select("a[href]");
            List<Callable<Void>> subpageTasks = new ArrayList<>();
            for (Element link : links) {
                String subUrl = link.attr("href").replace("/url?q=", "").split("&")[0];
                if (!subUrl.startsWith("http") || subUrl.contains("google.com")) {
                    continue;
                }
                subpageTasks.add(() -> {
                    try {
                        Document subDocument = Jsoup.connect(subUrl)
                                .userAgent(getRandomUserAgent())
                                .timeout(5000)
                                .get();

                        String subText = subDocument.body().text();
                        int subKeywordCount = countKeywordOccurrences(subText, searchKeyword);

                        synchronized (pageData) {
                            totalKeywordCount[0] += subKeywordCount;
                            Map<String, Object> subpageData = new HashMap<>();
                            subpageData.put("url", subUrl);
                            subpageData.put("keywordCount", subKeywordCount);
                            subpages.add(subpageData);

                            updateKeywordFrequency(subText, keywordFrequency);
                        }
                    } catch (Exception e) {
                        System.err.println("Error fetching subpage data from: " + subUrl);
                    }
                    return null;
                });
            }

            ExecutorService subpageExecutor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            subpageExecutor.invokeAll(subpageTasks);
            subpageExecutor.shutdown();

            System.out.println("完成母頁爬取: " + url);
        } catch (Exception e) {
            System.err.println("Error fetching page data from: " + url);
        }

        pageData.put("keywordCount", totalKeywordCount[0]);
        pageData.put("subpages", subpages);
        pageData.put("keywordFrequency", keywordFrequency);
        return pageData;
    }

    private int countKeywordOccurrences(String text, String keyword) {
        int count = 0, index = 0;
        while ((index = text.toLowerCase().indexOf(keyword.toLowerCase(), index)) != -1) {
            count++;
            index += keyword.length();
        }
        return count;
    }

    private void updateKeywordFrequency(String text, Map<String, Integer> keywordFrequency) {
        String[] words = text.toLowerCase().split("\\W+");
        for (String word : words) {
            if (word.length() > 3) {
                keywordFrequency.put(word, keywordFrequency.getOrDefault(word, 0) + 1);
            }
        }
    }

    private String getRandomUserAgent() {
        String[] userAgents = {
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 12_4_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.5195.125 Safari/537.36",
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:104.0) Gecko/20100101 Firefox/104.0",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:105.0) Gecko/20100101 Firefox/105.0",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 15_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.4 Mobile/15E148 Safari/604.1",
                "Mozilla/5.0 (Linux; Android 12; SM-G996B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.159 Mobile Safari/537.36",
                "Mozilla/5.0 (Linux; Android 10; Pixel 4 Build/QQ3A.200805.001) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.62 Mobile Safari/537.36",
                "Mozilla/5.0 (iPad; CPU OS 15_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.0 Mobile/15E148 Safari/604.1",
                "Mozilla/5.0 (Linux; Android 9; SM-G930V Build/PPR1.180610.011) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/10.1 Chrome/71.0.3578.99 Mobile Safari/537.36",
                "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.61 Safari/537.36",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_5_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36"
        };

        Random random = new Random();
        return userAgents[random.nextInt(userAgents.length)];
    }

    
    private void randomDelay(int minMillis, int maxMillis) {
        Random random = new Random();
        try {
            Thread.sleep(minMillis + random.nextInt(maxMillis - minMillis + 1));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}






