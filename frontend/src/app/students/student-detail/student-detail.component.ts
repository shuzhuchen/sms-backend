import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Student } from '../student.model';
import { StudentService } from '../student.service';

@Component({
  selector: 'app-student-detail',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="detail-panel">
      <div class="header">
        <h1>Student Detail</h1>
        <a routerLink="/students">Back</a>
      </div>

      @if (errorMessage) {
        <div class="alert">{{ errorMessage }}</div>
      }

      @if (student) {
        <dl>
          <div>
            <dt>First Name</dt>
            <dd>{{ student.firstName }}</dd>
          </div>
          <div>
            <dt>Last Name</dt>
            <dd>{{ student.lastName }}</dd>
          </div>
          <div>
            <dt>Email</dt>
            <dd>{{ student.email }}</dd>
          </div>
          <div>
            <dt>Enrollment date</dt>
            <dd>{{ student.enrollmentDate }}</dd>
          </div>
          <div>
            <dt>Majors</dt>
            <dd>{{ majorNames(student) }}</dd>
          </div>
        </dl>

        <a class="button primary" [routerLink]="['/students', student.id, 'edit']">Save</a>
      } @else if (!errorMessage) {
        <p>Loading student...</p>
      }
    </section>
  `,
  styles: [`
    .detail-panel {
      background: #ffffff;
      border: 1px solid #dbe3ef;
      border-radius: 8px;
      margin: 0 auto;
      max-width: 720px;
      padding: 24px;
    }

    .header {
      align-items: center;
      display: flex;
      justify-content: space-between;
      gap: 12px;
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

    dl {
      display: grid;
      gap: 14px;
      margin: 0 0 22px;
    }

    dl div {
      border-bottom: 1px solid #eef2f7;
      padding-bottom: 12px;
    }

    dt {
      color: #6b7280;
      font-size: 13px;
      font-weight: 800;
      text-transform: uppercase;
    }

    dd {
      margin: 4px 0 0;
    }

    .button {
      align-items: center;
      border-radius: 6px;
      color: #ffffff;
      display: inline-flex;
      min-height: 40px;
      padding: 0 14px;
    }

    .button.primary {
      color: #ffffff;
    }
  `]
})
export class StudentDetailComponent implements OnInit {
  student?: Student;
  errorMessage = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly studentService: StudentService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.studentService.getById(id).subscribe({
      next: (student) => this.student = student,
      error: () => this.errorMessage = 'Unable to load student details.'
    });
  }

  majorNames(student: Student): string {
    return student.majors?.map((major) => major.majorName).join(', ') || 'None';
  }
}
