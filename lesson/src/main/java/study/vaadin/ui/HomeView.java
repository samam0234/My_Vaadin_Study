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
