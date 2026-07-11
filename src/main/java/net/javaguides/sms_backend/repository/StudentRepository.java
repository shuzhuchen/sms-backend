package net.javaguides.sms_backend.repository;

import jakarta.persistence.PersistenceContext;
import net.javaguides.sms_backend.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class StudentRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Student save(Student student) {
        if (student.getId() == null) {
            entityManager.persist(student);
            return student;
        }
        return entityManager.merge(student);
    }

    @Transactional(readOnly = true)
    public Optional<Student> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Student.class, id));
    }

    @Transactional(readOnly = true)
    public List<Student> findAll() {
        return entityManager.createQuery("SELECT s FROM Student s", Student.class)
                .setHint("org.hibernate.cacheable", true)
                .getResultList();
    }

    @Transactional(readOnly = true)
    public Page<Student> findAll(Pageable pageable) {
        long total = count();
        if (total == 0) {
            return new PageImpl<>(Collections.emptyList(), pageable, total);
        }

        List<Student> students = entityManager.createQuery("SELECT s FROM Student s ORDER BY s.id", Student.class)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        return new PageImpl<>(students, pageable, total);
    }

    @Transactional(readOnly = true)
    public Page<Student> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrMajorsMajorNameContainingIgnoreCase(
            String firstName,
            String lastName,
            String email,
            String majorName,
            Pageable pageable
    ) {
        String firstNamePattern = "%" + firstName.toLowerCase() + "%";
        String lastNamePattern = "%" + lastName.toLowerCase() + "%";
        String emailPattern = "%" + email.toLowerCase() + "%";
        String majorNamePattern = "%" + majorName.toLowerCase() + "%";
        long total = entityManager.createQuery("""
                        SELECT count(DISTINCT s) FROM Student s
                        LEFT JOIN s.majors m
                        WHERE lower(s.firstName) LIKE :firstNamePattern
                           OR lower(s.lastName) LIKE :lastNamePattern
                           OR lower(s.email) LIKE :emailPattern
                           OR lower(m.majorName) LIKE :majorNamePattern
                        """, Long.class)
                .setParameter("firstNamePattern", firstNamePattern)
                .setParameter("lastNamePattern", lastNamePattern)
                .setParameter("emailPattern", emailPattern)
                .setParameter("majorNamePattern", majorNamePattern)
                .getSingleResult();

        if (total == 0) {
            return new PageImpl<>(Collections.emptyList(), pageable, total);
        }

        List<Student> students = entityManager.createQuery("""
                        SELECT DISTINCT s FROM Student s
                        LEFT JOIN s.majors m
                        WHERE lower(s.firstName) LIKE :firstNamePattern
                           OR lower(s.lastName) LIKE :lastNamePattern
                           OR lower(s.email) LIKE :emailPattern
                           OR lower(m.majorName) LIKE :majorNamePattern
                        ORDER BY s.id
                        """, Student.class)
                .setParameter("firstNamePattern", firstNamePattern)
                .setParameter("lastNamePattern", lastNamePattern)
                .setParameter("emailPattern", emailPattern)
                .setParameter("majorNamePattern", majorNamePattern)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        return new PageImpl<>(students, pageable, total);
    }

    @Transactional
    public void deleteById(Long id) {
        Student student = entityManager.find(Student.class, id);
        if (student != null) {
            entityManager.remove(student);
        }
    }

    @Transactional(readOnly = true)
    public long count() {
        return entityManager.createQuery("SELECT count(s) FROM Student s", Long.class)
                .getSingleResult();
    }
}
