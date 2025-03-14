package com.suppleit.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

@RestController
public class RecommendationController {

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private final ExecutorService executorService;

  @Value("${flask.api.url}")
  private String flaskServerUrl;

  @Value("${naver.api.client-id}")
  private String clientId;

  @Value("${naver.api.client-secret}")
  private String clientSecret;

  public RecommendationController(RestTemplate restTemplate, ObjectMapper objectMapper) {
    this.restTemplate = restTemplate;
    this.objectMapper = objectMapper;
    this.executorService = Executors.newFixedThreadPool(10);
  }

  @GetMapping("/api/recommend")
  public List<ProductResponse> getRecommendations(@RequestParam("keyword") String keyword) {
    // Step 1: Flask 서버에서 추천 목록 받기
    URI flaskUri = UriComponentsBuilder.fromHttpUrl(flaskServerUrl + "/recommend")
        .queryParam("keyword", keyword)
        .build().encode().toUri(); // URL 인코딩 적용

    ApiResponse apiResponse = restTemplate.getForObject(flaskUri, ApiResponse.class);
    if (apiResponse == null || apiResponse.getRecommendations() == null) {
      return new ArrayList<>(); // Flask 응답이 없거나 추천 목록이 null인 경우 빈 목록 반환
    }

    List<String> recommendations = apiResponse.getRecommendations();

    // Step 2: Naver API로 추천된 제품들을 비동기적으로 요청
    List<CompletableFuture<ProductResponse>> futures = new ArrayList<>();
    for (String recommendation : recommendations) {
      CompletableFuture<ProductResponse> future = CompletableFuture
          .supplyAsync(() -> getNaverProductResponse(recommendation), executorService);
      futures.add(future);
    }

    return futures.stream()
        .map(CompletableFuture::join)
        .filter(response -> response != null)
        .toList();
  }

  private ProductResponse getNaverProductResponse(String query) {
    try {
      URI naverUri = UriComponentsBuilder.fromHttpUrl("https://openapi.naver.com/v1/search/shop.json")
          .queryParam("query", URLEncoder.encode(query, StandardCharsets.UTF_8))
          .build().encode().toUri(); // URL 인코딩 적용

      HttpHeaders headers = new HttpHeaders();
      headers.add("X-Naver-Client-Id", clientId);
      headers.add("X-Naver-Client-Secret", clientSecret);

      HttpEntity<String> entity = new HttpEntity<>(headers);
      ResponseEntity<String> response = restTemplate.exchange(naverUri, HttpMethod.GET, entity, String.class);

      JsonNode root = objectMapper.readTree(response.getBody());
      if (root.has("items") && root.get("items").isArray() && root.get("items").size() > 0) {
        JsonNode item = root.get("items").get(0);
        return new ProductResponse(
            item.get("title").asText(),
            item.get("link").asText(),
            item.get("image").asText(),
            item.get("lprice").asInt());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  // Flask 응답을 받을 DTO
  public static class ApiResponse {
    private String keyword;
    private int count;
    private List<String> recommendations;

    // Getters and setters
    public List<String> getRecommendations() {
      return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
      this.recommendations = recommendations;
    }
  }

  public static class ProductResponse {
    private String title;
    private String link;
    private String image;
    private int price;

    public ProductResponse(String title, String link, String image, int price) {
      this.title = title;
      this.link = link;
      this.image = image;
      this.price = price;
    }

    // getters and setters
    public String getTitle() {
      return title;
    }

    public String getLink() {
      return link;
    }

    public String getImage() {
      return image;
    }

    public int getPrice() {
      return price;
    }
  }
}