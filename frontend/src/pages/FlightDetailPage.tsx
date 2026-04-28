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

  const checkinOpenTime = new Date(flight.departure.time);
  checkinOpenTime.setMinutes(checkinOpenTime.getMinutes() - flight.checkin.openMinutesBefore);
  const checkinCloseTime = new Date(flight.departure.time);
  checkinCloseTime.setMinutes(checkinCloseTime.getMinutes() - flight.checkin.closeMinutesBefore);

  return (
    <div className="space-y-6">
      {/* 항공편 기본 정보 */}
      <div className="card">
        <div className="text-sm text-slate-500">{flight.airline} · {flight.flightNumber} · {flight.aircraftType}</div>
        <div className="flex items-center gap-6 mt-4">
          <div>
            <div className="text-3xl font-bold">{flight.departure.airport}</div>
            <div className="text-slate-600">{flight.departure.airportName}</div>
            <div className="text-2xl font-bold mt-2">{formatTime(flight.departure.time)}</div>
            <div className="text-xs text-slate-400 mt-1">{flight.departure.timezone}</div>
          </div>
          <div className="flex-1 text-center">
            <div className="border-t-2 border-dashed border-slate-300 relative">
              <span className="absolute left-1/2 -translate-x-1/2 -translate-y-1/2 bg-white px-2">✈</span>
            </div>
          </div>
          <div className="text-right">
            <div className="text-3xl font-bold">{flight.arrival.airport}</div>
            <div className="text-slate-600">{flight.arrival.airportName}</div>
            <div className="text-2xl font-bold mt-2">{formatTime(flight.arrival.time)}</div>
            <div className="text-xs text-slate-400 mt-1">{flight.arrival.timezone}</div>
          </div>
        </div>
      </div>

      {/* 체크인 정보 */}
      <div className="card">
        <h2 className="text-lg font-semibold mb-3">체크인 안내</h2>
        <div className="grid grid-cols-2 gap-4">
          <div className="bg-blue-50 rounded-lg p-4">
            <div className="text-xs text-blue-500 font-medium mb-1">체크인 오픈</div>
            <div className="font-bold text-blue-800">{formatDateTime(checkinOpenTime)}</div>
            <div className="text-xs text-blue-600 mt-1">출발 {flight.checkin.openMinutesBefore / 60}시간 전부터 가능</div>
          </div>
          <div className="bg-red-50 rounded-lg p-4">
            <div className="text-xs text-red-500 font-medium mb-1">체크인 마감</div>
            <div className="font-bold text-red-800">{formatDateTime(checkinCloseTime)}</div>
            <div className="text-xs text-red-600 mt-1">출발 {flight.checkin.closeMinutesBefore}분 전 마감</div>
          </div>
        </div>
      </div>

      {/* 수하물 규정 */}
      <div className="card">
        <h2 className="text-lg font-semibold mb-3">수하물 규정</h2>
        <div className="grid grid-cols-2 gap-4">
          <div className="border border-slate-200 rounded-lg p-4">
            <div className="flex items-center gap-2 mb-2">
              <span className="text-xl">🎒</span>
              <span className="font-semibold text-sm">기내 수하물</span>
            </div>
            <div className="text-2xl font-bold text-navio-primary">{flight.baggage.carryOnKg}kg</div>
            <div className="text-sm text-slate-500 mt-1">{flight.baggage.carryOnSize} cm 이하</div>
          </div>
          <div className="border border-slate-200 rounded-lg p-4">
            <div className="flex items-center gap-2 mb-2">
              <span className="text-xl">🧳</span>
              <span className="font-semibold text-sm">위탁 수하물</span>
            </div>
            <div className="text-2xl font-bold text-navio-primary">{flight.baggage.checkedKg}kg</div>
            <div className="text-sm text-slate-500 mt-1">1개 포함</div>
          </div>
        </div>
      </div>

      {/* 좌석 등급 선택 */}
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
                <div className="text-sm text-slate-500">
                  {s.available > 0 ? `잔여 ${s.available}석` : '매진'}
                </div>
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
function formatDateTime(d: Date) {
  return d.toLocaleString('ko-KR', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' });
}
function seatClassKo(c: string) {
  return c === 'ECONOMY' ? '이코노미' : c === 'BUSINESS' ? '비즈니스' : '퍼스트';
}
