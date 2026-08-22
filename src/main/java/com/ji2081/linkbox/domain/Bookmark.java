package com.ji2081.linkbox.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity  // 이 클래스 = bookmark 테이블
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 번호는 DB가 매김
    private Long id;

    @Column(nullable = false, length = 500, unique = true)
    private String url;

    @Column(nullable = false, length = 500)
    private String title;

    private String category;

    @Column(length = 1000)
    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReadStatus status;

    @Column(nullable = false)
    private LocalDate savedAt;

    protected Bookmark() {  // JPA용 빈 생성자
    }

    public Bookmark(String url, String title, String category, String memo) {
        this.url = url;
        this.title = title;
        this.category = category;
        this.memo = memo;
        this.status = ReadStatus.TODO;  // 새로 저장하면 무조건 TODO
        this.savedAt = LocalDate.now();
    }

    // setter 대신 의미 있는 메서드로만 상태 변경
    public void markAsDone() {
        this.status = ReadStatus.DONE;
    }

    public void markAsTodo() {
        this.status = ReadStatus.TODO;
    }

    public Long getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getMemo() {
        return memo;
    }

    public ReadStatus getStatus() {
        return status;
    }

    public LocalDate getSavedAt() {
        return savedAt;
    }

}


