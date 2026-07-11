package net.javaguides.sms_backend.controller;

import net.javaguides.sms_backend.dto.PagedResponse;
import net.javaguides.sms_backend.dto.StudentDto;
import net.javaguides.sms_backend.exception.ResourceNotFoundException;
import net.javaguides.sms_backend.security.CustomOAuth2UserService;
import net.javaguides.sms_backend.security.JwtService;
import net.javaguides.sms_backend.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = StudentController.class, excludeAutoConfiguration = OAuth2ClientWebSecurityAutoConfiguration.class)
@WithMockUser(roles = "ADMIN")
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentService studentService;

    @MockitoBean
    private CacheManager cacheManager;

    @MockitoBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void createReturnsCreatedStudent() throws Exception {
        StudentDto request = student(null, "Ada", "Lovelace", "ada@example.com");
        StudentDto response = student(1L, "Ada", "Lovelace", "ada@example.com");
        when(studentService.create(any(StudentDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "firstName": "Ada",
                  "lastName": "Lovelace",
                  "email": "ada@example.com",
                  "majors": [],
                  "enrollmentDate": "2025-01-01"
                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Ada"));

        verify(studentService).create(any(StudentDto.class));
    }

    @Test
    void createRejectsInvalidStudent() throws Exception {
        mockMvc.perform(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "",
                                  "lastName": "Lovelace",
                                  "email": "not-an-email",
                                  "majors": [],
                                  "enrollmentDate": "2025-01-01"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]", containsString(":")));

        verifyNoInteractions(studentService);
    }

    @Test
    void getByIdReturnsStudent() throws Exception {
        when(studentService.getById(1L)).thenReturn(student(1L, "Ada", "Lovelace", "ada@example.com"));

        mockMvc.perform(get("/api/v1/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ada@example.com"));

        verify(studentService).getById(1L);
    }

    @Test
    void getByIdMapsMissingStudentToNotFound() throws Exception {
        when(studentService.getById(99L)).thenThrow(new ResourceNotFoundException("Student does not exist"));

        mockMvc.perform(get("/api/v1/students/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Student does not exist"));

        verify(studentService).getById(99L);
    }

    @Test
    void getAllReturnsStudents() throws Exception {
        when(studentService.getAll()).thenReturn(List.of(
                student(1L, "Ada", "Lovelace", "ada@example.com"),
                student(2L, "Grace", "Hopper", "grace@example.com")
        ));

        mockMvc.perform(get("/api/v1/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Ada"))
                .andExpect(jsonPath("$[1].firstName").value("Grace"));

        verify(studentService).getAll();
    }

    @Test
    void searchReturnsPagedStudents() throws Exception {
        when(studentService.searchStudents(any(String.class), any())).thenReturn(new PagedResponse<>(
                List.of(student(1L, "Ada", "Lovelace", "ada@example.com")),
                0,
                5,
                1,
                1,
                true
        ));

        mockMvc.perform(get("/api/v1/students/search")
                        .param("query", "computer")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].majors[0].majorName").value("Computer Science"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.last").value(true));

        verify(studentService).searchStudents(any(String.class), any());
    }

    @Test
    void countReturnsStudentCount() throws Exception {
        when(studentService.count()).thenReturn(2L);

        mockMvc.perform(get("/api/v1/students/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));

        verify(studentService).count();
    }

    @Test
    void updateReturnsUpdatedStudent() throws Exception {
        StudentDto request = student(null, "Ada", "Byron", "ada@example.com");
        StudentDto response = student(1L, "Ada", "Byron", "ada@example.com");
        when(studentService.update(any(Long.class), any(StudentDto.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "firstName": "Ada",
                  "lastName": "Byron",
                  "email": "ada@example.com",
                  "majors": [],
                  "enrollmentDate": "2025-01-01"
                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Byron"));

        verify(studentService).update(any(Long.class), any(StudentDto.class));
    }

    @Test
    void updateRejectsInvalidStudent() throws Exception {
        StudentDto invalid = student(null, "Ada", "", "ada@example.com");

        mockMvc.perform(put("/api/v1/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "firstName": "Ada",
                  "lastName": "",
                  "email": "ada@example.com",
                  "majors": [],
                  "enrollmentDate": "2025-01-01"
                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request body"));

        verifyNoInteractions(studentService);
    }

    @Test
    void deleteReturnsSuccessMessage() throws Exception {
        mockMvc.perform(delete("/api/v1/students/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Student deleted successfully"));

        verify(studentService).delete(1L);
    }

    private static StudentDto student(Long id, String firstName, String lastName, String email) {
        return new StudentDto(id, firstName, lastName, email, java.util.Set.of(new net.javaguides.sms_backend.entity.Major(10L, "Computer Science", java.util.Set.of())), LocalDate.of(2025, 1, 1));
    }
}
