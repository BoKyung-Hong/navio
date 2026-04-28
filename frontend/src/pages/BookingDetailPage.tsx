import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { bookingsApi } from '../api/bookings';
import type { Booking, BookingStatus } from '../types/booking';
import ChatWidget from '../components/ChatWidget';

const STATUS_LABEL: Record<BookingStatus, string> = {
  PENDING: '결제대기',
  CONFIRMED: '결제완료',
  TICKETED: '발권완료',
  CANCEL_REQUESTED: '취소요청',
  CANCELLED: '취소완료',
  REFUND_PENDING: '환불진행중',
  REFUNDED: '환불완료',
};

const STATUS_COLOR: Record<BookingStatus, string> = {
  PENDING: 'text-yellow-700 bg-yellow-50',
  CONFIRMED: 'text-blue-700 bg-blue-50',
  TICKETED: 'text-green-700 bg-green-50',
  CANCEL_REQUESTED: 'text-orange-700 bg-orange-50',
  CANCELLED: 'text-slate-600 bg-slate-100',
  REFUND_PENDING: 'text-purple-700 bg-purple-50',
  REFUNDED: 'text-slate-700 bg-slate-100',
};

const STATUS_DESC: Partial<Record<BookingStatus, string>> = {
  PENDING: '10분 내에 결제를 완료해주세요.',
  CONFIRMED: '결제가 완료되었습니다.',
  TICKETED: '발권이 완료되었습니다. 탑승 당일 여권을 지참해주세요.',
  CANCEL_REQUESTED: '취소 요청이 접수되었습니다. 환불이 진행됩니다.',
  REFUND_PENDING: '환불이 진행 중입니다. 영업일 3~5일 내 처리됩니다.',
  REFUNDED: '환불이 완료되었습니다.',
};

export default function BookingDetailPage() {
  const { bookingNumber } = useParams<{ bookingNumber: string }>();
  const navigate = useNavigate();
  const [booking, setBooking] = useState<Booking | null>(null);
  const [cancelling, setCancelling] = useState(false);

  useEffect(() => {
    if (!bookingNumber) return;
    bookingsApi.detail(bookingNumber).then((res) => setBooking(res.data));
  }, [bookingNumber]);

  const handleCancel = async () => {
    if (!booking || !confirm('예약을 취소하시겠습니까?')) return;
    setCancelling(true);
    try {
      const res = await bookingsApi.cancel(booking.bookingNumber);
      setBooking(res.data);
    } catch (e: unknown) {
      alert(e instanceof Error ? e.message : '취소에 실패했습니다.');
    } finally {
      setCancelling(false);
    }
  };

  if (!booking) return <p className="text-center mt-12">로딩 중...</p>;

  const canCancel = booking.status === 'PENDING' || booking.status === 'CONFIRMED' || booking.status === 'TICKETED';
  const desc = STATUS_DESC[booking.status];

  return (
    <div className="max-w-2xl mx-auto space-y-4">
      <div className="card">
        <div className="flex justify-between items-start mb-2">
          <h1 className="text-2xl font-bold">예약 상세</h1>
          <span className={`px-3 py-1 rounded-full text-sm font-medium ${STATUS_COLOR[booking.status]}`}>
            {STATUS_LABEL[booking.status]}
          </span>
        </div>
        {desc && (
          <p className="text-sm text-slate-500 mb-4">{desc}</p>
        )}

        <dl className="space-y-3 text-slate-700">
          <Row label="예약번호" value={booking.bookingNumber} />
          <Row label="좌석 등급" value={seatClassKo(booking.seatClass)} />
          <Row label="탑승객" value={`${booking.passengerCount}명`} />
          <Row label="총 결제액" value={`₩${booking.totalPrice.toLocaleString()}`} />
          {booking.status === 'PENDING' && (
            <Row
              label="결제 마감"
              value={new Date(booking.expiresAt).toLocaleString('ko-KR')}
              highlight
            />
          )}
          <Row label="예약일" value={new Date(booking.createdAt).toLocaleString('ko-KR')} />
        </dl>

        <div className="flex gap-3 mt-8">
          <button onClick={() => navigate('/my/bookings')} className="btn-secondary flex-1">
            목록으로
          </button>
          {canCancel && (
            <button
              onClick={handleCancel}
              disabled={cancelling}
              className="flex-1 inline-flex items-center justify-center rounded-lg border border-red-300 px-4 py-2 text-red-600 font-medium hover:bg-red-50 transition-colors disabled:opacity-50"
            >
              {cancelling ? '처리 중...' : '예약 취소'}
            </button>
          )}
        </div>
      </div>

      {/* 탑승 안내 타임라인 */}
      {(booking.status === 'CONFIRMED' || booking.status === 'TICKETED') && (
        <div className="card">
          <h2 className="text-base font-semibold mb-3">알림 발송 예정</h2>
          <div className="space-y-2 text-sm text-slate-600">
            {[
              { label: 'D-7', desc: '출발 7일 전 오전 7시' },
              { label: 'D-3', desc: '출발 3일 전 오전 7시' },
              { label: 'D-1', desc: '출발 1일 전 오전 7시' },
              { label: '체크인 오픈', desc: '체크인 가능 시각' },
              { label: '체크인 마감 2시간 전', desc: '마감 임박 알림' },
              { label: '탑승', desc: '출발 30분 전' },
            ].map((item) => (
              <div key={item.label} className="flex items-center gap-3">
                <span className="w-32 text-xs font-medium text-navio-primary bg-blue-50 rounded px-2 py-0.5">{item.label}</span>
                <span className="text-slate-500">{item.desc}</span>
              </div>
            ))}
          </div>
          <p className="text-xs text-slate-400 mt-3">* 예약 시 입력한 이메일로 자동 발송됩니다.</p>
        </div>
      )}

      {/* AI 여행 도우미 (결제 완료 이후에만 표시) */}
      {(booking.status === 'CONFIRMED' || booking.status === 'TICKETED') && (
        <ChatWidget bookingNumber={booking.bookingNumber} />
      )}
    </div>
  );
}

function Row({ label, value, highlight }: { label: string; value: string; highlight?: boolean }) {
  return (
    <div className="flex border-b border-slate-100 pb-2">
      <dt className="w-28 text-slate-500 text-sm">{label}</dt>
      <dd className={`flex-1 font-medium ${highlight ? 'text-orange-600' : ''}`}>{value}</dd>
    </div>
  );
}

function seatClassKo(c: string) {
  return c === 'ECONOMY' ? '이코노미' : c === 'BUSINESS' ? '비즈니스' : '퍼스트';
}
