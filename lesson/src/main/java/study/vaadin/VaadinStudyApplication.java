package study.vaadin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration;
import org.springframework.boot.data.mongodb.autoconfigure.DataMongoRepositoriesAutoConfiguration;
import org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import study.vaadin.config.AppProperties;
import study.vaadin.config.DotEnv;

@SpringBootApplication(exclude = {
        MongoAutoConfiguration.class,
        DataMongoAutoConfiguration.class,
        DataMongoRepositoriesAutoConfiguration.class
})
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(AppProperties.class)
public class VaadinStudyApplication {

    public static void main(String[] args) {
        DotEnv.load();
        SpringApplication.run(VaadinStudyApplication.class, args);
    }
}
