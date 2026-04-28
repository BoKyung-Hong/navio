import { useEffect, useState } from 'react';
import { adminApi } from '../../api/admin';
import type { Booking, BookingStatus } from '../../types/booking';

const STATUS_LABELS: Record<BookingStatus, string> = {
  PENDING: '결제대기', CONFIRMED: '결제완료', TICKETED: '발권완료',
  CANCEL_REQUESTED: '취소요청', CANCELLED: '취소완료',
  REFUND_PENDING: '환불진행중', REFUNDED: '환불완료',
};
const ALL_STATUSES = Object.keys(STATUS_LABELS) as BookingStatus[];

export default function AdminBookingPage() {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [filter, setFilter] = useState('');
  const [loading, setLoading] = useState(true);

  const load = (status?: string) => {
    setLoading(true);
    adminApi.allBookings(status || undefined)
      .then((r) => setBookings(r.data))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const handleFilterChange = (s: string) => {
    setFilter(s);
    load(s || undefined);
  };

  const handleStatusChange = async (bookingNumber: string, status: string) => {
    try {
      await adminApi.updateBookingStatus(bookingNumber, status);
      load(filter || undefined);
    } catch (err: unknown) {
      alert(err instanceof Error ? err.message : '변경 실패');
    }
  };

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">예약 관리</h1>

      <div className="flex gap-2 flex-wrap">
        <button onClick={() => handleFilterChange('')}
          className={`px-3 py-1 rounded-full text-sm border ${!filter ? 'bg-navio-primary text-white border-navio-primary' : 'border-slate-300'}`}>
          전체
        </button>
        {ALL_STATUSES.map(s => (
          <button key={s} onClick={() => handleFilterChange(s)}
            className={`px-3 py-1 rounded-full text-sm border ${filter === s ? 'bg-navio-primary text-white border-navio-primary' : 'border-slate-300'}`}>
            {STATUS_LABELS[s]}
          </button>
        ))}
      </div>

      {loading ? <p>로딩 중...</p> : (
        <div className="space-y-3">
          {bookings.length === 0 && <p className="text-slate-500">예약이 없습니다.</p>}
          {bookings.map((b) => (
            <div key={b.bookingNumber} className="card">
              <div className="flex justify-between items-start mb-2">
                <div>
                  <div className="font-semibold">{b.bookingNumber}</div>
                  <div className="text-sm text-slate-500">
                    {b.seatClass} · {b.passengerCount}명 · ₩{b.totalPrice.toLocaleString()}
                  </div>
                  <div className="text-xs text-slate-400">{new Date(b.createdAt).toLocaleString('ko-KR')}</div>
                </div>
                <span className="text-sm font-medium px-2 py-1 bg-slate-100 rounded">
                  {STATUS_LABELS[b.status]}
                </span>
              </div>
              <div className="flex gap-2 flex-wrap mt-2">
                {ALL_STATUSES.filter(s => s !== b.status).map(s => (
                  <button key={s} onClick={() => handleStatusChange(b.bookingNumber, s)}
                    className="text-xs border border-slate-300 rounded px-2 py-1 hover:bg-slate-50">
                    → {STATUS_LABELS[s]}
                  </button>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
