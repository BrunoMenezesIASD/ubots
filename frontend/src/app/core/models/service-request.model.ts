export interface ServiceRequest {
  id: number;
  customerName: string;
  subject: string;
  team: 'CARTOES' | 'EMPRESTIMOS' | 'OUTROS';
  status: 'NEW' | 'QUEUED' | 'ASSIGNED' | 'DONE';
  assignedAttendantId?: number | null;
  createdAt: string;
  updatedAt: string;
}
