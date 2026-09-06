import { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { productsAPI, cartAPI } from '../../api';
import { useAuth } from '../../context/AuthContext';
import ProductCard from '../../components/ProductCard';
import Icon from '../../components/Icon';

const categories = [
  { name: 'Electronics', hue: 'var(--hue-electronics)' },
  { name: 'Computers', hue: 'var(--hue-books)' },
  { name: 'Accessories', hue: 'var(--hue-fashion)' },
  { name: 'Home & Office', hue: 'var(--hue-home)' },
];

export default function Home() {
  const { user, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [featured, setFeatured] = useState([]);
  const [loading, setLoading] = useState(true);
  const [addingId, setAddingId] = useState(null);

  useEffect(() => {
    productsAPI.getAll(0, 8, 'id', 'desc')
      .then(res => setFeatured(res.data?.content || []))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const handleAdd = useCallback(async (product) => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    setAddingId(product.id);
    try {
      await cartAPI.add(user.id, product.id, 1);
    } catch (err) {
      alert(err.message || 'Could not add to cart');
    } finally {
      setAddingId(null);
    }
  }, [isAuthenticated, user, navigate]);

  return (
    <div>
      {/* Hero */}
      <section className="hero">
        <div className="container hero-inner">
          <div>
            <span className="hero-eyebrow"><Icon name="shield" size={14} /> Verified sellers · Secure checkout</span>
            <h1>The marketplace for things you&rsquo;ll actually use.</h1>
            <p className="lead">
              Shop independent stores across electronics, fashion, home and more — or open
              your own store and reach customers across India.
            </p>
            <div className="hero-actions">
              <Link to="/catalog" className="btn btn-primary btn-lg">
                Browse products <Icon name="arrowRight" size={18} />
              </Link>
              <Link to="/seller/register" className="btn btn-secondary btn-lg">
                Start selling
              </Link>
            </div>
            <div className="hero-trust" style={{ marginTop: 'var(--space-5)' }}>
              <span className="hero-stat"><Icon name="store" size={16} /> <strong>50+</strong>&nbsp;sellers</span>
              <span className="hero-stat" style={{ marginLeft: 'var(--space-5)' }}><Icon name="package" size={16} /> <strong>1,000+</strong>&nbsp;products</span>
            </div>
          </div>
          <div className="hero-art">
            <Icon name="store" size={56} strokeWidth={1.5} />
            <span>Uday</span>
            <span style={{ fontSize: '0.85rem' }}>Buy &amp; sell with confidence</span>
          </div>
        </div>
      </section>

      {/* Categories */}
      <section className="container" style={{ paddingTop: 'var(--space-6)', paddingBottom: 'var(--space-2)' }}>
        <div className="category-strip" role="list" aria-label="Shop by category">
          {categories.map(cat => (
            <Link key={cat.name} to={`/catalog?q=${encodeURIComponent(cat.name)}`} className="category-chip" role="listitem">
              <span className="category-dot" style={{ background: cat.hue }} aria-hidden="true" />
              {cat.name}
            </Link>
          ))}
        </div>
      </section>

      {/* Stats */}
      <section className="container" style={{ padding: 'var(--space-6) 0' }}>
        <div className="stats-band">
          <div className="stat-band-item">
            <span className="stat-band-icon"><Icon name="package" size={18} /></span>
            <div className="stat-band-value">1,000+</div>
            <div className="stat-band-label">Products listed</div>
          </div>
          <div className="stat-band-item">
            <span className="stat-band-icon"><Icon name="store" size={18} /></span>
            <div className="stat-band-value">50+</div>
            <div className="stat-band-label">Verified sellers</div>
          </div>
          <div className="stat-band-item">
            <span className="stat-band-icon"><Icon name="truck" size={18} /></span>
            <div className="stat-band-value">5,000+</div>
            <div className="stat-band-label">Orders delivered</div>
          </div>
        </div>
      </section>

      {/* Latest products */}
      <section className="container" style={{ paddingBottom: 'var(--space-8)' }}>
        <div className="section-head">
          <h2 className="section-title">Latest products</h2>
          <Link to="/catalog" className="btn btn-link">View all <Icon name="arrowRight" size={16} /></Link>
        </div>

        {loading ? (
          <div className="loading-screen"><div className="spinner" /><span>Loading products…</span></div>
        ) : featured.length === 0 ? (
          <div className="empty-state">
            <span className="icon"><Icon name="package" size={48} /></span>
            <p>No products yet. Be the first to sell!</p>
            <Link to="/seller/register" className="btn btn-primary" style={{ marginTop: 'var(--space-4)' }}>
              Start selling
            </Link>
          </div>
        ) : (
          <div className="grid-products">
            {featured.map(product => (
              <ProductCard
                key={product.id}
                product={product}
                onAdd={handleAdd}
                adding={addingId === product.id}
              />
            ))}
          </div>
        )}
      </section>

      {/* CTA */}
      <section className="cta-band">
        <div className="container">
          <h2>Have something great to sell?</h2>
          <p>Join our marketplace and reach thousands of shoppers.</p>
          <Link to="/seller/register" className="btn btn-secondary btn-lg">
            <Icon name="bag" size={18} /> Open your store
          </Link>
        </div>
      </section>
    </div>
  );
}
