import { Routes, Route, Navigate } from 'react-router-dom';
import Header from './components/layout/Header';
import PrivateRoute from './components/PrivateRoute';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import SearchPage from './pages/SearchPage';
import FlightDetailPage from './pages/FlightDetailPage';
import BookingFormPage from './pages/BookingFormPage';
import PaymentPage from './pages/PaymentPage';
import PaymentSuccessPage from './pages/PaymentSuccessPage';
import PaymentFailPage from './pages/PaymentFailPage';
import MyBookingsPage from './pages/MyBookingsPage';
import BookingDetailPage from './pages/BookingDetailPage';

export default function App() {
  return (
    <div className="min-h-screen bg-slate-50 text-slate-900">
      <Header />
      <main className="mx-auto max-w-6xl px-4 py-8">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route path="/search" element={<SearchPage />} />
          <Route path="/flights/:flightId" element={<FlightDetailPage />} />
          <Route path="/payment/success" element={<PaymentSuccessPage />} />
          <Route path="/payment/fail" element={<PaymentFailPage />} />

          {/* 인증 필요 */}
          <Route path="/bookings/new" element={<PrivateRoute><BookingFormPage /></PrivateRoute>} />
          <Route path="/payment/:bookingNumber" element={<PrivateRoute><PaymentPage /></PrivateRoute>} />
          <Route path="/my/bookings" element={<PrivateRoute><MyBookingsPage /></PrivateRoute>} />
          <Route path="/my/bookings/:bookingNumber" element={<PrivateRoute><BookingDetailPage /></PrivateRoute>} />

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </div>
  );
}
