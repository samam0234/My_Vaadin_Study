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
