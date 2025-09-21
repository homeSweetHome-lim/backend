package com.example.backend.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.backend.entity.PropertyDetail;
import com.example.backend.entity.PropertyKey;
import com.example.backend.repository.PropertyDetailRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.backend.common.BusinessException;
import com.example.backend.common.CommonStatus;
import com.example.backend.dto.request.PublicApiRequest;
import com.example.backend.dto.response.PublicApiResponse;
import com.example.backend.entity.LawdCode;
import com.example.backend.entity.Property;
import com.example.backend.entity.enums.AreaType;
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

        // 1. 먼저 totalCount 확인을 위해 첫 페이지 요청
        String initialRequestUrl = API_BASE_URL + request.serviceKey()
            + "&LAWD_CD=" + request.lawdCode()
            + "&DEAL_YMD=" + request.dealYmd()
            + "&pageNo=1&numOfRows=1";

        log.info("totalCount 확인용 요청 URL : {}", initialRequestUrl);

        PublicApiResponse initialResponse = restTemplate.getForObject(initialRequestUrl, PublicApiResponse.class);

        if (initialResponse == null || initialResponse.body() == null) {
            throw new BusinessException(CommonStatus.NO_RESPONSE);
        }

        int totalCount = initialResponse.body().totalCount();
        log.info("전체 데이터 개수: {}", totalCount);

        if (totalCount == 0) {
            throw new BusinessException(CommonStatus.NO_RESPONSE);
        }

        // 2. 페이지당 데이터 수 설정 (최대 1000개)
        int numOfRows = Math.min(1000, totalCount);
        int totalPages = (int) Math.ceil((double) totalCount / numOfRows);

        log.info("페이지당 데이터 수: {}, 총 페이지 수: {}", numOfRows, totalPages);

        // 3. 모든 페이지의 데이터를 수집
        List<PublicApiResponse.TradeItem> allTradeItems = new ArrayList<>();

        for (int pageNo = 1; pageNo <= totalPages; pageNo++) {
            String requestUrl = API_BASE_URL + request.serviceKey()
                + "&LAWD_CD=" + request.lawdCode()
                + "&DEAL_YMD=" + request.dealYmd()
                + "&pageNo=" + pageNo + "&numOfRows=" + numOfRows;

            log.info("페이지 {}/{} 요청 중...", pageNo, totalPages);

            PublicApiResponse response = restTemplate.getForObject(requestUrl, PublicApiResponse.class);

            if (response != null && response.body() != null
                && response.body().items() != null
                && response.body().items().itemList() != null) {

                allTradeItems.addAll(response.body().items().itemList());
                log.info("페이지 {} 완료: {}개 데이터 수집", pageNo, response.body().items().itemList().size());
            }
        }

        log.info("전체 데이터 수집 완료: {}개", allTradeItems.size());

        if (allTradeItems.isEmpty()) {
            throw new BusinessException(CommonStatus.NO_RESPONSE);
        }

        // 4. 수집된 모든 데이터를 저장
        saveAllPropertiesInfo(allTradeItems);
    }

    /**
     * 문자열을 정규화하여 일관성 있는 비교를 위한 유틸리티 메서드
     * @param str 정규화할 문자열
     * @return 정규화된 문자열
     */
    private String normalizeString(String str) {
        if (str == null) {
            return null;
        }
        return str.trim()                    // 앞뒤 공백 제거
                  .replaceAll("\\s+", " ")   // 연속된 공백을 하나로 변환
                  .replaceAll("[\\u200B-\\u200D\\uFEFF]", "") // 보이지 않는 문자 제거
                  .toUpperCase();            // 대소문자 통일
    }

    /**
     * PropertyKey를 안전하게 생성하는 유틸리티 메서드
     * @param aptName 아파트명
     * @param lawdCode 법정동코드
     * @return 정규화된 PropertyKey
     */
    private PropertyKey createNormalizedPropertyKey(String aptName, String lawdCode) {
        return new PropertyKey(normalizeString(aptName), normalizeString(lawdCode));
    }

    @Transactional
    protected void saveAllPropertiesInfo(List<PublicApiResponse.TradeItem> allTradeItems) {

        // --- 1. 조회에 필요한 aptName과 lawdCode 세트를 각각 추출 ---
        Set<String> aptNamesInRequest = allTradeItems.stream()
            .map(PublicApiResponse.TradeItem::aptName)
            .collect(Collectors.toSet());

        Set<String> lawdCodesInRequest = allTradeItems.stream()
            .map(PublicApiResponse.TradeItem::sggCode)
            .collect(Collectors.toSet());

        // --- 2. aptName과 lawdCode로 기존 Property 데이터를 한 번에 조회 ---
        //    (아래 메소드를 Repository에 추가해야 합니다)
        List<Property> existingProperties = propertyRepository.findByAptNameInAndLawdCodeLawdCodeIn(aptNamesInRequest, lawdCodesInRequest);


        // --- 3. 조회된 데이터를 정규화된 PropertyKey를 키로 하는 Map으로 변환 ---
        Map<PropertyKey, Property> existingPropertiesMap = existingProperties.stream()
            .collect(Collectors.toMap(
                // 정규화된 PropertyKey 생성
                property -> createNormalizedPropertyKey(property.getAptName(), property.getLawdCode().getLawdCode()),
                property -> property,
                (existing, replacement) -> existing // 중복 키 발생 시 기존 값 유지
            ));


        // 새로운 Property들을 먼저 저장
        List<Property> newPropertiesToSave = new ArrayList<>();
        for (PublicApiResponse.TradeItem p : allTradeItems) {
            LawdCode lawdCode = getLawdCode(p.sggCode(), p.umdName());
            PropertyKey propertyKey = createNormalizedPropertyKey(p.aptName(), lawdCode.getLawdCode());

            if (!existingPropertiesMap.containsKey(propertyKey)) {
                Property newProperty = Property.builder()
                        .propertyType(PropertyType.APT)
                        .buildYear(p.buildYear())
                        .aptName(p.aptName())
                        .lawdCode(lawdCode)
                        .build();
                newPropertiesToSave.add(newProperty);
                // 중복 체크를 위해 임시로 맵에 추가
                existingPropertiesMap.put(propertyKey, newProperty);
            }
        }

        if (!newPropertiesToSave.isEmpty()) {
            try {
                List<Property> savedProperties = propertyRepository.saveAll(newPropertiesToSave);
                // 저장된 Property들을 ID가 할당된 상태로 맵에 다시 추가
                for (Property property : savedProperties) {
                    PropertyKey propertyKey = createNormalizedPropertyKey(property.getAptName(), property.getLawdCode().getLawdCode());
                    existingPropertiesMap.put(propertyKey, property);
                }
                log.info("새로운 Property {} 개가 저장되었습니다.", savedProperties.size());
            } catch (Exception e) {
                // 중복 키 예외나 기타 예외 발생 시 기존 데이터를 다시 조회
                log.warn("Property 저장 중 오류 발생 (동시성 이슈 가능성): {}. 기존 데이터를 다시 조회합니다.", e.getMessage());

                // 실패한 Property들에 대해 다시 조회해서 맵에 추가
                for (Property newProperty : newPropertiesToSave) {
                    PropertyKey propertyKey = createNormalizedPropertyKey(newProperty.getAptName(), newProperty.getLawdCode().getLawdCode());
                    List<Property> existingPropsForApt = propertyRepository.findByAptNameInAndLawdCodeLawdCodeIn(
                        Set.of(newProperty.getAptName()),
                        Set.of(newProperty.getLawdCode().getLawdCode())
                    );

                    if (!existingPropsForApt.isEmpty()) {
                        existingPropertiesMap.put(propertyKey, existingPropsForApt.get(0));
                    }
                }
            }
        }

        // 모든 PropertyDetail을 일괄로 저장
        List<PropertyDetail> detailsToSave = new ArrayList<>();
        int skipCount = 0;

        for (PublicApiResponse.TradeItem p : allTradeItems) {
            try {
                LawdCode lawdCode = getLawdCode(p.sggCode(), p.umdName());
                PropertyKey propertyKey = createNormalizedPropertyKey(p.aptName(), lawdCode.getLawdCode());
                Property property = existingPropertiesMap.get(propertyKey);

                // Property가 존재하지 않는 경우 로그 기록 후 스킵
                if (property == null) {
                    log.warn("PropertyDetail 생성 실패: Property를 찾을 수 없음 (아파트명: {}, 법정동코드: {})",
                             p.aptName(), lawdCode.getLawdCode());
                    skipCount++;
                    continue;
                }

                PropertyDetail propertyDetail = PropertyDetail.builder()
                        .floor(p.floor())
                        .dealDate(LocalDate.of(p.dealYear(), p.dealMonth(), p.dealDay()))
                        .area(p.exclusiveUseArea())
                        .price(p.dealAmount())
                        .transactionType(TransactionType.SALE)
                        .areaType(AreaType.findByArea(p.exclusiveUseArea()))
                        .property(property)
                        .build();

                detailsToSave.add(propertyDetail);

            } catch (Exception e) {
                log.error("PropertyDetail 생성 중 오류 발생 (아파트명: {}, 거래일: {}-{}-{}): {}",
                         p.aptName(), p.dealYear(), p.dealMonth(), p.dealDay(), e.getMessage(), e);
                skipCount++;
            }
        }

        if (skipCount > 0) {
            log.warn("총 {}개의 PropertyDetail이 생성되지 않았습니다.", skipCount);
        }

        // PropertyDetail 일괄 저장
        propertyDetailRepository.saveAll(detailsToSave);
        log.info("총 {}개 거래 데이터가 저장되었습니다.", detailsToSave.size());

        // 저장 완료 후 아파트별로 가격 및 면적 범위 일괄 업데이트
        Set<String> aptNamesInBatch = allTradeItems.stream()
                .map(PublicApiResponse.TradeItem::aptName)
                .collect(Collectors.toSet());

        updatePriceRangesForApartments(aptNamesInBatch);
        updateAreaRangesForApartments(aptNamesInBatch);
        log.info("총 {}개 아파트의 가격 및 면적 범위가 업데이트되었습니다.", aptNamesInBatch.size());
    }

    @Transactional
    protected void savePropertiesInfo(PublicApiResponse response) {
        List<PublicApiResponse.TradeItem> tradeItems = response.body().items().itemList();
        saveAllPropertiesInfo(tradeItems);
    }

    private LawdCode getLawdCode(String lawdCode, String umdNm) {
        return lawdCodeRepository.findByLawdCodeAndDong(lawdCode, umdNm)
            .orElseThrow(() -> new BusinessException(CommonStatus.LAWD_CODE_NOT_FOUND));
    }

    /**
     * 특정 아파트 이름의 모든 Property에 대해 최소/최대 가격을 업데이트
     * @param aptName 아파트 이름
     */
    private void updatePriceRangeForApartment(String aptName) {
        log.info("아파트 '{}' 의 가격 범위 업데이트를 시작합니다.", aptName);
        
        // 1. 해당 아파트 이름의 모든 Property 조회
        List<Property> properties = propertyRepository.findAllByAptName(aptName);
        
        if (properties.isEmpty()) {
            log.warn("아파트 '{}'에 대한 Property가 존재하지 않습니다.", aptName);
            return;
        }
        
        // 2. 해당 아파트의 모든 거래 데이터에서 최소/최대 가격 조회
        Object[] minMaxPrices = propertyDetailRepository.findMinMaxPriceByAptName(aptName);
        
        if (minMaxPrices == null || minMaxPrices.length < 2) {
            log.warn("아파트 '{}'에 대한 가격 데이터 조회 결과가 없습니다.", aptName);
            return;
        }
        
        if (minMaxPrices[0] == null || minMaxPrices[1] == null) {
            log.warn("아파트 '{}'에 대한 유효한 가격 데이터가 존재하지 않습니다.", aptName);
            return;
        }
        
        // Long 타입으로 받아서 Integer로 변환 (가격이 클 수 있으므로)
        Long minPriceLong = (Long) minMaxPrices[0];
        Long maxPriceLong = (Long) minMaxPrices[1];
        
        // Integer 범위를 벗어나는 경우 처리
        Integer minPrice = minPriceLong > Integer.MAX_VALUE ? Integer.MAX_VALUE : minPriceLong.intValue();
        Integer maxPrice = maxPriceLong > Integer.MAX_VALUE ? Integer.MAX_VALUE : maxPriceLong.intValue();
        
        log.info("아파트 '{}' 의 계산된 가격 범위: {} ~ {}", aptName, minPrice, maxPrice);
        
        // 3. 해당 아파트의 모든 Property에 가격 범위 업데이트
        for (Property property : properties) {
            property.updatePriceRange(minPrice, maxPrice);
            log.debug("Property ID {} 의 가격 범위가 업데이트되었습니다.", property.getPropertyId());
        }
        
        log.info("아파트 '{}' 의 가격 범위 업데이트가 완료되었습니다. (총 {}개 Property 업데이트)", 
                 aptName, properties.size());
    }

    /**
     * 여러 아파트의 가격 범위를 효율적으로 일괄 업데이트하는 메서드
     * @param aptNames 업데이트할 아파트 이름 집합
     */
    private void updatePriceRangesForApartments(Set<String> aptNames) {
        log.info("{}개 아파트의 가격 범위 일괄 업데이트를 시작합니다.", aptNames.size());

        try {
            // 1. 한 번의 쿼리로 모든 아파트의 최소/최대 가격 조회
            List<Object[]> priceRanges = propertyDetailRepository.findMinMaxPricesByAptNames(aptNames);

            if (priceRanges.isEmpty()) {
                log.warn("요청한 아파트들에 대한 가격 데이터가 존재하지 않습니다.");
                return;
            }

            // 2. 아파트별로 Property들을 조회하고 가격 범위 업데이트
            int successCount = 0;
            int failCount = 0;

            for (Object[] priceRange : priceRanges) {
                String aptName = (String) priceRange[0];
                Long minPriceLong = (Long) priceRange[1];
                Long maxPriceLong = (Long) priceRange[2];

                if (minPriceLong == null || maxPriceLong == null) {
                    log.warn("아파트 '{}'에 대한 유효한 가격 데이터가 없습니다.", aptName);
                    failCount++;
                    continue;
                }

                try {
                    Integer minPrice = minPriceLong > Integer.MAX_VALUE ? Integer.MAX_VALUE : minPriceLong.intValue();
                    Integer maxPrice = maxPriceLong > Integer.MAX_VALUE ? Integer.MAX_VALUE : maxPriceLong.intValue();

                    // 해당 아파트의 모든 Property 조회 및 업데이트
                    List<Property> properties = propertyRepository.findAllByAptName(aptName);

                    if (properties.isEmpty()) {
                        log.warn("아파트 '{}'에 대한 Property가 존재하지 않습니다.", aptName);
                        failCount++;
                        continue;
                    }

                    // 모든 Property에 가격 범위 업데이트
                    for (Property property : properties) {
                        property.updatePriceRange(minPrice, maxPrice);
                    }

                    log.debug("아파트 '{}' 가격 범위 업데이트 완료: {} ~ {} ({}개 Property)",
                             aptName, minPrice, maxPrice, properties.size());
                    successCount++;

                } catch (Exception e) {
                    log.error("아파트 '{}' 가격 범위 업데이트 중 오류 발생: {}", aptName, e.getMessage(), e);
                    failCount++;
                }
            }

            log.info("가격 범위 일괄 업데이트 완료: 성공 {}개, 실패 {}개", successCount, failCount);

        } catch (Exception e) {
            log.error("가격 범위 일괄 업데이트 중 전체 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 여러 아파트의 면적 범위를 효율적으로 일괄 업데이트하는 메서드
     * @param aptNames 업데이트할 아파트 이름 집합
     */
    private void updateAreaRangesForApartments(Set<String> aptNames) {
        log.info("{}개 아파트의 면적 범위 일괄 업데이트를 시작합니다.", aptNames.size());

        try {
            // 1. 한 번의 쿼리로 모든 아파트의 최소/최대 면적 조회
            List<Object[]> areaRanges = propertyDetailRepository.findMinMaxAreasByAptNames(aptNames);

            if (areaRanges.isEmpty()) {
                log.warn("요청한 아파트들에 대한 면적 데이터가 존재하지 않습니다.");
                return;
            }

            // 2. 아파트별로 Property들을 조회하고 면적 범위 업데이트
            int successCount = 0;
            int failCount = 0;

            for (Object[] areaRange : areaRanges) {
                String aptName = (String) areaRange[0];
                Double minArea = (Double) areaRange[1];
                Double maxArea = (Double) areaRange[2];

                if (minArea == null || maxArea == null) {
                    log.warn("아파트 '{}'에 대한 유효한 면적 데이터가 없습니다.", aptName);
                    failCount++;
                    continue;
                }

                try {
                    // 해당 아파트의 모든 Property 조회 및 업데이트
                    List<Property> properties = propertyRepository.findAllByAptName(aptName);

                    if (properties.isEmpty()) {
                        log.warn("아파트 '{}'에 대한 Property가 존재하지 않습니다.", aptName);
                        failCount++;
                        continue;
                    }

                    // 모든 Property에 면적 범위 업데이트
                    for (Property property : properties) {
                        property.updateAreaRange(minArea, maxArea);
                    }

                    log.debug("아파트 '{}' 면적 범위 업데이트 완료: {}㎡ ~ {}㎡ ({}개 Property)",
                             aptName, minArea, maxArea, properties.size());
                    successCount++;

                } catch (Exception e) {
                    log.error("아파트 '{}' 면적 범위 업데이트 중 오류 발생: {}", aptName, e.getMessage(), e);
                    failCount++;
                }
            }

            log.info("면적 범위 일괄 업데이트 완료: 성공 {}개, 실패 {}개", successCount, failCount);

        } catch (Exception e) {
            log.error("면적 범위 일괄 업데이트 중 전체 오류 발생: {}", e.getMessage(), e);
        }
    }

}
