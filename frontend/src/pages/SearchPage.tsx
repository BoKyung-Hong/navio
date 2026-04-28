import { useEffect, useMemo, useState } from 'react';
import { Link, useSearchParams, useNavigate } from 'react-router-dom';
import { flightsApi } from '../api/flights';
import type { Flight } from '../types/flight';

type SortKey = 'time_asc' | 'price_asc' | 'price_desc';

const SORT_LABELS: Record<SortKey, string> = {
  time_asc: '출발 빠른순',
  price_asc: '가격 낮은순',
  price_desc: '가격 높은순',
};

export default function SearchPage() {
  const [params] = useSearchParams();
  const navigate = useNavigate();

  const departure = params.get('departure') ?? '';
  const arrival = params.get('arrival') ?? '';
  const date = params.get('date') ?? '';
  const returnDate = params.get('returnDate') ?? '';
  const tripType = params.get('tripType') ?? 'ONE_WAY';
  const isRound = tripType === 'ROUND' && !!returnDate;

  const [outboundFlights, setOutboundFlights] = useState<Flight[]>([]);
  const [returnFlights, setReturnFlights] = useState<Flight[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 필터/정렬 상태
  const [sortKey, setSortKey] = useState<SortKey>('time_asc');
  const [airlineFilter, setAirlineFilter] = useState('');

  useEffect(() => {
    if (!departure || !arrival || !date) {
      navigate('/');
      return;
    }
    setLoading(true);
    setError(null);

    const outboundReq = flightsApi.search({ departure, arrival, date });
    const returnReq = isRound
      ? flightsApi.search({ departure: arrival, arrival: departure, date: returnDate })
      : Promise.resolve(null);

    Promise.all([outboundReq, returnReq])
      .then(([out, ret]) => {
        setOutboundFlights(out.data);
        setReturnFlights(ret ? ret.data : []);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [departure, arrival, date, returnDate, isRound]);

  // 유니크 항공사 목록
  const airlines = useMemo(() => {
    const all = [...outboundFlights, ...returnFlights].map((f) => f.airline);
    return Array.from(new Set(all)).sort();
  }, [outboundFlights, returnFlights]);

  const applyFiltersAndSort = (list: Flight[]) => {
    let result = airlineFilter ? list.filter((f) => f.airline === airlineFilter) : list;
    return result.sort((a, b) => {
      const priceA = a.seats.find((s) => s.seatClass === 'ECONOMY')?.price ?? 0;
      const priceB = b.seats.find((s) => s.seatClass === 'ECONOMY')?.price ?? 0;
      if (sortKey === 'price_asc') return priceA - priceB;
      if (sortKey === 'price_desc') return priceB - priceA;
      return a.departure.time.localeCompare(b.departure.time);
    });
  };

  const filteredOutbound = useMemo(() => applyFiltersAndSort(outboundFlights), [outboundFlights, sortKey, airlineFilter]);
  const filteredReturn = useMemo(() => applyFiltersAndSort(returnFlights), [returnFlights, sortKey, airlineFilter]);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">
          {departure} → {arrival}
          {isRound && <span className="text-slate-400 mx-2">·</span>}
          {isRound && <span className="text-lg font-normal text-slate-500">{arrival} → {departure}</span>}
        </h1>
        <p className="text-slate-500 text-sm mt-1">
          {isRound ? `가는 날 ${date} · 오는 날 ${returnDate}` : date}
        </p>
      </div>

      {/* 필터/정렬 바 */}
      <div className="flex flex-wrap gap-3 items-center">
        <div className="flex gap-1">
          {(Object.keys(SORT_LABELS) as SortKey[]).map((k) => (
            <button
              key={k}
              onClick={() => setSortKey(k)}
              className={`px-3 py-1 rounded-full text-xs border ${
                sortKey === k
                  ? 'bg-navio-primary text-white border-navio-primary'
                  : 'border-slate-300 hover:border-navio-primary'
              }`}
            >
              {SORT_LABELS[k]}
            </button>
          ))}
        </div>

        {airlines.length > 1 && (
          <select
            className="input-base text-sm py-1 w-auto"
            value={airlineFilter}
            onChange={(e) => setAirlineFilter(e.target.value)}
          >
            <option value="">항공사 전체</option>
            {airlines.map((a) => <option key={a} value={a}>{a}</option>)}
          </select>
        )}
      </div>

      {loading && <p className="text-slate-500">검색 중...</p>}
      {error && <p className="text-red-600">{error}</p>}

      {!loading && (
        <>
          {isRound ? (
            <>
              <FlightSection
                title={`가는 편 · ${departure} → ${arrival} (${date})`}
                flights={filteredOutbound}
              />
              <FlightSection
                title={`오는 편 · ${arrival} → ${departure} (${returnDate})`}
                flights={filteredReturn}
              />
            </>
          ) : (
            <>
              {filteredOutbound.length === 0 && <p className="text-slate-500">검색 결과가 없습니다.</p>}
              <div className="space-y-4">
                {filteredOutbound.map((f) => <FlightCard key={f.id} flight={f} />)}
              </div>
            </>
          )}
        </>
      )}
    </div>
  );
}

function FlightSection({ title, flights }: { title: string; flights: Flight[] }) {
  return (
    <div>
      <h2 className="text-base font-semibold text-slate-700 mb-3 border-b pb-2">{title}</h2>
      {flights.length === 0
        ? <p className="text-slate-500 text-sm">해당 구간 항공편이 없습니다.</p>
        : <div className="space-y-3">{flights.map((f) => <FlightCard key={f.id} flight={f} />)}</div>
      }
    </div>
  );
}

function FlightCard({ flight }: { flight: Flight }) {
  const economy = flight.seats.find((s) => s.seatClass === 'ECONOMY');
  const business = flight.seats.find((s) => s.seatClass === 'BUSINESS');
  return (
    <div className="card flex items-center justify-between">
      <div className="flex-1">
        <div className="flex items-center gap-3 text-sm text-slate-500">
          <span className="font-semibold text-navio-primary">{flight.airline}</span>
          <span>{flight.flightNumber}</span>
          {flight.aircraftType && <span className="text-xs">{flight.aircraftType}</span>}
        </div>
        <div className="flex items-center gap-6 mt-2">
          <div>
            <div className="text-2xl font-bold">{formatTime(flight.departure.time)}</div>
            <div className="text-sm text-slate-500">{flight.departure.airport}</div>
          </div>
          <div className="flex-1 border-t-2 border-dashed border-slate-300 relative">
            <span className="absolute left-1/2 -translate-x-1/2 -translate-y-1/2 bg-white px-2 text-xs text-slate-400">✈</span>
          </div>
          <div>
            <div className="text-2xl font-bold">{formatTime(flight.arrival.time)}</div>
            <div className="text-sm text-slate-500">{flight.arrival.airport}</div>
          </div>
        </div>
      </div>
      <div className="ml-6 text-right space-y-1">
        {economy && (
          <div>
            <div className="text-xs text-slate-500">이코노미</div>
            <div className="text-lg font-bold text-navio-primary">₩{economy.price.toLocaleString()}</div>
            <div className="text-xs text-slate-500">잔여 {economy.available}석</div>
          </div>
        )}
        {business && (
          <div className="text-xs text-slate-500">
            비즈니스 ₩{business.price.toLocaleString()}
          </div>
        )}
        <Link to={`/flights/${flight.id}`} className="btn-primary mt-1 text-sm block">
          선택
        </Link>
      </div>
    </div>
  );
}

function formatTime(iso: string) {
  return iso.slice(11, 16);
}
