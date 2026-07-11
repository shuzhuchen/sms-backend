package net.javaguides.sms_backend.controller;

import net.javaguides.sms_backend.dto.NameAggregationRequest;
import net.javaguides.sms_backend.exception.NameAggregationException;
import net.javaguides.sms_backend.security.CustomOAuth2UserService;
import net.javaguides.sms_backend.security.JwtService;
import net.javaguides.sms_backend.service.NameAggregationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = NameAggregationController.class, excludeAutoConfiguration = OAuth2ClientWebSecurityAutoConfiguration.class)
@WithMockUser
class NameAggregationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NameAggregationService nameAggregationService;

    @MockitoBean
    private CacheManager cacheManager;

    @MockitoBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void aggregateReturnsServiceResultForValidRequest() throws Exception {
        when(nameAggregationService.forwardToNext(List.of("Alice", "Bob")))
                .thenReturn(new NameAggregationRequest(List.of("Alice", "Bob", "Charlie")));

        mockMvc.perform(post("/name/aggregation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":["Alice","Bob"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name[0]").value("Alice"))
                .andExpect(jsonPath("$.name[1]").value("Bob"))
                .andExpect(jsonPath("$.name[2]").value("Charlie"))
                .andExpect(jsonPath("$.fallbackReason").doesNotExist());

        verify(nameAggregationService).forwardToNext(List.of("Alice", "Bob"));
    }

    @Test
    void aggregateAcceptsMultipleNamesWithSameFirstLetter() throws Exception {
        when(nameAggregationService.forwardToNext(List.of("Amy", "Alex")))
                .thenReturn(new NameAggregationRequest(List.of("Amy", "Alex", "Avery")));

        mockMvc.perform(post("/name/aggregation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":["Amy","Alex"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name[0]").value("Amy"))
                .andExpect(jsonPath("$.name[1]").value("Alex"))
                .andExpect(jsonPath("$.name[2]").value("Avery"));

        verify(nameAggregationService).forwardToNext(List.of("Amy", "Alex"));
    }

    @Test
    void aggregateAcceptsNamesWithDifferentFirstLetters() throws Exception {
        when(nameAggregationService.forwardToNext(List.of("Amy", "Ben", "Cara")))
                .thenReturn(new NameAggregationRequest(List.of("Amy", "Ben", "Cara", "Drew")));

        mockMvc.perform(post("/name/aggregation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":["Amy","Ben","Cara"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name[3]").value("Drew"));

        verify(nameAggregationService).forwardToNext(List.of("Amy", "Ben", "Cara"));
    }

    @Test
    void aggregateRejectsEmptyNameList() throws Exception {
        mockMvc.perform(post("/name/aggregation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request body"))
                .andExpect(jsonPath("$.details[0]", containsString("name")));

        verifyNoInteractions(nameAggregationService);
    }

    @Test
    void aggregateRejectsMissingNameField() throws Exception {
        mockMvc.perform(post("/name/aggregation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request body"));

        verifyNoInteractions(nameAggregationService);
    }

    @Test
    void aggregateRejectsNullRequestBody() throws Exception {
        mockMvc.perform(post("/name/aggregation")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request body"));

        verifyNoInteractions(nameAggregationService);
    }

    @Test
    void aggregateRejectsBlankNames() throws Exception {
        mockMvc.perform(post("/name/aggregation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":[" "]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request body"));

        verifyNoInteractions(nameAggregationService);
    }

    @Test
    void aggregateRejectsInvalidJson() throws Exception {
        mockMvc.perform(post("/name/aggregation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request body"));

        verifyNoInteractions(nameAggregationService);
    }

    @Test
    void aggregateMapsDownstreamFailureToBadGateway() throws Exception {
        when(nameAggregationService.forwardToNext(anyList()))
                .thenThrow(new NameAggregationException("Downstream returned 500"));

        mockMvc.perform(post("/name/aggregation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":["Alice"]}
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value("Name aggregation downstream failed"))
                .andExpect(jsonPath("$.path").value("/name/aggregation"));

        verify(nameAggregationService).forwardToNext(List.of("Alice"));
    }

    @Test
    void aggregateReturnsFallbackPayloadWhenServiceDowngrades() throws Exception {
        when(nameAggregationService.forwardToNext(List.of("Alice")))
                .thenReturn(new NameAggregationRequest(List.of("Alice"), "fallback-downstream unavailable"));

        mockMvc.perform(post("/name/aggregation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":["Alice"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name[0]").value("Alice"))
                .andExpect(jsonPath("$.fallbackReason").value("fallback-downstream unavailable"));

        verify(nameAggregationService).forwardToNext(List.of("Alice"));
    }
}
