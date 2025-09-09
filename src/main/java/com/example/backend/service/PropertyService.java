package com.example.backend.service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
import com.example.backend.entity.enums.PropertyType;
import com.example.backend.repository.LawdCodeRepository;
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
        YearMonth startMonth = YearMonth.of(2024, 1);
        YearMonth endMonth = YearMonth.of(2024, 12);

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
                .price(p.getPrice())
                .floor(p.getFloor())
                .area(p.getArea())
                .dealDate(p.getDealDate())
                .aptName(p.getAptName())
                .state(lawdCode.getState())
                .dong(lawdCode.getDong())
                .si(lawdCode.getSi())
                .build())
            .toList();
    }

    @Transactional
    public GetPropertyPrices getPropertyPrices(Long propertyId) {

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(()->new BusinessException(CommonStatus.PROPERTY_NOT_FOUND));



        return GetPropertyPrices.builder()
                .maxPrice()
                .minPrice()
                .priceList()
                .build();
    }
}
