package com.example.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.backend.common.BusinessException;
import com.example.backend.common.CommonStatus;
import com.example.backend.dto.request.GetPropertyRequest;
import com.example.backend.dto.request.PublicApiRequest;
import com.example.backend.entity.LawdCode;
import com.example.backend.repository.LawdCodeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyService {

    // @Value("${PUBLIC_API_KEY}")
    // private String apiKey;

    private final LawdCodeRepository lawdCodeRepository;
    private final PublicApiService publicApiService;

    public void getProperties(GetPropertyRequest request) {
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
}
