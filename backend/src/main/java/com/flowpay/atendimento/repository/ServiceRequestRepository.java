package com.flowpay.atendimento.repository;

import com.flowpay.atendimento.entity.ServiceRequest;
import com.flowpay.atendimento.enums.ServiceRequestStatus;
import com.flowpay.atendimento.enums.Team;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long>, JpaSpecificationExecutor<ServiceRequest> {

  @Query("select count(r) from ServiceRequest r where r.assignedAttendantId = :attendantId and r.status = :status")
  long countByAttendantAndStatus(@Param("attendantId") Long attendantId, @Param("status") ServiceRequestStatus status);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select r from ServiceRequest r where r.id = :id")
  Optional<ServiceRequest> findByIdForUpdate(@Param("id") Long id);

  @Query("select r.status as status, count(r) as total from ServiceRequest r group by r.status")
  List<Object[]> countGroupedByStatus();

  @Query("select r.team as team, r.status as status, count(r) as total from ServiceRequest r group by r.team, r.status")
  List<Object[]> countGroupedByTeamAndStatus();
}
