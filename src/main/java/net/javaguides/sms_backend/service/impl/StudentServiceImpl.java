package net.javaguides.sms_backend.service.impl;

import lombok.AllArgsConstructor;
import net.javaguides.sms_backend.dto.PagedResponse;
import net.javaguides.sms_backend.dto.StudentDto;
import net.javaguides.sms_backend.entity.Major;
import net.javaguides.sms_backend.entity.Student;
import net.javaguides.sms_backend.exception.ResourceNotFoundException;
import net.javaguides.sms_backend.kafka.event.SmsEvent;
import net.javaguides.sms_backend.kafka.producer.SmsEventProducer;
import net.javaguides.sms_backend.mapper.StudentMapper;
import net.javaguides.sms_backend.repository.MajorRepository;
import net.javaguides.sms_backend.repository.StudentRepository;
import net.javaguides.sms_backend.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@AllArgsConstructor
public class StudentServiceImpl implements StudentService {

    private static final Logger log = LoggerFactory.getLogger(StudentServiceImpl.class);

    private StudentRepository studentRepository;
    private MajorRepository majorRepository;
    private SmsEventProducer smsEventProducer;

    @Override
    @CacheEvict(value = "students", allEntries = true)
    public StudentDto create(StudentDto dto) {

        Student student = StudentMapper.mapToStudent(dto);
        student.setMajors(resolveMajors(dto.getMajors()));
        Student saved = studentRepository.save(student);
        publishEvent("CREATED", saved.getId(), "Student created");
        return StudentMapper.mapToStudentDto(saved);
    }

    @Override
    @Cacheable(value = "students", key = "#id")
    public StudentDto getById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student does not exist with given id: " + id));
        return StudentMapper.mapToStudentDto(student);
    }

    @Override
    @Cacheable(value = "students", key = "'all'")
    public List<StudentDto> getAll() {
        return studentRepository.findAll()
                .stream()
                .map(StudentMapper::mapToStudentDto)
                .toList();
    }

    @Override
    public PagedResponse<StudentDto> searchStudents(String query, Pageable pageable) {
        Page<Student> students = StringUtils.hasText(query)
                ? studentRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrMajorsMajorNameContainingIgnoreCase(
                query.trim(), query.trim(), query.trim(), query.trim(), pageable)
                : studentRepository.findAll(pageable);

        List<StudentDto> content = students.getContent()
                .stream()
                .map(StudentMapper::mapToStudentDto)
                .toList();

        return new PagedResponse<>(
                content,
                students.getNumber(),
                students.getSize(),
                students.getTotalElements(),
                students.getTotalPages(),
                students.isLast()
        );
    }

    @Override
    @Cacheable(value = "students", key = "'count'")
    public long count() {
        return studentRepository.count();
    }

    @Override
    @CacheEvict(value = "students", allEntries = true)
    public StudentDto update(Long id, StudentDto dto) {

        Student student = studentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Student does not exist with given id: " + id)
        );
        student.setFirstName(dto.getFirstName());
        student.setLastName(dto.getLastName());
        student.setEmail(dto.getEmail());
        student.setMajors(resolveMajors(dto.getMajors()));
        student.setEnrollmentDate(dto.getEnrollmentDate());

        Student updated = studentRepository.save(student);
        publishEvent("UPDATED", updated.getId(), "Student updated");

        return StudentMapper.mapToStudentDto(updated);
    }

    @Override
    @CacheEvict(value = "students", allEntries = true)
    public void delete(Long id) {
        studentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Student does not exist with given id: " + id)
        );

        studentRepository.deleteById(id);
        publishEvent("DELETED", id, "Student deleted");
    }

    private Set<Major> resolveMajors(Set<Major> requestedMajors) {
        Set<Major> majors = new HashSet<>();
        if (requestedMajors == null) {
            return majors;
        }

        for (Major requestedMajor : requestedMajors) {
            if (requestedMajor == null) {
                continue;
            }

            if (requestedMajor.getId() != null) {
                majors.add(majorRepository.findById(requestedMajor.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Major does not exist with given id: " + requestedMajor.getId())));
                continue;
            }

            String majorName = requestedMajor.getMajorName();
            if (majorName == null || majorName.isBlank()) {
                continue;
            }

            String normalizedName = majorName.trim();
            majors.add(majorRepository.findByMajorName(normalizedName)
                    .orElseGet(() -> majorRepository.save(new Major(null, normalizedName, new HashSet<>()))));
        }

        return majors;
    }

    private void publishEvent(String eventType, Long entityId, String message) {
        try {
            smsEventProducer.publish(buildEvent(eventType, entityId, message));
        } catch (RuntimeException ex) {
            log.warn("Student event publish failed eventType={} entityId={}", eventType, entityId, ex);
        }
    }

    private SmsEvent buildEvent(String eventType, Long entityId, String message) {
        return new SmsEvent(
                UUID.randomUUID().toString(),
                eventType,
                entityId,
                message,
                Instant.now()
        );
    }
}
