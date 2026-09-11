// Stage 4 — Angular 22 shell
// Mirrors contract §4: UserSummary

export interface UserSummary {
  id: string;
  fullName: string;
  email: string;
  department?: string;
  active: boolean;
}
