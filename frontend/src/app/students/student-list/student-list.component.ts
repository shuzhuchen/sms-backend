import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog/confirm-dialog.component';
import { LoadingSkeletonComponent } from '../../shared/loading-skeleton/loading-skeleton.component';
import { Student } from '../student.model';
import { StudentService } from '../student.service';

@Component({
  selector: 'app-student-list',
  standalone: true,
  imports: [ConfirmDialogComponent, LoadingSkeletonComponent, ReactiveFormsModule, RouterLink],
  template: `
    <section class="toolbar">
      <div>
        <h1>Students</h1>
        <p>{{ totalElements }} records</p>
      </div>
      <a class="button primary" routerLink="/students/new">New Student</a>
    </section>

    <section class="filters">
      <label>
        Search
        <input type="search" [formControl]="searchControl" placeholder="Search name, email, or major">
      </label>
    </section>

    @if (errorMessage) {
      <div class="alert">{{ errorMessage }}</div>
    }

    <section class="table-wrap">
      @if (loading) {
        <div class="skeleton-pad">
          <app-loading-skeleton [rows]="pageSize" />
        </div>
      } @else {
        <table>
          <thead>
            <tr>
              <th>First Name</th>
              <th>Last Name</th>
              <th>Email</th>
              <th>Enrollment Date</th>
              <th>Majors</th>
              <th class="actions">Actions</th>
            </tr>
          </thead>
          <tbody>
            @for (student of students; track student.id) {
              <tr>
                <td>{{ student.firstName }}</td>
                <td>{{ student.lastName }}</td>
                <td>{{ student.email }}</td>
                <td>{{ student.enrollmentDate }}</td>
                <td>{{ majorNames(student) }}</td>
                <td class="actions">
                  <a [routerLink]="['/students', student.id]">View</a>
                  <a [routerLink]="['/students', student.id, 'edit']">Edit</a>
                  <button class="danger compact" type="button" (click)="openDeleteDialog(student)">Delete</button>
                </td>
              </tr>
            } @empty {
              <tr>
                <td colspan="6" class="empty">No students found.</td>
              </tr>
            }
          </tbody>
        </table>
      }
    </section>

    <section class="pagination">
      <button class="secondary" type="button" (click)="previousPage()" [disabled]="page === 0 || loading">Previous</button>
      <span>Page {{ displayPage }} / {{ displayTotalPages }}</span>
      <button class="secondary" type="button" (click)="nextPage()" [disabled]="last || loading">Next</button>
      <label>
        Page size
        <select [value]="pageSize" (change)="changePageSize($event)">
          <option value="5">5</option>
          <option value="10">10</option>
          <option value="20">20</option>
        </select>
      </label>
    </section>

    @if (pendingDeleteStudent) {
      <app-confirm-dialog
        title="Delete student"
        [message]="'Delete ' + pendingDeleteStudent.firstName + ' ' + pendingDeleteStudent.lastName + '?'"
        (confirm)="confirmDelete()"
        (cancel)="cancelDelete()"
      />
    }
  `,
  styles: [`
    .toolbar {
      align-items: center;
      display: flex;
      justify-content: space-between;
      margin-bottom: 18px;
      gap: 16px;
    }

    h1 {
      font-size: 30px;
      margin: 0;
    }

    p {
      color: #6b7280;
      margin: 4px 0 0;
    }

    .filters {
      margin-bottom: 16px;
      max-width: 420px;
    }

    .filters label,
    .pagination label {
      color: #374151;
      display: grid;
      font-weight: 700;
      gap: 8px;
    }

    .button {
      align-items: center;
      border-radius: 6px;
      display: inline-flex;
      min-height: 40px;
      padding: 0 14px;
      text-decoration: none;
    }

    .table-wrap {
      background: #ffffff;
      border: 1px solid #dbe3ef;
      border-radius: 8px;
      overflow-x: auto;
    }

    .skeleton-pad {
      padding: 16px;
    }

    table {
      border-collapse: collapse;
      min-width: 980px;
      width: 100%;
    }

    th,
    td {
      border-bottom: 1px solid #eef2f7;
      padding: 14px 16px;
      text-align: left;
      vertical-align: middle;
    }

    th {
      background: #f8fafc;
      color: #4b5563;
      font-size: 13px;
      text-transform: uppercase;
    }

    .actions {
      align-items: center;
      display: flex;
      gap: 10px;
      justify-content: flex-end;
      white-space: nowrap;
    }

    .actions a {
      color: #2563eb;
      font-weight: 700;
      text-decoration: none;
    }

    .compact {
      min-height: 34px;
      padding: 0 10px;
    }

    .empty {
      color: #6b7280;
      text-align: center;
    }

    .pagination {
      align-items: center;
      display: flex;
      gap: 14px;
      justify-content: flex-end;
      margin-top: 16px;
    }

    .pagination select {
      min-height: 36px;
      min-width: 76px;
    }
  `]
})
export class StudentListComponent implements OnInit, OnDestroy {
  readonly searchControl = new FormControl('', { nonNullable: true });
  private readonly destroy$ = new Subject<void>();

  students: Student[] = [];
  loading = false;
  errorMessage = '';
  page = 0;
  pageSize = 5;
  totalElements = 0;
  totalPages = 0;
  last = true;
  pendingDeleteStudent?: Student;

  constructor(private readonly studentService: StudentService) {}

  ngOnInit(): void {
    this.loadStudents(0);
    this.searchControl.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe(() => this.loadStudents(0));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadStudents(page = this.page): void {
    this.loading = true;
    this.errorMessage = '';
    this.studentService.search(this.searchControl.value.trim(), page, this.pageSize).subscribe({
      next: (response) => {
        this.students = response.content;
        this.page = response.page;
        this.pageSize = response.size;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.last = response.last;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Unable to load students. Please retry.';
        this.loading = false;
      }
    });
  }

  previousPage(): void {
    if (this.page > 0) {
      this.loadStudents(this.page - 1);
    }
  }

  nextPage(): void {
    if (!this.last) {
      this.loadStudents(this.page + 1);
    }
  }

  changePageSize(event: Event): void {
    this.pageSize = Number((event.target as HTMLSelectElement).value);
    this.loadStudents(0);
  }

  openDeleteDialog(student: Student): void {
    this.pendingDeleteStudent = student;
  }

  confirmDelete(): void {
    if (!this.pendingDeleteStudent?.id) {
      this.cancelDelete();
      return;
    }

    this.studentService.delete(this.pendingDeleteStudent.id).subscribe({
      next: () => {
        this.pendingDeleteStudent = undefined;
        this.loadStudents(this.page);
      },
      error: () => this.errorMessage = 'Unable to delete student.'
    });
  }

  cancelDelete(): void {
    this.pendingDeleteStudent = undefined;
  }

  majorNames(student: Student): string {
    return student.majors?.map((major) => major.majorName).join(', ') || 'None';
  }

  get displayPage(): number {
    return this.totalPages === 0 ? 0 : this.page + 1;
  }

  get displayTotalPages(): number {
    return this.totalPages;
  }
}
