package com.ji2081.linkbox.dto;

import com.ji2081.linkbox.domain.Bookmark;
import com.ji2081.linkbox.domain.ReadStatus;

import java.time.LocalDate;

public record BookmarkResponse(
        Long id,
        String url,
        String title,
        String category,
        String memo,
        ReadStatus status,
        LocalDate savedAt
) {

    // 엔티티 → 응답으로 변환
    public static BookmarkResponse from(Bookmark bookmark) {
        return new BookmarkResponse(
                bookmark.getId(),
                bookmark.getUrl(),
                bookmark.getTitle(),
                bookmark.getCategory(),
                bookmark.getMemo(),
                bookmark.getStatus(),
                bookmark.getSavedAt()
        );
    }
}
