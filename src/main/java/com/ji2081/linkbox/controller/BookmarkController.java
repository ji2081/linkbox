package com.ji2081.linkbox.controller;

import com.ji2081.linkbox.dto.BookmarkCreateRequest;
import com.ji2081.linkbox.dto.BookmarkResponse;
import com.ji2081.linkbox.service.BookmarkService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookmarks")   // 이 클래스의 모든 주소 앞에 /bookmarks 가 붙음
public class BookmarkController {

    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @PostMapping   // POST /bookmarks
    public BookmarkResponse create(@RequestBody BookmarkCreateRequest request) {
        return bookmarkService.save(request);
    }

    @GetMapping    // GET /bookmarks
    public List<BookmarkResponse> findAll() {
        return bookmarkService.findAll();
    }
}
