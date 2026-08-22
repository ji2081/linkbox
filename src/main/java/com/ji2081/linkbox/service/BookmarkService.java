package com.ji2081.linkbox.service;

import com.ji2081.linkbox.domain.Bookmark;
import com.ji2081.linkbox.dto.BookmarkCreateRequest;
import com.ji2081.linkbox.dto.BookmarkResponse;
import com.ji2081.linkbox.repository.BookmarkRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;

    // 생성자 주입. 스프링이 BookmarkRepository를 알아서 넣어줌
    public BookmarkService(BookmarkRepository bookmarkRepository) {
        this.bookmarkRepository = bookmarkRepository;
    }

    public BookmarkResponse save(BookmarkCreateRequest request) {
        Bookmark bookmark = new Bookmark(
                request.url(),
                request.title(),
                request.category(),
                request.memo()
        );
        Bookmark saved = bookmarkRepository.save(bookmark);
        return BookmarkResponse.from(saved);
    }

    public List<BookmarkResponse> findAll() {
        List<BookmarkResponse> result = new ArrayList<>();
        for (Bookmark bookmark : bookmarkRepository.findAll()) {
            result.add(BookmarkResponse.from(bookmark));
        }
        return result;
    }
}
