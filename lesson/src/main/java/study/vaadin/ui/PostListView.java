package study.vaadin.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import java.util.List;
import java.util.Map;
import study.vaadin.api.ApiModels.PostCard;
import study.vaadin.service.PostService;

@Route("posts")
@PageTitle("게시판")
@Menu(order = 1, icon = "vaadin:list")
@AnonymousAllowed
public class PostListView extends VerticalLayout implements BeforeEnterObserver {

    private final PostService posts;
    private final TextField query = new TextField("검색");
    private final Grid<PostCard> grid = new Grid<>();
    private String keyword = "";

    public PostListView(PostService posts) {
        this.posts = posts;
        grid.addColumn(PostCard::title).setHeader("제목").setSortable(true);
        grid.addColumn(card -> card.author().nickname()).setHeader("작성자");
        grid.addColumn(PostCard::commentCount).setHeader("댓글").setSortable(true);
        grid.addColumn(PostCard::createdAt).setHeader("작성일").setSortable(true);
        grid.setPageSize(10);
        grid.addItemClickListener(event -> event.getSource().getUI().ifPresent(ui ->
                ui.navigate(PostEditorView.class, event.getItem().id())));

        Button search = new Button("검색", event -> event.getSource().getUI().ifPresent(ui ->
                ui.navigate(PostListView.class, QueryParameters.simple(Map.of("q", query.getValue())))));
        Button write = new Button("글쓰기", event -> event.getSource().getUI().ifPresent(ui -> ui.navigate(PostEditorView.class)));
        add(new H2("게시판"), new HorizontalLayout(query, search, write), grid);
        reload();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        keyword = event.getLocation().getQueryParameters().getParameters()
                .getOrDefault("q", List.of(""))
                .getFirst();
        query.setValue(keyword);
        reload();
    }

    private void reload() {
        grid.setItems(DataProvider.fromCallbacks(fetch -> {
            String sort = "-createdAt";
            if (!fetch.getSortOrders().isEmpty()) {
                var order = fetch.getSortOrders().getFirst();
                sort = switch (order.getSorted()) {
                    case "commentCount" -> "-commentCount";
                    case "createdAt" -> order.getDirection() == com.vaadin.flow.data.provider.SortDirection.ASCENDING ? "createdAt" : "-createdAt";
                    default -> "-createdAt";
                };
            }
            int limit = Math.max(fetch.getLimit(), 1);
            int page = fetch.getOffset() / limit + 1;
            return posts.list(keyword, page, limit, sort).data().stream();
        }, count -> (int) posts.list(keyword, 1, 1, "-createdAt").meta().total()));
    }
}
