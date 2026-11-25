# studyFinal · 심화 응용

## ① 목표

study11부터 study18과 중간 게시판을 Vaadin 하나에서 배포 가능한 서비스 점검으로 닫는다.

## ② 📖 이론

최종 서비스는 새 프레임워크를 붙이지 않는다. 중간 지점의 게시판과 로그인 위에 DB 프로필, Hilla 목록, 파일·메일·결제 대역, OpenAPI, Push, 문서 질의, CI를 얹는다. 각 기능은 꺼져 있어도 게시판이 떠야 한다. 비밀과 컨테이너가 없을 때의 기본값이 그 안전장치다.

비유하면 중간 지점이 매장이고, 심화는 창고, 간판, 벨, 계산 연습이다. 창고 문이 잠겨도 매장 계산은 된다.

## ③ 🛠️ 실기

### 기능 목록

- 중간 지점의 게시판, 로그인, CRUD, 공통 API v1
- PostgreSQL 또는 MariaDB 프로필, Mongo 메모는 선택
- Hilla `/hilla-board`가 `PostCard` 필드로 목록을 표시
- 파일 업로드, 메일 로그, 결제 mock, OAuth 등록은 프로필
- OpenAPI `/api-docs`
- Push 알림 `/live`
- 문서 요약·질의 `/tools`, SSE `/api/ai/stream`, MCP 검색
- `mvn test`, GitHub Actions, Dockerfile, compose

### 연동 구성

```text
브라우저 Flow / Hilla
        │  세션 또는 Bearer
        ▼
PostService · AuthService · SummaryClient
        │
        ├── H2 또는 PostgreSQL 또는 MariaDB
        ├── Mongo 프로필의 notes
        ├── 디스크 uploads
        ├── 로그 메일 또는 SMTP
        └── mock 결제 · 선택 HTTP AI
```

### 구현 순서

1. studyMidpoint 시나리오가 로컬 H2에서 동작한다.
2. study14 계약과 `mvn test`를 고정한다.
3. study12 Hilla 목록 필드가 `/api/posts`와 같은지 본다.
4. study11에서 프로필 파일만 바꿔 DB를 교체한다. 컨테이너가 없으면 이 단계를 건너뛰고 이유를 적는다.
5. study13 외부 서비스는 비밀 없이 mock과 로그로 확인한다.
6. study17 두 탭으로 Push를 확인한다.
7. study18 문서 밖 질문이 거절되는지 확인한다.
8. study16 체크리스트에서 CI와 백업을 환경에 맞게 표시한다.
9. study10 환경변수와 HTTPS 프록시 항목을 배포 전에 다시 본다.

### 배포 체크리스트

- [ ] `APP_SECRET`, `DATABASE_URL`, `CORS_ORIGIN`, `PORT`가 환경변수다
- [ ] `.env`와 `data/`가 이미지와 Git에 없다
- [ ] `mvn test`가 통과한다
- [ ] `/health`, 로그인, 글 등록, 목록 `meta`, 타인 수정 403을 확인했다
- [ ] Hilla 목록 필드와 API 목록 필드가 같다
- [ ] 메일·결제·AI는 키가 없으면 mock 또는 로그다
- [ ] Push는 한 서버에서만 보장한다고 적었다
- [ ] 프로덕션 빌드 또는 그 빌드를 아직 못 한 이유를 적었다
- [ ] DB 백업 명령을 운영 엔진에 맞게 골랐다

### 최종 회고

이 저장소는 Vaadin Flow를 화면의 기본으로 두고 Hilla는 목록 한 장으로 붙였다. Spring Boot는 Vaadin을 실행하는 기반이라 같은 프로젝트 안에 있다. 다른 웹 프레임워크와 화면을 섞어 비교하지는 않았다.

확인된 실행은 `mvn test` 3건이다. Java 26 런타임이 Java 21 바이트코드를 컴파일하고 테스트를 통과했다. Docker 기동, 실제 결제, 구글 로그인, 외부 모델 응답, 공개 HTTPS 배포는 이 환경에서 실행하지 않았다. 해당 항목은 체크리스트에 남겨 둔다.

세션 메모리는 Flow의 대가다. Grid를 페이지로 읽고, Hilla 목록은 브라우저 상태를 쓰며, 큰 컬렉션을 뷰 필드에 두지 않는 것으로 그 대가를 줄였다.

## ④ 🔗 연동 포인트

배포 환경의 `DATABASE_URL`이 study11 프로필과 만나고, `AI_API_URL`이 study18과 만나며, `CORS_ORIGIN`이 study7의 API와 만난다. 한 키가 빠지면 그 기능만 기본값으로 남고 게시판 조회는 유지되는 것이 이 서비스의 연결 규칙이다.

## ⑤ 오류 노트

| 증상 | 원인 | 해결 |
| --- | --- | --- |
| 심화 기능을 켜자 게시판이 죽음 | 외부 서비스 실패를 기동 조건으로 두었다 | Mongo와 OAuth는 프로필, 메일와 결제는 기본 비활성이다 |
| Hilla와 API 필드가 다름 | 목록 DTO를 두 개 만들었다 | `PostCard` 하나만 쓴다 |
| 테스트는 통과하는데 배포 화면이 없음 | 프로덕션 프론트 빌드가 없다 | `mvn -Pproduction package`를 배포 산출물로 쓴다 |

## ⑥ 직접 해보기

1. 배포 체크리스트를 위에서 아래로 표시하고, 못 한 항목은 환경 이유를 적는다.
2. mock AI와 mock 결제가 키 없이 동작하는지 `/tools`에서 본다.
3. 회고에 적은 세션 메모리 제한을 Grid 페이지 크기와 연결해 한 문장으로 말한다.

## ⑦ ☐ 체크리스트

- [ ] 중간 게시판 시나리오가 그대로 동작한다
- [ ] DB 교체, Hilla, 외부 서비스, AI, Push, CI가 문서의 순서대로 연결된다
- [ ] 비밀이 없는 기본 실행이 가능하다
- [ ] 실행하지 못한 배포 항목을 숨기지 않고 적었다

## ⑧ 다음 단계

이 저장소의 Vaadin 학습은 여기까지다. 운영에 올릴 때는 배포 체크리스트의 빈 칸을 환경에서 채운 뒤, 프로덕션 JAR와 데이터베이스 백업을 한 번씩 복원해 본다.

참고: [studyMidpoint](studyMidpoint.md), [study11](study11.md)부터 [study18](study18.md)
