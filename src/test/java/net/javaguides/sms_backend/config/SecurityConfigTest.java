package net.javaguides.sms_backend.config;

import net.javaguides.sms_backend.dto.StudentDto;
import net.javaguides.sms_backend.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "auth"})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentService studentService;

    @Test
    void unauthenticatedRequestToProtectedApiIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/students"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(studentService);
    }

    @Test
    void loginReturnsJwtToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "admin123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    void loginRejectsInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "wrong"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void authenticatedUserCanReadApiData() throws Exception {
        when(studentService.getAll()).thenReturn(List.of(student(1L, "Ada", "Lovelace", "ada@example.com")));

        mockMvc.perform(get("/api/v1/students").header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("ada@example.com"));

        verify(studentService).getAll();
    }

    @Test
    void authenticatedAdminCanCreateUpdateAndDeleteApiData() throws Exception {
        when(studentService.create(any(StudentDto.class))).thenReturn(student(1L, "Ada", "Lovelace", "ada@example.com"));
        when(studentService.update(any(Long.class), any(StudentDto.class))).thenReturn(student(1L, "Ada", "Byron", "ada@example.com"));

        mockMvc.perform(post("/api/v1/students")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson()))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/v1/students/1")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson()))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/students/1").header("Authorization", bearerToken()))
                .andExpect(status().isOk());

        verify(studentService).create(any(StudentDto.class));
        verify(studentService).update(any(Long.class), any(StudentDto.class));
        verify(studentService).delete(1L);
    }

    @Test
    void actuatorHealthIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    private static StudentDto student(Long id, String firstName, String lastName, String email) {
        return new StudentDto(id, firstName, lastName, email, null, LocalDate.of(2025, 1, 1));
    }

    private static String studentJson() {
        return """
                {
                  "firstName": "Ada",
                  "lastName": "Lovelace",
                  "email": "ada@example.com",
                  "majors": [],
                  "enrollmentDate": "2025-01-01"
                }
                """;
    }

    private String bearerToken() throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "admin123"
                                }
                                """))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String token = response.replaceAll(".*\\\"token\\\":\\\"([^\\\"]+)\\\".*", "$1");
        return "Bearer " + token;
    }
}
