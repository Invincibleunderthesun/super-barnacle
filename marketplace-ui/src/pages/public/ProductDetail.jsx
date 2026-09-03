import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { productsAPI, cartAPI } from '../../api';
import { useAuth } from '../../context/AuthContext';

export default function ProductDetail() {
  const { id } = useParams();
  const { user, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [qty, setQty] = useState(1);
  const [addingToCart, setAddingToCart] = useState(false);
  const [message, setMessage] = useState('');

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
      setMessage('✅ Added to cart!');
      setTimeout(() => setMessage(''), 3000);
    } catch (err) {
      setMessage(`❌ ${err.message}`);
    } finally {
      setAddingToCart(false);
    }
  };

  if (loading) return <div className="loading-screen"><div className="spinner" /><span>Loading...</span></div>;
  if (!product) return (
    <div className="container empty-state" style={{ padding: 'var(--space-3xl)' }}>
      <div className="icon">🔍</div>
      <p>Product not found</p>
      <Link to="/catalog" className="btn btn-primary" style={{ marginTop: 'var(--space-md)' }}>Browse Catalog</Link>
    </div>
  );

  return (
    <div className="container" style={{ padding: 'var(--space-xl) var(--space-lg)' }}>
      <Link to="/catalog" className="text-secondary text-sm" style={{ display: 'inline-block', marginBottom: 'var(--space-lg)' }}>
        ← Back to Catalog
      </Link>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-2xl)', alignItems: 'start' }}>
        {/* Image */}
        <div style={{
          height: '400px', background: 'var(--bg-card)', borderRadius: 'var(--radius-lg)',
          display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '5rem',
          border: '1px solid var(--border-color)', overflow: 'hidden',
        }}>
          {product.imageUrl ? (
            <img src={product.imageUrl} alt={product.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
          ) : '📦'}
        </div>

        {/* Info */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-md)' }}>
          <div>
            <p className="text-secondary text-sm">{product.category || 'General'}</p>
            <h1 style={{ fontSize: '1.75rem', marginTop: '0.25rem' }}>{product.name}</h1>
          </div>
          <div className="price" style={{ fontSize: '2rem' }}>₹{product.price?.toLocaleString()}</div>
          <p className="text-secondary" style={{ lineHeight: 1.7 }}>{product.description || 'No description available.'}</p>

          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-md)' }}>
            <span className={`badge ${product.stock > 0 ? 'badge-paid' : 'badge-cancelled'}`}>
              {product.stock > 0 ? `${product.stock} in stock` : 'Out of Stock'}
            </span>
            {product.seller && (
              <Link to={`/seller/${product.seller.id}`} className="text-sm">
                by {product.seller.storeName || 'Seller'}
              </Link>
            )}
          </div>

          {product.stock > 0 && (
            <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-md)', marginTop: 'var(--space-sm)' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <button className="btn btn-secondary btn-sm" onClick={() => setQty(Math.max(1, qty - 1))}>−</button>
                <span style={{ minWidth: '2rem', textAlign: 'center', fontWeight: 600 }}>{qty}</span>
                <button className="btn btn-secondary btn-sm" onClick={() => setQty(Math.min(product.stock, qty + 1))}>+</button>
              </div>
              <button className="btn btn-primary btn-lg" onClick={handleAddToCart} disabled={addingToCart}
                style={{ flex: 1 }}>
                {addingToCart ? 'Adding...' : '🛒 Add to Cart'}
              </button>
            </div>
          )}

          {message && (
            <div className={`alert ${message.startsWith('✅') ? 'alert-success' : 'alert-error'}`}>{message}</div>
          )}
        </div>
      </div>
    </div>
  );
}
