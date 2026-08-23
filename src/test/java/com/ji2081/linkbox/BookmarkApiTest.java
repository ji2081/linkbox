package com.ji2081.linkbox;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional   // 각 테스트가 끝나면 DB를 원래대로 되돌림
class BookmarkApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("북마크를 저장하면 TODO 상태로 시작한다")
    void saveBookmark() throws Exception {
        String request = """
                {
                  "url": "https://example.com/save-test",
                  "title": "테스트 링크",
                  "category": "테스트",
                  "memo": "메모입니다"
                }
                """;

        mockMvc.perform(post("/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("테스트 링크"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    @DisplayName("추적 파라미터만 다른 같은 링크는 중복으로 거절된다")
    void rejectDuplicateUrl() throws Exception {
        String first = """
                { "url": "https://youtu.be/testVideo", "title": "첫 저장" }
                """;
        String second = """
                { "url": "https://youtu.be/testVideo?si=abc123", "title": "같은 영상" }
                """;

        mockMvc.perform(post("/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(first))
                .andExpect(status().isOk());

        mockMvc.perform(post("/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(second))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_URL"));
    }

    @Test
    @DisplayName("URL 없이 저장하면 400과 함께 이유를 알려준다")
    void rejectBlankUrl() throws Exception {
        String request = """
                { "url": "", "title": "제목만 있음" }
                """;

        mockMvc.perform(post("/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    @DisplayName("없는 북마크를 삭제하면 404가 반환된다")
    void deleteNotFound() throws Exception {
        mockMvc.perform(delete("/bookmarks/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BOOKMARK_NOT_FOUND"));
    }
}