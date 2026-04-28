import { useEffect, useRef, useState } from 'react';
import { useLocation, useParams } from 'react-router-dom';
import { useAuthStore } from '../hooks/useAuth';
import { loadPaymentWidget, type PaymentWidgetInstance } from '@tosspayments/payment-widget-sdk';

export default function PaymentPage() {
  const { bookingNumber } = useParams<{ bookingNumber: string }>();
  const state = useLocation().state as { booking: { totalPrice: number } } | null;
  const user = useAuthStore((s) => s.user);

  const widgetRef = useRef<PaymentWidgetInstance | null>(null);
  const [ready, setReady] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const totalPrice = state?.booking?.totalPrice ?? 0;
  const clientKey = import.meta.env.VITE_TOSS_CLIENT_KEY as string;

  useEffect(() => {
    if (!user || !bookingNumber || totalPrice === 0) return;

    let mounted = true;
    (async () => {
      try {
        const widget = await loadPaymentWidget(clientKey, String(user.id));
        if (!mounted) return;
        widgetRef.current = widget;

        await Promise.all([
          widget.renderPaymentMethods('#payment-method', { value: totalPrice }),
          widget.renderAgreement('#payment-agreement'),
        ]);
        if (mounted) setReady(true);
      } catch {
        if (mounted) setError('결제 위젯 로드에 실패했습니다. 새로고침 해주세요.');
      }
    })();

    return () => { mounted = false; };
  }, [user, bookingNumber, totalPrice, clientKey]);

  const handlePay = async () => {
    if (!widgetRef.current || !bookingNumber) return;
    setLoading(true);
    setError(null);
    try {
      await widgetRef.current.requestPayment({
        orderId: bookingNumber,
        orderName: 'Navio 항공권 예약',
        successUrl: `${window.location.origin}/payment/success`,
        failUrl: `${window.location.origin}/payment/fail`,
        customerName: user?.name,
        customerEmail: user?.email ?? undefined,
      });
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : '결제 요청에 실패했습니다.');
      setLoading(false);
    }
  };

  if (!state) {
    return <p className="text-center mt-12">잘못된 접근입니다. 예약 페이지에서 다시 진행해주세요.</p>;
  }

  return (
    <div className="max-w-2xl mx-auto space-y-4">
      <div className="card">
        <h1 className="text-2xl font-bold mb-2">결제</h1>
        <div className="text-slate-600">예약번호: <strong>{bookingNumber}</strong></div>
        <div className="text-slate-600 mt-1">
          결제 금액: <strong className="text-navio-primary text-xl">₩{totalPrice.toLocaleString()}</strong>
        </div>
      </div>

      <div className="card">
        {!ready && !error && <p className="text-slate-400 text-sm py-4 text-center">결제 위젯 로딩 중...</p>}
        {error && <p className="text-red-600 text-sm">{error}</p>}
        <div id="payment-method" />
        <div id="payment-agreement" />
      </div>

      <button
        onClick={handlePay}
        disabled={!ready || loading}
        className="btn-primary w-full py-3 text-base"
      >
        {loading ? '결제 처리 중...' : `₩${totalPrice.toLocaleString()} 결제하기`}
      </button>
    </div>
  );
}
