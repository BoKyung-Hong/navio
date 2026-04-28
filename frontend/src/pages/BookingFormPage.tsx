import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { bookingsApi } from '../api/bookings';
import type { Flight } from '../types/flight';
import type { PassengerInput } from '../types/booking';

interface LocationState {
  flight: Flight;
  seatClass: string;
}

export default function BookingFormPage() {
  const navigate = useNavigate();
  const state = useLocation().state as LocationState | null;
  const [passengers, setPassengers] = useState<PassengerInput[]>([
    { nameEnglish: '', birthDate: '', gender: 'MALE', passportNumber: '', nationality: '' },
  ]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!state) {
    return <p>잘못된 접근입니다. 항공편을 먼저 선택해주세요.</p>;
  }

  const { flight, seatClass } = state;
  const seatInfo = flight.seats.find((s) => s.seatClass === seatClass);
  const totalPrice = (seatInfo?.price ?? 0) * passengers.length;

  const updatePassenger = (i: number, patch: Partial<PassengerInput>) => {
    setPassengers((prev) => prev.map((p, idx) => (idx === i ? { ...p, ...patch } : p)));
  };

  const addPassenger = () => {
    setPassengers((prev) => [...prev, { nameEnglish: '', birthDate: '', gender: 'MALE', passportNumber: '', nationality: '' }]);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const res = await bookingsApi.create({
        flightId: flight.id,
        seatClass,
        passengers,
      });
      navigate(`/payment/${res.data.bookingNumber}`, { state: { booking: res.data, flight } });
    } catch (err: any) {
      setError(err.message ?? '예약 생성에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6 max-w-2xl mx-auto">
      <div className="card">
        <h2 className="text-lg font-semibold">선택한 항공편</h2>
        <div className="mt-2 text-slate-700">
          {flight.airline} {flight.flightNumber} · {flight.departure.airport} → {flight.arrival.airport} · {seatClass}
        </div>
      </div>

      <form onSubmit={handleSubmit} className="card space-y-4">
        <h2 className="text-lg font-semibold">탑승객 정보</h2>
        {passengers.map((p, i) => (
          <div key={i} className="border border-slate-200 rounded-lg p-4 space-y-3">
            <div className="font-medium">탑승객 {i + 1}</div>
            <div>
              <label className="block text-sm mb-1">영문 이름 (여권 표기)</label>
              <input className="input-base" required placeholder="HONG GILDONG"
                value={p.nameEnglish}
                onChange={(e) => updatePassenger(i, { nameEnglish: e.target.value.toUpperCase() })} />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-sm mb-1">생년월일</label>
                <input type="date" className="input-base" required
                  value={p.birthDate}
                  onChange={(e) => updatePassenger(i, { birthDate: e.target.value })} />
              </div>
              <div>
                <label className="block text-sm mb-1">성별</label>
                <select className="input-base" value={p.gender}
                  onChange={(e) => updatePassenger(i, { gender: e.target.value as 'MALE' | 'FEMALE' })}>
                  <option value="MALE">남자</option>
                  <option value="FEMALE">여자</option>
                </select>
              </div>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-sm mb-1">여권번호 (국제선)</label>
                <input className="input-base" placeholder="M12345678"
                  value={p.passportNumber ?? ''}
                  onChange={(e) => updatePassenger(i, { passportNumber: e.target.value.toUpperCase() })} />
              </div>
              <div>
                <label className="block text-sm mb-1">국적 (ISO 3166-1)</label>
                <input className="input-base" placeholder="KOR"
                  maxLength={3}
                  value={p.nationality ?? ''}
                  onChange={(e) => updatePassenger(i, { nationality: e.target.value.toUpperCase() })} />
              </div>
            </div>
          </div>
        ))}

        <button type="button" onClick={addPassenger} className="btn-secondary w-full">
          + 탑승객 추가
        </button>

        <div className="border-t pt-4 flex justify-between items-center">
          <span className="text-slate-600">총 결제 예정 금액</span>
          <span className="text-2xl font-bold text-navio-primary">
            ₩{totalPrice.toLocaleString()}
          </span>
        </div>

        {error && <p className="text-red-600">{error}</p>}
        <button type="submit" disabled={loading} className="btn-primary w-full">
          {loading ? '예약 생성 중...' : '예약하고 결제하기'}
        </button>
      </form>
    </div>
  );
}
