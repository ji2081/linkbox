package com.ji2081.linkbox.exception;

public class BookmarkNotFoundException extends RuntimeException {

    public BookmarkNotFoundException(Long id) {
        super("북마크를 찾을 수 없습니다. id=" + id);
    }
}
