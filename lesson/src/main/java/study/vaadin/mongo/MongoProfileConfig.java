package study.vaadin.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@Profile("mongo")
@EnableMongoRepositories(basePackageClasses = NoteRepository.class)
public class MongoProfileConfig {

    @Bean
    MongoClient mongoClient(Environment environment) {
        String uri = environment.getProperty("spring.mongodb.uri", "mongodb://127.0.0.1:27017/vaadin_study");
        return MongoClients.create(uri);
    }

    @Bean
    MongoTemplate mongoTemplate(MongoClient mongoClient) {
        return new MongoTemplate(mongoClient, "vaadin_study");
    }
}
