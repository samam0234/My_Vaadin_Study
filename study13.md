# study13 · 외부 서비스 연동

## ① 목표

메일, 파일 업로드, 결제 대역, OAuth 클라이언트 등록을 비밀 값 없이 기본 실행할 수 있게 붙인다.

## ② 📖 이론

외부 서비스는 실패해도 게시판이 죽으면 안 된다. 메일은 `MAIL_ENABLED`가 false면 보내지 않고 로그만 남긴다. 결제는 `PAYMENT_SECRET`이 비어 있으면 `mock` URL을 만든다. 실제 카드 청구는 하지 않는다. OAuth는 프로필 `oauth`와 `GOOGLE_CLIENT_ID`가 있을 때만 클라이언트 등록 빈을 만든다. 로그인 버튼을 기본 화면에 넣지 않는다. 키가 없는 상태에서 구글 로그인을 열면 실패하기 때문이다.

파일은 `UPLOAD_DIR` 아래 임의 이름으로 저장한다. 원본 이름에서 경로를 제거하고, 1MB를 넘으면 413 `PAYLOAD_TOO_LARGE`다.

비유하면 외부 서비스는 옆 가게다. 간판 열쇠가 없으면 우리 가게 문을 잠그지 않고, 연습용 영수증만 뽑아 둔다.

## ③ 🛠️ 실기

로그인 후 http://localhost:8080/tools 에서 문서와 질문을 넣고 `질문`을 누르면 문서 안의 문장만 답으로 나온다. 문서에 없는 질문은 `문서에서 찾을 수 없습니다.`다. `결제 시작`은 `mock`과 연습 URL을 보여 준다. 업로드는 1MB 이하 파일을 `data/uploads`에 저장한다.

글을 등록하면 `MailGateway.notifyNewPost`가 비동기로 호출된다. 기본값은 로그 `메일 대신 기록`이다. Mailpit을 쓸 때는 compose의 1025 포트와 아래 값으로 재시작한다.

```powershell
docker compose up -d mailpit
```

`.env`에 `MAIL_ENABLED=true`, `SMTP_HOST=127.0.0.1`, `SMTP_PORT=1025`를 넣고 서버를 재시작한다. 받은 편지는 http://localhost:8025 에서 본다. 이 환경에서는 Mailpit을 실행하지 않았다.

OAuth를 실제로 열려면 구글 콘솔에서 발급한 값을 환경변수에 넣고 프로필을 켠다.

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=oauth"
```

`OAuthClientConfig`는 인가 코드 등록만 만든다. Vaadin 로그인 폼과 동시에 동작시키려면 보안 체인에 `oauth2Login`을 추가하는 운영 설정이 더 필요하다. 키가 `unset`이면 등록 객체만 생기고 로그인은 열리지 않는다.

API:

```powershell
# 토큰이 필요하다
Invoke-RestMethod http://localhost:8080/api/checkout -Method Post -Headers $headers
```

## ④ 🔗 연동 포인트

업로드 경로와 메일 발신 주소는 배포 환경변수다. study16은 이 값을 서버 비밀로 넣는다. 결제 URL은 studyFinal 점검에서 `mock`인지 `configured`인지만 확인한다. 소셜 로그인 사용자는 아직 `accounts` 행과 자동으로 합쳐지지 않는다. 그 결합은 키가 있는 환경에서 별도로 잇는다.

## ⑤ 오류 노트

| 증상 | 원인 | 해결 |
| --- | --- | --- |
| 메일이 가지 않음 | `MAIL_ENABLED`가 false다 | 의도된 기본값이다. 켤 때만 SMTP를 지정한다 |
| 업로드 413 | 1MB를 넘었다 | 더 작은 파일로 확인한다 |
| OAuth 프로필이 부팅에 실패 | 클라이언트 비밀 형식 | 값이 없을 때는 `unset`으로 빈을 만들게 두었다 |
| 업로드 이름에 `..`가 보임 | 원본 이름을 그대로 경로에 붙였다 | `getFileName()`만 사용한다 |

## ⑥ 직접 해보기

1. `/tools`에서 문서에 있는 단어와 없는 단어로 각각 질문한다.
2. 결제 버튼 결과가 `mock`으로 시작하는지 본다.
3. `.env.example`에서 메일, 결제, 구글 키가 비어 있는 자리인지 확인한다.

## ⑦ ☐ 체크리스트

- [ ] 메일 기본값은 로그이고, 켤 때만 SMTP를 쓴다
- [ ] 업로드는 1MB를 넘으면 거절한다
- [ ] 결제 비밀이 없으면 청구 없이 mock URL만 만든다
- [ ] OAuth 등록은 프로필 `oauth`에서만 만들어진다

## ⑧ 다음 단계

[study14](study14.md)에서 이 API들의 경로, 오류 코드, OpenAPI 파일을 한 계약으로 모은다.

참고: [Vaadin Upload](https://vaadin.com/docs/latest/components/upload)
