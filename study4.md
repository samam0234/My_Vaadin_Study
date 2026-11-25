# study4 · 라우팅과 요청 처리

## ① 목표

Flow 주소, 선택 경로 매개변수, 쿼리, 없는 주소, API 경로가 한 서버에서 어떻게 갈라지는지 확인한다.

## ② 📖 이론

브라우저 주소는 두 종류다. `/posts`와 `/write/3`은 Vaadin 라우터가 뷰를 고른다. `/api/posts`와 `/health`는 Spring MVC 컨트롤러가 JSON을 만든다. 보안 필터 체인도 이 둘을 먼저 가른다. API 체인이 `/api/**`를 맡고, 나머지는 Vaadin 체인이 맡는다.

없는 Flow 주소는 `NotFoundView`가 404 문장을 보여 준다. 그 뷰가 보이려면 Spring Security가 모르는 주소라도 Vaadin 서블릿까지 요청을 보내야 한다. `anyRequest(permitAll)`이 그 역할이다. 뷰 자체의 접근 주석은 그대로 검사한다.

비유하면 주소는 건물 호수다. `posts`는 게시판 방, `write`는 글쓰기 방, `write/3`은 3번 글을 펼친 같은 방이다.

## ③ 🛠️ 실기

| 주소 | 담당 | 결과 |
| --- | --- | --- |
| `/` | `HomeView` `@Route("")` | 첫 화면 |
| `/posts` | `PostListView` | 목록 |
| `/posts?q=첫` | 같은 뷰의 `beforeEnter` | 검색어 반영 |
| `/write` | `PostEditorView` | 새 글 |
| `/write/1` | `HasUrlParameter` | 1번 글 수정 |
| `/login` | `LoginView`, `autoLayout = false` | 로그인 폼 |
| `/없는주소` | `NotFoundView` | 없는 주소입니다 |
| `/health` | `HealthController` | `{"status":"ok"}` |
| `/api/posts` | `PostController` | 목록 JSON |

`PostListView` 50~56행은 쿼리 `q`를 읽어 검색칸과 Grid를 다시 채운다. 43~45행은 검색 버튼을 누르면 `QueryParameters`로 같은 뷰에 다시 들어간다. 새로고침해도 검색어가 주소에 남아 있다.

`PostEditorView` 20행과 66~74행은 숫자가 없으면 새 글, 있으면 그 글의 제목과 본문을 채운다. 이동은 `ui.navigate(PostEditorView.class, id)`다.

`NotFoundView`는 `RouteNotFoundError`를 바꿔 문장만 남기고 HTTP 404를 돌려준다. `@AnonymousAllowed`가 있어 로그인 전에도 볼 수 있다.

API와 화면의 경로를 맞출 때는 화면 `/posts`와 API `/api/posts`를 같은 단어로 둔다. 화면은 사람이 보는 주소, API는 프로그램이 보는 주소다.

```powershell
Invoke-RestMethod http://localhost:8080/health
Invoke-WebRequest http://localhost:8080/api/posts | Select-Object -ExpandProperty Content
```

## ④ 🔗 연동 포인트

쿼리 `q`, `page`, `limit`, `sort`는 study6의 API와 같은 이름이다. 화면 Grid는 이 값을 서비스 메서드 인자로 넘기고, HTTP 클라이언트는 쿼리 문자열로 넘긴다. study14의 OpenAPI가 그 이름을 문서화한다.

## ⑤ 오류 노트

| 증상 | 원인 | 해결 |
| --- | --- | --- |
| 없는 주소가 로그인으로 감 | 보안 설정이 미등록 주소를 막는다 | Vaadin 체인의 `anyRequest(permitAll)`을 확인한다 |
| `/write/abc`가 500 | 경로 매개변수가 Long이 아니다 | 숫자 id만 넘긴다 |
| `/api/posts`가 HTML로 보임 | 주소가 Flow 라우트와 겹친다 | API는 항상 `/api`로 시작한다 |

## ⑥ 직접 해보기

1. `/posts?q=없는말`에서 표가 비는지 본다.
2. `/없는주소`의 문장과 개발자 도구 상태 코드를 확인한다.
3. `/health`와 `/posts`를 나란히 열어 하나는 JSON, 하나는 화면인지 비교한다.

## ⑦ ☐ 체크리스트

- [ ] `@Route` 주소와 `?q=` 쿼리가 동작한다
- [ ] `/write`와 `/write/{id}`가 한 뷰에서 갈린다
- [ ] 없는 주소는 `NotFoundView`다
- [ ] `/api/posts`와 `/posts`가 같은 서버에서 다른 담당으로 열린다

## ⑧ 다음 단계

[study5](study5.md)에서 `/write`의 Binder와 `/api-try`의 상태 코드 분기를 확인한다.

참고: [Routing](https://vaadin.com/docs/latest/flow/routing)
