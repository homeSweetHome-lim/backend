package com.example.backend.dto.request;

import lombok.Getter;

public record PostPropertyRequest(
    String state,
    String si,
    String dong,
    String apiKey
) {
    @Getter
    public static class LawdCode{
        private final String lawdCode;
        public LawdCode(String lawdCode){
            this.lawdCode = lawdCode;
        }
    }
}
