import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { addressAPI, ordersAPI, cartAPI } from '../../api';
import { useAuth } from '../../context/AuthContext';

export default function Checkout() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [addresses, setAddresses] = useState([]);
  const [selectedAddress, setSelectedAddress] = useState(null);
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);
  const [placing, setPlacing] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!user) return;
    Promise.all([
      addressAPI.getAll(user.id).then(res => {
        const addrs = res.data || [];
        setAddresses(addrs);
        const def = addrs.find(a => a.default) || addrs[0];
        if (def) setSelectedAddress(def.id);
      }),
      cartAPI.get(user.id).then(res => setCart(res.data)),
    ]).finally(() => setLoading(false));
  }, [user]);

  const handlePlaceOrder = async () => {
    if (!selectedAddress) { setError('Please select a delivery address'); return; }
    setPlacing(true);
    setError('');
    try {
      const res = await ordersAPI.checkout(user.id, selectedAddress);
      navigate(`/dashboard/orders`);
    } catch (err) {
      setError(err.message || 'Checkout failed');
    } finally {
      setPlacing(false);
    }
  };

  if (loading) return <div className="loading-screen"><div className="spinner" /></div>;

  const items = cart?.items || [];
  const subtotal = cart?.totalAmount || 0;
  const gst = subtotal * 0.18;
  const total = subtotal + gst;

  return (
    <div className="container" style={{ padding: 'var(--space-xl) var(--space-lg)', maxWidth: '800px' }}>
      <h1 style={{ marginBottom: 'var(--space-lg)' }}>Checkout</h1>

      {error && <div className="alert alert-error mb-md">⚠️ {error}</div>}

      {/* Address Selection */}
      <div className="card" style={{ marginBottom: 'var(--space-lg)' }}>
        <h3 style={{ marginBottom: 'var(--space-md)' }}>📍 Delivery Address</h3>
        {addresses.length === 0 ? (
          <p className="text-secondary text-sm">No addresses saved. Go to <a href="/dashboard/addresses">My Addresses</a> to add one.</p>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-sm)' }}>
            {addresses.map(addr => (
              <label key={addr.id} style={{
                display: 'flex', alignItems: 'flex-start', gap: 'var(--space-md)',
                padding: 'var(--space-md)', borderRadius: 'var(--radius-md)',
                border: `1px solid ${selectedAddress === addr.id ? 'var(--accent)' : 'var(--border-color)'}`,
                background: selectedAddress === addr.id ? 'rgba(124,58,237,0.05)' : 'transparent',
                cursor: 'pointer',
              }}>
                <input type="radio" name="address" checked={selectedAddress === addr.id}
                  onChange={() => setSelectedAddress(addr.id)} style={{ marginTop: '0.25rem' }} />
                <div>
                  <p style={{ fontWeight: 600 }}>{addr.fullName}</p>
                  <p className="text-secondary text-sm">
                    {addr.addressLine1}{addr.addressLine2 ? `, ${addr.addressLine2}` : ''}, {addr.city}, {addr.state} - {addr.pincode}
                  </p>
                  <p className="text-secondary text-sm">📞 {addr.phone}</p>
                </div>
              </label>
            ))}
          </div>
        )}
      </div>

      {/* Order Summary */}
      <div className="card">
        <h3 style={{ marginBottom: 'var(--space-md)' }}>📋 Order Summary</h3>
        {items.map(item => (
          <div key={item.id} style={{
            display: 'flex', justifyContent: 'space-between', padding: 'var(--space-sm) 0',
            borderBottom: '1px solid var(--border-color)',
          }}>
            <span>{item.product?.name} × {item.quantity}</span>
            <span>₹{(item.product?.price * item.quantity).toLocaleString()}</span>
          </div>
        ))}
        <div style={{ display: 'flex', justifyContent: 'space-between', padding: 'var(--space-sm) 0', color: 'var(--text-secondary)' }}>
          <span>Subtotal</span><span>₹{subtotal.toLocaleString()}</span>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', padding: 'var(--space-sm) 0', color: 'var(--text-secondary)' }}>
          <span>GST (18%)</span><span>₹{gst.toFixed(2)}</span>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', padding: 'var(--space-md) 0', fontWeight: 700, fontSize: '1.2rem', borderTop: '1px solid var(--border-color)' }}>
          <span>Total</span><span className="price">₹{total.toFixed(2)}</span>
        </div>

        <button className="btn btn-primary btn-lg" style={{ width: '100%', marginTop: 'var(--space-md)' }}
          onClick={handlePlaceOrder} disabled={placing || !selectedAddress}>
          {placing ? 'Placing order...' : '🛒 Place Order'}
        </button>
      </div>
    </div>
  );
}
