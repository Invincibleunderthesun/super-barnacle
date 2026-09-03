import { useState, useEffect } from 'react';
import { sellersAPI } from '../../api';
import Icon from '../../components/Icon';

export default function SellerEarnings() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    sellersAPI.getStats()
      .then(res => setStats(res.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="loading-screen"><div className="spinner" /></div>;

  const commission = stats?.commissionRate || 0;

  return (
    <div>
      <h2 style={{ marginBottom: 'var(--space-sm)' }}>Earnings & Payouts</h2>
      <p className="text-secondary" style={{ marginBottom: 'var(--space-lg)' }}>
        Track your store revenue and payouts. Marketplace commission is {commission}%.
      </p>

      <div className="grid-3" style={{ marginBottom: 'var(--space-xl)' }}>
        <div className="stat-card accent-stat">
          <span className="stat-icon"><Icon name="coins" size={20} /></span>
          <span className="stat-label">Total Gross Sales</span>
          <span className="stat-value">₹{stats?.totalGrossSales?.toLocaleString() || '0'}</span>
          <span className="stat-hint">Across {stats?.totalProducts || 0} products</span>
        </div>
        <div className="stat-card">
          <span className="stat-icon"><Icon name="trendingUp" size={20} /></span>
          <span className="stat-label">Net Earnings</span>
          <span className="stat-value">₹{stats?.netEarnings?.toLocaleString() || '0'}</span>
          <span className="stat-hint">After {commission}% commission</span>
        </div>
        <div className="stat-card">
          <span className="stat-icon"><Icon name="check" size={20} /></span>
          <span className="stat-label">Paid Out</span>
          <span className="stat-value">₹{stats?.paidOut?.toLocaleString() || '0'}</span>
          <span className="stat-hint">Settled to your bank</span>
        </div>
      </div>

      <div className="card">
        <h3 style={{ marginBottom: 'var(--space-md)' }}>Payout History</h3>
        <div className="empty-state" style={{ padding: 'var(--space-xl)' }}>
          <span className="icon"><Icon name="creditCard" size={32} /></span>
          <p>No payouts yet. Earnings will appear here once your orders are delivered.</p>
          <p className="text-muted text-sm" style={{ marginTop: '0.5rem' }}>
            Payouts are settled automatically to your registered bank account.
          </p>
        </div>
      </div>
    </div>
  );
}
