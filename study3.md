# study3 · 데이터 다루기

## ① 목표

게시글 데이터를 레코드로 다루고, Grid에 묶으며, 작성 중인 제목을 브라우저 로컬 저장소에 남긴다.

## ② 📖 이론

Flow에서 화면이 오래 붙잡는 값은 컴포넌트 필드다. 서버의 저장 모델은 JPA 엔티티이고, 화면과 API에 나가는 값은 `PostCard` 같은 레코드다. 엔티티를 브라우저 응답에 그대로 넣지 않는다. 비밀번호 해시가 따라 나가기 쉽기 때문이다.

Grid는 표다. `DataProvider.fromCallbacks`는 지금 페이지와 정렬만 서비스에 요청한다. 전체를 메모리에 올리지 않는다.

로컬 저장소는 브라우저 안의 작은 서랍이다. Flow는 `executeJs`로 `localStorage`를 읽고 쓴다. 이 값은 서버 세션과 별개라 새로고침 뒤에도 제목 초안이 남는다.

## ③ 🛠️ 실기

http://localhost:8080/posts 를 연다. 데모 글 `첫 글`이 표에 있다. 제목 헤더를 눌러 정렬하고, 검색칸에 `첫`을 넣은 뒤 검색한다. 주소가 `/posts?q=첫`로 바뀐다.

`PostListView`의 데이터 연결은 다음이다.

- 28~31행 서비스, 검색칸, Grid, 현재 검색어를 필드로 둔다.
- 33행 생성자로 `PostService`를 받는다. 화면이 저장소를 직접 만들지 않는다.
- 35~38행 열은 레코드의 접근자와 람다로 묶인다. `setSortable(true)`가 정렬 화살표를 만든다.
- 39행 한 페이지는 10행이다.
- 59~73행 콜백이 offset을 1부터 세는 page로 바꾸고 `posts.list`의 `data`만 표에 넣는다. count 콜백은 `meta.total`을 본다.

초안 저장은 `PostEditorView` 46행과 57~62행이다. 제목이 바뀔 때마다 `vaadin-draft-title` 키로 저장하고, 새 글 화면이면 그 값을 다시 넣는다.

서비스가 돌려주는 목록 모양은 `ApiModels.PostCard`다. 필드는 `id`, `title`, `author`, `commentCount`, `createdAt`이다. 이 필드는 study6의 `GET /api/posts` 목록과 같다.

파일 저장은 기본 설정에서 H2 파일 `lesson/data/board`다. `DATABASE_URL`을 바꾸기 전에는 서버를 내렸다 켜도 데모 글이 남는다. `data/`는 Git에서 빠진다.

## ④ 🔗 연동 포인트

Grid가 부르는 `PostService.list`는 study6에서 JPA 페이지 질의와 `/api/posts`가 같이 쓴다. 로컬 저장소의 초안은 서버로 가지 않는다. 저장 버튼을 눌러야 study5의 Binder가 서비스로 넘긴다.

## ⑤ 오류 노트

| 증상 | 원인 | 해결 |
| --- | --- | --- |
| 표가 비어 있음 | 시드가 돌기 전에 DB 연결이 실패했다 | 터미널의 H2 로그를 보고 `data` 폴더 잠금을 해제한 뒤 재시작한다 |
| 정렬해도 순서가 그대로 | 정렬 키가 서비스의 switch에 없다 | `createdAt`과 `commentCount`만 서버 정렬에 연결되어 있다 |
| 초안이 다른 브라우저에 없음 | localStorage는 그 브라우저 프로필에만 있다 | 같은 브라우저에서 `/write`를 다시 연다 |

## ⑥ 직접 해보기

1. 개발자 도구 Application → Local Storage에서 `vaadin-draft-title`을 확인한다.
2. 검색어를 비우고 검색해 `q`가 빠지거나 빈 값인지 주소창에서 본다.
3. Grid 한 줄을 눌러 `/write/{id}`로 이동하는지 확인한다.

## ⑦ ☐ 체크리스트

- [ ] Grid 열이 `PostCard` 필드와 연결된다
- [ ] 페이지 크기 10과 헤더 정렬이 동작한다
- [ ] 제목 초안이 localStorage에 저장된다
- [ ] 목록 데이터가 엔티티가 아니라 레코드다

## ⑧ 다음 단계

[study4](study4.md)에서 `/posts`, `/write/{id}`, 없는 주소, 쿼리 `q`가 어떤 뷰로 가는지 정리한다.

참고: [Grid](https://vaadin.com/docs/latest/components/grid)
