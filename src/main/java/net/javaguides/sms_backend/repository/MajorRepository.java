package net.javaguides.sms_backend.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import net.javaguides.sms_backend.entity.Major;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class MajorRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public Optional<Major> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Major.class, id));
    }

    @Transactional(readOnly = true)
    public Optional<Major> findByMajorName(String majorName) {
        return entityManager.createQuery("""
                        SELECT m FROM Major m
                        WHERE lower(m.majorName) = lower(:majorName)
                        """, Major.class)
                .setParameter("majorName", majorName)
                .getResultStream()
                .findFirst();
    }

    @Transactional
    public Major save(Major major) {
        if (major.getId() == null) {
            entityManager.persist(major);
            return major;
        }
        return entityManager.merge(major);
    }
}
