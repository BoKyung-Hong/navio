package com.navio.domain.booking;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Passenger (엔티티)
 *
 * 예약에 포함된 탑승객 정보를 저장하는 JPA 엔티티. DB 테이블명: passengers
 *
 * Booking과 ManyToOne 관계 (bookings 테이블의 FK).
 * 예약 생성 시 함께 저장되고, 예약 삭제 시 cascade로 함께 삭제된다.
 *
 * 필드:
 *   - nameKorean    : 한국어 이름 (선택)
 *   - nameEnglish   : 영문 이름 (여권 표기, 필수)
 *   - birthDate     : 생년월일
 *   - gender        : 성별 (MALE | FEMALE)
 *   - passportNumber: 여권 번호 (Phase 2에서 활성화 예정)
 *   - nationality   : 국적 코드 (Phase 2에서 활성화 예정)
 *
 * 관련: Booking, BookingService, CreateBookingRequest.PassengerInput
 */
@Entity
@Table(name = "passengers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "name_korean", length = 50)
    private String nameKorean;

    @Column(name = "name_english", nullable = false, length = 100)
    private String nameEnglish;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false, length = 10)
    private String gender;

    @Column(name = "passport_number", length = 20)
    private String passportNumber;

    @Column(length = 3)
    private String nationality;
}
