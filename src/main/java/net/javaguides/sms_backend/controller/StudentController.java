package net.javaguides.sms_backend.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import net.javaguides.sms_backend.dto.PagedResponse;
import net.javaguides.sms_backend.dto.StudentDto;
import net.javaguides.sms_backend.service.StudentService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/students")
@AllArgsConstructor

public class StudentController {
    private final StudentService studentService;

    @PostMapping
    public ResponseEntity<StudentDto> create(@Valid @RequestBody StudentDto dto){
        StudentDto student=studentService.create(dto);
        return new ResponseEntity<>(student, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentDto> getById(@PathVariable Long id){
        return ResponseEntity.ok(studentService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<StudentDto>> getAll(){
        return ResponseEntity.ok(studentService.getAll());
    }

    @GetMapping("/search")
    public ResponseEntity<PagedResponse<StudentDto>> search(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(studentService.searchStudents(query, pageable));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> count(){
        return ResponseEntity.ok(studentService.count());
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentDto> update(@PathVariable Long id, @Valid @RequestBody StudentDto dto){
        return ResponseEntity.ok(studentService.update(id,dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id){
        studentService.delete(id);
        return ResponseEntity.ok("Student deleted successfully");
    }

}
