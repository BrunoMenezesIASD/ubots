import { Attendant } from './attendant.model';

export interface DashboardSummary {
  totalByStatus: Record<string, number>;
  queueSizeByTeam: Record<string, number>;
  attendants: Attendant[];
  totalByTeamAndStatus: Record<string, Record<string, number>>;
}
