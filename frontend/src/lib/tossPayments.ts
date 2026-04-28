import { loadPaymentWidget, PaymentWidgetInstance } from '@tosspayments/payment-widget-sdk';

export type { PaymentWidgetInstance };

export async function initPaymentWidget(clientKey: string, customerKey: string): Promise<PaymentWidgetInstance> {
  return loadPaymentWidget(clientKey, customerKey);
}
