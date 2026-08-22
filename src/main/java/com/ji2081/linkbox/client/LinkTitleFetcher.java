package com.ji2081.linkbox.client;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class LinkTitleFetcher {

    private static final int TIMEOUT_MS = 3000;

    // 실패할 수 있으므로 Optional로 반환
    public Optional<String> fetchTitle(String url) {
        try {
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (compatible; linkbox/1.0)")
                    .timeout(TIMEOUT_MS)
                    .get();

            // 1순위: og:title (SNS 공유용 제목. 보통 더 깔끔함)
            String ogTitle = document.select("meta[property=og:title]").attr("content");
            if (!ogTitle.isBlank()) {
                return Optional.of(ogTitle.trim());
            }

            // 2순위: <title> 태그
            String title = document.title();
            if (!title.isBlank()) {
                return Optional.of(title.trim());
            }

            return Optional.empty();

        } catch (Exception e) {
            // 접속 실패, 타임아웃, 봇 차단(403) 등 → 제목만 못 가져올 뿐 저장은 계속되어야 함
            return Optional.empty();
        }
    }
}
