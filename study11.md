# study11 · DB 확장

## ① 목표

같은 JPA 엔티티로 H2, PostgreSQL, MariaDB를 바꾸고, MongoDB는 문서 저장소로 따로 연결한다.

## ② 📖 이론

JPA는 엔티티와 표 사이의 번역기다. H2, PostgreSQL, MariaDB는 번역 대상 방언만 다르다. 엔티티 필드를 바꾸지 않고 URL, 계정, 드라이버, 방언을 바꾼다. `ddl-auto: update`는 학습용으로 없는 열을 만든다. 운영은 study8의 `validate`다.

MongoDB는 표가 아니라 문서다. `NoteDocument`는 `@Document("notes")`이고 `NoteRepository`는 `MongoRepository`다. JPA 리포지토리와 한 트랜잭션으로 묶지 않는다. 기본 실행에서는 Mongo 자동 설정을 빼 두었다. 프로필 `mongo`일 때만 `MongoProfileConfig`가 클라이언트를 만든다.

비유하면 JPA 엔티티는 같은 편지지이고, DB는 우체국이다. Mongo는 편지지가 아니라 서류 봉투다.

## ③ 🛠️ 실기

저장소 루트에서 필요한 컨테이너만 띄운다.

```powershell
docker compose up -d postgres
Set-Location lesson
mvn spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

`application-postgres.yml`은 `jdbc:postgresql://127.0.0.1:5432/board`, 계정 `board` / `board`다. MariaDB는 프로필 `mariadb`, 포트 3306, 드라이버 `org.mariadb.jdbc.Driver`다.

```powershell
docker compose up -d mariadb
mvn spring-boot:run "-Dspring-boot.run.profiles=mariadb"
```

Mongo는 게시판 표를 대체하지 않는다. `/tools`의 Mongo 메모 버튼이 `notes` 컬렉션에 한 건을 넣는다.

```powershell
docker compose up -d mongo
mvn spring-boot:run "-Dspring-boot.run.profiles=mongo"
```

기본 프로필에서는 Mongo 클라이언트를 만들지 않는다. 그래서 Mongo가 꺼져 있어도 `mvn test`와 게시판이 뜬다. 이 환경에서는 Docker 컨테이너를 띄우지 않았고, 프로필 파일과 조건부 설정이 코드로 있다.

ORM 차이:

| | H2 / PostgreSQL / MariaDB | MongoDB |
| --- | --- | --- |
| 매핑 | `@Entity`, `@Table` | `@Document` |
| 저장소 | `JpaRepository` | `MongoRepository` |
| 스키마 | 열과 외래 키 | 문서 필드 |
| 켜는 방법 | 기본 또는 프로필 | 프로필 `mongo`만 |

## ④ 🔗 연동 포인트

API 경로는 그대로다. 바뀌는 것은 `DATABASE_URL`과 프로필이다. study16의 배포 환경변수는 이 URL을 컨테이너 서비스 이름으로 바꾼다. 예를 들어 같은 compose 네트워크에서는 `jdbc:postgresql://postgres:5432/board`다.

## ⑤ 오류 노트

| 증상 | 원인 | 해결 |
| --- | --- | --- |
| PostgreSQL 연결 거부 | 컨테이너가 없다 | `docker compose up -d postgres` 후 재시작 |
| MariaDB 인증 실패 | 비밀번호가 yml과 다르다 | `DB_PASSWORD`를 compose의 값과 맞춘다 |
| 기동 로그에 Mongo 경고 | 예전 설정이 자동 설정을 켜 두었다 | 기본 실행은 `MongoAutoConfiguration`을 제외한다 |
| JPA가 `NoteRepository`를 언급 | 인터페이스가 같은 앱 아래에 있다 | Mongo 프로필이 꺼져 있으면 JPA 저장소로 등록되지 않는다 |

## ⑥ 직접 해보기

1. 프로필 없이 기동해 H2로 `/api/posts`가 되는지 본다.
2. postgres 프로필의 JDBC URL에서 호스트와 데이터베이스 이름을 찾는다.
3. Mongo를 켜지 않고 `/tools`의 Mongo 버튼을 눌러 안내 문장이 나는지 본다.

## ⑦ ☐ 체크리스트

- [ ] H2, PostgreSQL, MariaDB가 같은 `Post` 엔티티를 쓴다
- [ ] 드라이버와 방언이 프로필 파일에 있다
- [ ] Mongo는 `@Document`와 `MongoRepository`이며 기본 기동에서 빠진다
- [ ] 연결 비밀번호는 compose와 환경변수에만 있다

## ⑧ 다음 단계

[study12](study12.md)에서 Hilla React 화면이 같은 목록 필드를 `@BrowserCallable`로 읽게 한다.

참고: [Vaadin Spring Boot](https://vaadin.com/docs/latest/flow/integrations/spring)
