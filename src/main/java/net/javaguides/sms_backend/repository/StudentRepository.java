package net.javaguides.sms_backend.repository;

import lombok.RequiredArgsConstructor;
import net.javaguides.sms_backend.entity.Student;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor // generates constructor for JdbcTemplate
public class StudentRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<Student> ROW_MAPPER = (rs, rowNum) -> new Student(
            rs.getLong("id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("email_id"),
            rs.getString("major"),
            rs.getObject("enrollment_date", LocalDate.class)
    );

    public Student save(Student student) {
        if (student.getId() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO students (first_name, last_name, email_id, major, enrollment_date) VALUES (?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, student.getFirstName());
                ps.setString(2, student.getLastName());
                ps.setString(3, student.getEmail());
                ps.setString(4, student.getMajor());
                ps.setDate(5, student.getEnrollmentDate() == null ? null : Date.valueOf(student.getEnrollmentDate()));
                return ps;
            }, keyHolder);
            student.setId(keyHolder.getKey().longValue());
        } else {
            jdbcTemplate.update(
                    "UPDATE students SET first_name = ?, last_name = ?, email_id = ?, major = ?, enrollment_date = ? WHERE id = ?",
                    student.getFirstName(), student.getLastName(), student.getEmail(), student.getMajor(), student.getEnrollmentDate(), student.getId()
            );
        }
        return student;
    }

    public Optional<Student> findById(Long id) {
        List<Student> results = jdbcTemplate.query(
                "SELECT * FROM students WHERE id = ?", ROW_MAPPER, id);
        return results.isEmpty()? Optional.empty() : Optional.of(results.get(0));
    }

    public List<Student> findAll() {
        return jdbcTemplate.query("SELECT * FROM students", ROW_MAPPER);
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM students WHERE id = ?", id);
    }


    public long count() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM students", Long.class);
        return count != null ? count : 0;
    }
}
