package study.vaadin.live;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@Component
public class LiveBoard {

    private final List<Consumer<String>> listeners = new CopyOnWriteArrayList<>();

    public AutoCloseable subscribe(Consumer<String> listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    public void publish(String message) {
        listeners.forEach(listener -> listener.accept(message));
    }
}
