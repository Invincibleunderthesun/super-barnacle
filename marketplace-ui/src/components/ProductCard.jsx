import { Link } from 'react-router-dom';
import Icon from './Icon';

const categoryHues = {
  electronics: 'var(--hue-electronics)',
  fashion: 'var(--hue-fashion)',
  home: 'var(--hue-home)',
  books: 'var(--hue-books)',
};

export default function ProductCard({ product, onAdd, adding }) {
  const inStock = product.stock > 0;
  const cat = (product.category || 'general').toLowerCase();

  return (
    <article className="product-card">
      <Link to={`/product/${product.id}`} className="product-media" aria-label={product.name}>
        {product.imageUrl ? (
          <img src={product.imageUrl} alt={product.name} loading="lazy" />
        ) : (
          <span
            className="product-fallback"
            style={{ background: `color-mix(in srgb, ${categoryHues[cat] || '#94a3b8'} 12%, var(--bg-subtle))`, color: categoryHues[cat] || 'var(--ink-faint)' }}
          >
            <Icon name="package" size={40} strokeWidth={1.5} />
          </span>
        )}
        <span className={`badge product-stock-badge ${inStock ? 'badge-success' : 'badge-neutral'}`}>
          <span className="dot" aria-hidden="true" />
          {inStock ? 'In stock' : 'Out of stock'}
        </span>
      </Link>

      <div className="product-body">
        <span className="product-category">{product.category || 'General'}</span>
        <h3 className="product-name">
          <Link to={`/product/${product.id}`}>{product.name}</Link>
        </h3>
        <div className="product-price-row">
          <span className="product-price">₹{Number(product.price || 0).toLocaleString('en-IN')}</span>
          <button
            className="product-add"
            disabled={!inStock || adding}
            onClick={() => onAdd && onAdd(product)}
            aria-label={`Add ${product.name} to cart`}
          >
            {adding ? 'Adding…' : (
              <>
                <Icon name="bag" size={15} />
                Add
              </>
            )}
          </button>
        </div>
      </div>
    </article>
  );
}
