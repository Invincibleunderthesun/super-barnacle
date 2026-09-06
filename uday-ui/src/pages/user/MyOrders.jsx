import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { ordersAPI } from '../../api';
import { useAuth } from '../../context/AuthContext';
import Icon from '../../components/Icon';

const statusBadge = (status) => {
  const map = {
    PAYMENT_PENDING: 'badge-pending', CONFIRMED: 'badge-paid', PROCESSING: 'badge-shipped',
    SHIPPED: 'badge-shipped', DELIVERED: 'badge-delivered', CANCELLED: 'badge-cancelled',
  };
  return map[status] || 'badge-pending';
};

export default function MyOrders() {
  const { user } = useAuth();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user) return;
    ordersAPI.getByUser(user.id)
      .then(res => setOrders(res.data || []))
      .catch(() => setOrders([]))
      .finally(() => setLoading(false));
  }, [user]);

  if (loading) return <div className="loading-screen"><div className="spinner" /></div>;

  return (
    <div>
      <h2 style={{ marginBottom: 'var(--space-lg)' }}>My Orders</h2>

      {orders.length === 0 ? (
        <div className="empty-state">
          <span className="icon"><Icon name="file" size={32} /></span>
          <p>No orders yet</p>
          <Link to="/catalog" className="btn btn-primary" style={{ marginTop: 'var(--space-md)' }}>Start Shopping</Link>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-md)' }}>
          {orders.map(order => (
            <div key={order.id} className="card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 'var(--space-md)' }}>
              <div>
                <p style={{ fontWeight: 600 }}>Order #{order.invoiceNumber || order.id}</p>
                <p className="text-secondary text-sm">
                  {new Date(order.createdAt).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })}
                  {' · '}{order.items?.length || 0} items
                </p>
              </div>
              <span className={`badge ${statusBadge(order.status)}`}>{order.status?.replace('_', ' ')}</span>
              <p className="price">₹{order.totalAmount?.toLocaleString()}</p>
              {order.status === 'PAYMENT_PENDING' && (
                <button className="btn btn-danger btn-sm" onClick={async () => {
                  try { await ordersAPI.cancel(order.id, user.id); setOrders(orders.filter(o => o.id !== order.id)); } catch {}
                }}>Cancel</button>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
