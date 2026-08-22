package com.ji2081.linkbox.exception;

public class DuplicateUrlException extends RuntimeException {

    public DuplicateUrlException(String url) {
        super("이미 저장한 링크입니다. url=" + url);
    }
}
