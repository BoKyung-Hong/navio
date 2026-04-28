package com.navio.domain.alarm;

import com.navio.domain.booking.Booking;
import com.navio.domain.flight.Flight;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AlarmService
 *
 * 예약 생성 시 알람 스케줄 등록 서비스.
 *
 * register():
 *   항공편 출발 시각 기준으로 6종류 알람을 계산해 alarm_schedules 테이블에 저장.
 *   - D7  : 출발 7일 전 오전 7시
 *   - D3  : 출발 3일 전 오전 7시
 *   - D1  : 출발 1일 전 오전 7시
 *   - CHECKIN_OPEN : 체크인 오픈 시각
 *   - CHECKIN_CLOSE: 체크인 마감 2시간 전
 *   - BOARDING     : 탑승 시작 (출발 30분 전)
 *   이미 지난 시각은 등록 생략.
 *
 * 관련: AlarmSchedule, AlarmSenderScheduler, BookingService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmScheduleRepository alarmScheduleRepository;

    @Transactional
    public void register(Booking booking, Flight flight, String userEmail) {
        if (alarmScheduleRepository.existsByBookingId(booking.getId())) {
            log.debug("[AlarmService] Already registered for bookingId={}", booking.getId());
            return;
        }

        LocalDateTime dep = flight.getDepartureTime();
        LocalDateTime now = LocalDateTime.now();
        List<AlarmSchedule> alarms = new ArrayList<>();

        addIfFuture(alarms, booking.getId(), userEmail, AlarmType.D7,
                dep.minusDays(7).withHour(7).withMinute(0).withSecond(0), now);
        addIfFuture(alarms, booking.getId(), userEmail, AlarmType.D3,
                dep.minusDays(3).withHour(7).withMinute(0).withSecond(0), now);
        addIfFuture(alarms, booking.getId(), userEmail, AlarmType.D1,
                dep.minusDays(1).withHour(7).withMinute(0).withSecond(0), now);
        addIfFuture(alarms, booking.getId(), userEmail, AlarmType.CHECKIN_OPEN,
                dep.minusMinutes(flight.getCheckinOpenMinutes()), now);
        addIfFuture(alarms, booking.getId(), userEmail, AlarmType.CHECKIN_CLOSE,
                dep.minusMinutes(flight.getCheckinCloseMinutes()).minusHours(2), now);
        addIfFuture(alarms, booking.getId(), userEmail, AlarmType.BOARDING,
                dep.minusMinutes(30), now);

        if (!alarms.isEmpty()) {
            alarmScheduleRepository.saveAll(alarms);
            log.info("[AlarmService] Registered {} alarms for bookingId={}", alarms.size(), booking.getId());
        }
    }

    private void addIfFuture(List<AlarmSchedule> list, Long bookingId, String email,
                              AlarmType type, LocalDateTime scheduledAt, LocalDateTime now) {
        if (scheduledAt.isAfter(now)) {
            list.add(AlarmSchedule.builder()
                    .bookingId(bookingId)
                    .email(email)
                    .alarmType(type)
                    .scheduledAt(scheduledAt)
                    .build());
        }
    }
}
