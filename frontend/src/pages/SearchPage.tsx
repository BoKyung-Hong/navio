import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { flightsApi } from '../api/flights';
import type { Flight } from '../types/flight';

export default function SearchPage() {
  const [params] = useSearchParams();
  const [flights, setFlights] = useState<Flight[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const departure = params.get('departure') ?? '';
  const arrival = params.get('arrival') ?? '';
  const date = params.get('date') ?? '';

  useEffect(() => {
    if (!departure || !arrival || !date) return;
    setLoading(true);
    flightsApi.search({ departure, arrival, date })
      .then((res) => setFlights(res.data))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [departure, arrival, date]);

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">
        {departure} → {arrival} <span className="text-slate-500 text-lg">({date})</span>
      </h1>

      {loading && <p>검색 중...</p>}
      {error && <p className="text-red-600">{error}</p>}
      {!loading && flights.length === 0 && <p>검색 결과가 없습니다.</p>}

      <div className="space-y-4">
        {flights.map((f) => (
          <FlightCard key={f.id} flight={f} />
        ))}
      </div>
    </div>
  );
}

function FlightCard({ flight }: { flight: Flight }) {
  const economy = flight.seats.find((s) => s.seatClass === 'ECONOMY');
  return (
    <div className="card flex items-center justify-between">
      <div className="flex-1">
        <div className="flex items-center gap-3 text-sm text-slate-500">
          <span className="font-semibold text-navio-primary">{flight.airline}</span>
          <span>{flight.flightNumber}</span>
          <span>{flight.aircraftType}</span>
        </div>
        <div className="flex items-center gap-6 mt-2">
          <div>
            <div className="text-2xl font-bold">{formatTime(flight.departure.time)}</div>
            <div className="text-sm text-slate-500">{flight.departure.airport}</div>
          </div>
          <div className="flex-1 border-t-2 border-dashed border-slate-300 relative">
            <span className="absolute left-1/2 -translate-x-1/2 -translate-y-1/2 bg-white px-2 text-xs text-slate-500">
              ✈
            </span>
          </div>
          <div>
            <div className="text-2xl font-bold">{formatTime(flight.arrival.time)}</div>
            <div className="text-sm text-slate-500">{flight.arrival.airport}</div>
          </div>
        </div>
      </div>
      <div className="ml-6 text-right">
        <div className="text-sm text-slate-500">Economy</div>
        <div className="text-xl font-bold text-navio-primary">
          ₩{economy?.price.toLocaleString()}
        </div>
        <div className="text-xs text-slate-500">잔여 {economy?.available}석</div>
        <Link
          to={`/flights/${flight.id}`}
          className="btn-primary mt-2 text-sm"
        >
          선택
        </Link>
      </div>
    </div>
  );
}

function formatTime(iso: string) {
  return iso.slice(11, 16);
}
