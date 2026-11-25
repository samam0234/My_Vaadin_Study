# study9 · 테스트·디버깅

## ① 목표

리포지토리 대역 테스트와 API 요청 테스트를 실행하고, 로그와 자주 나는 오류를 구분한다.

## ② 📖 이론

단위 테스트는 이웃 객체를 대역으로 바꿔 규칙만 본다. `PostServiceMockTest`는 `PostRepository`가 빈 Optional을 돌려주게 하고, 서비스가 404 `NOT_FOUND`를 던지는지만 확인한다. 데이터베이스도 Vaadin도 올라가지 않는다.

요청 테스트는 `PostApiTest`다. `@SpringBootTest`와 `@AutoConfigureMockMvc`가 서블릿까지 띄우되 포트는 열지 않는다. 프로필 `test`는 H2 메모리 DB를 쓴다. 가입, 로그인, 등록, 목록 `meta`, 403, 404, 로그아웃 후 401을 한 메서드에서 확인한다.

로그의 `requestId`는 MDC에 들어 있다. 오류 응답의 `requestId`와 터미널 로그를 맞추면 어느 요청인지 찾을 수 있다. 디버거는 IDE에서 `PostService.create`에 중단점을 걸고 `mvn spring-boot:run` 대신 Debug로 같은 메인 클래스를 띄운다.

## ③ 🛠️ 실기

```powershell
Set-Location lesson
mvn test
```

이 환경에서 테스트 3건이 통과했다. `McpStudyServerTest`, `PostApiTest`, `PostServiceMockTest`다.

`PostServiceMockTest`는 Mockito로 저장소를 비어 있게 만든다. `@InjectMocks`가 서비스 생성자에 대역을 넣는다. 메일과 실시간 게시판도 대역이라 테스트가 네트워크를 열지 않는다.

API 테스트는 `MockMvc`로 JSON을 보내고 `jsonPath("$.error.code")`로 코드를 확인한다. 로그인 응답에 `passwordHash`가 없는지도 검사한다.

### 자주 나는 오류

| # | 증상 | 원인 | 해결 |
| --- | --- | --- | --- |
| 1 | 포트 8080 사용 중 | 이전 서버가 살아 있다 | Java 프로세스를 종료하거나 `PORT`를 바꾼다 |
| 2 | 모든 뷰가 거절됨 | 레이아웃 접근 주석 누락 | `@AnonymousAllowed`를 `MainLayout`에 둔다 |
| 3 | 로그인이 레이아웃 안에 보임 | `autoLayout = false` 누락 | `LoginView` 주소를 수정한다 |
| 4 | Node 버전 오류 | Node 24가 아니다 | `node -v`를 맞추고 프론트를 다시 준비한다 |
| 5 | 프론트 생성물이 낡음 | `generated`가 이전 빌드다 | `src/main/frontend/generated`를 지우고 다시 실행한다 |
| 6 | Binder가 런타임에 실패 | getter 이름이 칸과 다르다 | `Draft`의 get/set과 `bind`를 맞춘다 |
| 7 | `LazyInitializationException` | 트랜잭션 밖에서 연관 엔티티를 읽음 | 서비스 안에서 레코드로 변환한다 |
| 8 | API POST가 403 | 세션 CSRF를 API에 적용했다 | API 체인만 CSRF를 끈다 |
| 9 | 목록이 한 페이지 밀림 | page를 0부터 보냈다 | 쿼리는 1부터, 저장소만 0부터 |
| 10 | H2 파일 잠금 | 서버 두 개가 같은 파일을 연다 | 하나면 종료한다 |

Vaadin 개발 모드의 오른쪽 아래 표시와 브라우저 Network의 UIDL이 화면 디버그 도구다. API는 같은 Network 탭의 `/api` 응답 상태 코드를 본다.

## ④ 🔗 연동 포인트

테스트 프로필은 `application-test.yml`에서 DB URL만 메모리로 바꾼다. 운영 URL은 읽지 않는다. AI 호출은 `AI_PROVIDER=mock`이라 외부 모델로 나가지 않는다. study16의 GitHub Actions도 `mvn test`만 실행한다.

## ⑤ 오류 노트

| 증상 | 원인 | 해결 |
| --- | --- | --- |
| `AutoConfigureMockMvc`를 찾을 수 없음 | Spring Boot 4에서 패키지가 이동했다 | `spring-boot-starter-webmvc-test`와 `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`를 쓴다 |
| 테스트가 8080에 붙음 | 이미 떠 있는 서버를 호출했다 | `MockMvc`는 서버 없이 컨텍스트를 호출한다 |
| Mockito 에이전트 경고 | Java 최신 런타임의 안내 | 테스트 결과 자체는 통과하면 된다 |

## ⑥ 직접 해보기

1. `mvn test`를 실행하고 3건이 통과하는지 본다.
2. `PostServiceMockTest`의 id를 9가 아닌 값으로 바꿔도 404가 유지되는지 본다.
3. 일부러 제목 검증을 깨뜨려 테스트가 400이 아닌 상태에서 실패 메시지를 읽는다. 확인 후 되돌린다.

## ⑦ ☐ 체크리스트

- [ ] 리포지토리를 Mockito 대역으로 바꾼 테스트가 있다
- [ ] `MockMvc`로 200, 201, 204, 400, 401, 403, 404, 409를 확인했다
- [ ] 오류 응답의 `requestId`와 로그를 연결할 수 있다
- [ ] 위 10가지 증상의 원인을 한 줄로 말할 수 있다

## ⑧ 다음 단계

[study10](study10.md)에서 빌드와 배포 환경의 환경변수, HTTPS를 정리한다.

참고: [Spring Boot 테스트](https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html)
