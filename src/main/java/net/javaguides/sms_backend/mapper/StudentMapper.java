package net.javaguides.sms_backend.mapper;

import net.javaguides.sms_backend.dto.StudentDto;
import net.javaguides.sms_backend.entity.Major;
import net.javaguides.sms_backend.entity.Student;

import java.util.HashSet;
import java.util.Set;

public class StudentMapper {

    public static StudentDto mapToStudentDto(Student student){
        return new StudentDto(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                copyMajors(student.getMajors()),
                student.getEnrollmentDate()
        );
    }

    public static Student mapToStudent(StudentDto studentDto){
        return new Student(
                studentDto.getId(),
                studentDto.getFirstName(),
                studentDto.getLastName(),
                studentDto.getEmail(),
                copyMajors(studentDto.getMajors()),
                studentDto.getEnrollmentDate()
        );
    }

    private static Set<Major> copyMajors(Set<Major> majors) {
        return majors == null ? new HashSet<>() : new HashSet<>(majors);
    }
}
