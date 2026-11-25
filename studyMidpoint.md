# studyMidpoint · 중간 응용

## ① 목표

study0부터 study10까지로 Vaadin 하나에서 게시판, 로그인, CRUD를 끝까지 실행한다.

## ② 📖 이론

중간 응용은 새 라이브러리를 더하지 않는다. 이미 만든 Flow 화면, H2, Spring Security, `/api`를 한 사용 시나리오로 묶는다. 사용자는 글을 찾고, 로그인하고, 글을 쓰고, 자기 글만 고친다.

비유하면 기초 공부가 부품이고, 이 문서는 그 부품으로 책상을 조립하는 순서다. 부품 설명은 각 study에 있으므로 여기서는 순서와 확인만 둔다.

## ③ 🛠️ 실기

### 기능 목록

- 홈, 공통 서랍, 게시판 Grid, 검색 `q`
- 회원가입, 로그인, 로그아웃, 관리자 전용 `/admin`
- 글 등록, 상세, 수정, 삭제, 작성자만 수정
- `GET /health`, `/api/posts` 목록·상세, 인증이 필요한 등록
- 비밀번호 해시, CORS, 검증 400, 충돌 409

### 폴더

```text
lesson/src/main/java/study/vaadin/ui        화면
lesson/src/main/java/study/vaadin/api       JSON
lesson/src/main/java/study/vaadin/service   규칙
lesson/src/main/java/study/vaadin/domain    표
lesson/src/main/resources/application.yml   로컬 H2
```

### 구현 순서

1. study0 도구와 `.env`
2. study1 홈
3. study2 레이아웃과 메뉴
4. study4 주소와 없는 페이지
5. study7 로그인과 역할
6. study3 Grid와 로컬 초안
7. study5 Binder와 `/api-try`
8. study6 H2와 CRUD API
9. study8 패키지와 프로필
10. study9 `mvn test`
11. study10 배포 전 점검표

### DB·API

```text
브라우저 /posts  → PostListView → PostService → H2
브라우저 /write  → PostEditorView → PostService → H2
curl /api/posts  → PostController → PostService → H2
로그인 세션      → AccountUserDetails → accounts
Bearer 토큰      → TokenService → 같은 accounts
```

데모 계정 `mina@example.com` / `password123` 로 `/write`에서 글을 하나 만들고, `/posts`에서 보이는지, `GET /api/posts?q=`로 같은 제목이 나오는지를 확인한다.

### 1차 배포 체크리스트

- [ ] `mvn test` 통과
- [ ] `.env`가 커밋에 없다
- [ ] `APP_SECRET`을 개발 문장에서 바꿨다
- [ ] `GET /health`와 로그인, 글 한 건의 등록과 조회가 된다
- [ ] 다른 사용자 토큰으로 수정하면 403이다
- [ ] 프로덕션 빌드 여부와 forward header 필요 여부를 적었다

## ④ 🔗 연동 포인트

이 게시판의 `PostCard` 필드를 유지한 채로 study11이 DB만 바꾸고, study12가 Hilla 화면을 같은 서비스에 붙인다. 중간 지점에서는 H2와 Flow만으로도 시나리오가 닫힌다.

## ⑤ 오류 노트

| 증상 | 원인 | 해결 |
| --- | --- | --- |
| 글쓰기가 로그인으로 이동 | `@PermitAll` 화면이다 | 데모 계정으로 로그인한다 |
| API와 화면의 글 수가 다름 | 검색어 `q`가 남아 있다 | 쿼리 없이 `/api/posts`를 호출한다 |
| 테스트는 되는데 화면만 실패 | 개발 서버를 재시작하지 않았다 | `spring-boot:run`을 다시 띄운다 |

## ⑥ 직접 해보기

1. 새 이메일로 가입하고 그 계정으로 글을 쓴 뒤 mina 토큰으로는 수정이 403인지 본다.
2. 로그아웃 후 `/posts`는 열리고 `/write`는 로그인으로 가는지 본다.
3. 1차 배포 체크리스트를 체크하지 않은 항목의 이유를 한 줄로 적는다.

## ⑦ ☐ 체크리스트

- [ ] 비로그인 목록 조회와 로그인 글쓰기가 된다
- [ ] CRUD가 화면과 `/api/posts` 양쪽에서 된다
- [ ] 작성자가 아닌 수정은 403이다
- [ ] `mvn test`를 이 시나리오 후에 다시 실행했다

## ⑧ 다음 단계

[study11](study11.md)에서 같은 엔티티를 PostgreSQL과 MariaDB로 옮기고, MongoDB는 별도 문서로 붙인다.

참고: [study0](study0.md)부터 [study10](study10.md)
