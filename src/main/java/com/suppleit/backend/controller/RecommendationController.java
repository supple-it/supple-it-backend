package com.suppleit.backend.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suppleit.backend.dto.ProductResponse;
import com.suppleit.backend.dto.SearchRequest;
import com.suppleit.backend.service.RecommendationService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class RecommendationController {

  private final RecommendationService recommendationService;
  private final ExecutorService executorService;

  @Value("${naver.api.client-id}")
  private String clientId;

  @Value("${naver.api.client-secret}")
  private String clientSecret;

  @Value("${naver.api.url}")
  private String naverApiUrl;

  @Value("${flask.api.url}")
  private String flaskUrl;

  public RecommendationController(RecommendationService recommendationService) {
    this.recommendationService = recommendationService;
    this.executorService = Executors.newFixedThreadPool(10);
  }

  @GetMapping("/api/recommend")
  public List<ProductResponse> getRecommendations(@RequestParam("keyword") String keyword) {
    log.info("Request received to get recommendations for keyword: {}", keyword);

    // Flask 서버에서 추천 목록 요청
    List<String> recommendations = recommendationService.getRecommendations(keyword);
    if (recommendations.isEmpty()) {
      log.warn("No recommendations found for keyword: {}", keyword);
      return new ArrayList<>();
    }

    // 네이버 API에서 추천 상품 검색
    List<CompletableFuture<ProductResponse>> futures = new ArrayList<>();
    for (String recommendation : recommendations) {
      futures.add(CompletableFuture.supplyAsync(() -> getNaverProductResponse(recommendation), executorService)
          .exceptionally(ex -> {
            log.error("Error occurred for query: {}, Exception: {}", recommendation, ex.getMessage());
            return null;
          }));
    }

    List<ProductResponse> products = futures.stream()
        .map(CompletableFuture::join)
        .filter(response -> response != null)
        .toList();

    log.info("Fetched {} products from Naver API", products.size());

    return products;
  }

  // Flask 서버로부터 POST 요청 처리를 위한 추가 메서드
  @PostMapping("/api/recommend")
  public List<ProductResponse> receiveRecommendations(@RequestBody SearchRequest request) {
    log.info("Received POST request with {} recommendations", request.getProducts().size());

    List<String> recommendations = request.getProducts();
    if (recommendations.isEmpty()) {
      log.warn("No recommendations received in POST request");
      return new ArrayList<>();
    }

    // 네이버 API에서 추천 상품 검색
    List<CompletableFuture<ProductResponse>> futures = new ArrayList<>();
    for (String recommendation : recommendations) {
      futures.add(CompletableFuture.supplyAsync(() -> getNaverProductResponse(recommendation), executorService)
          .exceptionally(ex -> {
            log.error("Error occurred for query: {}, Exception: {}", recommendation, ex.getMessage());
            return null;
          }));
    }

    List<ProductResponse> products = futures.stream()
        .map(CompletableFuture::join)
        .filter(response -> response != null)
        .toList();

    log.info("Fetched {} products from Naver API", products.size());

    return products;
  }

  private ProductResponse getNaverProductResponse(String query) {
    log.debug("Searching for product on Naver with query: {}", query);

    try {
      URI naverUri = UriComponentsBuilder.fromUriString(naverApiUrl)
          .queryParam("query", query)
          .build().encode().toUri();

      HttpHeaders headers = new HttpHeaders();
      headers.set("X-Naver-Client-Id", clientId);
      headers.set("X-Naver-Client-Secret", clientSecret);

      HttpEntity<String> entity = new HttpEntity<>(headers);
      ResponseEntity<String> response = new RestTemplate().exchange(naverUri, HttpMethod.GET, entity, String.class);

      if (response.getBody() == null) {
        log.warn("No response body received for query: {}", query);
        return null;
      }

      JsonNode root = new ObjectMapper().readTree(response.getBody());
      JsonNode items = root.path("items");

      if (items.isArray() && items.size() > 0) {
        JsonNode item = items.get(0);
        log.info("Found product: {} with price: {}", item.path("title").asText(), item.path("lprice").asInt(0));

        return new ProductResponse(
            item.path("title").asText(),
            item.path("link").asText(),
            item.path("image").asText(),
            item.path("lprice").asInt(0));
      } else {
        log.warn("No items found for query: {}", query);
      }
    } catch (Exception e) {
      log.error("Error occurred while processing query: {}, Exception: {}", query, e.getMessage());
    }
    return null;
  }
}