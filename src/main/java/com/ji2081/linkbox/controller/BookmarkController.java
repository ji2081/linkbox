package com.ji2081.linkbox.controller;

import com.ji2081.linkbox.domain.ReadStatus;
import com.ji2081.linkbox.dto.BookmarkCreateRequest;
import com.ji2081.linkbox.dto.BookmarkResponse;
import com.ji2081.linkbox.service.BookmarkService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @PostMapping
    public BookmarkResponse create(@Valid @RequestBody BookmarkCreateRequest request) {
        return bookmarkService.save(request);
    }

    // GET /bookmarks?category=여행&status=TODO  (둘 다 생략 가능)
    @GetMapping
    public List<BookmarkResponse> findAll(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) ReadStatus status
    ) {
        return bookmarkService.findAll(category, status);
    }

    @PatchMapping("/{id}/done")
    public BookmarkResponse markAsDone(@PathVariable Long id) {
        return bookmarkService.markAsDone(id);
    }

    @PatchMapping("/{id}/todo")
    public BookmarkResponse markAsTodo(@PathVariable Long id) {
        return bookmarkService.markAsTodo(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)   // 성공했지만 돌려줄 내용 없음 (204)
    public void delete(@PathVariable Long id) {
        bookmarkService.delete(id);
    }
}