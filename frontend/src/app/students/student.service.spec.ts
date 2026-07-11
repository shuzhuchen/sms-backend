import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { StudentService } from './student.service';
import { environment } from '../../environments/environment';

describe('StudentService', () => {
  let service: StudentService;
  let httpTestingController: HttpTestingController;
  const studentsUrl = `${environment.apiBaseUrl}/api/v1/students`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        StudentService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(StudentService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTestingController.verify());

  it('loads all students', () => {
    service.getAll().subscribe((students) => expect(students.length).toBe(1));

    const request = httpTestingController.expectOne(studentsUrl);
    expect(request.request.method).toBe('GET');
    request.flush([
      { id: 1, firstName: 'Ada', lastName: 'Lovelace', email: 'ada@example.com', majors: [], enrollmentDate: '2025-01-01' }
    ]);
  });

  it('searches students with pagination', () => {
    service.search('eng', 0, 5).subscribe((response) => expect(response.totalElements).toBe(1));

    const request = httpTestingController.expectOne((req) =>
      req.url === `${studentsUrl}/search`
      && req.params.get('query') === 'eng'
      && req.params.get('page') === '0'
      && req.params.get('size') === '5'
    );
    expect(request.request.method).toBe('GET');
    request.flush({
      content: [
        { id: 1, firstName: 'Ada', lastName: 'Lovelace', email: 'ada@example.com', majors: [], enrollmentDate: '2025-01-01' }
      ],
      page: 0,
      size: 5,
      totalElements: 1,
      totalPages: 1,
      last: true
    });
  });

  it('creates a student', () => {
    const student = { firstName: 'Ada', lastName: 'Lovelace', email: 'ada@example.com', majors: [], enrollmentDate: '2025-01-01' };
    service.create(student).subscribe((created) => expect(created.id).toBe(1));

    const request = httpTestingController.expectOne(studentsUrl);
    expect(request.request.method).toBe('POST');
    request.flush({ id: 1, ...student });
  });
});
