import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { bookingsApi } from '../api/bookings';
import type { Booking } from '../types/booking';

const STATUS_LABEL: Record<Booking['status'], string> = {
  PENDING: '결제대기',
  CONFIRMED: '예약확정',
  CANCELLED: '취소',
  REFUNDED: '환불완료',
};

const STATUS_COLOR: Record<Booking['status'], string> = {
  PENDING: 'text-yellow-700 bg-yellow-50',
  CONFIRMED: 'text-green-700 bg-green-50',
  CANCELLED: 'text-slate-600 bg-slate-100',
  REFUNDED: 'text-blue-700 bg-blue-50',
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

  const canCancel = booking.status === 'PENDING' || booking.status === 'CONFIRMED';

  return (
    <div className="card max-w-2xl mx-auto">
      <div className="flex justify-between items-start mb-6">
        <h1 className="text-2xl font-bold">예약 상세</h1>
        <span className={`px-3 py-1 rounded-full text-sm font-medium ${STATUS_COLOR[booking.status]}`}>
          {STATUS_LABEL[booking.status]}
        </span>
      </div>

      <dl className="space-y-3 text-slate-700">
        <Row label="예약번호" value={booking.bookingNumber} />
        <Row label="좌석 등급" value={booking.seatClass} />
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
            {cancelling ? '취소 중...' : '예약 취소'}
          </button>
        )}
      </div>
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
