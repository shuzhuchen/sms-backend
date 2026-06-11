package net.javaguides.sms_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentDto {
    private Long id;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must be 50 characters or fewer")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must be 50 characters or fewer")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Major is required")
    @Size(max = 100, message = "Major must be 100 characters or fewer")
    private String major;

    @NotNull(message = "Enrollment date is required")
    @PastOrPresent(message = "Enrollment date cannot be in the future")
    private LocalDate enrollmentDate;
}
