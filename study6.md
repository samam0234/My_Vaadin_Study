# study6 · DB·API 연동

## ① 목표

H2에 게시글을 저장하고, 공통 API 스펙 v1의 목록·상세·등록·수정·삭제와 오류 형식을 맞춘다.

## ② 📖 이론

엔티티는 표의 한 줄이다. `Post`는 `posts` 표, `UserAccount`는 `accounts` 표, 태그는 `post_tags` 표다. 리포지토리는 그 표를 조회하는 메서드 모음이다. 서비스는 트랜잭션 안에서 엔티티를 레코드로 바꾼 뒤 뷰와 컨트롤러에 넘긴다. 뷰는 `PostService`를 생성자로 받는다. 컨트롤러도 같은 서비스를 호출한다. 화면과 API가 규칙을 두 번 만들지 않는다.

공통 스펙의 목록은 `data`와 `meta`다. `page`는 1부터다. Spring Data의 페이지는 0부터라 `PageQuery`가 1을 빼서 넘긴다. `limit` 기본값은 10이고 50을 넘으면 50이다. `sort`는 `createdAt`, `-createdAt`, `-commentCount`만 인정하고 나머지는 `-createdAt`이다.

연결 정보는 `application.yml`의 `${DATABASE_URL}`이다. 기본값은 `jdbc:h2:file:./data/board`다.

## ③ 🛠️ 실기

서버를 띄운 뒤 아래를 순서대로 실행한다. 데모 사용자 토큰으로 등록까지 확인하면 된다.

```powershell
$login = Invoke-RestMethod http://localhost:8080/api/auth/login -Method Post -ContentType 'application/json' -Body '{"email":"mina@example.com","password":"password123"}'
$token = $login.data.accessToken
$headers = @{ Authorization = "Bearer $token" }
Invoke-RestMethod http://localhost:8080/api/posts?q=첫"&"page=1"&"limit=10"&"sort=-createdAt
Invoke-RestMethod http://localhost:8080/api/posts -Method Post -Headers $headers -ContentType 'application/json' -Body '{"title":"API 글","body":"본문입니다","tags":["study"]}'
```

`PostController`가 경로를 연다.

- `GET /api/posts`는 `PostPage`를 그대로 반환한다. `data`와 `meta`가 최상위다.
- `GET /api/posts/{id}`는 `{"data": PostDetail}`이다. 본문과 태그가 여기 있다.
- `POST /api/posts`는 201과 `Location: /api/posts/{id}`다.
- `PATCH /api/posts/{id}`는 작성자만 200이다. 다른 사용자는 403 `FORBIDDEN`이다.
- `DELETE /api/posts/{id}`는 본문 없는 204다.

`PostRepository.search`는 제목과 본문을 대소문자 무시로 찾고, `@EntityGraph`로 작성자를 함께 읽는다. 목록에서 작성자 닉네임을 찍어도 `LazyInitializationException`이 나지 않게 서비스 메서드에 `@Transactional(readOnly = true)`를 붙였다. `open-in-view`는 false다.

오류 본문은 `ApiExceptionHandler`가 만든다.

```json
{
  "error": {
    "code": "NOT_FOUND",
    "message": "글을 찾을 수 없습니다.",
    "requestId": "..."
  }
}
```

검증 실패만 `fields`를 넣는다. 500은 예외 문장을 응답에 넣지 않고 로그에만 남긴다. 응답 헤더 `X-Request-Id`와 `error.requestId`는 같은 값이다.

테스트 `PostApiTest`가 이 흐름을 H2 메모리 DB로 재현한다. `mvn test`에서 통과했다.

Hilla 목록이 같은 필드를 쓰는지는 study12에서 화면과 이 JSON을 나란히 확인한다. 필드 이름은 `PostCard` 하나로 맞춘다.

## ④ 🔗 연동 포인트

`CORS_ORIGIN`이 `/api`의 브라우저 출처를 제한한다. 화면 Grid는 HTTP를 거치지 않고 같은 `PostService`를 호출한다. 외부 화면을 붙일 때는 이 JSON 모양만 맞으면 된다. study11은 같은 엔티티로 PostgreSQL과 MariaDB URL만 바꾼다.

## ⑤ 오류 노트

| 증상 | 원인 | 해결 |
| --- | --- | --- |
| 목록 `page`가 0부터임 | Spring Pageable 기본값을 그대로 썼다 | `PageQuery`가 1을 빼서 저장소에 넘긴다 |
| `LazyInitializationException` | 트랜잭션 밖에서 작성자를 읽었다 | 서비스에서 레코드로 바꾼 뒤 반환한다 |
| H2 파일이 잠김 | 서버가 이미 떠 있다 | 기존 `spring-boot:run`을 종료한다 |
| 401 JSON이 스펙과 다름 | 필터가 컨트롤러에 도달하기 전에 거절했다 | `SecurityConfiguration` 61~63행의 JSON을 확인한다 |

## ⑥ 직접 해보기

1. `limit=100`으로 목록을 호출해 `meta.limit`이 50인지 본다.
2. 다른 계정 토큰으로 `PATCH`해 403을 확인한다.
3. `/api/posts/999999`의 `error.code`가 `NOT_FOUND`인지 본다.

## ⑦ ☐ 체크리스트

- [ ] `Grid`로 게시판 목록, 페이지와 정렬
- [ ] 기존 Spring Boot 서비스와 JPA 리포지토리를 뷰에 주입
- [ ] 등록 201, 수정 200, 삭제 204, 검증 400, 권한 403, 없음 404
- [ ] 연결 정보는 `DATABASE_URL` 환경변수다
- [ ] 목록 응답에 `meta.page`, `meta.limit`, `meta.total`, `meta.totalPages`, `meta.q`가 있다

## ⑧ 다음 단계

[study7](study7.md)에서 이 API의 Bearer 토큰과 Flow 로그인 세션이 같은 사용자를 어떻게 가리키는지 본다.

참고: [공통 API 스펙 페이지](https://app.notion.com/p/3d6d7dec5e2e8139adbfd36dd506c65e)
