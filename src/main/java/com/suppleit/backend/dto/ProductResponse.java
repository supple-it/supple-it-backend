package com.suppleit.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductResponse {

  private String title; // 상품명
  private String link; // 상품 링크
  private String image; // 상품 이미지 URL
  private String lprice; // 최저가 (String 타입으로 통일)

  // lprice를 int로 받는 생성자
  public ProductResponse(String title, String link, String image, int lprice) {
    this.title = title;
    this.link = link;
    this.image = image;
    this.lprice = String.valueOf(lprice); // int를 String으로 변환해서 저장
  }

  // lprice를 String으로 받는 생성자
  public ProductResponse(String title, String link, String image, String lprice) {
    this.title = title;
    this.link = link;
    this.image = image;
    this.lprice = lprice;
  }
}
