package com.flowpay.atendimento.entity;

import com.flowpay.atendimento.enums.Team;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter @Setter
@Builder @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "attendants")
public class Attendant {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 120)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private Team team;

  @Column(nullable = false)
  private boolean active = true;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();
}
