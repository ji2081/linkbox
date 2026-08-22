package com.ji2081.linkbox.dto;

// 저장 요청으로 들어오는 JSON의 모양
public record BookmarkCreateRequest(
        String url,
        String title,
        String category,
        String memo
) {
}
