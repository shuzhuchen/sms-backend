package net.javaguides.sms_backend.repository;

import net.javaguides.sms_backend.entity.Major;
import net.javaguides.sms_backend.entity.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({MajorRepository.class, StudentRepository.class})
class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private MajorRepository majorRepository;

    @Test
    void searchMatchesMajorCaseInsensitive() {
        Major computerScience = majorRepository.save(new Major(null, "Computer Science", Set.of()));
        Major mathematics = majorRepository.save(new Major(null, "Mathematics", Set.of()));

        studentRepository.save(new Student(
                null,
                "Ada",
                "Lovelace",
                "ada@example.com",
                Set.of(computerScience),
                LocalDate.of(2025, 1, 1)
        ));
        studentRepository.save(new Student(
                null,
                "Grace",
                "Hopper",
                "grace@example.com",
                Set.of(mathematics),
                LocalDate.of(2025, 1, 1)
        ));

        Page<Student> result = studentRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrMajorsMajorNameContainingIgnoreCase(
                "COMPUTER",
                "COMPUTER",
                "COMPUTER",
                "COMPUTER",
                PageRequest.of(0, 5)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).extracting(Student::getEmail).containsExactly("ada@example.com");
    }

    @TestConfiguration
    static class CacheTestConfig {
        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("students");
        }
    }
}
