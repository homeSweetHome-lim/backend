package com.example.backend.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.backend.common.BusinessException;
import com.example.backend.common.CommonStatus;
import com.example.backend.dto.request.PublicApiRequest;
import com.example.backend.dto.response.PublicApiResponse;
import com.example.backend.entity.LawdCode;
import com.example.backend.entity.Property;
import com.example.backend.entity.enums.PropertyType;
import com.example.backend.entity.enums.TransactionType;
import com.example.backend.repository.LawdCodeRepository;
import com.example.backend.repository.PropertyRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicApiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final PropertyRepository propertyRepository;
    private final LawdCodeRepository lawdCodeRepository;

    @Transactional
    public void requestInfoToPublicApi(PublicApiRequest request) {

        log.info("PublicApi Service 진입");
        String API_BASE_URL = "https://apis.data.go.kr/1613000/RTMSDataSvcAptTradeDev/getRTMSDataSvcAptTradeDev?serviceKey=";

        String requestUrl = API_BASE_URL+request.serviceKey()
            +"&LAWD_CD="+request.lawdCode()
            +"&DEAL_YMD="+request.dealYmd()
            +"&pageNo=1&numOfRows=20";
        log.info("request url : " + requestUrl);

        PublicApiResponse response = restTemplate
            .getForObject(
                requestUrl,
                PublicApiResponse.class);

        if (response.body().items().itemList()==null || response.body().items().itemList().isEmpty()) {
            throw new BusinessException(CommonStatus.NO_RESPONSE);
        }
        savePropertiesInfo(response);
    }

    private void savePropertiesInfo(PublicApiResponse response) {

        log.info("받은 답변을 저장하기 로직 진입");
        List<Property> properties = response.body().items().itemList().stream()
            .map(
                item -> Property.builder()
                    .floor(item.floor())
                    .price(item.dealAmount())
                    .area(item.exclusiveUseArea())
                    .propertyType(PropertyType.APT)
                    .buildYear(item.buildYear())
                    .aptName(item.aptName())
                    .dealDate(LocalDate.of(item.dealYear(), item.dealMonth(), item.dealDay()).toString())
                    .lawdCode(getLawdCode(item.sggCode(), item.umdName()))
                    .transactionType(TransactionType.SALE)
                    .build()).collect(Collectors.toList());

        log.info("리스트로 변환 완료");
        propertyRepository.saveAll(properties);
    }

    private LawdCode getLawdCode(String lawdCode, String umdNm) {
        return lawdCodeRepository.findByLawdCodeAndDong(lawdCode, umdNm)
            .orElseThrow(() -> new BusinessException(CommonStatus.LAWD_CODE_NOT_FOUND));
    }


}
