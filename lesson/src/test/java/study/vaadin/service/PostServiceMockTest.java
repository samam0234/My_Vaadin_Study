package study.vaadin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import study.vaadin.live.LiveBoard;
import study.vaadin.mail.MailGateway;
import study.vaadin.repo.PostRepository;
import study.vaadin.security.CurrentUser;

@ExtendWith(MockitoExtension.class)
class PostServiceMockTest {

    @Mock
    PostRepository posts;
    @Mock
    CurrentUser currentUser;
    @Mock
    MailGateway mail;
    @Mock
    LiveBoard liveBoard;
    @InjectMocks
    PostService service;

    @Test
    void missingPostIs404() {
        when(posts.findById(9L)).thenReturn(Optional.empty());
        ApiException error = assertThrows(ApiException.class, () -> service.find(9L));
        assertEquals(404, error.status());
        assertEquals("NOT_FOUND", error.code());
    }
}
