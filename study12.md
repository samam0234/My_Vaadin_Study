# study12 · 프론트·UI 연동

## ① 목표

Vaadin 안에서 Hilla React 화면, 시그널 대신 `useState`, 그리고 서버의 `@BrowserCallable`을 연결한다.

## ② 📖 이론

Hilla는 Vaadin에 포함된 React 연동이다. `src/main/frontend/views/hilla-board.tsx`의 파일 이름이 주소 `/hilla-board`가 된다. 화면은 브라우저에서 상태를 가지고, 데이터는 `PostBrowserService.list`가 서버에서 만든다. 이 메서드는 Flow Grid와 같은 `PostService.list`를 호출하고 `PostCard`를 돌려준다.

`@BrowserCallable`은 공개 메서드를 브라우저에서 부를 수 있게 한다. 기본은 거절이므로 `@AnonymousAllowed`를 붙여 목록 조회만 연다. 등록이나 삭제를 이 서비스에 넣을 때는 `@PermitAll`이나 `@RolesAllowed`를 메서드에 붙인다.

폼 라이브러리를 따로 얹지 않는다. Flow 폼은 study5의 Binder다. React 화면은 목록만 보여주고, 입력은 Flow `/write`로 보낸다. 상태를 서버 세션에 두지 않으므로 이 화면은 Flow보다 세션 메모리를 덜 쓴다.

비유하면 Flow는 서버가 칠판을 들고 있는 수업이고, Hilla는 학생이 자기 노트를 보다가 질문만 서버에 던지는 수업이다.

## ③ 🛠️ 실기

`hilla-spring-boot-starter`가 `pom.xml`에 있다. 이것이 없으면 `views` 폴더의 tsx는 주소가 되지 않고 TypeScript 클라이언트도 생기지 않는다.

`PostBrowserService` 전체:

```java
package study.vaadin.hilla;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import java.util.List;
import study.vaadin.api.ApiModels.PostCard;
import study.vaadin.service.PostService;

@BrowserCallable
@AnonymousAllowed
public class PostBrowserService {

    private final PostService posts;

    public PostBrowserService(PostService posts) {
        this.posts = posts;
    }

    public List<PostCard> list(String q, int page, int limit) {
        return posts.list(q, page, limit, "-createdAt").data();
    }
}
```

- 9행 이 클래스의 public 메서드를 브라우저 엔드포인트로 연다.
- 10행 로그인 없이도 목록을 읽을 수 있다.
- 20행 반환 필드는 `id`, `title`, `author`, `commentCount`, `createdAt`이다.

React 화면은 `PostBrowserService.list("", 1, 10)`을 `useEffect`에서 호출한다. 생성 타입이 `undefined`를 허용하므로 빈 배열로 바꾸고, 항목이 비어 있으면 걸러 낸 뒤 `useState`에 넣는다. 실패하면 `role="alert"` 문장을 보여 준다. 서랍의 `Hilla 게시판`이 `/hilla-board`로 간다.

필드 확인:

```powershell
Invoke-RestMethod http://localhost:8080/api/posts
```

JSON `data[0]`의 키와 화면 목록 항목의 키가 같은지 본다. 상세 전용인 `body`, `tags`, `updatedAt`은 목록 화면과 목록 API에 없다.

개발 모드에서는 첫 실행 때 `Frontend/generated/endpoints`가 생긴다. 이 폴더는 Git에 넣지 않는다.

## ④ 🔗 연동 포인트

Hilla와 Flow는 세션 쿠키를 공유할 수 있다. 로그인한 뒤 Hilla에서 인증이 필요한 메서드를 부르면 같은 보안 주체가 적용된다. 이 목록 메서드는 익명이라 `/api/posts` GET과 같다. study14의 필드 표를 바꿀 때는 `PostCard`만 고치면 화면과 API가 같이 바뀐다.

## ⑤ 오류 노트

| 증상 | 원인 | 해결 |
| --- | --- | --- |
| `/hilla-board`가 404 | Hilla 스타터가 없다 | `hilla-spring-boot-starter`를 넣고 재시작한다 |
| `Frontend/generated/endpoints`가 없음 | Java 컴파일 전에 프론트가 생성됐다 | 서버를 한 번 더 재시작한다 |
| 브라우저 호출이 401 | 서비스에 접근 주석이 없다 | 목록은 `@AnonymousAllowed`를 붙인다 |

## ⑥ 직접 해보기

1. `/hilla-board`와 `/posts`에 같은 제목이 보이는지 비교한다.
2. `GET /api/posts`의 첫 항목 키를 화면 문구의 필드 목록과 맞춘다.
3. `list`의 `limit`을 1로 바꿔 화면 항목 수가 줄어드는지 본다. 확인 후 10으로 되돌린다.

## ⑦ ☐ 체크리스트

- [ ] Hilla 맛보기: `@BrowserCallable` 서비스를 React 화면에서 호출
- [ ] Hilla 쪽 화면이 기존 `/api/posts` 스펙과 같은 필드를 쓰는지 확인
- [ ] React 상태는 `useState`, 서버 데이터는 `PostCard`다
- [ ] 생성 TypeScript는 Git에서 빠져 있다

## ⑧ 다음 단계

[study13](study13.md)에서 메일, 파일, 결제 대역, OAuth 등록을 같은 앱에 붙인다.

참고: [React에서 Java 호출](https://vaadin.com/docs/latest/building-apps/react/call-services)
