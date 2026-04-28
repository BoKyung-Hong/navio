package com.navio.domain.alarm;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * AlarmSchedule (엔티티)
 *
 * 예약별 알람 발송 스케줄을 저장하는 JPA 엔티티. DB 테이블명: alarm_schedules
 *
 * 예약 생성 시 AlarmService.register()에 의해 6종류 알람이 일괄 등록된다.
 * AlarmSenderScheduler가 1분마다 scheduledAt이 지난 미발송 알람을 조회해 이메일을 발송한다.
 *
 * 필드:
 *   - bookingId   : 연결된 예약 ID (Booking.id)
 *   - email       : 수신자 이메일 (예약 시점 사용자 이메일)
 *   - alarmType   : 알람 종류 (AlarmType enum)
 *   - scheduledAt : 발송 예정 시각
 *   - sent        : 발송 완료 여부
 *   - sentAt      : 실제 발송 시각
 *
 * 인덱스: sent + scheduledAt (스케줄러 쿼리 최적화)
 *
 * 관련: AlarmType, AlarmService, AlarmSenderScheduler
 */
@Entity
@Table(name = "alarm_schedules",
       indexes = @Index(name = "idx_alarm_pending", columnList = "sent,scheduled_at"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AlarmSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(nullable = false, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "alarm_type", nullable = false, length = 20)
    private AlarmType alarmType;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean sent = false;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }

    public void markSent() {
        this.sent = true;
        this.sentAt = LocalDateTime.now();
    }
}
