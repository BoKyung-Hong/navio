package com.navio.domain.alarm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AlarmSenderScheduler
 *
 * 1분마다 발송 대기 알람을 조회해 이메일을 발송하는 스케줄러.
 *
 * 동작:
 *   1. alarm_schedules 중 sent=false, scheduledAt <= now 조회
 *   2. 각 알람 타입에 맞는 이메일 제목·본문 생성
 *   3. JavaMailSender로 발송
 *   4. AlarmSchedule.markSent() 호출 → sent=true, sentAt=now
 *   5. 발송 실패 시 로그만 기록 (재시도는 다음 1분 뒤)
 *
 * 관련: AlarmSchedule, AlarmType, AlarmService
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmSenderScheduler {

    private final AlarmScheduleRepository alarmScheduleRepository;
    private final JavaMailSender mailSender;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void sendPendingAlarms() {
        List<AlarmSchedule> pending = alarmScheduleRepository.findPendingAlarms(LocalDateTime.now());
        if (pending.isEmpty()) return;

        log.info("[AlarmSender] Processing {} alarms", pending.size());

        for (AlarmSchedule alarm : pending) {
            try {
                send(alarm);
                alarm.markSent();
            } catch (Exception e) {
                log.error("[AlarmSender] Failed to send alarm id={} type={}: {}", alarm.getId(), alarm.getAlarmType(), e.getMessage());
            }
        }
    }

    private void send(AlarmSchedule alarm) throws Exception {
        String subject = buildSubject(alarm.getAlarmType());
        String body = buildBody(alarm.getAlarmType());

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
        helper.setTo(alarm.getEmail());
        helper.setSubject("[Navio] " + subject);
        helper.setText(body, true);
        mailSender.send(message);
    }

    private String buildSubject(AlarmType type) {
        return switch (type) {
            case D7 -> "출발 7일 전! 여행 준비를 시작하세요";
            case D3 -> "출발 3일 전! 짐 싸기 시작할 시간이에요";
            case D1 -> "내일 출발! 체크인 시간을 확인하세요";
            case CHECKIN_OPEN -> "온라인 체크인이 오픈되었습니다";
            case CHECKIN_CLOSE -> "⚠️ 체크인 마감 2시간 전 알림";
            case BOARDING -> "탑승 게이트가 오픈되었습니다";
        };
    }

    private String buildBody(AlarmType type) {
        String content = switch (type) {
            case D7 -> "출발까지 7일 남았습니다. 여권, 비자, 숙소를 다시 한번 확인해보세요.";
            case D3 -> "출발까지 3일 남았습니다. 짐 싸기를 시작하고 수하물 규정을 확인하세요.";
            case D1 -> "내일 출발입니다! 체크인 오픈 시간을 확인하고 공항 이동 계획을 세워두세요.";
            case CHECKIN_OPEN -> "지금 온라인 체크인이 가능합니다. Navio 앱에서 바로 체크인하세요!";
            case CHECKIN_CLOSE -> "체크인 마감이 2시간 남았습니다. 아직 체크인을 안 하셨다면 지금 바로 진행해주세요!";
            case BOARDING -> "탑승 게이트가 오픈되었습니다. 게이트로 이동해주세요. 탑승구를 확인하는 것 잊지 마세요!";
        };
        return "<div style='font-family:sans-serif;padding:20px'>"
                + "<h2 style='color:#1e40af'>✈️ Navio 탑승 알림</h2>"
                + "<p>" + content + "</p>"
                + "<hr/><p style='color:#64748b;font-size:12px'>Navio 항공 예약 서비스</p>"
                + "</div>";
    }
}
