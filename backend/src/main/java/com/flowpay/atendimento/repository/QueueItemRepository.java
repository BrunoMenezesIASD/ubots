package com.flowpay.atendimento.repository;

import com.flowpay.atendimento.entity.QueueItem;
import com.flowpay.atendimento.enums.Team;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QueueItemRepository extends JpaRepository<QueueItem, Long> {

  long countByTeam(Team team);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select q from QueueItem q where q.team = :team order by q.enqueuedAt asc, q.id asc")
  Optional<QueueItem> findFirstByTeamForUpdate(@Param("team") Team team);
}
