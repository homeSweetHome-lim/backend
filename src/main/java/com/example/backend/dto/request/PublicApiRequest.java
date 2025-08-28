package com.example.backend.dto.request;

import java.net.URI;

import org.springframework.web.util.UriComponentsBuilder;

public record PublicApiRequest(
    String serviceKey,
    int pageNo,
    int numOfRows,
    String lawdCd,    // LAWD_CD
    String dealYmd    // DEAL_YMD
) {
    public URI toUri(String baseUrl) {
        // UriComponentsBuilder를 사용하여 파라미터를 안전하게 URL 쿼리 스트링으로 추가합니다.
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
            .queryParam("serviceKey", this.serviceKey)
            .queryParam("pageNo", this.pageNo)
            .queryParam("numOfRows", this.numOfRows)
            .queryParam("LAWD_CD", this.lawdCd) // API 명세의 파라미터명(LAWD_CD)과 일치시킵니다.
            .queryParam("DEAL_YMD", this.dealYmd) // API 명세의 파라미터명(DEAL_YMD)과 일치시킵니다.
            .build(true) // build(true)를 사용하여 serviceKey가 이미 인코딩된 경우 중복 인코딩을 방지합니다.
            .toUri();
    }

}
