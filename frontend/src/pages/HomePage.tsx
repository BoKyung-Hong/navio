import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const AIRPORTS = [
  { code: 'ICN', name: '인천' },
  { code: 'NRT', name: '도쿄(나리타)' },
  { code: 'KIX', name: '오사카(간사이)' },
  { code: 'LAX', name: '로스앤젤레스' },
  { code: 'JFK', name: '뉴욕(JFK)' },
  { code: 'CDG', name: '파리' },
];

function nextDate(offset: number) {
  const d = new Date();
  d.setDate(d.getDate() + offset);
  return d.toISOString().slice(0, 10);
}

export default function HomePage() {
  const navigate = useNavigate();
  const [tripType, setTripType] = useState<'ONE_WAY' | 'ROUND'>('ONE_WAY');
  const [departure, setDeparture] = useState('ICN');
  const [arrival, setArrival] = useState('NRT');
  const [date, setDate] = useState(nextDate(1));
  const [returnDate, setReturnDate] = useState(nextDate(8));

  const handleSearch = () => {
    const params = new URLSearchParams({ departure, arrival, date, tripType });
    if (tripType === 'ROUND') params.set('returnDate', returnDate);
    navigate(`/search?${params.toString()}`);
  };

  const swapAirports = () => {
    setDeparture(arrival);
    setArrival(departure);
  };

  return (
    <div className="space-y-12">
      <section className="text-center py-16">
        <h1 className="text-5xl font-bold text-navio-dark mb-4">
          항로를 탐색하듯, <br /> 예약 과정에서 길을 잃지 않게
        </h1>
        <p className="text-lg text-slate-600">Navio와 함께 명확하고 신뢰할 수 있는 항공 예약을 경험하세요.</p>
      </section>

      <section className="card max-w-4xl mx-auto">
        <div className="flex gap-3 mb-5">
          <button
            onClick={() => setTripType('ONE_WAY')}
            className={`px-4 py-1.5 rounded-full text-sm font-medium border transition-colors ${
              tripType === 'ONE_WAY'
                ? 'bg-navio-primary text-white border-navio-primary'
                : 'border-slate-300 hover:border-navio-primary'
            }`}
          >
            편도
          </button>
          <button
            onClick={() => setTripType('ROUND')}
            className={`px-4 py-1.5 rounded-full text-sm font-medium border transition-colors ${
              tripType === 'ROUND'
                ? 'bg-navio-primary text-white border-navio-primary'
                : 'border-slate-300 hover:border-navio-primary'
            }`}
          >
            왕복
          </button>
        </div>

        <div className={`grid gap-4 ${tripType === 'ROUND' ? 'grid-cols-2 md:grid-cols-5' : 'grid-cols-1 md:grid-cols-4'}`}>
          <div>
            <label className="block text-sm font-medium mb-1">출발</label>
            <select className="input-base" value={departure} onChange={(e) => setDeparture(e.target.value)}>
              {AIRPORTS.map((a) => <option key={a.code} value={a.code}>{a.name} ({a.code})</option>)}
            </select>
          </div>

          <div className="flex items-end gap-1">
            <div className="flex-1">
              <label className="block text-sm font-medium mb-1">도착</label>
              <select className="input-base" value={arrival} onChange={(e) => setArrival(e.target.value)}>
                {AIRPORTS.map((a) => <option key={a.code} value={a.code}>{a.name} ({a.code})</option>)}
              </select>
            </div>
            <button
              onClick={swapAirports}
              title="출발/도착 교환"
              className="mb-0.5 p-2 rounded-lg border border-slate-300 hover:bg-slate-100 text-slate-500 text-xs"
            >
              ⇆
            </button>
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">출발 날짜</label>
            <input type="date" className="input-base" value={date}
              onChange={(e) => setDate(e.target.value)} />
          </div>

          {tripType === 'ROUND' && (
            <div>
              <label className="block text-sm font-medium mb-1">귀국 날짜</label>
              <input type="date" className="input-base" value={returnDate}
                min={date}
                onChange={(e) => setReturnDate(e.target.value)} />
            </div>
          )}

          <div className="flex items-end">
            <button onClick={handleSearch} className="btn-primary w-full">검색</button>
          </div>
        </div>
      </section>
    </div>
  );
}
