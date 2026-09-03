import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { productsAPI } from '../../api';
import ProductCard from '../../components/ProductCard';

const categories = ['electronics', 'fashion', 'home', 'beauty', 'books', 'sports'];

export default function Home() {
  const [featured, setFeatured] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    productsAPI.getAll(0, 8, 'id', 'desc')
      .then(res => setFeatured(res.data?.content || []))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  return (
    <div>
      {/* Hero Section */}
      <section className="hero">
        <div className="hero-glow" />
        <div className="container hero-content">
          <span className="hero-badge">⚡ Trusted Multi-Seller Marketplace</span>
          <h1 className="hero-title">
            Discover. Shop.{' '}
            <span className="hero-gradient">Sell.</span>
          </h1>
          <p className="hero-subtitle">
            India's premium multi-seller marketplace — shop from hundreds of verified sellers
            or start your own store today.
          </p>
          <div className="hero-actions">
            <Link to="/catalog" className="btn btn-primary btn-lg">🛍️ Browse Products</Link>
            <Link to="/seller/register" className="btn btn-secondary btn-lg">🏪 Start Selling</Link>
          </div>
        </div>
      </section>

      {/* Category Pills */}
      <section className="container category-strip">
        {categories.map(cat => (
          <Link key={cat} to={`/catalog?q=${cat}`} className="category-pill">
            {cat}
          </Link>
        ))}
      </section>

      {/* Stats */}
      <section className="container">
        <div className="stats-band">
          {[
            { icon: '📦', label: 'Products', value: '1,000+' },
            { icon: '🏪', label: 'Verified Sellers', value: '50+' },
            { icon: '🚚', label: 'Orders Delivered', value: '5,000+' },
          ].map(stat => (
            <div key={stat.label} className="stat-band-item">
              <div className="stat-band-icon">{stat.icon}</div>
              <div className="stat-band-value">{stat.value}</div>
              <div className="stat-band-label">{stat.label}</div>
            </div>
          ))}
        </div>
      </section>

      {/* Featured Products */}
      <section className="container section-space">
        <div className="section-head">
          <h2>Latest Products</h2>
          <Link to="/catalog" className="btn btn-ghost btn-sm">View All →</Link>
        </div>

        {loading ? (
          <div className="loading-screen"><div className="spinner" /><span>Loading products...</span></div>
        ) : featured.length === 0 ? (
          <div className="empty-state">
            <div className="icon">📭</div>
            <p>No products yet. Be the first to sell!</p>
            <Link to="/seller/register" className="btn btn-primary" style={{ marginTop: 'var(--space-md)' }}>
              Start Selling
            </Link>
          </div>
        ) : (
          <div className="grid-products">
            {featured.map(product => <ProductCard key={product.id} product={product} />)}
          </div>
        )}
      </section>

      {/* CTA */}
      <section className="cta-band">
        <div className="container" style={{ textAlign: 'center' }}>
          <h2 style={{ marginBottom: 'var(--space-sm)' }}>Ready to start selling?</h2>
          <p className="text-secondary" style={{ marginBottom: 'var(--space-lg)' }}>
            Join our marketplace and reach thousands of customers across India.
          </p>
          <Link to="/seller/register" className="btn btn-primary btn-lg">🚀 Create Your Store</Link>
        </div>
      </section>
    </div>
  );
}
