package com.ji2081.linkbox.exception;

public class NoBookmarkToReadException extends RuntimeException {

    public NoBookmarkToReadException() {
        super("아직 안 본 북마크가 없습니다.");
    }
}
