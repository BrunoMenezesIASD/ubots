import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { Subscription } from 'rxjs';

import { KpiCardComponent } from '../../shared/components/kpi-card/kpi-card.component';
import { DashboardService } from '../../core/services/dashboard.service';
import { ServiceRequestService } from '../../core/services/service-request.service';
import { AttendantService } from '../../core/services/attendant.service';
import { ServiceRequest } from '../../core/models/service-request.model';
import { Attendant } from '../../core/models/attendant.model';
import { DashboardSummary } from '../../core/models/dashboard.model';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule, ReactiveFormsModule,
    MatCardModule, MatButtonModule, MatTableModule, MatSelectModule, MatFormFieldModule,
    KpiCardComponent
  ],
  templateUrl: './dashboard.page.html',
  styleUrl: './dashboard.page.scss'
})
export class DashboardPage implements OnInit, OnDestroy {

  summary: DashboardSummary | null = null;

  requests: ServiceRequest[] = [];
  attendants: Attendant[] = [];

  teamCtrl = new FormControl<string>('');
  statusCtrl = new FormControl<string>('');

  private sub = new Subscription();

  displayedRequestsColumns = ['id','customerName','subject','team','status','assignedAttendantId','actions'];
  displayedAttendantsColumns = ['id','name','team','active','activeAssignments','remainingCapacity','actions'];

  constructor(
    private dashboardService: DashboardService,
    private requestService: ServiceRequestService,
    private attendantService: AttendantService
  ) {}

  ngOnInit(): void {
    this.dashboardService.load();
    this.sub.add(this.dashboardService.summary$.subscribe(s => {
      this.summary = s;
      // refresh lists opportunistically (small dataset)
      this.loadRequests();
      this.loadAttendants();
    }));
    this.sub.add(this.dashboardService.connectStream());
    this.loadRequests();
    this.loadAttendants();
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  loadRequests() {
    const team = this.teamCtrl.value || undefined;
    const status = this.statusCtrl.value || undefined;
    this.requestService.list(team, status).subscribe(list => this.requests = list);
  }

  loadAttendants() {
    this.attendantService.list().subscribe(list => this.attendants = list);
  }

  finishRequest(id: number) {
    this.requestService.finish(id).subscribe(() => {
      this.dashboardService.load();
    });
  }

  toggleAttendant(a: Attendant) {
    this.attendantService.toggleActive(a.id, !a.active).subscribe(() => {
      this.dashboardService.load();
    });
  }
}
