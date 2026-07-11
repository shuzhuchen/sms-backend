package net.javaguides.sms_backend.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import net.javaguides.sms_backend.entity.AppUser;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class AppUserRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public Optional<AppUser> findByProviderAndProviderId(String provider, String providerId) {
        return entityManager.createQuery("""
                        SELECT u FROM AppUser u
                        WHERE u.provider = :provider AND u.providerId = :providerId
                        """, AppUser.class)
                .setParameter("provider", provider)
                .setParameter("providerId", providerId)
                .getResultStream()
                .findFirst();
    }

    @Transactional(readOnly = true)
    public Optional<AppUser> findByEmail(String email) {
        return entityManager.createQuery("""
                        SELECT u FROM AppUser u
                        WHERE u.email = :email
                        """, AppUser.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst();
    }

    @Transactional(readOnly = true)
    public Optional<AppUser> findByUsername(String username) {
        return entityManager.createQuery("""
                        SELECT u FROM AppUser u
                        WHERE u.username = :username
                        """, AppUser.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

    @Transactional
    public AppUser save(AppUser user) {
        if (user.getId() == null) {
            entityManager.persist(user);
            return user;
        }
        return entityManager.merge(user);
    }
}
