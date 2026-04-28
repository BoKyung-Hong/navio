import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../hooks/useAuth';
import { authApi } from '../../api/auth';

export default function Header() {
  const { user, clear } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await authApi.logout();
    } catch {
      // 토큰 만료 등 오류가 있어도 로컬 상태는 초기화
    } finally {
      clear();
      navigate('/');
    }
  };

  return (
    <header className="border-b border-slate-200 bg-white">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-4">
        <Link to="/" className="text-2xl font-bold text-navio-primary">
          ✈️ Navio
        </Link>
        <nav className="flex items-center gap-4 text-sm">
          <Link to="/search" className="hover:text-navio-primary">항공편 검색</Link>
          {user ? (
            <>
              <Link to="/my/bookings" className="hover:text-navio-primary">내 예약</Link>
              <span className="text-slate-500">{user.name}님</span>
              <button onClick={handleLogout} className="hover:text-navio-primary">로그아웃</button>
            </>
          ) : (
            <>
              <Link to="/login" className="hover:text-navio-primary">로그인</Link>
              <Link to="/signup" className="btn-primary text-sm">회원가입</Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
