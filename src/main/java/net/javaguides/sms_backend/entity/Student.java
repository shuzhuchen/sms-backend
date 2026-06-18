package net.javaguides.sms_backend.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Student {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String major;

    private LocalDate enrollmentDate;
}

