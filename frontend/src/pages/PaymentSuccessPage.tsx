import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { paymentsApi } from '../api/payments';

export default function PaymentSuccessPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [status, setStatus] = useState<'loading' | 'done' | 'error'>('loading');
  const [errorMsg, setErrorMsg] = useState('');

  useEffect(() => {
    const paymentKey = searchParams.get('paymentKey') ?? '';
    const orderId = searchParams.get('orderId') ?? '';
    const amount = Number(searchParams.get('amount') ?? '0');

    if (!paymentKey || !orderId || !amount) {
      setErrorMsg('잘못된 접근입니다.');
      setStatus('error');
      return;
    }

    paymentsApi
      .confirm({ paymentKey, orderId, amount })
      .then(() => setStatus('done'))
      .catch((e: Error) => {
        setErrorMsg(e.message ?? '결제 확인에 실패했습니다.');
        setStatus('error');
      });
  }, [searchParams]);

  if (status === 'loading') {
    return (
      <div className="text-center mt-20">
        <p className="text-lg text-slate-600">결제 확인 중...</p>
      </div>
    );
  }

  if (status === 'error') {
    return (
      <div className="card max-w-md mx-auto text-center">
        <div className="text-4xl mb-4">❌</div>
        <h1 className="text-xl font-bold mb-2">결제 확인 실패</h1>
        <p className="text-slate-600 mb-6">{errorMsg}</p>
        <button onClick={() => navigate('/')} className="btn-primary">홈으로</button>
      </div>
    );
  }

  return (
    <div className="card max-w-md mx-auto text-center">
      <div className="text-5xl mb-4">✅</div>
      <h1 className="text-2xl font-bold mb-2">결제 완료!</h1>
      <p className="text-slate-600 mb-6">예약이 확정되었습니다.</p>
      <div className="flex gap-3 justify-center">
        <button onClick={() => navigate('/my/bookings')} className="btn-primary">내 예약 확인</button>
        <button onClick={() => navigate('/')} className="btn-secondary">홈으로</button>
      </div>
    </div>
  );
}
