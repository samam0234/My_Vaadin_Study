# Vaadin 학습

Java로 화면까지 만드는 Vaadin 학습 저장소다. Flow는 서버가 컴포넌트 트리를 들고 브라우저와 맞추고, Hilla는 React 화면에서 Java 서비스를 타입 안전하게 호출한다. 실행 코드는 `lesson/` 하나다.

| 문서 | 내용 |
| --- | --- |
| [study0.md](study0.md) ~ [study10.md](study10.md) | 환경, 화면, 데이터, 라우팅, 폼, API, 보안, 구조, 테스트, 배포 |
| [studyMidpoint.md](studyMidpoint.md) | 게시판·로그인·CRUD |
| [study11.md](study11.md) ~ [study18.md](study18.md) | DB 교체, Hilla, 외부 서비스, API 설계, 성능, CI, 실시간, AI·MCP |
| [studyFinal.md](studyFinal.md) | 위 내용을 한 서비스로 마무리 |

## 버전

이 저장소를 만든 시점의 재현 버전은 Vaadin 25.3.6, Spring Boot 4.1.3, Java 바이트코드 21이다. 로컬 확인은 Java 26.0.2.1, Node 24.14.0, npm 11.9.0, Maven 3.9.11에서 `mvn test` 3건이 통과했다. 브라우저로 개발 서버를 연 기록은 각 문서의 체크리스트를 직접 채우기 전에 확인한다.

## 실행

```powershell
Set-Location lesson
Copy-Item .env.example .env
mvn spring-boot:run
```

브라우저에서 http://localhost:8080 을 연다. 데모 계정은 `mina@example.com` / `password123`, 관리자는 `admin@example.com` / `password123` 이다. 비밀번호는 로컬 학습용이다.

API 확인:

```powershell
Invoke-RestMethod http://localhost:8080/health
```

## 공식 문서

- [Vaadin 시작](https://vaadin.com/docs/latest/getting-started)
- [Vaadin 프로젝트 생성](https://start.vaadin.com)
- [호환 버전](https://vaadin.com/docs/latest/compatibility)
