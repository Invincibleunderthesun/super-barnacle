import { useState, useEffect, useCallback } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { productsAPI, cartAPI } from '../../api';
import { useAuth } from '../../context/AuthContext';
import ProductCard from '../../components/ProductCard';
import Icon from '../../components/Icon';

const categories = [
  { label: 'All', query: '' },
  { label: 'Electronics', query: 'Electronics', hue: 'var(--hue-electronics)' },
  { label: 'Computers', query: 'Computers', hue: 'var(--hue-books)' },
  { label: 'Accessories', query: 'Accessories', hue: 'var(--hue-fashion)' },
  { label: 'Home & Office', query: 'Home & Office', hue: 'var(--hue-home)' },
];

export default function Catalog() {
  const { user, isAuthenticated } = useAuth();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [totalPages, setTotalPages] = useState(0);
  const [addingId, setAddingId] = useState(null);
  const [searchParams, setSearchParams] = useSearchParams();

  const page = parseInt(searchParams.get('page') || '0');
  const query = searchParams.get('q') || '';
  const [searchInput, setSearchInput] = useState(query);

  useEffect(() => {
    setLoading(true);
    const fetcher = query
      ? productsAPI.search(query, page, 12)
      : productsAPI.getAll(page, 12, 'id', 'desc');
    fetcher
      .then(res => {
        const data = res.data?.content || res.data || [];
        setProducts(Array.isArray(data) ? data : []);
        setTotalPages(res.data?.totalPages || 1);
        window.scrollTo({ top: 0, behavior: 'smooth' });
      })
      .catch(() => setProducts([]))
      .finally(() => setLoading(false));
  }, [page, query]);

  const handleSearch = (e) => {
    e.preventDefault();
    setSearchParams(searchInput ? { q: searchInput, page: '0' } : {});
  };

  const pickCategory = (q) => {
    setSearchParams(q ? { q, page: '0' } : {});
  };

  const goToPage = (p) => {
    const params = {};
    if (query) params.q = query;
    params.page = String(p);
    setSearchParams(params);
  };

  const handleAdd = useCallback(async (product) => {
    if (!isAuthenticated) {
      window.location.href = '/login';
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
  }, [isAuthenticated, user]);

  return (
    <div>
      <section className="page-band">
        <div className="container">
          <h1>Browse products</h1>
          <p>
            {query ? `Results for “${query}”` : 'Find something from verified sellers across India.'}
          </p>
          <form onSubmit={handleSearch} className="search-bar" style={{ marginTop: 'var(--space-4)' }}>
            <input
              type="search"
              placeholder="Search products…"
              value={searchInput}
              onChange={e => setSearchInput(e.target.value)}
              aria-label="Search products"
            />
            <button type="submit" className="btn btn-primary"><Icon name="search" size={16} /> Search</button>
          </form>
        </div>
      </section>

      <div className="container" style={{ paddingTop: 'var(--space-5)', paddingBottom: 'var(--space-8)' }}>
        <div className="category-strip" style={{ marginBottom: 'var(--space-5)' }}>
          {categories.map(cat => {
            const active = cat.query === '' ? !query : query === cat.query;
            return (
              <button
                key={cat.label}
                className={`category-chip ${active ? 'active' : ''}`}
                onClick={() => pickCategory(cat.query)}
                aria-pressed={active}
              >
                {cat.hue && <span className="category-dot" style={{ background: cat.hue }} aria-hidden="true" />}
                {cat.label}
              </button>
            );
          })}
        </div>

        {loading ? (
          <div className="loading-screen"><div className="spinner" /><span>Loading…</span></div>
        ) : products.length === 0 ? (
          <div className="empty-state">
            <span className="icon"><Icon name="search" size={48} /></span>
            <p>{query ? `No results for “${query}”` : 'No products available yet'}</p>
          </div>
        ) : (
          <>
            <div className="grid-products">
              {products.map(product => (
                <ProductCard
                  key={product.id}
                  product={product}
                  onAdd={handleAdd}
                  adding={addingId === product.id}
                />
              ))}
            </div>

            {totalPages > 1 && (
              <div className="pagination">
                <button disabled={page === 0} onClick={() => goToPage(page - 1)} aria-label="Previous page">
                  <Icon name="arrowLeft" size={16} />
                </button>
                {Array.from({ length: Math.min(totalPages, 5) }, (_, i) => (
                  <button key={i} className={page === i ? 'active' : ''} onClick={() => goToPage(i)}>
                    {i + 1}
                  </button>
                ))}
                <button disabled={page >= totalPages - 1} onClick={() => goToPage(page + 1)} aria-label="Next page">
                  <Icon name="arrowRight" size={16} />
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
