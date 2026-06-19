package net.javaguides.sms_backend.service;

import net.javaguides.sms_backend.dto.StudentDto;

import java.util.List;

public interface StudentService {
    StudentDto create(StudentDto dto);

    StudentDto getById(Long id);

    List<StudentDto> getAll();

    long count();

    StudentDto update(Long id, StudentDto dto);

    void delete(Long id);
}
