package com.flowpay.atendimento.entity;

import com.flowpay.atendimento.enums.ServiceRequestStatus;
import com.flowpay.atendimento.enums.Team;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter @Setter
@Builder @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "service_requests")
public class ServiceRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "customer_name", nullable = false, length = 120)
  private String customerName;

  @Column(nullable = false, length = 200)
  private String subject;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private Team team;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private ServiceRequestStatus status;

  @Column(name = "assigned_attendant_id")
  private Long assignedAttendantId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  @Version
  @Column(nullable = false)
  private Long version;
}
