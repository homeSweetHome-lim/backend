package com.example.backend.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * 공공데이터 포털 부동산 실거래가 API의 전체 응답을 담는 최상위 레코드입니다.
 * @param header 응답 헤더 (결과 코드 및 메시지)
 * @param body 응답 본문 (실제 데이터 및 페이징 정보)
 */
@JacksonXmlRootElement(localName = "response")
public record PublicApiResponse(
    @JsonProperty("header")
    TradeHeader header,

    @JsonProperty("body")
    TradeBody body
) {
    /**
     * 응답 헤더 정보를 담는 레코드입니다.
     * @param resultCode 결과 코드 (e.g., "000")
     * @param resultMsg 결과 메시지 (e.g., "OK")
     */
    public record TradeHeader(
        @JsonProperty("resultCode")
        String resultCode,

        @JsonProperty("resultMsg")
        String resultMsg
    ) {}

    /**
     * 응답 본문 정보를 담는 레코드입니다.
     * @param items 실제 거래 아이템 리스트를 감싸는 객체
     * @param numOfRows 한 페이지 결과 수
     * @param pageNo 페이지 번호
     * @param totalCount 전체 결과 수
     */
    public record TradeBody(
        @JsonProperty("items")
        TradeItems items,

        @JsonProperty("numOfRows")
        int numOfRows,

        @JsonProperty("pageNo")
        int pageNo,

        @JsonProperty("totalCount")
        int totalCount
    ) {}

    /**
     * 거래 아이템 리스트를 담는 컨테이너 레코드입니다.
     * @param itemList 실제 거래 아이템의 리스트
     */
    public record TradeItems(
        @JsonProperty("item")
        @JacksonXmlElementWrapper(useWrapping = false) // <items> 안에 <item>이 바로 오므로 중복 래핑 방지
        List<TradeItem> itemList
    ) {}

    /**
     * 개별 부동산 거래 정보를 담는 레코드입니다.
     * XML의 <item> 태그에 해당합니다.
     */
    public record TradeItem(
        @JsonProperty("aptDong") String aptDong,
        @JsonProperty("aptNm") String aptName,
        @JsonProperty("buildYear") int buildYear,
        @JsonProperty("buyerGbn") String buyerGbn,
        @JsonProperty("cdealDay") String cancelDealDay,
        @JsonProperty("cdealType") String cancelDealType,
        @JsonProperty("dealAmount") String dealAmount, // "12,000" 형태이므로 String으로 받고 후처리 필요
        @JsonProperty("dealDay") int dealDay,
        @JsonProperty("dealMonth") int dealMonth,
        @JsonProperty("dealYear") int dealYear,
        @JsonProperty("dealingGbn") String dealingGbn,
        @JsonProperty("estateAgentSggNm") String estateAgentSggName,
        @JsonProperty("excluUseAr") double exclusiveUseArea,
        @JsonProperty("floor") int floor,
        @JsonProperty("jibun") String jibun,
        @JsonProperty("landLeaseholdGbn") String landLeaseholdGbn,
        @JsonProperty("rgstDate") String rgstDate,
        @JsonProperty("sggCd") String sggCode,
        @JsonProperty("slerGbn") String sellerGbn,
        @JsonProperty("umdNm") String umdName
    ) {}
}
