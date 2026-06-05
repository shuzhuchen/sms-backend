package net.javaguides.sms_backend.mapper;

import net.javaguides.sms_backend.dto.StudentDto;
import net.javaguides.sms_backend.entity.Student;

public class StudentMapper {

    public static StudentDto mapToStudentDto(Student student){
        return new StudentDto(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                student.getMajor(),
                student.getEnrollmentDate()
        );
    }

    public static Student mapToStudent(StudentDto studentDto){
        return new Student(
                studentDto.getId(),
                studentDto.getFirstName(),
                studentDto.getLastName(),
                studentDto.getEmail(),
                studentDto.getMajor(),
                studentDto.getEnrollmentDate()
        );
    }
}
