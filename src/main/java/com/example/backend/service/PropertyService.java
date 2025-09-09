package com.example.backend.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.backend.dto.response.GetPropertyPrices;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.backend.common.BusinessException;
import com.example.backend.common.CommonStatus;
import com.example.backend.dto.request.GetPropertiesByFilterRequest;
import com.example.backend.dto.request.PostPropertyRequest;
import com.example.backend.dto.request.PublicApiRequest;
import com.example.backend.dto.response.GetPropertyInfoResponse;
import com.example.backend.entity.LawdCode;
import com.example.backend.entity.Property;
import com.example.backend.entity.PropertyDetail;
import com.example.backend.entity.enums.PropertyType;
import com.example.backend.repository.LawdCodeRepository;
import com.example.backend.repository.PropertyDetailRepository;
import com.example.backend.repository.PropertyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyService {

    @Value("${public.api.key}")
    private String apiKey;

    private final LawdCodeRepository lawdCodeRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyDetailRepository propertyDetailRepository;
    private final PublicApiService publicApiService;

    @Async // ✅ 이 메서드는 별도의 스레드에서 비동기로 실행됩니다.
    public void startScrapingAllRegions() {
        // 1. DB에서 모든 지역 코드(LawdCode) 엔티티를 가져옵니다.
        List<String> allLawdCodes = lawdCodeRepository.findAll().stream().map(LawdCode::getLawdCode).distinct().toList();
        if (allLawdCodes.isEmpty()) {
            log.warn("DB에 저장된 지역 코드가 없어 데이터 수집을 시작할 수 없습니다.");
            return;
        }

        log.info("총 {}개 지역에 대한 1년치 데이터 수집을 시작합니다...", allLawdCodes.size());

        // 2. 수집할 기간을 설정합니다. (예: 2015년 1월 ~ 2024년 12월)
        YearMonth startMonth = YearMonth.of(2023, 8);
        YearMonth endMonth = YearMonth.of(2025, 8);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
        // 3. 중첩 반복문: (바깥쪽) 지역 코드 -> (안쪽) 날짜
        for (String lawdCode : allLawdCodes) {
            for (YearMonth currentMonth = startMonth; !currentMonth.isAfter(endMonth); currentMonth = currentMonth.plusMonths(1)) {
                String dealYmd = currentMonth.format(formatter);
                log.info("거래 년월 : {}", dealYmd);
                try {
                    PublicApiRequest publicApiRequest = PublicApiRequest.builder()
                        .serviceKey(apiKey)
                        .lawdCode(lawdCode)
                        .dealYmd(dealYmd)
                        .build();
                    publicApiService.requestInfoToPublicApi(publicApiRequest);
                    log.info("  - {} 데이터 저장 완료.", currentMonth);

                    // 5. (매우 중요) 상대방 API 서버에 부담을 주지 않도록 잠시 대기합니다.
                    Thread.sleep(500); // 0.5초 대기

                } catch (InterruptedException e) {
                    log.error("데이터 수집 스레드가 중단되었습니다.", e);
                    Thread.currentThread().interrupt(); // 인터럽트 상태를 다시 설정
                    return; // 작업 즉시 중단
                } catch (Exception e) {
                    // API 호출 실패 등 예외가 발생해도 전체 작업이 중단되지 않도록 처리
                    log.error("  - {} 데이터 처리 중 오류 발생: {}", currentMonth, e.getMessage());
                }
            }
        }
        log.info("<<<<< 모든 지역의 데이터 수집 작업을 완료했습니다!");
    }

    public void getProperties(PostPropertyRequest request) {
        String state = request.state();
        String si = request.si();
        String dong = request.dong();
        if (dong == null) {
            dong = si;
        }
        log.info("서비스 진입 및 요청 지역 : {}, {}, {}", state, si, dong);

        LawdCode lawdCode = lawdCodeRepository.findByStateAndSiAndDong(state, si, dong)
            .orElseThrow(()->new BusinessException(CommonStatus.LAWD_CODE_NOT_FOUND));

        log.info("코드 존재함 => {}", lawdCode);
        PublicApiRequest publicApiRequest = PublicApiRequest.builder()
            .serviceKey(request.apiKey())
            .lawdCode(lawdCode.getLawdCode())
            .dealYmd("202503")
            .build();

        publicApiService.requestInfoToPublicApi(publicApiRequest);
    }

    public List<GetPropertyInfoResponse> getPropertiesByFilterResponse(String state, String si, String dong) {

        log.info("도 : {}, 시: {}, 동 : {}", state, si, dong);
        LawdCode lawdCode = lawdCodeRepository.findByStateAndSiAndDong(state, si, dong)
            .orElseThrow(()-> new BusinessException(CommonStatus.LAWD_CODE_NOT_FOUND));

        List<Property> properties = propertyRepository.findByLawdCode(lawdCode);

        return properties.stream()
            .map(p -> GetPropertyInfoResponse.builder()
                .propertyId(p.getPropertyId())
                .propertyType(PropertyType.APT)
                .buildYear(p.getBuildYear())
                .minPrice(p.getMinPrice())
                .maxPrice(p.getMaxPrice())
                .aptName(p.getAptName())
                .state(lawdCode.getState())
                .dong(lawdCode.getDong())
                .si(lawdCode.getSi())
                .build())
            .toList();
    }


    /**
     * 특정 아파트 이름의 모든 Property에 대해 최소/최대 가격을 업데이트
     * @param aptName 아파트 이름
     */
    @Transactional
    public void updatePriceRangeForApartment(String aptName) {
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
        
        // 4. 변경사항 저장 (트랜잭션으로 인해 자동 저장됨)
        log.info("아파트 '{}' 의 가격 범위 업데이트가 완료되었습니다. (총 {}개 Property 업데이트)", 
                 aptName, properties.size());
    }
    /**
     * 모든 아파트의 가격 범위를 일괄 업데이트 (초기 설정 또는 전체 재계산 시 사용)
     */
    @Transactional
    public void updateAllApartmentPriceRanges() {
        log.info("모든 아파트의 가격 범위 일괄 업데이트를 시작합니다.");
        
        // 모든 고유한 아파트 이름 조회
        List<String> allAptNames = propertyRepository.findAll()
                .stream()
                .map(Property::getAptName)
                .distinct()
                .toList();
        
        log.info("총 {}개의 고유한 아파트에 대해 가격 범위를 업데이트합니다.", allAptNames.size());
        
        updatePriceRangesForMultipleApartments(new HashSet<>(allAptNames));
        
        log.info("모든 아파트의 가격 범위 일괄 업데이트가 완료되었습니다.");
    }

    /**
     * 여러 아파트의 가격 범위를 효율적으로 일괄 업데이트 (최적화된 쿼리 사용)
     * @param aptNames 업데이트할 아파트 이름 집합
     */
    @Transactional
    public void updatePriceRangesForMultipleApartments(Set<String> aptNames) {
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

    public GetPropertyPrices getPropertyPrices(Long propertyId) {
        Property property = propertyRepository.findById(propertyId).orElseThrow(
            () -> new BusinessException(CommonStatus.PROPERTY_NOT_FOUND));
        List<PropertyDetail> properties = propertyDetailRepository.findByProperty(property);

        Map<LocalDate, String> datePriceMap = new HashMap<>();
        for (PropertyDetail p : properties) {
            datePriceMap.put(p.getDealDate(), p.getPrice());
        }
        return GetPropertyPrices.builder()
            .maxPrice(property.getMaxPrice().toString())
            .minPrice(property.getMinPrice().toString())
            .priceWithDateList(datePriceMap)
            .build();
    }
}

