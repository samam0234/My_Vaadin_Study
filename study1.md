# study1 · Hello World와 기본 문법

## ① 목표

Flow 첫 화면을 띄우고, Java 변수·조건·메서드 참조가 화면 문장이 되는 방식을 확인한다.

## ② 📖 이론

Flow에는 HTML 템플릿 문법이 없다. 화면에 보일 문장은 Java 표현식이고, 그 결과를 `Paragraph`나 `H2` 같은 컴포넌트에 넣는다. `"안녕, %s".formatted(name)`이 템플릿 표현의 자리다. Hilla의 React 파일에서는 `{row.title}`이 그 역할을 한다. 그 파일은 study12에서 연다.

비유하면 컴포넌트는 스티커다. 스티커에 문장을 붙이면 서버가 브라우저의 같은 자리에 스티커를 다시 붙인다.

개발 모드에서는 Java를 저장하면 애플리케이션이 다시 뜨고, 프론트 파일을 저장하면 Vite가 브라우저 모듈을 갈아 끼운다. 결과는 브라우저 개발자 도구의 Network에서 Vaadin UIDL 요청으로 볼 수 있다.

## ③ 🛠️ 실기

`lesson`에서 `mvn spring-boot:run`이 떠 있는 상태로 http://localhost:8080 을 연다. 이름 칸을 바꾸고 `인사 바꾸기`를 누른다. 개발자 도구의 Network를 켜 둔 채 버튼을 누르면 문서 전체가 아니라 Vaadin 요청만 오가는 것이 보인다.

`lesson/src/main/java/study/vaadin/ui/HomeView.java` 전체:

```java
package study.vaadin.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("")
@PageTitle("홈")
@Menu(order = 0, icon = "vaadin:home")
@AnonymousAllowed
public class HomeView extends VerticalLayout {

    public HomeView() {
        String visitor = "방문자";
        String greeting = "안녕, %s".formatted(visitor);
        int year = 2026;
        boolean ready = year >= 2026;

        TextField name = new TextField("이름");
        name.setValue(visitor);
        Paragraph message = new Paragraph(greeting + (ready ? " — 학습을 시작할 수 있습니다." : ""));
        Button rename = new Button("인사 바꾸기", event -> {
            String value = name.getValue().isBlank() ? "방문자" : name.getValue().trim();
            message.setText("안녕, %s".formatted(value));
        });

        add(new H2("Flow Hello"), message, name, rename,
                new Paragraph("Flow에는 HTML 템플릿이 없습니다. 화면 문장은 Java 표현식이 컴포넌트에 넣는 값입니다."),
                new Button("게시판", event -> event.getSource().getUI().ifPresent(ui -> ui.navigate(PostListView.class))));
    }
}
```

- 13행 빈 경로는 `http://localhost:8080/` 이다.
- 14행 브라우저 탭 제목이다.
- 15행 공통 레이아웃 메뉴에 이 뷰를 올린다. 레이아웃은 study2다.
- 16행 로그인하지 않아도 이 화면을 연다.
- 17행 `VerticalLayout`은 자식을 세로로 쌓는 기본 단위다.
- 20~23행 변수, 형식 문자열, 정수, 조건이다. 이 값들이 화면 문장의 재료다.
- 27행 삼항 연산자 결과가 문단에 들어간다.
- 28~31행 버튼 클릭은 람다다. 입력칸의 현재 값을 다시 문단에 넣는다.
- 33~35행 `add` 순서가 화면 순서다. 마지막 버튼은 게시판 뷰로 이동한다.

`VaadinStudyApplication`이 `spring-boot-devtools`를 선택 의존성으로 둔다. Java 저장 후 터미널이 재시작하면 브라우저를 한 번 새로고침한다.

## ④ 🔗 연동 포인트

이 화면의 `게시판` 버튼은 `PostListView`로 넘어간다. 그 뷰는 study3에서 데이터를, study6에서 저장소를 읽는다. 개발자 도구로 확인한 UIDL 요청은 study4의 라우팅 요청과 같은 서버 세션을 탄다.

## ⑤ 오류 노트

| 증상 | 원인 | 해결 |
| --- | --- | --- |
| 흰 화면만 보임 | 개발 모드 프론트 준비 중이다 | 터미널에 `Frontend compiled` 또는 Started 로그가 나올 때까지 기다린다 |
| 수정이 반영되지 않음 | 브라우저 캐시 또는 재시작 전 | 강력 새로고침 후 터미널 재시작 여부를 본다 |
| 버튼이 아무 일도 하지 않음 | 람다 안에서 UI가 없다 | `getUI().ifPresent`로 이동한다. 생성자 시점의 이동은 피한다 |
| 글자가 배경에 묻힘 | Lumo 테마가 없다 | `ShellSettings`의 `@Theme("vaadin-study")`와 `frontend/themes/vaadin-study`를 확인한다 |

## ⑥ 직접 해보기

1. `visitor` 초기 문장을 바꾸고 저장한 뒤 새로고침한다.
2. 개발자 도구 Console에 빨간 오류가 없는지 본다.
3. `year`를 2020으로 바꾸면 삼항 연산자의 어느 쪽이 화면에 남는지 확인한다.

## ⑦ ☐ 체크리스트

- [ ] http://localhost:8080 에 `Flow Hello`가 보인다
- [ ] 이름 변경 버튼이 문단을 바꾼다
- [ ] 개발자 도구 Network에서 버튼 클릭 요청을 보았다
- [ ] Java 저장 후 개발 서버가 화면을 다시 보여 준다

## ⑧ 다음 단계

[study2](study2.md)에서 이 뷰를 공통 레이아웃 안에 넣고, 뷰와 레이아웃이 서로 어떻게 붙는지 만든다.

참고: [Vaadin 컴포넌트](https://vaadin.com/docs/latest/components)
