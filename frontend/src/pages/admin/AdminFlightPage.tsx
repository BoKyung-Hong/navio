import { useEffect, useState } from 'react';
import { adminApi, type FlightCreatePayload } from '../../api/admin';
import type { Flight } from '../../types/flight';

const AIRPORTS = ['ICN', 'NRT', 'KIX', 'LAX', 'JFK', 'CDG'];

const emptyForm = (): FlightCreatePayload => ({
  flightNumber: '', airline: '',
  departureAirport: 'ICN', arrivalAirport: 'NRT',
  departureTime: '', arrivalTime: '',
  aircraftType: '', checkinOpenMinutes: 1440, checkinCloseMinutes: 60,
  baggageCarryOnKg: 10, baggageCarryOnSize: '55x40x20', baggageCheckedKg: 23,
  economySeats: 180, economyPrice: 0, businessSeats: 24, businessPrice: 0,
});

export default function AdminFlightPage() {
  const [flights, setFlights] = useState<Flight[]>([]);
  const [form, setForm] = useState<FlightCreatePayload>(emptyForm());
  const [editId, setEditId] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = () => adminApi.flights().then((r) => setFlights(r.data));
  useEffect(() => { load(); }, []);

  const set = (patch: Partial<FlightCreatePayload>) => setForm((f) => ({ ...f, ...patch }));

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true); setError(null);
    try {
      if (editId != null) await adminApi.updateFlight(editId, form);
      else await adminApi.createFlight(form);
      setForm(emptyForm()); setEditId(null);
      await load();
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : '저장 실패');
    } finally { setLoading(false); }
  };

  const handleEdit = (f: Flight) => {
    setEditId(f.id);
    setForm({
      flightNumber: f.flightNumber, airline: f.airline,
      departureAirport: f.departure.airport, arrivalAirport: f.arrival.airport,
      departureTime: f.departure.time.slice(0, 16),
      arrivalTime: f.arrival.time.slice(0, 16),
      aircraftType: f.aircraftType,
      checkinOpenMinutes: f.checkin.openMinutesBefore,
      checkinCloseMinutes: f.checkin.closeMinutesBefore,
      baggageCarryOnKg: f.baggage.carryOnKg, baggageCarryOnSize: f.baggage.carryOnSize,
      baggageCheckedKg: f.baggage.checkedKg,
      economySeats: f.seats.find(s => s.seatClass === 'ECONOMY')?.available ?? 180,
      economyPrice: f.seats.find(s => s.seatClass === 'ECONOMY')?.price ?? 0,
      businessSeats: f.seats.find(s => s.seatClass === 'BUSINESS')?.available ?? 24,
      businessPrice: f.seats.find(s => s.seatClass === 'BUSINESS')?.price ?? 0,
    });
  };

  const handleDelete = async (id: number) => {
    if (!confirm('삭제하시겠습니까?')) return;
    try { await adminApi.deleteFlight(id); await load(); }
    catch (err: unknown) { alert(err instanceof Error ? err.message : '삭제 실패'); }
  };

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">항공편 관리</h1>

      <form onSubmit={handleSubmit} className="card space-y-4">
        <h2 className="text-lg font-semibold">{editId ? '항공편 수정' : '항공편 추가'}</h2>
        <div className="grid grid-cols-2 gap-3">
          <div><label className="block text-sm mb-1">편명</label>
            <input className="input-base" required value={form.flightNumber}
              onChange={(e) => set({ flightNumber: e.target.value.toUpperCase() })} /></div>
          <div><label className="block text-sm mb-1">항공사</label>
            <input className="input-base" required value={form.airline}
              onChange={(e) => set({ airline: e.target.value })} /></div>
          <div><label className="block text-sm mb-1">출발 공항</label>
            <select className="input-base" value={form.departureAirport}
              onChange={(e) => set({ departureAirport: e.target.value })}>
              {AIRPORTS.map(a => <option key={a}>{a}</option>)}
            </select></div>
          <div><label className="block text-sm mb-1">도착 공항</label>
            <select className="input-base" value={form.arrivalAirport}
              onChange={(e) => set({ arrivalAirport: e.target.value })}>
              {AIRPORTS.map(a => <option key={a}>{a}</option>)}
            </select></div>
          <div><label className="block text-sm mb-1">출발 일시</label>
            <input type="datetime-local" className="input-base" required value={form.departureTime}
              onChange={(e) => set({ departureTime: e.target.value })} /></div>
          <div><label className="block text-sm mb-1">도착 일시</label>
            <input type="datetime-local" className="input-base" required value={form.arrivalTime}
              onChange={(e) => set({ arrivalTime: e.target.value })} /></div>
          <div><label className="block text-sm mb-1">기종</label>
            <input className="input-base" required value={form.aircraftType}
              onChange={(e) => set({ aircraftType: e.target.value })} /></div>
          <div><label className="block text-sm mb-1">이코노미 가격 (원)</label>
            <input type="number" className="input-base" required value={form.economyPrice}
              onChange={(e) => set({ economyPrice: +e.target.value })} /></div>
          <div><label className="block text-sm mb-1">이코노미 좌석 수</label>
            <input type="number" className="input-base" required value={form.economySeats}
              onChange={(e) => set({ economySeats: +e.target.value })} /></div>
          <div><label className="block text-sm mb-1">비즈니스 가격 (원)</label>
            <input type="number" className="input-base" required value={form.businessPrice}
              onChange={(e) => set({ businessPrice: +e.target.value })} /></div>
          <div><label className="block text-sm mb-1">비즈니스 좌석 수</label>
            <input type="number" className="input-base" required value={form.businessSeats}
              onChange={(e) => set({ businessSeats: +e.target.value })} /></div>
        </div>
        {error && <p className="text-red-600 text-sm">{error}</p>}
        <div className="flex gap-3">
          <button type="submit" disabled={loading} className="btn-primary">
            {loading ? '저장 중...' : editId ? '수정 저장' : '추가'}
          </button>
          {editId && <button type="button" onClick={() => { setEditId(null); setForm(emptyForm()); }}
            className="btn-secondary">취소</button>}
        </div>
      </form>

      <div className="space-y-3">
        {flights.map((f) => (
          <div key={f.id} className="card flex items-center justify-between">
            <div>
              <div className="font-semibold">{f.airline} {f.flightNumber}</div>
              <div className="text-sm text-slate-500">
                {f.departure.airport} → {f.arrival.airport} · {f.departure.time.slice(0, 16)}
              </div>
              <div className="text-sm text-slate-500">
                이코노미 ₩{f.seats.find(s => s.seatClass === 'ECONOMY')?.price?.toLocaleString()}
                · 비즈니스 ₩{f.seats.find(s => s.seatClass === 'BUSINESS')?.price?.toLocaleString()}
              </div>
            </div>
            <div className="flex gap-2">
              <button onClick={() => handleEdit(f)} className="btn-secondary text-sm">수정</button>
              <button onClick={() => handleDelete(f.id)}
                className="text-red-600 border border-red-300 rounded-lg px-3 py-1.5 text-sm hover:bg-red-50">삭제</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
