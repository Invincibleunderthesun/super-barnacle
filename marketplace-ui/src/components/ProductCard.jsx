import { Link } from 'react-router-dom';

export default function ProductCard({ product }) {
  return (
    <Link to={`/product/${product.id}`} className="product-card">
      <div className="product-card-media">
        {product.imageUrl ? (
          <img src={product.imageUrl} alt={product.name} loading="lazy" />
        ) : (
          <span className="product-card-fallback">📦</span>
        )}
        <span className={`badge product-card-stock ${product.stock > 0 ? 'badge-paid' : 'badge-cancelled'}`}>
          {product.stock > 0 ? 'In Stock' : 'Out of Stock'}
        </span>
      </div>
      <div className="product-card-body">
        <div className="product-card-category">{product.category || 'General'}</div>
        <h4 className="product-card-name">{product.name}</h4>
        <div className="product-card-footer">
          <span className="price">₹{Number(product.price || 0).toLocaleString()}</span>
          <span className="product-card-add" aria-hidden="true">+</span>
        </div>
      </div>
    </Link>
  );
}
