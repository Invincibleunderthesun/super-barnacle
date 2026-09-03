import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { cartAPI } from '../../api';
import { useAuth } from '../../context/AuthContext';
import Icon from '../../components/Icon';

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
    if (newQty < 1) return;
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
      <h1 className="page-title-cart" style={{ marginBottom: 'var(--space-lg)' }}><Icon name="cart" size={22} /> Your cart</h1>

      {items.length === 0 ? (
        <div className="empty-state">
          <span className="icon"><Icon name="bag" size={48} /></span>
          <p>Your cart is empty</p>
          <Link to="/catalog" className="btn btn-primary" style={{ marginTop: 'var(--space-md)' }}>Browse products</Link>
        </div>
      ) : (
        <>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-md)' }}>
            {items.map(item => (
              <div key={item.id} className="card cart-item">
                <div className="cart-item-media">
                  {item.product?.imageUrl ? (
                    <img src={item.product.imageUrl} alt={item.product.name} />
                  ) : (
                    <span className="cart-item-fallback"><Icon name="package" size={24} /></span>
                  )}
                </div>

                <div className="cart-item-name">
                  <Link to={`/product/${item.product?.id}`}>{item.product?.name}</Link>
                  <p className="text-muted text-sm">₹{Number(item.product?.price || 0).toLocaleString('en-IN')} each</p>
                </div>

                <div className="qty-control">
                  <button className="btn btn-secondary btn-sm" onClick={() => updateQty(item.product?.id, item.quantity - 1)} aria-label="Decrease quantity">
                    <Icon name="minus" size={14} />
                  </button>
                  <span className="qty-value">{item.quantity}</span>
                  <button className="btn btn-secondary btn-sm" onClick={() => updateQty(item.product?.id, item.quantity + 1)} aria-label="Increase quantity">
                    <Icon name="plus" size={14} />
                  </button>
                </div>

                <span className="cart-item-price">
                  ₹{Number(item.product?.price * item.quantity || 0).toLocaleString('en-IN')}
                </span>

                <button className="btn btn-ghost btn-icon" onClick={() => removeItem(item.product?.id)} aria-label="Remove item" title="Remove">
                  <Icon name="trash" size={18} />
                </button>
              </div>
            ))}
          </div>

          <div className="card cart-summary">
            <div>
              <p className="text-muted text-sm">{items.length} {items.length === 1 ? 'item' : 'items'}</p>
              <p className="pd-price">₹{total.toLocaleString('en-IN')}</p>
            </div>
            <button className="btn btn-primary btn-lg" onClick={() => navigate('/checkout')}>
              Proceed to checkout <Icon name="arrowRight" size={18} />
            </button>
          </div>
        </>
      )}
    </div>
  );
}
