import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { cartAPI } from '../../api';
import { useAuth } from '../../context/AuthContext';

export default function Cart() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetchCart = () => {
    if (!user) return;
    setLoading(true);
    cartAPI.get(user.id)
      .then(res => setCart(res.data))
      .catch(() => setCart(null))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchCart(); }, [user]);

  const updateQty = async (productId, newQty) => {
    try {
      await cartAPI.updateQty(user.id, productId, newQty);
      fetchCart();
    } catch {}
  };

  const removeItem = async (productId) => {
    try {
      await cartAPI.remove(user.id, productId);
      fetchCart();
    } catch {}
  };

  if (loading) return <div className="loading-screen"><div className="spinner" /></div>;

  const items = cart?.items || [];
  const total = cart?.totalAmount || 0;

  return (
    <div className="container" style={{ padding: 'var(--space-xl) var(--space-lg)', maxWidth: '900px' }}>
      <h1 style={{ marginBottom: 'var(--space-lg)' }}>🛒 Your Cart</h1>

      {items.length === 0 ? (
        <div className="empty-state">
          <div className="icon">🛍️</div>
          <p>Your cart is empty</p>
          <Link to="/catalog" className="btn btn-primary" style={{ marginTop: 'var(--space-md)' }}>Browse Products</Link>
        </div>
      ) : (
        <>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-md)' }}>
            {items.map(item => (
              <div key={item.id} className="card" style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-lg)' }}>
                <div style={{
                  width: '80px', height: '80px', background: 'var(--bg-secondary)', borderRadius: 'var(--radius-md)',
                  display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '2rem', flexShrink: 0,
                  overflow: 'hidden',
                }}>
                  {item.product?.imageUrl ? (
                    <img src={item.product.imageUrl} alt={item.product.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                  ) : '📦'}
                </div>

                <div style={{ flex: 1, minWidth: 0 }}>
                  <Link to={`/product/${item.product?.id}`} style={{ fontWeight: 600, color: 'var(--text-primary)' }}>
                    {item.product?.name}
                  </Link>
                  <p className="text-secondary text-sm">₹{item.product?.price?.toLocaleString()} each</p>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <button className="btn btn-secondary btn-sm" onClick={() => updateQty(item.product?.id, item.quantity - 1)}>−</button>
                  <span style={{ fontWeight: 600, minWidth: '1.5rem', textAlign: 'center' }}>{item.quantity}</span>
                  <button className="btn btn-secondary btn-sm" onClick={() => updateQty(item.product?.id, item.quantity + 1)}>+</button>
                </div>

                <span className="price" style={{ minWidth: '80px', textAlign: 'right' }}>
                  ₹{(item.product?.price * item.quantity).toLocaleString()}
                </span>

                <button className="btn btn-ghost btn-sm" onClick={() => removeItem(item.product?.id)} title="Remove">
                  🗑️
                </button>
              </div>
            ))}
          </div>

          <div className="card" style={{ marginTop: 'var(--space-lg)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div>
              <p className="text-secondary text-sm">Subtotal ({items.length} items)</p>
              <p className="price" style={{ fontSize: '1.5rem' }}>₹{total.toLocaleString()}</p>
            </div>
            <button className="btn btn-primary btn-lg" onClick={() => navigate('/checkout')}>
              Proceed to Checkout →
            </button>
          </div>
        </>
      )}
    </div>
  );
}
