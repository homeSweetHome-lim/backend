package com.example.backend.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.backend.entity.PropertyDetail;
import com.example.backend.repository.PropertyDetailRepository;
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
    private final PropertyDetailRepository propertyDetailRepository;
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

    @Transactional
    protected void savePropertiesInfo(PublicApiResponse response) {
        List<PublicApiResponse.TradeItem> tradeItems = response.body().items().itemList();

        Set<String> aptNamesInRequest = tradeItems.stream()
                .map(PublicApiResponse.TradeItem::aptName)
                .collect(Collectors.toSet());

        Map<String, Property> existingPropertiesMap = propertyRepository.findByAptNameIn(aptNamesInRequest).stream()
                .collect(Collectors.toMap(Property::getAptName, property -> property));

        List<Property> newPropertiesToSave = new ArrayList<>();
        List<PropertyDetail> detailsToSave = new ArrayList<>();

        for (PublicApiResponse.TradeItem p : response.body().items().itemList()){
            Property property = existingPropertiesMap.get(p.aptName());
            if (property == null) {
                property = Property.builder()
                        .propertyType(PropertyType.APT)
                        .buildYear(p.buildYear())
                        .aptName(p.aptName())
                        .lawdCode(getLawdCode(p.sggCode(), p.umdName()))
                        .build();
                newPropertiesToSave.add(property);
                existingPropertiesMap.put(p.aptName(), property);
            }
            PropertyDetail propertyDetail = PropertyDetail.builder()
                    .floor(p.floor())
                    .dealDate(LocalDate.of(p.dealYear(), p.dealMonth(), p.dealDay()).toString())
                    .area(p.exclusiveUseArea())
                    .price(p.dealAmount())
                    .transactionType(TransactionType.SALE)
                    .property(property) // 기존 또는 새로 만들어진 Property 객체와 연결
                    .build();
            detailsToSave.add(propertyDetail);
        }
        propertyRepository.saveAll(newPropertiesToSave);
        propertyDetailRepository.saveAll(detailsToSave);
    }

    private LawdCode getLawdCode(String lawdCode, String umdNm) {
        return lawdCodeRepository.findByLawdCodeAndDong(lawdCode, umdNm)
            .orElseThrow(() -> new BusinessException(CommonStatus.LAWD_CODE_NOT_FOUND));
    }


}
