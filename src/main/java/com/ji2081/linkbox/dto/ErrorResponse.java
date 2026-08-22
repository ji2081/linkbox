package com.ji2081.linkbox.dto;

// 에러가 났을 때 나가는 JSON의 모양
public record ErrorResponse(
        String code,
        String message
) {
}
