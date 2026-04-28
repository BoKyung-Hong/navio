import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { flightsApi } from '../api/flights';
import type { Flight } from '../types/flight';

export default function FlightDetailPage() {
  const { flightId } = useParams();
  const navigate = useNavigate();
  const [flight, setFlight] = useState<Flight | null>(null);

  useEffect(() => {
    if (!flightId) return;
    flightsApi.detail(Number(flightId)).then((res) => setFlight(res.data));
  }, [flightId]);

  if (!flight) return <p>로딩 중...</p>;

  return (
    <div className="space-y-6">
      <div className="card">
        <div className="text-sm text-slate-500">{flight.airline} · {flight.flightNumber}</div>
        <div className="flex items-center gap-6 mt-4">
          <div>
            <div className="text-3xl font-bold">{flight.departure.airport}</div>
            <div className="text-slate-600">{flight.departure.airportName}</div>
            <div className="text-2xl font-bold mt-2">{formatTime(flight.departure.time)}</div>
          </div>
          <div className="flex-1 text-center">
            <div className="border-t-2 border-dashed border-slate-300 relative">
              <span className="absolute left-1/2 -translate-x-1/2 -translate-y-1/2 bg-white px-2">✈</span>
            </div>
          </div>
          <div>
            <div className="text-3xl font-bold">{flight.arrival.airport}</div>
            <div className="text-slate-600">{flight.arrival.airportName}</div>
            <div className="text-2xl font-bold mt-2">{formatTime(flight.arrival.time)}</div>
          </div>
        </div>
        <div className="mt-4 text-sm text-slate-600">
          체크인 오픈: 출발 {flight.checkin.openMinutesBefore / 60}시간 전 ·
          체크인 마감: 출발 {flight.checkin.closeMinutesBefore}분 전
        </div>
      </div>

      <div className="card">
        <h2 className="text-lg font-semibold mb-4">좌석 등급 선택</h2>
        <div className="space-y-3">
          {flight.seats.map((s) => (
            <button
              key={s.seatClass}
              onClick={() => navigate(`/bookings/new`, { state: { flight, seatClass: s.seatClass } })}
              disabled={s.available === 0}
              className="w-full flex justify-between items-center border border-slate-200 rounded-lg p-4 hover:border-navio-primary disabled:opacity-50"
            >
              <div className="text-left">
                <div className="font-semibold">{seatClassKo(s.seatClass)}</div>
                <div className="text-sm text-slate-500">잔여 {s.available}석</div>
              </div>
              <div className="text-xl font-bold text-navio-primary">
                ₩{s.price.toLocaleString()}
              </div>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}

function formatTime(iso: string) {
  return iso.slice(11, 16);
}
function seatClassKo(c: string) {
  return c === 'ECONOMY' ? '이코노미' : c === 'BUSINESS' ? '비즈니스' : '퍼스트';
}
