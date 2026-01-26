package com.flowpay.atendimento.repository;

import com.flowpay.atendimento.entity.QueueItem;
import com.flowpay.atendimento.enums.Team;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QueueItemRepository extends JpaRepository<QueueItem, Long> {

  long countByTeam(Team team);

  @Query(value = """
      SELECT *
      FROM queue_items q
      WHERE q.team = :team
      ORDER BY q.enqueued_at ASC, q.id ASC
      FOR UPDATE SKIP LOCKED
      LIMIT 1
      """, nativeQuery = true)
  Optional<QueueItem> findNextByTeamForUpdateSkipLocked(@Param("team") String team);
}

