package com.example.backend.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.backend.dto.response.FindRegionNameResponse;
import com.example.backend.entity.LawdCode;
import com.example.backend.repository.LawdCodeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LawdCodeService {
    private final LawdCodeRepository lawdCodeRepository;

    public FindRegionNameResponse getSiNames(String state) {
        Set<String> siList = lawdCodeRepository.findByState(state)
            .stream().map(LawdCode::getSi)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        return new FindRegionNameResponse(state, siList.stream().toList());
    }

    public FindRegionNameResponse getDongNames(String si) {
        List<String> dongList = lawdCodeRepository.findBySi(si)
            .stream().map(LawdCode::getDong).toList();

        return new FindRegionNameResponse(si, dongList);
    }

}
