package com.ji2081.linkbox.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 저장 요청으로 들어오는 JSON의 모양
public record BookmarkCreateRequest(
        @NotBlank(message = "URL은 필수입니다")
        @Size(max = 500, message = "URL은 500자를 넘을 수 없습니다")
        String url,

        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 255, message = "제목은 255자를 넘을 수 없습니다")
        String title,

        @Size(max = 50, message = "카테고리는 50자를 넘을 수 없습니다")
        String category,

        @Size(max = 1000, message = "메모는 1000자를 넘을 수 없습니다")
        String memo
) {
}
