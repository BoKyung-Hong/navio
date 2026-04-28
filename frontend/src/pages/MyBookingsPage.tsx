import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { bookingsApi } from '../api/bookings';
import type { Booking } from '../types/booking';

export default function MyBookingsPage() {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    bookingsApi.myBookings()
      .then((res) => setBookings(res.data))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p>로딩 중...</p>;

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">내 예약</h1>
      {bookings.length === 0 ? (
        <p className="text-slate-500">아직 예약 내역이 없습니다.</p>
      ) : (
        <div className="space-y-4">
          {bookings.map((b) => (
            <Link key={b.bookingNumber} to={`/my/bookings/${b.bookingNumber}`} className="card block hover:border-navio-primary">
              <div className="flex justify-between items-center">
                <div>
                  <div className="text-sm text-slate-500">{b.bookingNumber}</div>
                  <div className="text-lg font-semibold mt-1">
                    {b.seatClass} · 탑승객 {b.passengerCount}명
                  </div>
                </div>
                <div className="text-right">
                  <StatusBadge status={b.status} />
                  <div className="text-lg font-bold mt-1">₩{b.totalPrice.toLocaleString()}</div>
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}

function StatusBadge({ status }: { status: Booking['status'] }) {
  const colors = {
    PENDING: 'bg-yellow-100 text-yellow-800',
    CONFIRMED: 'bg-green-100 text-green-800',
    CANCELLED: 'bg-slate-100 text-slate-600',
    REFUNDED: 'bg-blue-100 text-blue-800',
  };
  const labels = { PENDING: '결제대기', CONFIRMED: '예약확정', CANCELLED: '취소', REFUNDED: '환불완료' };
  return <span className={`px-2 py-1 rounded text-xs ${colors[status]}`}>{labels[status]}</span>;
}
