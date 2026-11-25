package study.vaadin.api;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PostApiTest {

    @Autowired
    MockMvc mvc;

    @Test
    void healthAndBoardFlow() throws Exception {
        mvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));

        mvc.perform(post("/api/auth/signup")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\":\"bad\",\"password\":\"short\",\"nickname\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.fields.email").isArray());

        mvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\":\"mina@example.com\",\"password\":\"wrong-pass\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));

        MvcResult login = mvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\":\"mina@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value(not(emptyString())))
                .andExpect(jsonPath("$.data.user.passwordHash").doesNotExist())
                .andReturn();
        String token = login.getResponse().getContentAsString().split("\"accessToken\":\"")[1].split("\"")[0];

        mvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("mina@example.com"));

        mvc.perform(post("/api/posts")
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content("{\"title\":\"\",\"body\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        MvcResult created = mvc.perform(post("/api/posts")
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content("{\"title\":\"API 글\",\"body\":\"본문입니다\",\"tags\":[\"study\"]}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.data.title").value("API 글"))
                .andExpect(jsonPath("$.data.author.nickname").value("mina"))
                .andReturn();
        String location = created.getResponse().getHeader(HttpHeaders.LOCATION);

        mvc.perform(get("/api/posts").param("q", "API").param("page", "1").param("limit", "10").param("sort", "-createdAt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.page").value(1))
                .andExpect(jsonPath("$.meta.q").value("API"))
                .andExpect(jsonPath("$.data[0].title").value("API 글"));

        mvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.body").value("본문입니다"));

        MvcResult other = mvc.perform(post("/api/auth/signup")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\":\"other@example.com\",\"password\":\"password123\",\"nickname\":\"other\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        mvc.perform(post("/api/auth/signup")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\":\"other@example.com\",\"password\":\"password123\",\"nickname\":\"other\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("EMAIL_TAKEN"));
        MvcResult otherLogin = mvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\":\"other@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String otherToken = otherLogin.getResponse().getContentAsString().split("\"accessToken\":\"")[1].split("\"")[0];

        mvc.perform(patch(location)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherToken)
                        .content("{\"title\":\"가로채기\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));

        mvc.perform(get("/api/posts/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));

        mvc.perform(delete(location).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());

        mvc.perform(post("/api/auth/logout").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());

        other.getResponse();
    }
}
