package com.navio.domain.alarm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AlarmScheduleRepository
 *
 * AlarmSchedule 엔티티의 JPA 레포지토리.
 *
 * findPendingAlarms : 발송 시각이 지났고 아직 발송되지 않은 알람 조회 (스케줄러 전용)
 * existsByBookingId : 예약에 이미 알람이 등록됐는지 확인 (중복 등록 방지)
 *
 * 관련: AlarmSchedule, AlarmSenderScheduler
 */
public interface AlarmScheduleRepository extends JpaRepository<AlarmSchedule, Long> {

    @Query("SELECT a FROM AlarmSchedule a WHERE a.sent = false AND a.scheduledAt <= :now ORDER BY a.scheduledAt")
    List<AlarmSchedule> findPendingAlarms(@Param("now") LocalDateTime now);

    boolean existsByBookingId(Long bookingId);
}
