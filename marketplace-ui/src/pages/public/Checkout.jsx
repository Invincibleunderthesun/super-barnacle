import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { addressAPI, ordersAPI, cartAPI } from '../../api';
import { useAuth } from '../../context/AuthContext';
import Icon from '../../components/Icon';

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
      addressAPI.getAll().then(res => {
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
      await ordersAPI.checkout(user.id, selectedAddress);
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
    <div className="container" style={{ padding: 'var(--space-xl) var(--space-lg)', maxWidth: '820px' }}>
      <h1 className="page-title-cart" style={{ marginBottom: 'var(--space-lg)' }}>
        <Icon name="creditCard" size={22} /> Checkout
      </h1>

      {error && <div className="alert alert-error mb-md"><Icon name="info" size={16} /> {error}</div>}

      <div className="card" style={{ marginBottom: 'var(--space-lg)' }}>
        <h3 className="card-title"><Icon name="mapPin" size={18} /> Delivery address</h3>
        {addresses.length === 0 ? (
          <p className="text-muted text-sm">
            No addresses saved. <Link to="/dashboard/addresses">Add an address</Link> to continue.
          </p>
        ) : (
          <div className="address-list">
            {addresses.map(addr => (
              <label key={addr.id} className={`address-option ${selectedAddress === addr.id ? 'selected' : ''}`}>
                <input
                  type="radio"
                  name="address"
                  checked={selectedAddress === addr.id}
                  onChange={() => setSelectedAddress(addr.id)}
                />
                <div>
                  <p className="address-name">{addr.fullName}</p>
                  <p className="text-muted text-sm">
                    {addr.addressLine1}{addr.addressLine2 ? `, ${addr.addressLine2}` : ''}, {addr.city}, {addr.state} - {addr.pincode}
                  </p>
                  <p className="text-muted text-sm"><Icon name="user" size={13} /> {addr.phone}</p>
                </div>
              </label>
            ))}
          </div>
        )}
      </div>

      <div className="card">
        <h3 className="card-title"><Icon name="file" size={18} /> Order summary</h3>
        <div className="order-lines">
          {items.map(item => (
            <div key={item.id} className="order-line">
              <span>{item.product?.name} <span className="text-muted">× {item.quantity}</span></span>
              <span>₹{Number(item.product?.price * item.quantity || 0).toLocaleString('en-IN')}</span>
            </div>
          ))}
        </div>
        <div className="order-line muted"><span>Subtotal</span><span>₹{subtotal.toLocaleString('en-IN')}</span></div>
        <div className="order-line muted"><span>GST (18%)</span><span>₹{gst.toFixed(2)}</span></div>
        <div className="order-total"><span>Total</span><span>₹{total.toFixed(2)}</span></div>

        <button className="btn btn-primary btn-lg" style={{ width: '100%', marginTop: 'var(--space-md)' }}
          onClick={handlePlaceOrder} disabled={placing || !selectedAddress}>
          <Icon name="shield" size={18} /> {placing ? 'Placing order…' : 'Place order'}
        </button>
      </div>
    </div>
  );
}
