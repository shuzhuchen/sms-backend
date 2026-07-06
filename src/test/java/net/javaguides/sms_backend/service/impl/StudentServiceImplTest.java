package net.javaguides.sms_backend.service.impl;

import net.javaguides.sms_backend.dto.StudentDto;
import net.javaguides.sms_backend.entity.Major;
import net.javaguides.sms_backend.entity.Student;
import net.javaguides.sms_backend.exception.ResourceNotFoundException;
import net.javaguides.sms_backend.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentServiceImpl studentService;

    @Test
    void createSavesStudentAndReturnsDto() {
        Student saved = student(1L, "Ada", "Lovelace", "ada@example.com");
        when(studentRepository.save(any(Student.class))).thenReturn(saved);

        StudentDto result = studentService.create(studentDto(null, "Ada", "Lovelace", "ada@example.com"));

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("Ada");
        assertThat(result.getMajors()).extracting(Major::getMajorName).containsExactly("Computer Science");
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void getByIdReturnsStudentWhenFound() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student(1L, "Ada", "Lovelace", "ada@example.com")));

        StudentDto result = studentService.getById(1L);

        assertThat(result.getEmail()).isEqualTo("ada@example.com");
        verify(studentRepository).findById(1L);
    }

    @Test
    void getByIdThrowsWhenStudentIsMissing() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Student does not exist with given id: 99");
    }

    @Test
    void getAllReturnsMappedStudents() {
        when(studentRepository.findAll()).thenReturn(List.of(
                student(1L, "Ada", "Lovelace", "ada@example.com"),
                student(2L, "Grace", "Hopper", "grace@example.com")
        ));

        List<StudentDto> result = studentService.getAll();

        assertThat(result).extracting(StudentDto::getFirstName).containsExactly("Ada", "Grace");
        verify(studentRepository).findAll();
    }

    @Test
    void countDelegatesToRepository() {
        when(studentRepository.count()).thenReturn(7L);

        long result = studentService.count();

        assertThat(result).isEqualTo(7L);
        verify(studentRepository).count();
    }

    @Test
    void updateMutatesExistingStudentAndSavesIt() {
        Student existing = student(1L, "Ada", "Lovelace", "ada@example.com");
        StudentDto update = studentDto(null, "Augusta", "Byron", "augusta@example.com");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(studentRepository.save(existing)).thenReturn(existing);

        StudentDto result = studentService.update(1L, update);

        assertThat(result.getFirstName()).isEqualTo("Augusta");
        assertThat(result.getLastName()).isEqualTo("Byron");
        assertThat(result.getEmail()).isEqualTo("augusta@example.com");
        verify(studentRepository).save(existing);
    }

    @Test
    void updateThrowsWhenStudentIsMissing() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.update(99L, studentDto(null, "Ada", "Lovelace", "ada@example.com")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Student does not exist with given id: 99");

        verify(studentRepository, never()).save(any());
    }

    @Test
    void deleteRemovesStudentWhenFound() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student(1L, "Ada", "Lovelace", "ada@example.com")));

        studentService.delete(1L);

        verify(studentRepository).deleteById(1L);
    }

    @Test
    void deleteThrowsWhenStudentIsMissing() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Student does not exist with given id: 99");

        verify(studentRepository, never()).deleteById(99L);
    }

    private static Student student(Long id, String firstName, String lastName, String email) {
        return new Student(
                id,
                firstName,
                lastName,
                email,
                Set.of(new Major(10L, "Computer Science", Set.of())),
                LocalDate.of(2025, 1, 1)
        );
    }

    private static StudentDto studentDto(Long id, String firstName, String lastName, String email) {
        return new StudentDto(
                id,
                firstName,
                lastName,
                email,
                Set.of(new Major(10L, "Computer Science", Set.of())),
                LocalDate.of(2025, 1, 1)
        );
    }
}
