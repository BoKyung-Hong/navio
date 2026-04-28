import { useNavigate, useSearchParams } from 'react-router-dom';

export default function PaymentFailPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const message = searchParams.get('message') ?? '결제가 취소되었거나 실패했습니다.';
  const code = searchParams.get('code');

  return (
    <div className="card max-w-md mx-auto text-center">
      <div className="text-5xl mb-4">⚠️</div>
      <h1 className="text-2xl font-bold mb-2">결제 실패</h1>
      {code && <p className="text-xs text-slate-400 mb-1">코드: {code}</p>}
      <p className="text-slate-600 mb-6">{message}</p>
      <div className="flex gap-3 justify-center">
        <button onClick={() => navigate(-1)} className="btn-primary">다시 시도</button>
        <button onClick={() => navigate('/')} className="btn-secondary">홈으로</button>
      </div>
    </div>
  );
}
