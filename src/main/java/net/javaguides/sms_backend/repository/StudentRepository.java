package net.javaguides.sms_backend.repository;

import net.javaguides.sms_backend.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
