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

export default function HomePage() {
  const navigate = useNavigate();
  const [departure, setDeparture] = useState('ICN');
  const [arrival, setArrival] = useState('NRT');
  const [date, setDate] = useState(() => {
    const d = new Date();
    d.setDate(d.getDate() + 1);
    return d.toISOString().slice(0, 10);
  });

  const handleSearch = () => {
    const params = new URLSearchParams({ departure, arrival, date });
    navigate(`/search?${params.toString()}`);
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
        <h2 className="text-xl font-semibold mb-4">항공편 검색</h2>
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div>
            <label className="block text-sm font-medium mb-1">출발</label>
            <select className="input-base" value={departure} onChange={(e) => setDeparture(e.target.value)}>
              {AIRPORTS.map((a) => <option key={a.code} value={a.code}>{a.name} ({a.code})</option>)}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">도착</label>
            <select className="input-base" value={arrival} onChange={(e) => setArrival(e.target.value)}>
              {AIRPORTS.map((a) => <option key={a.code} value={a.code}>{a.name} ({a.code})</option>)}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">출발 날짜</label>
            <input type="date" className="input-base" value={date} onChange={(e) => setDate(e.target.value)} />
          </div>
          <div className="flex items-end">
            <button onClick={handleSearch} className="btn-primary w-full">검색</button>
          </div>
        </div>
      </section>
    </div>
  );
}
