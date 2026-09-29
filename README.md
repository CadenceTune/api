# CadenceTune API

> CadenceTune 서비스의 핵심 비즈니스 로직 및 데이터를 처리하는 Spring Boot 기반 API 서버입니다.

---

## 1. 개요 (Overview)

CadenceTune API는 사용자 인증, 데이터 관리, 외부 서비스(Processor 등) 연동 및 주요 비즈니스 로직을 담당하는 백엔드 RESTful API 서버입니다.

---

## 2. 기술 스택 (Tech Stack)

| 구분             | 기술 스택                                               |
|----------------|-----------------------------------------------------|
| **Language**   | Java 17                                             |
| **Framework**  | Spring Boot 4.1.1, Spring Data JPA, Spring Security |
| **Build Tool** | Gradle                                              |
| **Database**   | PostgreSQL                                          |

---

## 3. 개발 및 커밋 규칙 (Development Rules)

### Commit Convention

커밋 메시지는 `[type] 설명 (#이슈번호)` 형식으로 작성합니다.

| Type           | 설명                              | 작성 예시                                 |
|----------------|---------------------------------|---------------------------------------|
| **[feat]**     | 새로운 기능 개발 및 추가                  | `[feat] 사용자 로그인 API 구현 (#12)`         |
| **[fix]**      | 버그 수정                           | `[fix] 토큰 만료 에러 처리 수정 (#24)`          |
| **[docs]**     | 문서 작성 및 수정 (README 등)           | `[docs] README 개발 규칙 업데이트 (#3)`       |
| **[style]**    | 코드 포맷팅, 세미콜론 수정 (비즈니스 로직 변경 없음) | `[style] Google Java Format 적용 (#5)`  |
| **[refactor]** | 코드 리팩토링 (기능 변경 없이 구조 개선)        | `[refactor] Service 로직 분리 (#18)`      |
| **[test]**     | 테스트 코드 작성 및 수정                  | `[test] 회원가입 단위 테스트 추가 (#7)`          |
| **[chore]**    | 빌드 설정, 의존성 패키지 관리 등 단순 작업       | `[chore] Spring Security 의존성 추가 (#1)` |

* **제목 작성 규칙**
    * 50자 이내로 작성하며 끝에 마침표(`.`)를 사용하지 않음
    * 명사형 어미(`~ 구현`, `~ 추가`, `~ 수정`, `~ 적용`)로 작성
    * 이슈 트래커 연동을 위해 제목 끝에 `(#이슈번호)` 명시

---

### Branch Strategy (Git Flow)

* **`main`**: Production 환경 배포용 브랜치
* **`develop`**: 다음 버전을 위한 중심 개발 브랜치
* **`feat/{issue-number}-{feature-name}`**: 기능 개발 브랜치 (예: `feat/12-user-login`)
* **`fix/{issue-number}-{bug-name}`**: 버그 수정 브랜치 (예: `fix/24-token-error`)

---

### Code Style & Formatting

* **IDE Formatting**: Google Java Format 세팅 후 자동 포맷팅 적용
* **Lombok 사용 가이드**
    * `@Getter`, `@RequiredArgsConstructor` 위주로 사용
    * `@Data` 및 `@Setter` 사용 지양 (불변성 유지)
* **Entity 작성 규칙**
    * 객체 생성 안정성을 위해 `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 필수 적용
    * `@Builder` 패턴을 사용하여 객체 생성

---

## 4. 디렉토리 및 패키지 구조 (Package Structure)

도메인 기반 패키지 구조를 따르며, 구현 코드는 지정된 위치에 작성합니다.

```text
src/main/java/com/cadencetune/api/
├── global/                     # 프로젝트 전역 공통 모듈
│   ├── config/                 # Security, JPA, Swagger 등 설정
│   ├── error/                  # 공통 예외 처리 (GlobalExceptionHandler, CustomException)
│   └── util/                   # 공통 유틸리티
│
└── domain/                     # 도메인 단위 패키지
    └── {domain_name}/          # 예: user, track, playlist 등
        ├── controller/         # API 엔드포인트 (@RestController)
        ├── service/            # 비즈니스 로직
        ├── repository/         # Spring Data JPA Repository
        ├── entity/             # JPA 엔티티 (@Entity)
        └── dto/                # Request / Response DTO
            ├── request/
            └── response/
