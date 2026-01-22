export interface Attendant {
  id: number;
  name: string;
  team: 'CARTOES' | 'EMPRESTIMOS' | 'OUTROS';
  active: boolean;
  activeAssignments: number;
  remainingCapacity: number;
}
