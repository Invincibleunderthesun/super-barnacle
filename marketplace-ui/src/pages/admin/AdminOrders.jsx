import { useState, useEffect } from 'react';
import { ordersAPI } from '../../api';

const statusBadge = (s) => ({
  PAYMENT_PENDING: 'badge-pending', CONFIRMED: 'badge-paid', PROCESSING: 'badge-shipped',
  SHIPPED: 'badge-shipped', DELIVERED: 'badge-delivered', CANCELLED: 'badge-cancelled',
}[s] || 'badge-pending');

export default function AdminOrders() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    ordersAPI.getAll(0, 50)
      .then(res => setOrders(res.data?.content || res.data || []))
      .catch(() => setOrders([]))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="loading-screen"><div className="spinner" /></div>;

  return (
    <div>
      <h2 style={{ marginBottom: 'var(--space-lg)' }}>All Orders</h2>
      {orders.length === 0 ? (
        <div className="empty-state"><div className="icon">📋</div><p>No orders yet</p></div>
      ) : (
        <div className="table-container">
          <table>
            <thead>
              <tr><th>Order #</th><th>Customer</th><th>Date</th><th>Items</th><th>Total</th><th>Status</th></tr>
            </thead>
            <tbody>
              {orders.map(o => (
                <tr key={o.id}>
                  <td style={{ fontWeight: 600 }}>#{o.invoiceNumber || o.id}</td>
                  <td className="text-secondary">{o.user?.username || 'N/A'}</td>
                  <td className="text-secondary">{new Date(o.createdAt).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })}</td>
                  <td>{o.items?.length || 0}</td>
                  <td className="price">₹{o.totalAmount?.toLocaleString()}</td>
                  <td><span className={`badge ${statusBadge(o.status)}`}>{o.status?.replace('_', ' ')}</span></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
