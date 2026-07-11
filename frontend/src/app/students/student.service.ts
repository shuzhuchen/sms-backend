import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PagedResponse, Student } from './student.model';

@Injectable({ providedIn: 'root' })
export class StudentService {
  private readonly studentsUrl = `${environment.apiBaseUrl}/api/v1/students`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<Student[]> {
    return this.http.get<Student[]>(this.studentsUrl);
  }

  search(query: string, page: number, size: number): Observable<PagedResponse<Student>> {
    return this.http.get<PagedResponse<Student>>(`${this.studentsUrl}/search`, {
      params: {
        query,
        page,
        size
      }
    });
  }

  getById(id: number): Observable<Student> {
    return this.http.get<Student>(`${this.studentsUrl}/${id}`);
  }

  create(student: Student): Observable<Student> {
    return this.http.post<Student>(this.studentsUrl, student);
  }

  update(id: number, student: Student): Observable<Student> {
    return this.http.put<Student>(`${this.studentsUrl}/${id}`, student);
  }

  delete(id: number): Observable<string> {
    return this.http.delete(`${this.studentsUrl}/${id}`, { responseType: 'text' });
  }
}
