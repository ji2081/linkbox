package com.ji2081.linkbox.repository;

import com.ji2081.linkbox.domain.Bookmark;
import com.ji2081.linkbox.domain.ReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import java.time.LocalDate;

// 구현 클래스 없음. 스프링이 실행 시점에 만들어 넣어줌
// JpaRepository 상속으로 save/findAll/findById/deleteById 자동 제공
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    // 메서드 이름만으로 쿼리 생성 → WHERE url = ?
    Optional<Bookmark> findByUrl(String url);

    boolean existsByUrl(String url);

    List<Bookmark> findByCategory(String category);

    List<Bookmark> findByStatus(ReadStatus status);

    List<Bookmark> findByCategoryAndStatus(String category, ReadStatus status);

    // 안 본 것 중 가장 오래 묵은 것 하나
    Optional<Bookmark> findFirstByStatusOrderBySavedAtAsc(ReadStatus status);

    // 특정 날짜 이전에 저장된 것들
    List<Bookmark> findByStatusAndSavedAtLessThanEqual(ReadStatus status, LocalDate date);

}
