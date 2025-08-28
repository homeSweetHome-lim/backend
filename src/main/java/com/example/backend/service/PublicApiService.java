package com.example.backend.service;

import java.net.URI;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.backend.dto.request.PublicApiRequest;
import com.example.backend.dto.response.PublicApiResponse;
import com.example.backend.entity.Property;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PublicApiService {

    private final RestTemplate restTemplate = new RestTemplate();;

    public String requestInfoToPublicApi(PublicApiRequest request) {

        String API_BASE_URL = "https://apis.data.go.kr/1613000/RTMSDataSvcAptTrade/getRTMSDataSvcAptTrade";

        URI requestUri = request.toUri(API_BASE_URL);
        // PublicApiRequest request = new PublicApiRequest(
        //                 "YOUR_SERVICE_KEY_ENCODED", // 실제 인코딩된 서비스 키
        //                 1,
        //                 10,
        //                 "11110", // 예: 서울 종로구
        //                 "202407" // 예: 2024년 7월
        //             );

        PublicApiResponse response = restTemplate
            .getForObject(
                requestUri,
                PublicApiResponse.class);
        return "정보를 가져왔습니다.";
    }

    private void saveInfo(PublicApiResponse response) {

        response.body().items().itemList().stream()
            .map(
                item -> Property.builder()
                    .address(item.))
        Property propertyInfo = Property.builder()
            .address(response.body().items().itemList()).build();
    }


}
