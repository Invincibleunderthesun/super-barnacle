import { useState, useEffect } from 'react';
import { sellersAPI, ordersAPI } from '../../api';

export default function AdminOverview() {
  const [stats, setStats] = useState({});
  const [sellers, setSellers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      ordersAPI.getStats().then(res => setStats(res.data || {})).catch(() => {}),
      sellersAPI.getAll(0, 10).then(res => setSellers(res.data?.content || [])).catch(() => {}),
    ]).finally(() => setLoading(false));
  }, []);

  const handleVerify = async (id, verified) => {
    try {
      await sellersAPI.verify(id, verified);
      setSellers(sellers.map(s => s.id === id ? { ...s, verified } : s));
    } catch {}
  };

  if (loading) return <div className="loading-screen"><div className="spinner" /></div>;

  return (
    <div>
      <h2 style={{ marginBottom: 'var(--space-lg)' }}>🔧 Admin Dashboard</h2>

      <div className="grid-4" style={{ marginBottom: 'var(--space-xl)' }}>
        <div className="stat-card">
          <span className="stat-icon">📊</span>
          <span className="stat-label">Total Orders</span>
          <span className="stat-value">{stats.totalOrders || 0}</span>
        </div>
        <div className="stat-card">
          <span className="stat-icon">💰</span>
          <span className="stat-label">Revenue</span>
          <span className="stat-value">₹{(stats.totalRevenue || 0).toLocaleString()}</span>
        </div>
        <div className="stat-card">
          <span className="stat-icon">🏪</span>
          <span className="stat-label">Sellers</span>
          <span className="stat-value">{sellers.length}</span>
        </div>
        <div className="stat-card">
          <span className="stat-icon">📅</span>
          <span className="stat-label">This Month</span>
          <span className="stat-value">{stats.ordersThisMonth || 0}</span>
        </div>
      </div>

      <h3 style={{ marginBottom: 'var(--space-md)' }}>Seller Management</h3>
      {sellers.length === 0 ? (
        <p className="text-secondary">No sellers registered yet.</p>
      ) : (
        <div className="table-container">
          <table>
            <thead>
              <tr><th>Store</th><th>Status</th><th>Commission</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {sellers.map(s => (
                <tr key={s.id}>
                  <td style={{ fontWeight: 600 }}>{s.storeName}</td>
                  <td>
                    <span className={`badge ${s.verified ? 'badge-verified' : 'badge-unverified'}`}>
                      {s.verified ? 'Verified' : 'Pending'}
                    </span>
                  </td>
                  <td className="text-secondary">{s.commissionRate}%</td>
                  <td>
                    {!s.verified ? (
                      <button className="btn btn-primary btn-sm" onClick={() => handleVerify(s.id, true)}>✅ Verify</button>
                    ) : (
                      <button className="btn btn-danger btn-sm" onClick={() => handleVerify(s.id, false)}>❌ Revoke</button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
