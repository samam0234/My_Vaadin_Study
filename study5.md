# study5 · 폼·입력·검증

## ① 목표

Binder로 입력 오류를 필드에 보여주고, 저장 버튼을 잠가 중복 제출을 막으며, API 상태 코드에 따라 문장을 바꾼다.

## ② 📖 이론

Binder는 입력 칸과 Java 빈을 묶는 자물쇠다. `asRequired`와 `withValidator`가 실패하면 `writeBean`이 `ValidationException`을 던지고, 오류 문장은 칸 아래에 남는다. 서버의 `@NotBlank`와 `@Size`는 HTTP API가 한 번 더 검사한다. 화면 검사를 통과해도 API 검사는 남아 있다.

이 프로젝트의 공통 스펙은 검증 실패를 400 `VALIDATION_ERROR`로 보낸다. 422는 쓰지 않는다. 그래도 `ApiTryView`는 400과 422를 같은 입력 오류 문장으로 처리해, 다른 서버가 422를 보내도 화면이 깨지지 않게 했다.

중복 제출은 사용자가 저장을 연타하는 일이다. `setDisableOnClick(true)`로 누르자마자 버튼을 잠그고, 처리가 끝나면 `finally`에서 다시 연다.

## ③ 🛠️ 실기

로그인 전 `/write`는 로그인 화면으로 간다. `mina@example.com` / `password123`으로 로그인한 뒤 제목을 비우고 저장하면 칸 오류가 난다. 제목과 본문을 채우면 알림에 201에 해당한다는 문장이 나오고 주소가 `/write/{id}`가 된다.

`PostEditorView`의 묶음은 38~54행이다.

- 38~40행 제목은 필수이고 200자를 넘기면 `1~200자로 입력하세요.`를 보여 준다.
- 41~43행 본문은 필수다. 태그는 선택이다.
- 44행 빈 `Draft`를 현재 값으로 둔다.
- 48행 클릭 즉시 버튼을 끈다.
- 49~54행 예외가 나도 `finally`에서 버튼을 다시 켠다.

`saveDraft` 77~83행은 `writeBean`이 실패하면 알림만 보여주고 서버를 호출하지 않는다. 86~90행은 새 글이면 `PostService.create`를 호출한다. 서비스가 `ApiException`을 던지면 91행 이후에서 상태 코드와 메시지를 알림으로 보여 준다.

HTTP로 같은 검증을 보려면 로그인한 뒤 http://localhost:8080/api-try 를 연다. 제목을 비우고 `API로 등록`을 누르면 400과 `VALIDATION_ERROR`가 문단에 찍힌다. 제목과 본문을 채우면 201이다.

```powershell
Invoke-RestMethod http://localhost:8080/api/posts -Method Post -ContentType 'application/json' -Body '{"title":"","body":""}'
```

토큰 없이 호출하면 401이다. 토큰을 넣고 빈 제목이면 400, `fields.title` 배열이 온다.

## ④ 🔗 연동 포인트

화면 Binder의 200자 제한과 API `@Size(max = 200)`은 같은 규칙이다. 성공하면 `Location: /api/posts/{id}`가 생기고, Flow는 그 id로 `/write/{id}`에 다시 들어간다. study6이 이 저장을 JPA 트랜잭션으로 확정한다.

## ⑤ 오류 노트

| 증상 | 원인 | 해결 |
| --- | --- | --- |
| 저장이 아무 반응이 없음 | Binder 오류가 칸에만 있고 알림 전에 반환했다 | 제목 칸 아래 문장을 본다 |
| 버튼이 계속 꺼져 있음 | `finally` 전에 화면이 이동했다 | 이동은 저장이 끝난 뒤에만 한다 |
| 400 대신 401 | Authorization 헤더가 없다 | 로그인 토큰을 넣거나 Flow 세션으로 `/write`를 연다 |

## ⑥ 직접 해보기

1. 제목에 201자를 넣어 Binder 메시지를 확인한다.
2. `/api-try`에서 본문만 비워 400 문장을 본다.
3. 저장을 연타해도 글이 하나만 늘어나는지 목록에서 확인한다.

## ⑦ ☐ 체크리스트

- [ ] `Binder`로 폼 바인딩과 검증
- [ ] 빈 제목은 화면과 API 모두에서 거절된다
- [ ] 저장 버튼이 처리 중 잠긴다
- [ ] 201, 400, 422 분기가 `/api-try`에 있다. 이 서버의 검증 실패는 400이다

## ⑧ 다음 단계

[study6](study6.md)에서 같은 입력으로 만든 글이 H2와 `/api/posts`에 어떻게 저장되는지 따라간다.

참고: [Binder](https://vaadin.com/docs/latest/flow/binding-data/components-binder-beans)
