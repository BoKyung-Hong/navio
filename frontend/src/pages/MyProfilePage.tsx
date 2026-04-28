import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../api/auth';
import { useAuthStore } from '../hooks/useAuth';

export default function MyProfilePage() {
  const navigate = useNavigate();
  const { user, setAuth, accessToken, refreshToken, clear } = useAuthStore();

  // 기본 정보
  const [name, setName] = useState(user?.name ?? '');
  const [phone, setPhone] = useState(user?.phone ?? '');
  const [infoLoading, setInfoLoading] = useState(false);
  const [infoSuccess, setInfoSuccess] = useState(false);
  const [infoError, setInfoError] = useState<string | null>(null);

  // 비밀번호 변경
  const [currentPw, setCurrentPw] = useState('');
  const [newPw, setNewPw] = useState('');
  const [newPwConfirm, setNewPwConfirm] = useState('');
  const [pwLoading, setPwLoading] = useState(false);
  const [pwSuccess, setPwSuccess] = useState(false);
  const [pwError, setPwError] = useState<string | null>(null);

  // 회원 탈퇴
  const [deletePw, setDeletePw] = useState('');
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

  useEffect(() => {
    authApi.me().then((res) => {
      setName(res.data.name);
      setPhone(res.data.phone ?? '');
    });
  }, []);

  const handleInfoSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setInfoLoading(true);
    setInfoError(null);
    setInfoSuccess(false);
    try {
      const res = await authApi.updateMe({ name, phone });
      if (user && accessToken && refreshToken) {
        setAuth({ ...user, name: res.data.name, phone: res.data.phone }, accessToken, refreshToken);
      }
      setInfoSuccess(true);
    } catch (err: unknown) {
      setInfoError(err instanceof Error ? err.message : '수정에 실패했습니다.');
    } finally {
      setInfoLoading(false);
    }
  };

  const handlePasswordSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (newPw !== newPwConfirm) {
      setPwError('새 비밀번호가 일치하지 않습니다.');
      return;
    }
    if (newPw.length < 8) {
      setPwError('새 비밀번호는 8자 이상이어야 합니다.');
      return;
    }
    setPwLoading(true);
    setPwError(null);
    setPwSuccess(false);
    try {
      await authApi.changePassword({ currentPassword: currentPw, newPassword: newPw });
      setPwSuccess(true);
      setCurrentPw('');
      setNewPw('');
      setNewPwConfirm('');
    } catch (err: unknown) {
      setPwError(err instanceof Error ? err.message : '비밀번호 변경에 실패했습니다.');
    } finally {
      setPwLoading(false);
    }
  };

  const handleDeleteAccount = async () => {
    if (!deletePw) return;
    setDeleteLoading(true);
    setDeleteError(null);
    try {
      await authApi.deleteAccount(deletePw);
      clear();
      navigate('/');
    } catch (err: unknown) {
      setDeleteError(err instanceof Error ? err.message : '탈퇴에 실패했습니다.');
    } finally {
      setDeleteLoading(false);
    }
  };

  return (
    <div className="max-w-md mx-auto space-y-6">
      {/* 기본 정보 */}
      <div className="card">
        <h1 className="text-xl font-bold mb-1">내 정보 수정</h1>
        <p className="text-sm text-slate-500 mb-4">{user?.email}</p>
        <form onSubmit={handleInfoSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium mb-1">이름</label>
            <input className="input-base" required value={name} onChange={(e) => setName(e.target.value)} />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">전화번호</label>
            <input type="tel" className="input-base" value={phone} onChange={(e) => setPhone(e.target.value)} />
          </div>
          {infoError && <p className="text-sm text-red-600">{infoError}</p>}
          {infoSuccess && <p className="text-sm text-green-600">정보가 수정되었습니다.</p>}
          <button type="submit" disabled={infoLoading} className="btn-primary w-full">
            {infoLoading ? '저장 중...' : '저장'}
          </button>
        </form>
      </div>

      {/* 비밀번호 변경 */}
      <div className="card">
        <h2 className="text-lg font-bold mb-4">비밀번호 변경</h2>
        <form onSubmit={handlePasswordSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium mb-1">현재 비밀번호</label>
            <input type="password" className="input-base" required value={currentPw}
              onChange={(e) => setCurrentPw(e.target.value)} />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">새 비밀번호 (8자 이상)</label>
            <input type="password" className="input-base" required value={newPw}
              onChange={(e) => setNewPw(e.target.value)} />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">새 비밀번호 확인</label>
            <input type="password" className="input-base" required value={newPwConfirm}
              onChange={(e) => setNewPwConfirm(e.target.value)} />
          </div>
          {pwError && <p className="text-sm text-red-600">{pwError}</p>}
          {pwSuccess && <p className="text-sm text-green-600">비밀번호가 변경되었습니다.</p>}
          <button type="submit" disabled={pwLoading} className="btn-primary w-full">
            {pwLoading ? '변경 중...' : '비밀번호 변경'}
          </button>
        </form>
      </div>

      {/* 회원 탈퇴 */}
      <div className="card border-red-200">
        <h2 className="text-lg font-bold text-red-600 mb-2">회원 탈퇴</h2>
        <p className="text-sm text-slate-500 mb-4">탈퇴 시 모든 예약 및 결제 데이터가 삭제되며 복구할 수 없습니다.</p>
        {!showDeleteConfirm ? (
          <button
            onClick={() => setShowDeleteConfirm(true)}
            className="text-sm border border-red-300 text-red-600 rounded-lg px-4 py-2 hover:bg-red-50"
          >
            탈퇴하기
          </button>
        ) : (
          <div className="space-y-3">
            <div>
              <label className="block text-sm font-medium mb-1">비밀번호 확인</label>
              <input type="password" className="input-base border-red-300" value={deletePw}
                onChange={(e) => setDeletePw(e.target.value)}
                placeholder="비밀번호를 입력해 확인하세요" />
            </div>
            {deleteError && <p className="text-sm text-red-600">{deleteError}</p>}
            <div className="flex gap-2">
              <button
                onClick={handleDeleteAccount}
                disabled={deleteLoading || !deletePw}
                className="bg-red-600 text-white rounded-lg px-4 py-2 text-sm hover:bg-red-700 disabled:opacity-50"
              >
                {deleteLoading ? '처리 중...' : '탈퇴 확인'}
              </button>
              <button
                onClick={() => { setShowDeleteConfirm(false); setDeletePw(''); setDeleteError(null); }}
                className="btn-secondary text-sm"
              >
                취소
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
