package net.javaguides.sms_backend.service;

import net.javaguides.sms_backend.dto.PagedResponse;
import net.javaguides.sms_backend.dto.StudentDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudentService {
    StudentDto create(StudentDto dto);

    StudentDto getById(Long id);

    List<StudentDto> getAll();

    PagedResponse<StudentDto> searchStudents(String query, Pageable pageable);

    long count();

    StudentDto update(Long id, StudentDto dto);

    void delete(Long id);
}
