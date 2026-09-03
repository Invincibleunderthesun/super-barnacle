import { useState, useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { productsAPI } from '../../api';
import ProductCard from '../../components/ProductCard';

const categories = ['All', 'electronics', 'fashion', 'home', 'beauty', 'books', 'sports'];

export default function Catalog() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [totalPages, setTotalPages] = useState(0);
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

  const pickCategory = (cat) => {
    setSearchParams(cat === 'All' ? {} : { q: cat, page: '0' });
  };

  const goToPage = (p) => {
    const params = {};
    if (query) params.q = query;
    params.page = String(p);
    setSearchParams(params);
  };

  return (
    <div className="container" style={{ padding: 'var(--space-xl) var(--space-lg)' }}>
      <div className="section-head" style={{ marginBottom: 'var(--space-lg)' }}>
        <h1>Product Catalog</h1>
        <form onSubmit={handleSearch} className="search-bar">
          <input
            type="search" placeholder="Search products..."
            value={searchInput} onChange={e => setSearchInput(e.target.value)}
          />
          <button type="submit" className="btn btn-primary">Search</button>
        </form>
      </div>

      <div className="category-strip" style={{ marginBottom: 'var(--space-lg)', justifyContent: 'flex-start' }}>
        {categories.map(cat => (
          <button
            key={cat}
            className={`category-pill ${(cat === 'All' ? !query : query === cat) ? 'active' : ''}`}
            onClick={() => pickCategory(cat)}
          >
            {cat}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="loading-screen"><div className="spinner" /><span>Loading...</span></div>
      ) : products.length === 0 ? (
        <div className="empty-state">
          <div className="icon">🔍</div>
          <p>{query ? `No results for "${query}"` : 'No products available yet'}</p>
        </div>
      ) : (
        <>
          <div className="grid-products">
            {products.map(product => <ProductCard key={product.id} product={product} />)}
          </div>

          {totalPages > 1 && (
            <div className="pagination">
              <button disabled={page === 0} onClick={() => goToPage(page - 1)}>← Prev</button>
              {Array.from({ length: Math.min(totalPages, 5) }, (_, i) => (
                <button key={i} className={page === i ? 'active' : ''} onClick={() => goToPage(i)}>{i + 1}</button>
              ))}
              <button disabled={page >= totalPages - 1} onClick={() => goToPage(page + 1)}>Next →</button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
