package com.ji2081.linkbox.service;

import com.ji2081.linkbox.domain.Bookmark;
import com.ji2081.linkbox.domain.ReadStatus;
import com.ji2081.linkbox.dto.BookmarkCreateRequest;
import com.ji2081.linkbox.dto.BookmarkResponse;
import com.ji2081.linkbox.repository.BookmarkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ji2081.linkbox.exception.BookmarkNotFoundException;
import com.ji2081.linkbox.exception.DuplicateUrlException;

import java.util.ArrayList;
import java.util.List;

import java.util.Map;

import com.ji2081.linkbox.exception.NoBookmarkToReadException;
import java.time.LocalDate;

@Service
public class BookmarkService {

    // 링크 본질과 무관한 추적용 파라미터들
    private static final List<String> TRACKING_PARAMS = List.of(
            "si", "igsh", "fbclid", "gclid",
            "utm_source", "utm_medium", "utm_campaign", "utm_term", "utm_content"
    );

    // URL에 이 문자열이 들어있으면 해당 카테고리로 추측
    private static final Map<String, String> DOMAIN_CATEGORY = Map.of(
            "youtube.com", "영상",
            "youtu.be", "영상",
            "github.com", "개발",
            "velog.io", "개발",
            "tistory.com", "블로그",
            "blog.naver.com", "블로그",
            "instagram.com", "인스타",
            "brunch.co.kr", "글"
    );

    private final BookmarkRepository bookmarkRepository;

    public BookmarkService(BookmarkRepository bookmarkRepository) {
        this.bookmarkRepository = bookmarkRepository;
    }

    public BookmarkResponse save(BookmarkCreateRequest request) {
        String url = normalizeUrl(request.url());

        if (bookmarkRepository.existsByUrl(url)) {
            throw new DuplicateUrlException(url);
        }

        Bookmark bookmark = new Bookmark(
                url,
                request.title(),
                resolveCategory(request.category(), url),
                request.memo()
        );
        return BookmarkResponse.from(bookmarkRepository.save(bookmark));
    }

    // category, status 둘 다 없으면 전체 조회
    public List<BookmarkResponse> findAll(String category, ReadStatus status) {
        List<Bookmark> bookmarks;

        if (category != null && status != null) {
            bookmarks = bookmarkRepository.findByCategoryAndStatus(category, status);
        } else if (category != null) {
            bookmarks = bookmarkRepository.findByCategory(category);
        } else if (status != null) {
            bookmarks = bookmarkRepository.findByStatus(status);
        } else {
            bookmarks = bookmarkRepository.findAll();
        }

        return toResponses(bookmarks);
    }

    @Transactional
    public BookmarkResponse markAsDone(Long id) {
        Bookmark bookmark = findOrThrow(id);
        bookmark.markAsDone();
        return BookmarkResponse.from(bookmark);
    }

    @Transactional
    public BookmarkResponse markAsTodo(Long id) {
        Bookmark bookmark = findOrThrow(id);
        bookmark.markAsTodo();
        return BookmarkResponse.from(bookmark);
    }

    @Transactional
    public void delete(Long id) {
        Bookmark bookmark = findOrThrow(id);
        bookmarkRepository.delete(bookmark);
    }

    private Bookmark findOrThrow(Long id) {
        return bookmarkRepository.findById(id)
                .orElseThrow(() -> new BookmarkNotFoundException(id));
    }

    private List<BookmarkResponse> toResponses(List<Bookmark> bookmarks) {
        List<BookmarkResponse> result = new ArrayList<>();
        for (Bookmark bookmark : bookmarks) {
            result.add(BookmarkResponse.from(bookmark));
        }
        return result;
    }

    // 같은 링크를 같은 문자열로 통일
    private String normalizeUrl(String rawUrl) {
        String url = rawUrl.trim();

        int queryIndex = url.indexOf('?');
        if (queryIndex == -1) {
            return removeTrailingSlash(url);
        }

        String base = removeTrailingSlash(url.substring(0, queryIndex));
        String query = url.substring(queryIndex + 1);

        // 추적용 파라미터만 버리고 나머지는 살림
        List<String> kept = new ArrayList<>();
        for (String param : query.split("&")) {
            if (param.isEmpty()) {
                continue;
            }
            String key = param.split("=")[0];
            if (!TRACKING_PARAMS.contains(key)) {
                kept.add(param);
            }
        }

        if (kept.isEmpty()) {
            return base;
        }
        return base + "?" + String.join("&", kept);
    }

    private String removeTrailingSlash(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

    // 카테고리를 안 줬으면 URL 보고 추측
    private String resolveCategory(String category, String url) {
        if (category != null && !category.isBlank()) {
            return category;   // 직접 준 게 있으면 그걸 존중
        }

        for (Map.Entry<String, String> entry : DOMAIN_CATEGORY.entrySet()) {
            if (url.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return "기타";
    }

    // 안 본 것 중 가장 오래 묵은 것 하나만
    public BookmarkResponse findTodayPick() {
        Bookmark bookmark = bookmarkRepository
                .findFirstByStatusOrderBySavedAtAsc(ReadStatus.TODO)
                .orElseThrow(() -> new NoBookmarkToReadException());

        return BookmarkResponse.from(bookmark);
    }

    // 저장한 지 days일 이상 지났는데 아직 안 본 것들
    public List<BookmarkResponse> findRotten(int days) {
        LocalDate threshold = LocalDate.now().minusDays(days);
        return toResponses(
                bookmarkRepository.findByStatusAndSavedAtLessThanEqual(ReadStatus.TODO, threshold)
        );
    }

}