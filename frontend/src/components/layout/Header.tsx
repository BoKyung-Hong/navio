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
          <Link to="/" className="hover:text-navio-primary">항공편 검색</Link>
          {user ? (
            <>
              <Link to="/my/bookings" className="hover:text-navio-primary">내 예약</Link>
              <Link to="/my/profile" className="hover:text-navio-primary">{user.name}님</Link>
              {user.role === 'ADMIN' && (
                <>
                  <Link to="/admin/flights" className="hover:text-navio-primary text-orange-600">항공편 관리</Link>
                  <Link to="/admin/bookings" className="hover:text-navio-primary text-orange-600">예약 관리</Link>
                  <Link to="/admin/users" className="hover:text-navio-primary text-orange-600">사용자 관리</Link>
                </>
              )}
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
