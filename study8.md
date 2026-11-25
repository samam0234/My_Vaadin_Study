# study8 · 구조화·재사용

## ① 목표

패키지와 설정 파일을 역할별로 나누고, 개발 기본값과 운영 프로필이 같은 키를 다르게 읽게 한다.

## ② 📖 이론

한 클래스에 화면, SQL, 보안이 섞이면 고칠 때마다 전체가 흔들린다. 이 프로젝트는 역할을 패키지로 나눈다.

| 패키지 | 역할 |
| --- | --- |
| `ui` | Flow 뷰와 레이아웃 |
| `api` | JSON 컨트롤러와 오류 형식 |
| `service` | 트랜잭션과 업무 규칙 |
| `repo` | JPA 저장소 |
| `domain` | 엔티티 |
| `security` | 토큰, 필터, 현재 사용자 |
| `config` | `.env`와 `AppProperties` |
| `hilla` | 브라우저에서 부르는 서비스 |
| `mongo` | Mongo 프로필에서만 켜는 메모 |

`CurrentUser`는 인터페이스다. 화면과 서비스는 보안 저장소를 직접 보지 않고 이 인터페이스만 호출한다. 테스트는 구현 대신 Mockito 대역을 넣는다.

환경은 파일로 나눈다. `application.yml`은 로컬 H2다. `application-postgres.yml`, `application-mariadb.yml`, `application-mongo.yml`은 주소만 바꾼다. `application-prod.yml`은 `ddl-auto`를 `validate`로 두고 devtools 재시작을 끈다.

비유하면 패키지는 서랍 라벨이다. 라벨이 있으면 양말을 부엌에서 찾지 않는다.

## ③ 🛠️ 실기

운영에 가깝게 읽으려면 프로필만 바꾼다. 데이터베이스가 없으면 부팅이 실패하므로, 아래는 설정이 갈라지는 위치만 확인한다.

```powershell
Set-Location lesson
mvn spring-boot:run "-Dspring-boot.run.profiles=prod"
```

H2 파일에 표가 이미 있으면 `validate`가 통과한다. 새 빈 데이터베이스면 표가 없어 실패한다. 그때는 프로필 없이 한 번 띄워 `update`가 표를 만들게 한 뒤 운영 프로필을 쓴다.

`AppProperties`는 `app.cors-origin` 같은 YAML을 레코드 필드 `corsOrigin`으로 받는다. 비밀은 YAML에 적지 않고 `${APP_SECRET}`으로 남긴다.

중복을 줄인 예는 글 목록이다. Grid와 `PostController`와 `PostBrowserService`가 각자 쿼리를 만들지 않고 `PostService.list`만 호출한다.

## ④ 🔗 연동 포인트

개발 PC의 `.env`와 운영 서버의 환경변수는 키가 같다. study10과 study16은 `PORT`, `DATABASE_URL`, `APP_SECRET`, `CORS_ORIGIN`을 배포 환경에 주입한다. 프로필 `postgres`는 study11의 컨테이너 주소와 맞물린다.

## ⑤ 오류 노트

| 증상 | 원인 | 해결 |
| --- | --- | --- |
| 운영 프로필에서 표가 없음 | `validate`는 표를 만들지 않는다 | 개발 프로필로 스키마를 만든 뒤 전환한다 |
| 설정 키가 적용되지 않음 | 환경변수가 YAML보다 우선이다 | `Get-ChildItem Env:APP_SECRET`으로 확인한다 |
| 순환 참조 | 뷰가 서비스를, 서비스가 뷰를 주입한다 | 서비스는 뷰를 모르고 레코드만 반환한다 |

## ⑥ 직접 해보기

1. `application-prod.yml`과 `application.yml`의 `ddl-auto` 차이를 찾는다.
2. `PostBrowserService`가 `PostService` 외에 리포지토리를 주입하지 않는지 본다.
3. `.env.example`의 키 이름이 `application.yml`의 `${...}`와 일치하는지 한 줄씩 비교한다.

## ⑦ ☐ 체크리스트

- [ ] 화면, API, 서비스, 저장소, 보안이 패키지로 나뉜다
- [ ] 비밀은 환경변수이고 YAML에는 자리만 있다
- [ ] 개발 `update`와 운영 `validate`가 파일로 갈린다
- [ ] 목록 조회는 `PostService.list` 한 곳이다

## ⑧ 다음 단계

[study9](study9.md)에서 리포지토리를 대역으로 바꾼 테스트와 API 테스트를 실행한다.

참고: [Spring Boot 프로필](https://docs.spring.io/spring-boot/reference/features/profiles.html)
