package study.vaadin.pay;

import java.util.UUID;
import org.springframework.stereotype.Service;
import study.vaadin.api.ApiModels.Checkout;
import study.vaadin.config.AppProperties;

@Service
public class CheckoutClient {

    private final AppProperties properties;

    public CheckoutClient(AppProperties properties) {
        this.properties = properties;
    }

    public Checkout start(String title) {
        String secret = properties.paymentSecret();
        if (secret == null || secret.isBlank()) {
            return new Checkout("https://pay.example.test/mock/" + UUID.randomUUID(), "mock");
        }
        return new Checkout("https://pay.example.test/live/" + UUID.randomUUID() + "?item=" + title, "configured");
    }
}
