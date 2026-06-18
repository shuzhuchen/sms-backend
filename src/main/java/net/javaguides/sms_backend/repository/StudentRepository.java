package net.javaguides.sms_backend.repository;

import jakarta.persistence.PersistenceContext;
import net.javaguides.sms_backend.entity.Student;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
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
