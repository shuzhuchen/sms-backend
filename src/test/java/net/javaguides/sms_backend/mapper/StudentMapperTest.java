package net.javaguides.sms_backend.mapper;

import net.javaguides.sms_backend.dto.StudentDto;
import net.javaguides.sms_backend.entity.Major;
import net.javaguides.sms_backend.entity.Student;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class StudentMapperTest {

    @Test
    void mapToStudentDtoCopiesStudentFieldsAndMajors() {
        Major major = new Major(1L, "Math", Set.of());
        Student student = new Student(1L, "Ada", "Lovelace", "ada@example.com", Set.of(major), LocalDate.of(2025, 1, 1));

        StudentDto result = StudentMapper.mapToStudentDto(student);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("Ada");
        assertThat(result.getMajors()).containsExactly(major);
        assertThat(result.getMajors()).isNotSameAs(student.getMajors());
    }

    @Test
    void mapToStudentCopiesDtoFieldsAndMajors() {
        Major major = new Major(1L, "Math", Set.of());
        StudentDto dto = new StudentDto(1L, "Ada", "Lovelace", "ada@example.com", Set.of(major), LocalDate.of(2025, 1, 1));

        Student result = StudentMapper.mapToStudent(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getLastName()).isEqualTo("Lovelace");
        assertThat(result.getMajors()).containsExactly(major);
        assertThat(result.getMajors()).isNotSameAs(dto.getMajors());
    }

    @Test
    void mapToStudentDtoUsesEmptySetWhenMajorsAreNull() {
        Student student = new Student(1L, "Ada", "Lovelace", "ada@example.com", null, LocalDate.of(2025, 1, 1));

        StudentDto result = StudentMapper.mapToStudentDto(student);

        assertThat(result.getMajors()).isEmpty();
    }

    @Test
    void mapToStudentUsesEmptySetWhenMajorsAreNull() {
        StudentDto dto = new StudentDto(1L, "Ada", "Lovelace", "ada@example.com", null, LocalDate.of(2025, 1, 1));

        Student result = StudentMapper.mapToStudent(dto);

        assertThat(result.getMajors()).isEmpty();
    }
}
