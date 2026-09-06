import { useState, useEffect } from 'react';
import { usersAPI } from '../../api';

export default function AdminUsers() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    usersAPI.getAll(0, 50)
      .then(res => setUsers(res.data?.content || res.data || []))
      .catch(() => setUsers([]))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="loading-screen"><div className="spinner" /></div>;

  return (
    <div>
      <h2 style={{ marginBottom: 'var(--space-lg)' }}>User Management</h2>
      {users.length === 0 ? (
        <p className="text-secondary">No users found.</p>
      ) : (
        <div className="table-container">
          <table>
            <thead>
              <tr><th>Username</th><th>Email</th><th>Role</th><th>Status</th></tr>
            </thead>
            <tbody>
              {users.map(u => (
                <tr key={u.id}>
                  <td style={{ fontWeight: 600 }}>{u.username}</td>
                  <td className="text-secondary">{u.email}</td>
                  <td><span className={`badge ${u.role === 'ROLE_ADMIN' ? 'badge-shipped' : u.role === 'ROLE_SELLER' ? 'badge-pending' : 'badge-paid'}`}>
                    {u.role?.replace('ROLE_', '')}
                  </span></td>
                  <td><span className={`badge ${u.active !== false ? 'badge-paid' : 'badge-cancelled'}`}>
                    {u.active !== false ? 'Active' : 'Inactive'}
                  </span></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
