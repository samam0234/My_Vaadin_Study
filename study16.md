# study16 · 운영·CI/CD

## ① 목표

Docker, GitHub Actions, 로그, 백업을 체크리스트로 실행할 수 있게 정리한다.

## ② 📖 이론

컨테이너는 앱과 데이터베이스를 같은 방식으로 켜는 상자다. `docker-compose.yml`은 PostgreSQL, MariaDB, MongoDB, Mailpit을 따로 둔다. 앱 이미지는 루트 `Dockerfile`이 Temurin 21로 프로덕션 JAR를 만들어 실행한다. CI는 이미지를 배포하지 않고 `mvn test`만 돌려 회귀를 막는다.

로그는 `requestId`가 있는 한 줄로 요청을 찾는다. 모니터링은 이 학습에서 외부 서비스 계정을 만들지 않는다. `GET /health`가 프로세스 생존 확인이다.

백업은 데이터베이스 파일을 복사하는 일이다. H2는 `data/` 디렉터리를 끈 뒤에 복사한다. PostgreSQL은 `pg_dump`다. 복원 연습을 하지 않은 백업은 백업이 아니다.

## ③ 🛠️ 실기

체크리스트만 순서대로 둔다. 이 환경에서 실행한 것은 `mvn test`다. Docker 데몬과 공개 배포는 사용자 환경에서 체크한다.

### 로컬 컨테이너

- [ ] `docker compose up -d postgres` 후 포트 5432가 열린다
- [ ] 프로필 `postgres`로 앱이 기동하고 `/health`가 ok다
- [ ] `docker compose stop postgres` 후 앱이 연결 오류를 로그에 남긴다
- [ ] Mailpit은 필요할 때만 1025와 8025를 연다

### 이미지

- [ ] `docker build -t vaadin-study .` 가 프로덕션 빌드를 끝낸다
- [ ] 컨테이너 환경변수 `APP_SECRET`, `DATABASE_URL`, `CORS_ORIGIN`, `PORT`를 넣는다
- [ ] 이미지 안에 `.env` 파일을 복사하지 않는다

### GitHub Actions

`.github/workflows/test.yml`은 push와 pull request에서 Java 21, `lesson` 디렉터리, `mvn -B test`를 실행한다. 비밀 키를 워크플로에 적지 않는다.

- [ ] Actions 탭에서 `test`가 성공한다
- [ ] 실패 로그에서 테스트 이름을 찾을 수 있다

### 로그와 백업

- [ ] 400 오류의 `requestId`를 서버 로그에서 찾는다
- [ ] `pg_dump -h 127.0.0.1 -U board board > board.sql`로 덤프한다
- [ ] 빈 데이터베이스에 그 파일을 넣어 글이 돌아오는지 확인한다
- [ ] H2를 쓸 때는 프로세스를 끈 뒤 `data/`를 복사한다

## ④ 🔗 연동 포인트

CI는 테스트 프로필의 메모리 DB만 본다. 운영 DB 비밀번호는 GitHub Environment 비밀로 `DATABASE_URL`에 넣는다. study10의 forward header는 컨테이너 앞 프록시에서 설정한다. study11의 compose 서비스 이름이 운영 URL의 호스트가 된다.

## ⑤ 오류 노트

| 증상 | 원인 | 해결 |
| --- | --- | --- |
| Actions에서 Node 오류 | 러너 Node가 24가 아닌데 프로덕션 빌드를 했다 | 워크플로는 `mvn test`만 한다. 프로덕션 빌드는 이미지 단계에서 한다 |
| compose 포트 충돌 | 로컬 DB가 5432를 쓰고 있다 | 다른 포트를 매핑하거나 로컬 DB를 끈다 |
| 백업 파일이 0바이트 | 컨테이너가 뜨기 전에 덤프했다 | `docker compose ps`로 준비 상태를 본다 |

## ⑥ 직접 해보기

1. 워크플로 파일에서 Java 버전과 작업 디렉터리를 찾는다.
2. `Dockerfile`이 테스트 없이 패키지하는 이유를 한 줄로 적는다. 테스트는 CI가 이미 한다.
3. 백업 체크리스트에서 아직 못 한 항목을 환경 제한과 함께 적는다.

## ⑦ ☐ 체크리스트

- [ ] compose로 DB를 켜고 끌 수 있다
- [ ] 이미지 빌드에 비밀 파일을 넣지 않는다
- [ ] GitHub Actions는 `mvn test`다
- [ ] 백업과 복원 절차를 문서로 가지고 있다

## ⑧ 다음 단계

[study17](study17.md)에서 글 변경을 실시간 알림으로 다른 화면에 전달한다.

참고: [Docker Compose](https://docs.docker.com/compose/)
