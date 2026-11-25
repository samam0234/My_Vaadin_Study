# study0 · 환경 설정

## ① 목표

Vaadin 25와 Spring Boot 프로젝트를 준비하고, 버전 확인·폴더 구조·개발 서버·Git·환경변수를 한 번에 맞춘다.

## ② 📖 이론

Vaadin Flow는 화면을 Java 객체로 만들고, 그 객체를 서버 세션에 둔 뒤 브라우저와 동기화한다. Hilla는 같은 서버 안에서 React 파일이 Java 메서드를 호출하게 해 준다. Spring Boot는 그 둘을 띄우는 실행기다. Maven은 의존성을 가져오고, Node는 Flow가 브라우저에 보낼 프론트 묶음을 만들 때 쓰인다.

비유하면 Java 클래스는 주방이고, 브라우저는 홀이다. 주문이 들어오면 주방이 접시를 다시 홀에 내보낸다. HTML 파일을 직접 고치지 않아도 접시 구성은 Java가 정한다.

| 용어 | 이 실습에서 하는 일 |
| --- | --- |
| Flow | `@Route` 뷰와 `@Layout` 레이아웃 |
| Hilla | `src/main/frontend/views`의 React 화면과 `@BrowserCallable` |
| BOM | `vaadin-bom`이 Vaadin 모듈 버전을 맞춰 준다 |
| 개발 모드 | `mvn spring-boot:run`이 Vite 개발 서버를 곁에 둔다 |
| `.env` | 비밀번호와 주소처럼 저장소에 올리면 안 되는 값 |

## ③ 🛠️ 실기

확인한 도구는 Java 26.0.2.1, Node v24.14.0, npm 11.9.0이다. 프로젝트는 Java 21 바이트코드를 만든다. Maven이 PATH에 없으면 3.9.11을 설치한 뒤 같은 명령을 쓴다. 빈 폴더에서 새로 만들 때는 [start.vaadin.com](https://start.vaadin.com) 또는 [Spring Initializr](https://start.spring.io)에서 Vaadin과 Spring Boot를 고른다. 이 저장소는 그 결과를 `lesson/`에 이미 맞춰 두었다.

```powershell
java -version
node -v
npm -v
mvn -version
Set-Location lesson
Copy-Item .env.example .env
mvn spring-boot:run
```

서버가 뜨면 http://localhost:8080 을 연다. 종료는 터미널에서 `Ctrl+C`다.

```text
lesson/
  pom.xml                         Vaadin 25.3.6, Spring Boot 4.1.3
  .env.example                    환경변수 예시
  src/main/java/study/vaadin/     화면, API, 보안, 서비스
  src/main/frontend/views/        Hilla React 화면
  src/main/frontend/themes/vaadin-study/   Lumo를 잇는 테마
  src/main/resources/             application.yml, openapi.yaml
  src/test/java/                  서비스 목 테스트와 API 테스트
```

에디터는 IntelliJ의 Vaadin 플러그인 또는 VS Code의 Vaadin 확장을 켠다. Java 파일에서 `@Route`로 이동이 되면 확장이 동작하는 것이다.

`lesson/pom.xml`의 버전 선언은 아래다.

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.1.3</version>
    <relativePath/>
</parent>
```

- 7행 부모 POM이 Spring Boot 플러그인과 의존성 버전을 가져온다.
- 9행 이 실습의 Spring Boot 버전이다.
- 10행 상위 폴더의 POM을 찾지 않는다.

```xml
<java.version>21</java.version>
<vaadin.version>25.3.6</vaadin.version>
```

- 21행 컴파일 대상은 Java 21이다. 더 새 런타임도 이 바이트코드를 실행한다.
- 22행 Vaadin BOM 버전이다.

`lesson/src/main/java/study/vaadin/config/DotEnv.java` 전체:

```java
package study.vaadin.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DotEnv {

    private DotEnv() {
    }

    public static void load() {
        Path file = Path.of(".env");
        if (!Files.exists(file)) {
            file = Path.of("lesson", ".env");
        }
        if (!Files.exists(file)) {
            return;
        }
        try {
            for (String raw : Files.readAllLines(file)) {
                String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
                    continue;
                }
                int split = line.indexOf('=');
                String key = line.substring(0, split).trim();
                String value = line.substring(split + 1).trim();
                if (!key.isEmpty() && System.getenv(key) == null && System.getProperty(key) == null) {
                    System.setProperty(key, value);
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException(".env 파일을 읽지 못했습니다.", ex);
        }
    }
}
```

- 12행 애플리케이션 `main`이 Spring을 띄우기 전에 호출한다.
- 13~16행 `lesson`에서 실행하면 `.env`, 저장소 루트에서 실행하면 `lesson/.env`를 찾는다.
- 17~19행 파일이 없어도 예시 값인 `application.yml`의 기본값으로 올라간다.
- 23행 빈 줄, 주석, 등호가 없는 줄은 건너뛴다.
- 29행 이미 있는 환경변수와 JVM 속성은 덮어쓰지 않는다. 배포 환경의 값이 우선이다.
- 30행 Spring의 `${APP_SECRET}`이 읽을 수 있도록 시스템 속성으로 넣는다.

Git은 저장소 루트에서 이미 초기화되어 있다. 새 폴더라면 `git init` 한 뒤 루트 `.gitignore`가 `target/`, `node_modules/`, `data/`, `.env`를 제외하는지 `git check-ignore -v lesson/.env`로 확인한다.

## ④ 🔗 연동 포인트

`.env`의 `DATABASE_URL`과 `APP_SECRET`은 study6의 H2 파일과 study7의 토큰 서명이 읽는다. `CORS_ORIGIN`은 `/api` 응답의 허용 출처다. 운영에서는 같은 키에 다른 값을 넣는다. 이 상태로 study1이 첫 화면을 연다.

## ⑤ 오류 노트

| 증상 | 원인 | 해결 |
| --- | --- | --- |
| `mvn`을 찾을 수 없음 | Maven이 PATH에 없다 | Maven 3.9 이상을 설치하거나 IDE의 Maven으로 `lesson/pom.xml`을 연다 |
| Node 버전이 24가 아님 | Vaadin 25는 Node 24를 요구한다 | `node -v`가 v24인지 확인하고 다시 `mvn spring-boot:run` |
| 8080이 이미 사용 중 | 다른 서버가 포트를 잡고 있다 | `.env`의 `PORT`를 바꾸고 재시작한다 |
| `.env`를 고쳐도 반영되지 않음 | 이미 설정된 환경변수가 우선이다 | 터미널을 새로 열거나 해당 변수를 지운 뒤 재시작한다 |
| `vaadin-dev-server` not found | 개발 모드 의존성이 없다 | `pom.xml`의 `vaadin-dev`를 확인한다 |

## ⑥ 직접 해보기

1. `java -version`, `node -v`, `npm -v` 출력을 메모한다.
2. `Copy-Item .env.example .env` 뒤에 `git check-ignore lesson/.env`가 무시된다고 나오는지 본다.
3. http://localhost:8080 을 새로고침하고, 터미널에 컴파일 로그가 다시 찍히는지 본다.

## ⑦ ☐ 체크리스트

- [ ] Vaadin 공식 시작 페이지 또는 Spring Initializr로 Vaadin과 Spring Boot 프로젝트 생성
- [ ] Java, Node, npm, Maven 버전을 확인했다
- [ ] `lesson`에서 개발 서버가 8080으로 뜬다
- [ ] `.env`는 Git에서 빠지고 `.env.example`만 남는다
- [ ] IntelliJ Vaadin 플러그인 또는 VS Code Vaadin 확장을 확인했다

## ⑧ 다음 단계

[study1](study1.md)에서 같은 서버의 첫 화면과 Java 표현이 브라우저에 어떻게 보이는지 확인한다.

참고: [Vaadin 시작하기](https://vaadin.com/docs/latest/getting-started), [호환표](https://vaadin.com/docs/latest/compatibility)
