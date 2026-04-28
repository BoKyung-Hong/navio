import { useEffect, useState } from 'react';
import { adminApi, type AdminUser } from '../../api/admin';

export default function AdminUserPage() {
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  const load = () => {
    setLoading(true);
    adminApi.users()
      .then((r) => setUsers(r.data))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const handleRoleToggle = async (user: AdminUser) => {
    const newRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN';
    if (!confirm(`${user.email}의 역할을 ${newRole}로 변경하시겠습니까?`)) return;
    try {
      await adminApi.updateUserRole(user.id, newRole);
      load();
    } catch (err: unknown) {
      alert(err instanceof Error ? err.message : '변경 실패');
    }
  };

  const filtered = users.filter((u) =>
    u.email.includes(search) || u.name.includes(search)
  );

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">사용자 관리</h1>
        <span className="text-sm text-slate-500">총 {users.length}명</span>
      </div>

      <input
        className="input-base max-w-sm"
        placeholder="이메일 또는 이름 검색"
        value={search}
        onChange={(e) => setSearch(e.target.value)}
      />

      {loading ? <p>로딩 중...</p> : (
        <div className="space-y-2">
          {filtered.length === 0 && <p className="text-slate-500">사용자가 없습니다.</p>}
          {filtered.map((u) => (
            <div key={u.id} className="card flex items-center justify-between">
              <div>
                <div className="font-medium">{u.name}</div>
                <div className="text-sm text-slate-500">{u.email}</div>
                <div className="text-xs text-slate-400">
                  {u.phone || '전화번호 없음'} · 가입 {new Date(u.createdAt).toLocaleDateString('ko-KR')}
                </div>
              </div>
              <div className="flex items-center gap-3">
                <span className={`text-xs font-semibold px-2 py-1 rounded-full ${
                  u.role === 'ADMIN'
                    ? 'bg-orange-100 text-orange-700'
                    : 'bg-slate-100 text-slate-600'
                }`}>
                  {u.role === 'ADMIN' ? '관리자' : '일반'}
                </span>
                <button
                  onClick={() => handleRoleToggle(u)}
                  className="text-xs border border-slate-300 rounded px-2 py-1 hover:bg-slate-50"
                >
                  {u.role === 'ADMIN' ? '일반으로 변경' : '관리자로 변경'}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
