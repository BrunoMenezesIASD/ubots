package com.flowpay.atendimento.repository;

import com.flowpay.atendimento.entity.Attendant;
import com.flowpay.atendimento.enums.Team;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AttendantRepository extends JpaRepository<Attendant, Long> {

  List<Attendant> findByTeamOrderByIdAsc(Team team);

  @Query(value =
      "SELECT a.id " +
      "FROM attendants a " +
      "LEFT JOIN service_requests r " +
      "  ON r.assigned_attendant_id = a.id " +
      " AND r.status = 'ASSIGNED' " +
      "WHERE a.team = :team " +
      "  AND a.active = true " +
      "GROUP BY a.id " +
      "HAVING COUNT(r.id) < 3 " +
      "ORDER BY COUNT(r.id) ASC, a.id ASC " +
      "LIMIT 1 " +
      "FOR UPDATE",
      nativeQuery = true)
  Long lockBestAvailableAttendantId(@Param("team") String team);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select a from Attendant a where a.id = :id")
  Optional<Attendant> findByIdForUpdate(@Param("id") Long id);
}
