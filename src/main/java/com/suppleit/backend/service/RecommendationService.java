package com.suppleit.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.suppleit.backend.dto.NaverSearchResponse;
import com.suppleit.backend.dto.ProductResponse;
import com.suppleit.backend.dto.RecommendationResponse;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

  private final RestTemplate restTemplate;

  @Value("${flask.api.url}")
  private String flaskUrl;

  @Value("${naver.api.url}")
  private String naverApiUrl;

  @Value("${naver.api.client-id}")
  private String clientId;

  @Value("${naver.api.client-secret}")
  private String clientSecret;

  private HttpHeaders createHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-Naver-Client-Id", clientId);
    headers.set("X-Naver-Client-Secret", clientSecret);
    return headers;
  }

  public List<String> getRecommendations(String keyword) {
    URI uri = UriComponentsBuilder.fromHttpUrl(flaskUrl)
        .queryParam("keyword", keyword)
        .build()
        .toUri();
    try {
      ResponseEntity<RecommendationResponse> response = restTemplate.getForEntity(uri, RecommendationResponse.class);
      if (response.getBody() != null && response.getBody().getRecommendations() != null) {
        return response.getBody().getRecommendations();
      } else {
        return new ArrayList<>();
      }
    } catch (Exception e) {
      // 예외 처리 로직 추가 (로깅 등)
      return new ArrayList<>();
    }
  }

  public List<ProductResponse> searchNaver(List<String> productNames) {
    return productNames.stream()
        .flatMap(product -> searchNaverSingleProduct(product).stream())
        .collect(Collectors.toList());
  }

  private List<ProductResponse> searchNaverSingleProduct(String product) {
    List<ProductResponse> results = new ArrayList<>();
    URI uri = UriComponentsBuilder.fromHttpUrl(naverApiUrl)
        .queryParam("query", URLEncoder.encode(product, StandardCharsets.UTF_8))
        .build()
        .toUri();
    try {
      HttpEntity<String> entity = new HttpEntity<>(createHeaders());
      ResponseEntity<NaverSearchResponse> response = restTemplate.exchange(uri, HttpMethod.GET, entity,
          NaverSearchResponse.class);
      if (response.getBody() != null && response.getBody().getItems() != null) {
        List<ProductResponse> productResponses = (List<ProductResponse>) response.getBody().getItems().stream()
            .map(item -> new ProductResponse(
                item.getTitle(),
                item.getLink(),
                item.getImage(),
                item.getLprice()))
            .collect(Collectors.toList());
        results.addAll(productResponses);
      }
    } catch (Exception e) {
      // 예외 처리 로직 추가 (로깅 등)
    }
    return results;
  }
}