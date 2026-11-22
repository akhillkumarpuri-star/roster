export type Role = 'ADMIN' | 'USER';
export type EmployeeStatus = 'ACTIVE' | 'ON_LEAVE' | 'INACTIVE';

export interface Session { token: string; username: string; fullName: string; role: Role; }
export interface LoginResponse extends Session { expiresInSeconds: number; }

export interface Employee {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  department: string;
  jobTitle: string;
  hireDate: string; // yyyy-MM-dd
  status: EmployeeStatus;
}
export type EmployeeInput = Omit<Employee, 'id'>;
export interface EmployeePage { content: Employee[]; totalElements: number; page: number; size: number; }

export const STATUS_LABEL: Record<EmployeeStatus, string> = {
  ACTIVE: 'Active',
  ON_LEAVE: 'On leave',
  INACTIVE: 'Inactive',
};
export const DEPARTMENTS = ['Engineering', 'Design', 'Product', 'Finance', 'HR', 'Operations', 'Sales'];

export interface DepartmentCount { department: string; count: number; }
export interface ReportSummary {
  from: string; // yyyy-MM-dd
  to: string;
  total: number;
  byStatus: Record<EmployeeStatus, number>;
  byDepartment: DepartmentCount[];
}
export interface ReportFilter { from: string; to: string; status: EmployeeStatus | ''; }
export type SortDir = 'asc' | 'desc';
