package com.navio.domain.flight;

import jakarta.persistence.*;
import lombok.*;

/**
 * Airport (엔티티)
 *
 * 공항 정보를 저장하는 JPA 엔티티. DB 테이블명: airports
 *
 * 필드:
 *   - code     : IATA 3자리 코드 (PK, 예: ICN, NRT, LAX)
 *   - nameKo   : 한국어 공항명 (예: 인천국제공항)
 *   - nameEn   : 영문 공항명 (예: Incheon Intl)
 *   - city     : 도시명
 *   - country  : 국가 코드 (KR, JP, US, FR)
 *   - timezone : IANA 시간대 (예: Asia/Seoul, America/Los_Angeles)
 *
 * 현지 시간 변환에 timezone 필드를 사용한다.
 * DataInitializer가 시드 데이터(6개 공항)를 주입한다.
 *
 * 관련: Flight, FlightSearchService, FlightResponse
 */
@Entity
@Table(name = "airports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Airport {

    @Id
    @Column(length = 3)
    private String code;

    @Column(name = "name_ko", nullable = false, length = 50)
    private String nameKo;

    @Column(name = "name_en", nullable = false, length = 100)
    private String nameEn;

    @Column(nullable = false, length = 50)
    private String city;

    @Column(nullable = false, length = 50)
    private String country;

    @Column(nullable = false, length = 50)
    private String timezone;
}
