import { useState, useEffect } from 'react';
import { sellersAPI } from '../../api';
import Icon from '../../components/Icon';

export default function SellerOverview() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    sellersAPI.getStats()
      .then(res => setStats(res.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="loading-screen"><div className="spinner" /></div>;

  return (
    <div>
      <h2 style={{ marginBottom: 'var(--space-lg)' }}><Icon name="store" size={20} /> Seller Dashboard</h2>

      <div className="grid-3" style={{ marginBottom: 'var(--space-xl)' }}>
        <div className="stat-card">
          <span className="stat-icon"><Icon name="package" size={20} /></span>
          <span className="stat-label">Total Products</span>
          <span className="stat-value">{stats?.totalProducts || 0}</span>
        </div>
        <div className="stat-card">
          <span className="stat-icon"><Icon name="check" size={20} /></span>
          <span className="stat-label">Verified</span>
          <span className="stat-value">{stats?.verified ? 'Yes' : 'Pending'}</span>
        </div>
        <div className="stat-card">
          <span className="stat-icon"><Icon name="coins" size={20} /></span>
          <span className="stat-label">Commission Rate</span>
          <span className="stat-value">{stats?.commissionRate || 0}%</span>
        </div>
      </div>

      <div className="card">
        <h3 style={{ marginBottom: 'var(--space-md)' }}>Store Info</h3>
        <p><strong>Store:</strong> {stats?.storeName}</p>
        <p className="text-secondary"><strong>Seller ID:</strong> {stats?.sellerId}</p>
      </div>
    </div>
  );
}
