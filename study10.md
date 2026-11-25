# study10 · 배포·정리

## ① 목표

프로덕션 프론트 빌드와 실행 JAR, 도메인·HTTPS, 기초 구간에서 만든 게시판을 배포 전에 점검한다.

## ② 📖 이론

개발 모드는 Vite가 떠 있어 수정이 빠르다. 배포는 그 도구 없이 JAR 하나가 정적 프론트와 서버를 같이 담는다. `mvn -Pproduction package`가 Vaadin `build-frontend`를 돌린다. 결과 JAR는 `java -jar`로 실행한다.

도메인은 사람이 외우는 이름이고, HTTPS는 그 이름으로 오가는 내용을 암호화한다. 이 실습은 인증서를 발급하지 않는다. 리버스 프록시가 HTTPS를 끝내면 앱은 `server.forward-headers-strategy: framework`로 원래 프로토콜을 알아야 리다이렉트가 `http`로 떨어지지 않는다.

기초 구간은 여기까지다. 화면은 Flow, 저장은 H2, 인증은 세션과 Bearer, API는 공통 스펙 v1이다.

## ③ 🛠️ 실기

```powershell
Set-Location lesson
mvn -Pproduction -DskipTests package
java -jar target/lesson-0.1.0.jar
```

프로덕션 빌드는 Node 24로 프론트를 묶으므로 시간이 걸린다. 이 문서 작성 시점에는 `mvn test`만 실행했고, 프로덕션 JAR 실행은 체크리스트로 남겨 둔다.

배포 환경에 넣을 키는 `.env.example`과 같다.

| 키 | 배포에서 |
| --- | --- |
| `PORT` | 프로세스 포트. 프록시가 바깥 443을 이 포트로 보낸다 |
| `DATABASE_URL` | 운영 DB 주소. H2 파일 경로를 서버에 두지 않는다 |
| `APP_SECRET` | 토큰 HMAC 비밀. 개발 기본값을 그대로 두지 않는다 |
| `CORS_ORIGIN` | 실제 사이트 출처 |
| `MAIL_ENABLED` | 메일을 보낼 때만 true |

배포 후 점검:

1. `GET /health`가 `ok`다.
2. `/` 화면이 열린다.
3. 로그인 후 `/write`가 열린다.
4. `GET /api/posts`의 `meta`가 있다.
5. 응답에 비밀번호 해시가 없다.
6. `APP_SECRET`이 개발 문장이 아니다.

`Dockerfile`은 Temurin 21에서 프로덕션 패키지를 만들고 JRE 이미지에서 JAR를 실행한다. 이미지 빌드는 네트워크와 Node가 필요하다. 자동 배포 워크플로는 study16이 `mvn test`까지만 담당한다.

## ④ 🔗 연동 포인트

배포 서버의 환경변수가 `application-prod.yml`의 `validate`와 만난다. DB 주소가 PostgreSQL이면 프로필 `prod,postgres`를 같이 켠다. 그 전환은 study11이다. HTTPS 프록시 뒤의 로그인 리다이렉트는 forward header가 맞아야 study7의 `/login`이 깨지지 않는다.

## ⑤ 오류 노트

| 증상 | 원인 | 해결 |
| --- | --- | --- |
| 배포 후 화면이 비어 있음 | 개발 모드 프론트만 있고 프로덕션 빌드가 없다 | `-Pproduction package`로 다시 만든다 |
| 로그인 후 http로 돌아감 | 프록시가 HTTPS를 앱에 알리지 않는다 | forward header와 프록시 설정을 맞춘다 |
| `validate`에서 기동 실패 | 운영 DB에 표가 없다 | 마이그레이션을 먼저 적용한다 |

## ⑥ 직접 해보기

1. `GET /health`와 `GET /api/posts`를 배포 전 로컬에서 한 번씩 호출한다.
2. `.env`의 `APP_SECRET`을 긴 임의 문자열로 바꾼 뒤 기존 토큰이 거절되는지 본다.
3. `Dockerfile`의 JAR 이름이 `lesson/pom.xml` 버전과 같은지 비교한다.

## ⑦ ☐ 체크리스트

- [ ] 개발 서버와 프로덕션 빌드의 차이를 말할 수 있다
- [ ] 도메인은 이름, HTTPS는 암호화라는 점을 구분한다
- [ ] 배포 환경변수 표의 키를 채울 수 있다
- [ ] 배포 후 점검 6항목을 실행할 수 있다

## ⑧ 다음 단계

[studyMidpoint](studyMidpoint.md)에서 study0부터 여기까지를 게시판 서비스 하나로 다시 조립한다.

참고: [Vaadin 프로덕션 모드](https://vaadin.com/docs/latest/flow/production)
