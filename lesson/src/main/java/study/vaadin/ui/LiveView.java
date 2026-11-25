package study.vaadin.ui;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import study.vaadin.live.LiveBoard;

@Route("live")
@PageTitle("실시간")
@Menu(order = 2, icon = "vaadin:bell")
@PermitAll
public class LiveView extends VerticalLayout {

    private AutoCloseable subscription;

    public LiveView(LiveBoard liveBoard) {
        Paragraph log = new Paragraph("글을 등록하면 이 문장이 바뀝니다.");
        add(new H2("실시간 알림"), log);
        subscription = liveBoard.subscribe(message -> getUI().ifPresent(ui -> ui.access(() -> log.setText(message))));
        addDetachListener(event -> closeQuietly());
    }

    private void closeQuietly() {
        if (subscription == null) {
            return;
        }
        try {
            subscription.close();
        } catch (Exception ignored) {
            // 구독 해제는 화면을 막을 필요가 없다.
        }
    }
}
