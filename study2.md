# study2 · 핵심 구조

## ① 목표

Flow의 기본 단위인 뷰와 레이아웃을 만들고, 메뉴가 뷰를 부르는 방식을 확인한다.

## ② 📖 이론

뷰는 주소 하나에 대응하는 화면이다. 레이아웃은 그 화면들이 같이 쓰는 서랍과 윗줄이다. Vaadin 25에서는 뷰에 `@Route`, 레이아웃에 `@Layout`을 붙인다. `@Layout`이 있으면 뷰마다 `layout = MainLayout.class`를 반복하지 않는다. 로그인 화면만 `@Route(value = "login", autoLayout = false)`로 레이아웃 밖에 둔다.

뷰는 Spring이 생성하므로 생성자 인자로 서비스와 `AuthenticationContext`를 받을 수 있다. 이것이 뷰가 다른 단위를 부르는 방법이다. 자식 컴포넌트에게 값을 넘길 때는 생성자 인자를 쓰고, 버튼이 부모에게 알릴 때는 클릭 람다를 쓴다.

비유하면 레이아웃은 건물 로비이고 뷰는 방이다. 로비의 안내판은 `@Menu`가 붙은 방만 보여 준다.

Flow와 Hilla는 둘 다 Vaadin이다. Flow는 서버가 컴포넌트 상태를 세션에 둔다. Hilla 화면은 브라우저의 React 상태이고, 데이터는 `@BrowserCallable`로 서버에 요청한다. 세션에 컴포넌트 트리가 쌓이면 사용자 수만큼 서버 메모리가 늘어난다. 그래서 화면에 거대한 컬렉션을 필드로 붙잡지 않고, Grid는 필요한 페이지만 가져온다.

## ③ 🛠️ 실기

http://localhost:8080 왼쪽 서랍에 홈과 게시판이 보여야 한다. 서랍은 좁은 화면에서 `DrawerToggle`로 연다.

`lesson/src/main/java/study/vaadin/ui/MainLayout.java` 전체:

```java
package study.vaadin.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.userdetails.UserDetails;

@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout {

    public MainLayout(AuthenticationContext authContext) {
        H1 title = new H1("Vaadin 게시판");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)").set("margin", "0");
        HorizontalLayout navbar = new HorizontalLayout(new DrawerToggle(), title);
        navbar.setWidthFull();
        navbar.setAlignItems(FlexComponent.Alignment.CENTER);
        authContext.getAuthenticatedUser(UserDetails.class).ifPresent(user -> {
            Span name = new Span(user.getUsername());
            Button logout = new Button("로그아웃", event -> authContext.logout());
            navbar.add(name, logout);
        });
        addToNavbar(navbar);

        SideNav menu = new SideNav();
        MenuConfiguration.getMenuEntries().forEach(entry -> {
            SideNavItem item = entry.icon() == null
                    ? new SideNavItem(entry.title(), entry.menuClass())
                    : new SideNavItem(entry.title(), entry.menuClass(), new Icon(entry.icon()));
            item.setMatchNested(true);
            menu.addItem(item);
        });
        menu.addItem(new SideNavItem("Hilla 게시판", "hilla-board"));
        addToDrawer(new Scroller(menu));
        setPrimarySection(Section.DRAWER);
    }
}
```

- 20행 이 클래스를 모든 자동 레이아웃으로 등록한다.
- 21행 레이아웃에도 접근 주석이 있어야 한다. 없으면 안의 뷰까지 막힌다.
- 24행 Spring이 보안 도우미를 생성자로 넣는다.
- 27~29행 윗줄은 서랍 스위치와 제목이다.
- 30~34행 로그인한 사용자에게만 이름과 로그아웃을 붙인다. 값의 방향은 보안 컨텍스트에서 레이아웃으로다.
- 38~44행 `@Menu`가 있는 뷰를 안내판 항목으로 만든다. `setMatchNested`는 `/write/1` 같은 하위 주소에서도 항목을 표시할 때 쓴다.
- 45행 Hilla 화면은 Java 뷰가 아니므로 주소 문자열로 항목을 하나 더한다.
- 47행 서랍을 세로로 꽉 쓴다.

뷰에서 레이아웃으로 데이터를 보낼 때는 공유 서비스나 `LiveBoard`를 쓴다. 레이아웃이 뷰의 필드를 직접 읽지 않는다.

## ④ 🔗 연동 포인트

`@Menu`를 단 뷰는 study4의 주소와 study7의 접근 주석을 같이 가진다. 메뉴에서 항목을 숨기는 것은 편의를 위한 것이고, 실제 거절은 뷰의 `@RolesAllowed`가 한다. Hilla 주소 `hilla-board`는 study12의 React 파일이 연다.

## ⑤ 오류 노트

| 증상 | 원인 | 해결 |
| --- | --- | --- |
| 모든 주소가 거절됨 | 레이아웃에 `@AnonymousAllowed` 또는 `@PermitAll`이 없다 | `MainLayout`에 접근 주석을 붙인다 |
| 로그인 화면이 서랍 안에 보임 | 로그인 뷰가 자동 레이아웃에 들어갔다 | `@Route(value = "login", autoLayout = false)` |
| 메뉴가 비어 있음 | 뷰에 `@Menu`가 없다 | 뷰에 `@Menu(order = ..., icon = "...")`를 붙인다 |

## ⑥ 직접 해보기

1. `HomeView`의 `@Menu` order를 5로 바꿔 메뉴 순서가 바뀌는지 본다.
2. 레이아웃의 제목 문자열을 바꾸고 새로고침한다.
3. 로그인 전에는 로그아웃 버튼이 없고, 로그인 후에만 생기는지 확인한다. 로그인 방법은 study7이다.

## ⑦ ☐ 체크리스트

- [ ] `@Route` 뷰와 공통 레이아웃 (Flow)
- [ ] `@Layout` 서랍과 `@Menu` 항목이 연결된다
- [ ] 로그인 뷰는 `autoLayout = false`로 레이아웃 밖에 있다
- [ ] 레이아웃 접근 주석이 없으면 어떤 일이 생기는지 말할 수 있다

## ⑧ 다음 단계

[study3](study3.md)에서 뷰가 서비스의 데이터와 브라우저 로컬 저장소를 어떻게 묶는지 본다.

참고: [라우터 레이아웃](https://vaadin.com/docs/latest/flow/routing/layout)
