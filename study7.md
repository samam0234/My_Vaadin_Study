# study7 · 인증·보안 기초

## ① 목표

회원가입과 로그인, 세션과 Bearer 토큰, 역할, 비밀번호 해시, CORS, XSS, CSRF를 Vaadin 화면과 API에 나눠 둔다.

## ② 📖 이론

Flow 로그인은 HTTP 세션이다. 로그인 폼이 `POST /login`으로 아이디와 비밀번호를 보내면 Spring Security가 세션을 만들고, 이후 Vaadin 요청은 그 세션을 따라간다. API는 세션을 만들지 않는다. `POST /api/auth/login`이 HMAC 토큰을 주고, 클라이언트는 `Authorization: Bearer`로 다시 보낸다. 로그아웃은 그 토큰을 서버의 폐기 집합에 넣는다. 화면의 로그아웃은 `AuthenticationContext.logout()`으로 세션을 지운다.

두 방식이 같은 사용자를 가리키는 이유는 `AccountUserDetails`다. 세션이든 토큰이든 이메일로 `accounts` 행을 찾고, 그 행의 `role`로 `ROLE_USER` 또는 `ROLE_ADMIN`을 만든다.

비밀번호는 BCrypt 해시만 저장한다. 응답 레코드 `UserPublic`에는 이메일과 별명만 있다.

CSRF는 브라우저가 자동으로 붙이는 쿠키를 악용하는 공격이다. Flow 세션은 Vaadin 보안 설정이 CSRF를 처리한다. Bearer API는 브라우저가 헤더를 자동으로 붙이지 않으므로 API 체인에서만 CSRF를 끈다.

XSS는 사용자가 넣은 문장을 HTML로 실행하는 공격이다. Flow 컴포넌트의 `setText`와 Grid 열은 문자를 이스케이프한다. `innerHTML`에 글 본문을 넣지 않는다.

CORS는 다른 출처의 브라우저가 `/api`를 부를 수 있는 범위다. 허용 출처는 `CORS_ORIGIN` 하나다.

## ③ 🛠️ 실기

http://localhost:8080/login 에서 `mina@example.com` / `password123`으로 들어간다. 잘못된 비밀번호는 쿼리 `error`가 붙어 폼에 오류가 표시된다. 관리자 `admin@example.com` / `password123`만 `/admin`에 들어간다. 일반 사용자는 그 주소가 거절된다.

`SecurityConfiguration`의 두 체인은 46~74행이다.

- 47~49행 API, 건강 확인, 문서만 1번 체인이 맡는다.
- 50행 API는 CSRF를 끈다.
- 52행 API는 세션을 만들지 않는다.
- 55~56행 가입, 로그인, 글 조회는 토큰 없이 된다.
- 57행 그 외 API는 토큰이 필요하다.
- 71~73행 Vaadin 체인은 로그인 뷰를 등록하고, 모르는 주소는 Vaadin이 404를 그리게 통과시킨다.

`@PermitAll`은 로그인한 사용자, `@AnonymousAllowed`는 누구나, `@RolesAllowed("ADMIN")`은 관리자만이다. `AdminView`가 관리자 예시다.

회원가입 중복은 409 `EMAIL_TAKEN`이다. 로그인 실패는 401 `INVALID_CREDENTIALS`이고, 이메일과 비밀번호 중 어느 쪽이 틀렸는지는 말하지 않는다.

```powershell
Invoke-RestMethod http://localhost:8080/api/auth/signup -Method Post -ContentType 'application/json' -Body '{"email":"new@example.com","password":"password123","nickname":"new"}'
```

## ④ 🔗 연동 포인트

Flow 세션과 API 토큰은 저장소의 같은 `accounts` 행을 본다. 화면에서 쓴 글의 작성자와 `POST /api/posts`의 작성자 검사가 `CurrentUser` 하나다. study13의 OAuth 프로필은 이 로그인 폼을 대체하지 않고, 프로필 `oauth`에서 클라이언트 등록만 준비한다.

## ⑤ 오류 노트

| 증상 | 원인 | 해결 |
| --- | --- | --- |
| 로그인 후에도 글쓰기가 거절됨 | 뷰에 접근 주석이 없다 | `PostEditorView`의 `@PermitAll`을 확인한다 |
| 관리자 화면이 일반 계정에 열림 | `@RolesAllowed`가 빠졌다 | `AdminView` 주석을 확인한다 |
| 브라우저에서 API가 CORS로 막힘 | `CORS_ORIGIN`이 프론트 주소와 다르다 | `.env`를 화면 출처와 같게 하고 재시작한다 |
| 응답에 비밀번호 해시가 보임 | 엔티티를 그대로 직렬화했다 | `UserPublic`만 반환한다 |

## ⑥ 직접 해보기

1. 잘못된 비밀번호로 로그인하고 URL의 `error`를 확인한다.
2. mina 토큰으로 `/admin` 대신 `GET /api/auth/me`를 호출해 이메일을 본다.
3. 응답 JSON에 `password` 또는 `passwordHash` 키가 없는지 검색한다.

## ⑦ ☐ 체크리스트

- [ ] Spring Security 로그인 뷰와 접근 제어 (`@PermitAll`, `@RolesAllowed`)
- [ ] 비밀번호는 BCrypt로만 저장된다
- [ ] API는 Bearer, 화면은 세션이다
- [ ] API CSRF는 끄고 Flow CSRF는 Vaadin 설정에 맡긴다
- [ ] `CORS_ORIGIN`으로 허용 출처를 제한한다

## ⑧ 다음 단계

[study8](study8.md)에서 이 보안 코드와 서비스를 패키지로 나누고 개발·운영 설정을 가른다.

참고: [Vaadin 보안](https://vaadin.com/docs/latest/flow/security/enabling-security)
