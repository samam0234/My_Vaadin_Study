package study.vaadin.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import study.vaadin.service.ApiException;
import study.vaadin.service.PostService;

@Route("write")
@PageTitle("글쓰기")
@PermitAll
public class PostEditorView extends VerticalLayout implements HasUrlParameter<Long> {

    private final PostService posts;
    private final TextField title = new TextField("제목");
    private final TextArea body = new TextArea("본문");
    private final TextField tags = new TextField("태그");
    private final Binder<Draft> binder = new Binder<>(Draft.class);
    private final Draft draft = new Draft();
    private Long editingId;

    public PostEditorView(PostService posts) {
        this.posts = posts;
        title.setWidthFull();
        body.setWidthFull();
        body.setMinHeight("160px");
        binder.forField(title).asRequired("제목을 입력하세요.")
                .withValidator(value -> value.trim().length() <= 200, "1~200자로 입력하세요.")
                .bind(Draft::getTitle, Draft::setTitle);
        binder.forField(body).asRequired("본문을 입력하세요.")
                .bind(Draft::getBody, Draft::setBody);
        binder.forField(tags).bind(Draft::getTags, Draft::setTags);
        binder.setBean(draft);

        title.addValueChangeListener(event -> getElement().executeJs("localStorage.setItem('vaadin-draft-title', $0)", event.getValue()));
        Button save = new Button("저장");
        save.setDisableOnClick(true);
        save.addClickListener(event -> {
            try {
                saveDraft();
            } finally {
                save.setEnabled(true);
            }
        });
        add(new H2("글쓰기"), title, body, tags, save);
        getElement().executeJs("return localStorage.getItem('vaadin-draft-title')")
                .then(String.class, stored -> {
                    if (stored != null && !stored.isBlank() && title.getValue().isBlank() && editingId == null) {
                        title.setValue(stored);
                    }
                });
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Long parameter) {
        if (parameter == null) {
            return;
        }
        editingId = parameter;
        var found = posts.find(editingId);
        title.setValue(found.title());
        body.setValue(found.body());
        tags.setValue(String.join(",", found.tags()));
    }

    private void saveDraft() {
        try {
            binder.writeBean(draft);
        } catch (ValidationException ex) {
            Notification.show("입력값을 확인하세요.");
            return;
        }
        var tagList = java.util.Arrays.stream(draft.getTags().split(",")).map(String::trim).filter(tag -> !tag.isEmpty()).toList();
        try {
            if (editingId == null) {
                var created = posts.create(new study.vaadin.api.ApiModels.PostWrite(draft.getTitle(), draft.getBody(), tagList));
                Notification.show("등록했습니다. 상태 201에 해당합니다.");
                getUI().ifPresent(ui -> ui.navigate(PostEditorView.class, created.id()));
            } else {
                posts.update(editingId, new study.vaadin.api.ApiModels.PostPatch(draft.getTitle(), draft.getBody(), tagList));
                Notification.show("수정했습니다. 상태 200에 해당합니다.");
            }
        } catch (ApiException ex) {
            Notification.show(ex.status() + " " + ex.getMessage());
        }
    }

    public static class Draft {
        private String title = "";
        private String body = "";
        private String tags = "";

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
        public String getTags() { return tags; }
        public void setTags(String tags) { this.tags = tags; }
    }
}
