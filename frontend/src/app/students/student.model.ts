export interface Major {
  id?: number;
  majorName: string;
}

export interface Student {
  id?: number;
  firstName: string;
  lastName: string;
  email: string;
  majors: Major[];
  enrollmentDate: string;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}
