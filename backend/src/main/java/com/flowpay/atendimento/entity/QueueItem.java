package com.flowpay.atendimento.entity;

import com.flowpay.atendimento.enums.Team;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter @Setter
@Builder @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "queue_items")
public class QueueItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "service_request_id", nullable = false, unique = true)
  private Long serviceRequestId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private Team team;

  @Column(name = "enqueued_at", nullable = false)
  private Instant enqueuedAt = Instant.now();
}
