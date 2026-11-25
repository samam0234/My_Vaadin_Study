# study14 · API 설계

## ① 목표

공통 API 스펙 v1에 맞춘 경로, 페이지, 오류, OpenAPI 파일을 한 문서로 고정한다.

## ② 📖 이론

REST에서 경로 이름은 명사 복수형이다. `/api/posts`, `/api/posts/{id}`, `/api/posts/{id}/comments`, `/api/comments/{id}`를 쓴다. 동사 경로를 만들지 않는다. 수정은 PUT 대신 PATCH 하나다.

성공 본문은 단건과 인증이 `data`, 목록이 `data`와 `meta`다. 오류는 항상 `error.code`, `error.message`, `error.requestId`다. 검증만 `error.fields`를 더한다. 상태와 코드는 스펙 표를 따른다. 없는 경로는 404 `NOT_FOUND`다.

버전은 URL에 `/v1`을 넣지 않는다. 지금 계약이 v1이고 경로 `/api`가 그 계약이다. 필드 의미가 깨질 때만 `/api/v2`를 새로 연다. 이 저장소는 v2를 만들지 않았다.

페이지는 1부터, limit 1~50, 정렬은 `createdAt`, `-createdAt`, `-commentCount`다. 잘못된 정렬은 400이 아니라 기본값 `-createdAt`으로 고친다.

비유하면 OpenAPI 파일은 메뉴판이다. 주방이 바뀌어도 메뉴판의 음식 이름이 같으면 손님 코드는 그대로다.

## ③ 🛠️ 실기

계약 파일은 `lesson/src/main/resources/openapi.yaml`이다. 서버는 그 파일을 `GET /api-docs`로 돌려준다. Swagger UI 의존성은 넣지 않았다. Spring Boot 4와 맞는 UI 라이브러리를 억지로 올리면 런타임이 갈라질 수 있어, 계약 파일을 기준 문서로 둔다. 파일을 열어 `openapi: 3.0.3`과 경로 목록을 본다.

```powershell
Invoke-RestMethod http://localhost:8080/api-docs
Invoke-RestMethod "http://localhost:8080/api/posts?page=1&limit=10&sort=-createdAt"
```

오류 코드:

| 상태 | code | 언제 |
| --- | --- | --- |
| 400 | `VALIDATION_ERROR` | 필드 검증 |
| 400 | `BAD_REQUEST` | JSON이 깨짐 |
| 401 | `UNAUTHORIZED` | 토큰 없음, 폐기, 위조 |
| 401 | `INVALID_CREDENTIALS` | 로그인 실패 |
| 403 | `FORBIDDEN` | 남의 글 |
| 404 | `NOT_FOUND` | 없는 글, 없는 댓글 |
| 409 | `EMAIL_TAKEN` | 중복 이메일 |
| 413 | `PAYLOAD_TOO_LARGE` | 업로드 한도 |
| 500 | `INTERNAL` | 그 외. 원문 예외는 로그에만 |

`/health`만 `/api` 접두어가 없다. 시각은 `Instant`가 직렬화한 ISO-8601 UTC다.

## ④ 🔗 연동 포인트

Flow Grid와 Hilla 화면은 이 목록 필드를 공유한다. 프론트를 나중에 붙여도 `data`와 `meta`, `error` 모양을 지키면 된다. study6의 테스트가 이 계약의 회귀 테스트다.

## ⑤ 오류 노트

| 증상 | 원인 | 해결 |
| --- | --- | --- |
| 목록이 배열만 옴 | 컨트롤러가 `List`를 그대로 반환했다 | `PostPage`로 `data`와 `meta`를 감싼다 |
| 오류가 Spring 기본 `timestamp` 모양 | 예외 처리기가 API 패키지를 보지 않는다 | `@RestControllerAdvice(basePackages = "study.vaadin.api")` |
| 페이지가 한 칸 어긋남 | 0부터 세는 저장소를 그대로 노출 | `PageQuery`가 1부터 받는다 |

## ⑥ 직접 해보기

1. `openapi.yaml`의 경로와 컨트롤러 `@RequestMapping`을 하나씩 맞춘다.
2. 깨진 JSON을 POST해 `BAD_REQUEST`인지 본다.
3. `sort=title`처럼 허용하지 않은 값을 보내 `meta`가 기본 정렬로 나오는지 본다.

## ⑦ ☐ 체크리스트

- [ ] 경로는 복수 명사이고 수정은 PATCH다
- [ ] OpenAPI 파일을 `/api-docs`에서 받을 수 있다
- [ ] 페이지, limit, 정렬 기본값이 스펙과 같다
- [ ] 오류는 `error` 객체 하나다

## ⑧ 다음 단계

[study15](study15.md)에서 같은 조회를 캐시하고, 메일과 토큰 정리를 비동기로 보낸다.

참고: [공통 API 스펙](https://app.notion.com/p/3d6d7dec5e2e8139adbfd36dd506c65e)
