package com.navio.domain.alarm;

/**
 * AlarmType
 *
 * 스마트 알람의 발송 시점 유형.
 *
 * 각 타입별 발송 기준:
 *   D7           : 출발 7일 전 오전 7시
 *   D3           : 출발 3일 전 오전 7시
 *   D1           : 출발 1일 전 오전 7시
 *   CHECKIN_OPEN : 체크인 오픈 시각 (출발 - checkinOpenMinutes)
 *   CHECKIN_CLOSE: 체크인 마감 2시간 전 (출발 - checkinCloseMinutes - 2h)
 *   BOARDING     : 탑승 시작 (출발 30분 전)
 *
 * 관련: AlarmSchedule, AlarmService, AlarmSenderScheduler
 */
public enum AlarmType {
    D7,
    D3,
    D1,
    CHECKIN_OPEN,
    CHECKIN_CLOSE,
    BOARDING
}
