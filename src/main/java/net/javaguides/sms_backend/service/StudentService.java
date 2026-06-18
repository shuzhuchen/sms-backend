package net.javaguides.sms_backend.service;

import net.javaguides.sms_backend.dto.StudentDto;

public interface StudentService {
    StudentDto create(StudentDto dto);

    StudentDto getById(Long id);

    StudentDto update(Long id, StudentDto dto);

    void delete(Long id);
}
