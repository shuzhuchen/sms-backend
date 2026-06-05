package net.javaguides.sms_backend.service.impl;

import lombok.AllArgsConstructor;
import net.javaguides.sms_backend.dto.StudentDto;
import net.javaguides.sms_backend.entity.Student;
import net.javaguides.sms_backend.exception.ResourceNotFoundException;
import net.javaguides.sms_backend.mapper.StudentMapper;
import net.javaguides.sms_backend.repository.StudentRepository;
import net.javaguides.sms_backend.service.StudentService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StudentServiceImpl implements StudentService {

    private StudentRepository studentRepository;

    @Override
    public StudentDto createStudent(StudentDto studentDto) {

        Student student = StudentMapper.mapToStudent(studentDto);
        Student savedStudent = studentRepository.save(student);
        return StudentMapper.mapToStudentDto(savedStudent);
    }

    @Override
    public StudentDto getStudentById(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student does not exist with given id: " + studentId));
        return StudentMapper.mapToStudentDto(student);
    }

    @Override
    public List<StudentDto> getAllStudents() {
        List<Student> students = studentRepository.findAll();
        return students.stream().map((student) -> StudentMapper.mapToStudentDto(student))
                .collect(Collectors.toList());

    }

    @Override
    public StudentDto updateStudent(Long studentId, StudentDto updatedStudent) {

        Student student = studentRepository.findById(studentId).orElseThrow(
                () -> new ResourceNotFoundException("Student does not exist with given id: " + studentId)
        );
        student.setFirstName(updatedStudent.getFirstName());
        student.setLastName(updatedStudent.getLastName());
        student.setEmail(updatedStudent.getEmail());
        student.setMajor(updatedStudent.getMajor());
        student.setEnrollmentDate(updatedStudent.getEnrollmentDate());

        Student updatedStudentObj = studentRepository.save(student);

        return StudentMapper.mapToStudentDto(updatedStudentObj);
    }

    @Override
    public void deleteStudent(Long studentId) {
        studentRepository.findById(studentId).orElseThrow(
                () -> new ResourceNotFoundException("Student does not exist with given id: " + studentId)
        );

        studentRepository.deleteById(studentId);
    }
}
