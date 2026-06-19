package net.javaguides.sms_backend.service.impl;

import lombok.AllArgsConstructor;
import net.javaguides.sms_backend.dto.StudentDto;
import net.javaguides.sms_backend.entity.Student;
import net.javaguides.sms_backend.exception.ResourceNotFoundException;
import net.javaguides.sms_backend.mapper.StudentMapper;
import net.javaguides.sms_backend.repository.StudentRepository;
import net.javaguides.sms_backend.service.StudentService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class StudentServiceImpl implements StudentService {

    private StudentRepository studentRepository;

    @Override
    @CacheEvict(value = "students", allEntries = true)
    public StudentDto create(StudentDto dto) {

        Student student = StudentMapper.mapToStudent(dto);
        Student saved = studentRepository.save(student);
        return StudentMapper.mapToStudentDto(saved);
    }

    @Override
    @Cacheable(value = "students", key = "#id")
    public StudentDto getById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student does not exist with given id: " + id));
        return StudentMapper.mapToStudentDto(student);
    }

    @Override
    @Cacheable(value = "students", key = "'all'")
    public List<StudentDto> getAll() {
        return studentRepository.findAll()
                .stream()
                .map(StudentMapper::mapToStudentDto)
                .toList();
    }

    @Override
    @Cacheable(value = "students", key = "'count'")
    public long count() {
        return studentRepository.count();
    }

    @Override
    @CachePut(value = "students", key = "#id")
    public StudentDto update(Long id, StudentDto dto) {

        Student student = studentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Student does not exist with given id: " + id)
        );
        student.setFirstName(dto.getFirstName());
        student.setLastName(dto.getLastName());
        student.setEmail(dto.getEmail());
        student.setMajors(dto.getMajors());
        student.setEnrollmentDate(dto.getEnrollmentDate());

        Student updated = studentRepository.save(student);

        return StudentMapper.mapToStudentDto(updated);
    }

    @Override
    @CacheEvict(value = "students", allEntries = true)
    public void delete(Long id) {
        studentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Student does not exist with given id: " + id)
        );

        studentRepository.deleteById(id);
    }
}
