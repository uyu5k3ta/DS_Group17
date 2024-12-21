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
    private static final String API_KEY = "0b4e744f6953e2f4d1ef85a812a7f2cd"; // 替换为您的 TMDB API Key
    private static final String TMDB_SEARCH_URL = "https://api.themoviedb.org/3/search/movie";
    private static final int THREAD_POOL_SIZE = 100; // 增加线程数量以加速爬取

    private String searchKeyword;
    private int page;

    public MovieQuery(String searchKeyword, int page) {
        this.searchKeyword = searchKeyword;
        this.page = page;
    }

    public MovieQuery(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    // 保留原始 TMDB 查询功能
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
    public HashMap<String, Object> fetchWebsitesWithKeywordsAndSubpagesSorted() {
        HashMap<String, Map<String, Object>> siteData = new HashMap<>();
        Map<String, Integer> globalKeywordFrequency = new HashMap<>();
        List<String> urls = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        int currentPage = 1;

        try {
            // 爬取 10 个有效的母页
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

            // 处理每个母页，提取数据并更新全局词频
            List<Callable<Void>> tasks = new ArrayList<>();
            for (String url : urls) {
                tasks.add(() -> {
                    try {
                        Map<String, Object> pageData = fetchPageData(url);
                        synchronized (siteData) {
                            siteData.put(url, pageData);
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
            System.out.println("已完成所有页面爬取");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }

        // 根据总关键字频率对网站进行排序
        List<Map.Entry<String, Map<String, Object>>> sortedSites = siteData.entrySet().stream()
                .sorted((entry1, entry2) -> {
                    int count1 = (int) entry1.getValue().get("keywordCount");
                    int count2 = (int) entry2.getValue().get("keywordCount");
                    return Integer.compare(count2, count1);
                })
                .toList();

        // 转换为有序的 HashMap
        LinkedHashMap<String, Map<String, Object>> sortedSiteData = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Object>> entry : sortedSites) {
            sortedSiteData.put(entry.getKey(), entry.getValue());
        }

        // 返回结果
        HashMap<String, Object> result = new HashMap<>();
        result.put("siteData", sortedSiteData);
        result.put("relatedKeywords", getTopNounKeywords(globalKeywordFrequency));
        return result;
    }

    private List<Map<String, Object>> getTopNounKeywords(Map<String, Integer> keywordFrequency) {
        List<Map<String, Object>> nounKeywords = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : keywordFrequency.entrySet()) {
            String word = entry.getKey();
            int count = entry.getValue();

            // 使用 Stanford CoreNLP 判断是否为名词
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

            System.out.println("完成母页爬取: " + url);
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
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36",
                "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:90.0) Gecko/20100101 Firefox/90.0",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1",
                "Mozilla/5.0 (Linux; Android 10; SM-G973F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.72 Mobile Safari/537.36"
        };

        Random random = new Random();
        return userAgents[random.nextInt(userAgents.length)];
    }
}





