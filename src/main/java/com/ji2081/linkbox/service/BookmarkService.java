package com.ji2081.linkbox.service;

import com.ji2081.linkbox.domain.Bookmark;
import com.ji2081.linkbox.domain.ReadStatus;
import com.ji2081.linkbox.dto.BookmarkCreateRequest;
import com.ji2081.linkbox.dto.BookmarkResponse;
import com.ji2081.linkbox.repository.BookmarkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ji2081.linkbox.exception.BookmarkNotFoundException;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;

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
}