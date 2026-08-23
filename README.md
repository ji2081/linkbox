# linkbox

**나중에 볼 링크를 카테고리별로 모아두고, 다 본 건 체크해서 지워나가는 REST API**

여행 준비하며 모은 여행 관련 꿀팁 링크, 공부하려고 찾아둔 유튜브·velog·논문 링크가 브라우저 북마크에 쌓이기만 하고 다시 열어보지 않는 문제에서 아이디어를 찾았습니다. 

여행, 개발 등의 카테고리로 묶고, 어떤 내용인지 알 수 있게 메모를 달고, 다 본 것은 완료 처리해 남은 것만 보이게 하는 API입니다.

> 스프링 부트를 독학하며 만든 개인 토이 프로젝트입니다. 

---

## 기능

| 기능 | 설명 |
|---|---|
| 링크 저장 | URL·제목·카테고리·메모를 저장. 상태는 `TODO`로 시작 |
| **제목 자동 추출** | 제목을 생략하면 해당 페이지의 `og:title`을 읽어 채움 |
| **카테고리 자동 추천** | 카테고리를 생략하면 URL 도메인으로 추측 (`youtu.be` → 영상) |
| **중복 저장 방지** | 추적 파라미터·슬래시를 정규화해 같은 링크로 판단되면 거절 |
| 목록 조회 | 카테고리·상태로 필터 |
| 완료 체크 / 취소 | `TODO` ↔ `DONE` 전환 |
| **오늘의 한 개** | 안 본 것 중 가장 오래 묵은 링크 하나만 추천 |
| **썩은 링크** | N일 이상 방치된 링크 목록 |
| 삭제 | 링크 삭제 |

---

## 기술 스택

- **Java 25**, **Spring Boot 4.1.1**
- Spring Web, Spring Data JPA, Bean Validation
- H2 (인메모리)
- Jsoup 1.23.1 — 링크 제목 추출
- springdoc-openapi 3.1.0 — Swagger UI
- JUnit 5 + MockMvc
- Gradle

---

## 실행 방법

```bash
git clone https://github.com/ji2081/linkbox.git
cd linkbox
./gradlew bootRun
```

| | 주소 |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| H2 콘솔 | http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:linkbox`, User: `sa`) |

테스트 실행:

```bash
./gradlew test
```

> H2 인메모리 DB를 사용하므로 서버를 재시작하면 데이터가 초기화되는 것에 유의.

---

## API

| Method | Path | 설명 |
|---|---|---|
| `POST` | `/bookmarks` | 링크 저장 |
| `GET` | `/bookmarks` | 전체 조회 (`?category=여행&status=TODO`) |
| `GET` | `/bookmarks/today` | 안 본 것 중 가장 오래된 하나 |
| `GET` | `/bookmarks/rotten` | N일 이상 방치된 링크 (`?days=30`) |
| `PATCH` | `/bookmarks/{id}/done` | 완료 처리 |
| `PATCH` | `/bookmarks/{id}/todo` | 완료 취소 |
| `DELETE` | `/bookmarks/{id}` | 삭제 |

### 요청 예시

`url` 외에는 모두 선택.

```json
POST /bookmarks
{
  "url": "https://github.com/spring-projects/spring-boot",
  "memo": "스프링 부트 소스코드"
}
```

```json
200 OK
{
  "id": 1,
  "url": "https://github.com/spring-projects/spring-boot",
  "title": "GitHub - spring-projects/spring-boot: Spring Boot helps you...",
  "category": "개발",
  "memo": "스프링 부트 소스코드",
  "status": "TODO",
  "savedAt": "2026-08-23"
}
```

제목과 카테고리가 자동으로 채워짐.

### 에러 응답

모든 에러는 같은 형식으로 반환됨.

```json
{
  "code": "DUPLICATE_URL",
  "message": "이미 저장한 링크입니다. url=https://youtu.be/EDBwUca64o4"
}
```

| 상황 | 코드 | HTTP |
|---|---|---|
| 입력값 검증 실패 | `INVALID_INPUT` | 400 |
| 없는 북마크 조회/수정/삭제 | `BOOKMARK_NOT_FOUND` | 404 |
| 안 본 북마크가 없음 | `NO_BOOKMARK_TO_READ` | 404 |
| 이미 저장한 링크 | `DUPLICATE_URL` | 409 |

---

## 설계 핵심

### 1. URL 정규화를 통한 중복 방지

같은 링크라도 공유할 때 붙는 추적 파라미터나 끝의 슬래시 때문에 문자열이 달라져 중복 저장되는 문제 발생.

```
https://youtu.be/EDBwUca64o4
https://youtu.be/EDBwUca64o4?si=Yy1LuoFZZMne4jkK    ← 유튜브 공유 링크
https://youtu.be/EDBwUca64o4/
```

초반 피드백 과정에선 물음표 뒤를 통째로 잘라내려 했지만, 그럴 시 `youtube.com/watch?v=xxx` 처럼 쿼리 파라미터가 링크의 정체인 경우 링크 자체가 망가지는 문제가 발생함.

그래서 **버릴 파라미터 목록(`si`, `igsh`, `utm_*`, `fbclid`, `gclid`)을 정의해 선택적으로 제거**하는 방식을 택함.

```
https://www.youtube.com/watch?v=aircAruvnKk&si=xxxxx
                        ↓
https://www.youtube.com/watch?v=aircAruvnKk    (si만 제거, v는 유지)
```

또한 애플리케이션 레벨의 중복 검사는 **동시에 같은 URL로 두 요청이 들어오면 둘 다 통과**할 수 있으므로, DB에도 `unique` 제약을 걸어 이중으로 방어함.

### 2. 외부 호출 실패가 저장 실패로 이어지지 않게

제목 자동 추출은 외부 사이트에 직접 접속하는 기능이라 언제든 실패할 수 있음 (봇 차단, 타임아웃, 이미 죽은 링크 등)

**제목을 못 가져온 것이 링크 저장 실패의 이유가 되어서는 안 된다**고 판단해 다음과 같이 처리함.

- `LinkTitleFetcher`는 `Optional<String>`을 반환 → 호출하는 쪽이 실패를 처리하도록 강제
- 예외를 잡아 빈 값으로 변환
- **3단 폴백**: 사용자가 준 제목 → 페이지에서 추출 → URL을 제목으로
- 3초 타임아웃 → 느린 사이트 하나가 요청 전체를 붙잡지 못하게
- 외부에서 온 제목은 컬럼 길이 제한(500자)에 맞춰 잘라냄

### 3. 전역 예외 처리로 응답 형식 통일

`@RestControllerAdvice`로 예외를 한곳에서 받아, 어떤 실패든 `{ code, message }` 형태로 나가도록 함. Controller에는 `try-catch`를 쓰지 않았음.

상황별로 예외 클래스를 나누게 되었는데, 이는 하나의 예외로 뭉치면 404와 409를 구분해서 응답할 수 없는 문제를 해결하기 위함임.

### 4. setter 대신 의미 있는 메서드

엔티티의 상태 변경은 `markAsDone()` / `markAsTodo()` 로만 가능하게 하고 setter는 제작하지 않음. 값이 아무 데서나 바뀌는 것을 막고, 나중에 완료 시각도 함께 기록 같은 요구가 생기면 한 곳만 고치면 되도록 함.

---

## 테스트

핵심 동작 4가지를 MockMvc 통합 테스트로 검증.

- 저장 시 상태가 `TODO`로 시작하는지
- 추적 파라미터만 다른 같은 링크가 중복으로 거절되는지 (URL 정규화 회귀 방지)
- URL 없이 저장하면 400과 이유가 반환되는지
- 없는 북마크 삭제 시 404가 반환되는지

---

## 향후 과제

- [ ] **죽은 링크 자동 검사** :  `@Scheduled`로 주기적으로 저장된 URL을 확인해 404면 표시
- [ ] **카테고리를 별도 엔티티로 분리** : 현재는 문자열이라 이름 변경 시 전체를 수정해야 하고 카테고리 목록 조회가 비효율적. `@ManyToOne` 관계로 전환 필요
- [ ] **URL 정규화 고도화** : `http`/`https`, `www.` 유무, `m.blog.naver.com` vs `blog.naver.com` 은 아직 다른 URL로 인식됨
- [ ] **제목 추출 비동기화** : 현재는 동기 호출이라 최대 3초 응답 지연. 저장 후 백그라운드 갱신 방식으로 개선
- [ ] **테스트에서 외부 호출 격리** : `LinkTitleFetcher`를 목(mock)으로 대체
- [ ] 카테고리 공유 기능 (로그인 필요)
- [ ] 주간 리마인드 알림
- [ ] PostgreSQL 전환, Docker + GitHub Actions CI
- [ ] 프론트엔드 제작

---

## 라이선스

MIT