package com.navio.domain.user;

import com.navio.common.BusinessException;
import com.navio.common.ErrorCode;
import com.navio.domain.booking.BookingRepository;
import com.navio.domain.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * UserService
 *
 * 사용자 계정 관련 비즈니스 로직 (비밀번호 변경, 회원 탈퇴).
 *
 * changePassword():
 *   - 현재 비밀번호 BCrypt 검증 → 새 비밀번호 인코딩 후 교체
 *   - 현재 비밀번호 불일치 시 WRONG_PASSWORD 예외 (400)
 *
 * deleteAccount():
 *   - 비밀번호로 본인 확인 → 결제·예약 데이터 순서대로 삭제 → 사용자 삭제
 *   - 삭제 순서: Payment → Booking (Passenger cascade) → User
 *
 * 관련: User, UserRepository, BookingRepository, PaymentRepository
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = findUser(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.WRONG_PASSWORD);
        }
        user.changePassword(passwordEncoder.encode(newPassword));
    }

    @Transactional
    public void deleteAccount(Long userId, String password) {
        User user = findUser(userId);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.WRONG_PASSWORD);
        }
        List<com.navio.domain.booking.Booking> bookings =
                bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
        for (com.navio.domain.booking.Booking booking : bookings) {
            paymentRepository.findByBookingId(booking.getId())
                    .ifPresent(paymentRepository::delete);
        }
        bookingRepository.deleteAll(bookings);
        userRepository.delete(user);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
    }
}
