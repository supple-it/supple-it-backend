package com.suppleit.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductResponse {
  public ProductResponse(String title2, String link2, String image2, String lprice2) {
    //TODO Auto-generated constructor stub
  }
  private String title; // 상품명
  private String link; // 상품 링크
  private String image; // 상품 이미지 URL
  private String lprice; // 최저가
}
