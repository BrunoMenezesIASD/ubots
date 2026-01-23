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

    @Query(value = """
        SELECT a.id
        FROM attendants a
        LEFT JOIN LATERAL (
          SELECT count(*) AS cnt
          FROM service_requests r
          WHERE r.assigned_attendant_id = a.id
            AND r.status = 'ASSIGNED'
        ) w ON true
        WHERE a.team = :team
          AND a.active = true
          AND w.cnt < 3
        ORDER BY w.cnt ASC, a.id ASC
        LIMIT 1
        FOR UPDATE OF a SKIP LOCKED
        """, nativeQuery = true)
    Optional<Long> lockBestAvailableAttendantId(@Param("team") String team);

    @Query("select a from Attendant a where a.id = :id")
    Optional<Attendant> findByIdForUpdate(@Param("id") Long id);
}
