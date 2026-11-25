package study.vaadin.ui;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route("admin")
@PageTitle("관리")
@Menu(order = 9, icon = "vaadin:key")
@RolesAllowed("ADMIN")
public class AdminView extends VerticalLayout {

    public AdminView() {
        add(new H2("관리자"), new Paragraph("이 화면은 ADMIN 역할만 들어올 수 있습니다. 메뉴에서 숨기지 않아도 주소로 들어오면 거절됩니다."));
    }
}
