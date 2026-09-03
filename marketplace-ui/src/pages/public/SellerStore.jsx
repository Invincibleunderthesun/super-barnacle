import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { sellersAPI } from '../../api';
import ProductCard from '../../components/ProductCard';

export default function SellerStore() {
  const { id } = useParams();
  const [seller, setSeller] = useState(null);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      sellersAPI.getPublicProfile(id).then(res => setSeller(res.data)),
      sellersAPI.getPublicProducts(id, 0, 20).then(res => setProducts(res.data?.content || [])),
    ]).catch(() => {}).finally(() => setLoading(false));
  }, [id]);

  if (loading) return <div className="loading-screen"><div className="spinner" /></div>;
  if (!seller) return <div className="container empty-state"><div className="icon">🔍</div><p>Seller not found</p></div>;

  return (
    <div className="container" style={{ padding: 'var(--space-xl) var(--space-lg)' }}>
      <div className="card" style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-lg)', marginBottom: 'var(--space-xl)' }}>
        <div style={{
          width: '80px', height: '80px', borderRadius: 'var(--radius-lg)', background: 'var(--accent-gradient)',
          display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '2rem', flexShrink: 0,
        }}>🏪</div>
        <div style={{ flex: 1 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-sm)' }}>
            <h2>{seller.storeName}</h2>
            {seller.verified && <span className="badge badge-verified">✓ Verified</span>}
          </div>
          <p className="text-secondary" style={{ marginTop: '0.25rem' }}>{seller.storeDescription || 'No description'}</p>
          <p className="text-muted text-sm" style={{ marginTop: '0.25rem' }}>{seller.productCount} products</p>
        </div>
      </div>

      <h3 style={{ marginBottom: 'var(--space-lg)' }}>Products from {seller.storeName}</h3>
      {products.length === 0 ? (
        <div className="empty-state"><div className="icon">📦</div><p>No products listed yet</p></div>
      ) : (
        <div className="grid-products">
          {products.map(p => <ProductCard key={p.id} product={p} />)}
        </div>
      )}
    </div>
  );
}
