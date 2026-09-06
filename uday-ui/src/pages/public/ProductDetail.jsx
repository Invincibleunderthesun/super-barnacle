import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { productsAPI, cartAPI } from '../../api';
import { useAuth } from '../../context/AuthContext';
import Icon from '../../components/Icon';

export default function ProductDetail() {
  const { id } = useParams();
  const { user, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [qty, setQty] = useState(1);
  const [addingToCart, setAddingToCart] = useState(false);
  const [message, setMessage] = useState(null);

  useEffect(() => {
    productsAPI.getById(id)
      .then(res => setProduct(res.data))
      .catch(() => setProduct(null))
      .finally(() => setLoading(false));
  }, [id]);

  const handleAddToCart = async () => {
    if (!isAuthenticated) { navigate('/login'); return; }
    setAddingToCart(true);
    try {
      await cartAPI.add(user.id, product.id, qty);
      setMessage({ type: 'success', text: 'Added to cart' });
      setTimeout(() => setMessage(null), 3000);
    } catch (err) {
      setMessage({ type: 'error', text: err.message });
    } finally {
      setAddingToCart(false);
    }
  };

  if (loading) return <div className="loading-screen"><div className="spinner" /><span>Loading...</span></div>;
  if (!product) return (
    <div className="container empty-state" style={{ padding: 'var(--space-3xl)' }}>
      <span className="icon"><Icon name="search" size={48} /></span>
      <p>Product not found</p>
      <Link to="/catalog" className="btn btn-primary" style={{ marginTop: 'var(--space-md)' }}>Browse catalog</Link>
    </div>
  );

  const inStock = product.stock > 0;

  return (
    <div className="container" style={{ padding: 'var(--space-xl) var(--space-lg)' }}>
      <Link to="/catalog" className="back-link" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem', marginBottom: 'var(--space-lg)' }}>
        <Icon name="arrowLeft" size={16} /> Back to catalog
      </Link>

      <div className="pd-grid">
        <div className="pd-media">
          {product.imageUrl ? (
            <img src={product.imageUrl} alt={product.name} />
          ) : (
            <span className="product-fallback">
              <Icon name="package" size={72} strokeWidth={1.25} />
            </span>
          )}
        </div>

        <div className="pd-info">
          <div>
            <span className="product-category">{product.category || 'General'}</span>
            <h1 className="pd-title">{product.name}</h1>
          </div>
          <div className="pd-price">₹{Number(product.price || 0).toLocaleString('en-IN')}</div>
          <p className="pd-desc">{product.description || 'No description available.'}</p>

          <div className="pd-meta">
            <span className={`badge ${inStock ? 'badge-success' : 'badge-neutral'}`}>
              <span className="dot" aria-hidden="true" />
              {inStock ? `${product.stock} in stock` : 'Out of stock'}
            </span>
            {product.seller && (
              <Link to={`/seller/${product.seller.id}`} className="pd-seller">
                by {product.seller.storeName || 'Seller'}
              </Link>
            )}
          </div>

          {inStock && (
            <div className="pd-buy">
              <div className="qty-control">
                <button className="btn btn-secondary btn-sm" onClick={() => setQty(Math.max(1, qty - 1))} aria-label="Decrease quantity">
                  <Icon name="minus" size={14} />
                </button>
                <span className="qty-value">{qty}</span>
                <button className="btn btn-secondary btn-sm" onClick={() => setQty(Math.min(product.stock, qty + 1))} aria-label="Increase quantity">
                  <Icon name="plus" size={14} />
                </button>
              </div>
              <button className="btn btn-primary btn-lg" onClick={handleAddToCart} disabled={addingToCart} style={{ flex: 1 }}>
                <Icon name="bag" size={18} /> {addingToCart ? 'Adding…' : 'Add to cart'}
              </button>
            </div>
          )}

          {message && (
            <div className={`alert ${message.type === 'success' ? 'alert-success' : 'alert-error'}`}>
              <Icon name={message.type === 'success' ? 'check' : 'info'} size={16} /> {message.text}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
