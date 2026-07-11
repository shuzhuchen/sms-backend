import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { LoadingSkeletonComponent } from '../../shared/loading-skeleton/loading-skeleton.component';
import { Student } from '../student.model';
import { StudentService } from '../student.service';

@Component({
  selector: 'app-student-form',
  standalone: true,
  imports: [LoadingSkeletonComponent, ReactiveFormsModule, RouterLink],
  template: `
    <section class="form-panel">
      <div class="header">
        <h1>{{ studentId ? 'Edit Student' : 'New Student' }}</h1>
        <a routerLink="/students">Back</a>
      </div>

      @if (errorMessage) {
        <div class="alert">{{ errorMessage }}</div>
      }

      @if (loading) {
        <app-loading-skeleton [rows]="5" />
      } @else {
        <form [formGroup]="form" (ngSubmit)="save()">
        <label>
          First name
          <input type="text" formControlName="firstName">
          @if (showError('firstName')) {
            <span class="field-error">First name is required.</span>
          }
        </label>

        <label>
          Last name
          <input type="text" formControlName="lastName">
          @if (showError('lastName')) {
            <span class="field-error">Last name is required.</span>
          }
        </label>

        <label>
          Email
          <input type="email" formControlName="email">
          @if (showError('email')) {
            <span class="field-error">A valid email is required.</span>
          }
        </label>

        <label>
          Enrollment date
          <input type="date" formControlName="enrollmentDate">
          @if (showError('enrollmentDate')) {
            <span class="field-error">Enrollment date is required.</span>
          }
        </label>

        <label>
          Majors
          <input type="text" formControlName="majorsText" placeholder="Computer Science, Mathematics">
        </label>

        <div class="actions">
          <button class="primary" type="submit" [disabled]="form.invalid || saving">
            {{ saving ? 'Saving...' : 'Save' }}
          </button>
          <a class="button secondary" routerLink="/students">Cancel</a>
        </div>
        </form>
      }
    </section>
  `,
  styles: [`
    .form-panel {
      background: #ffffff;
      border: 1px solid #dbe3ef;
      border-radius: 8px;
      margin: 0 auto;
      max-width: 720px;
      padding: 24px;
    }

    .header,
    .actions {
      align-items: center;
      display: flex;
      justify-content: space-between;
      gap: 12px;
    }

    .header {
      margin-bottom: 20px;
    }

    h1 {
      font-size: 28px;
      margin: 0;
    }

    a {
      color: #2563eb;
      font-weight: 700;
      text-decoration: none;
    }

    form {
      display: grid;
      gap: 16px;
    }

    label {
      color: #374151;
      display: grid;
      font-weight: 700;
      gap: 8px;
    }

    .actions {
      justify-content: flex-start;
      margin-top: 8px;
    }

    .button {
      align-items: center;
      border-radius: 6px;
      display: inline-flex;
      min-height: 40px;
      padding: 0 14px;
      text-decoration: none;
    }

    .field-error {
      color: #b91c1c;
      font-size: 13px;
      font-weight: 600;
    }
  `]
})
export class StudentFormComponent implements OnInit {
  studentId?: number;
  loading = false;
  saving = false;
  errorMessage = '';

  readonly form = this.formBuilder.nonNullable.group({
    firstName: ['', [Validators.required, Validators.maxLength(50)]],
    lastName: ['', [Validators.required, Validators.maxLength(50)]],
    email: ['', [Validators.required, Validators.email]],
    enrollmentDate: ['', Validators.required],
    majorsText: ['']
  });

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly studentService: StudentService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      return;
    }

    this.studentId = Number(id);
    this.loading = true;
    this.studentService.getById(this.studentId).subscribe({
      next: (student) => this.form.patchValue({
        firstName: student.firstName,
        lastName: student.lastName,
        email: student.email,
        enrollmentDate: student.enrollmentDate,
        majorsText: student.majors?.map((major) => major.majorName).join(', ') || ''
      }),
      error: () => {
        this.errorMessage = 'Unable to load student details.';
        this.loading = false;
      },
      complete: () => this.loading = false
    });
  }

  save(): void {
    if (this.form.invalid) {
      this.errorMessage = 'Please fix the highlighted fields.';
      return;
    }

    this.saving = true;
    this.errorMessage = '';
    const student = this.toStudent();
    const request = this.studentId
      ? this.studentService.update(this.studentId, student)
      : this.studentService.create(student);

    request.subscribe({
      next: () => this.router.navigate(['/students']),
      error: () => {
        this.errorMessage = 'Unable to save student. Check the form values and retry.';
        this.saving = false;
      }
    });
  }

  private toStudent(): Student {
    const value = this.form.getRawValue();
    return {
      firstName: value.firstName,
      lastName: value.lastName,
      email: value.email,
      enrollmentDate: value.enrollmentDate,
      majors: value.majorsText
        .split(',')
        .map((majorName) => majorName.trim())
        .filter(Boolean)
        .map((majorName) => ({ majorName }))
    };
  }

  showError(controlName: keyof typeof this.form.controls): boolean {
    const control = this.form.controls[controlName];
    return control.invalid && (control.dirty || control.touched);
  }
}
