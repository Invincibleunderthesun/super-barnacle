import { useState, useEffect } from 'react';
import { sellersAPI } from '../../api';
import Icon from '../../components/Icon';

export default function SellerProducts() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editId, setEditId] = useState(null);
  const [form, setForm] = useState({ name: '', description: '', price: '', stock: '', category: '', imageUrl: '' });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const fetchProducts = () => {
    setLoading(true);
    sellersAPI.getProducts(0, 50)
      .then(res => setProducts(res.data?.content || []))
      .catch(() => setProducts([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchProducts(); }, []);

  const openEdit = (product) => {
    setEditId(product.id);
    setForm({
      name: product.name, description: product.description || '',
      price: product.price, stock: product.stock,
      category: product.category || '', imageUrl: product.imageUrl || '',
    });
    setShowForm(true);
  };

  const openNew = () => {
    setEditId(null);
    setForm({ name: '', description: '', price: '', stock: '', category: '', imageUrl: '' });
    setShowForm(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      const payload = { ...form, price: parseFloat(form.price), stock: parseInt(form.stock) };
      if (editId) {
        await sellersAPI.updateProduct(editId, payload);
      } else {
        await sellersAPI.addProduct(payload);
      }
      setShowForm(false);
      fetchProducts();
    } catch (err) {
      setError(err.message || 'Failed to save');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this product?')) return;
    try {
      await sellersAPI.deleteProduct(id);
      fetchProducts();
    } catch {}
  };

  const set = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  if (loading) return <div className="loading-screen"><div className="spinner" /></div>;

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--space-lg)' }}>
        <h2>My Products</h2>
        <button className="btn btn-primary" onClick={openNew}>+ Add Product</button>
      </div>

      {/* Product Form Modal */}
      {showForm && (
        <div className="modal-overlay" onClick={() => setShowForm(false)}>
          <div className="modal" onClick={e => e.stopPropagation()} style={{ maxWidth: '550px' }}>
            <div className="modal-header">
              <h3>{editId ? 'Edit Product' : 'Add Product'}</h3>
              <button className="modal-close" onClick={() => setShowForm(false)}>×</button>
            </div>
            {error && <div className="alert alert-error mb-md">{error}</div>}
            <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-md)' }}>
              <div className="input-group">
                <label>Product Name *</label>
                <input type="text" required value={form.name} onChange={set('name')} />
              </div>
              <div className="input-group">
                <label>Description</label>
                <textarea value={form.description} onChange={set('description')} />
              </div>
              <div className="grid-2">
                <div className="input-group">
                  <label>Price (₹) *</label>
                  <input type="number" required min="1" step="0.01" value={form.price} onChange={set('price')} />
                </div>
                <div className="input-group">
                  <label>Stock *</label>
                  <input type="number" required min="0" value={form.stock} onChange={set('stock')} />
                </div>
              </div>
              <div className="grid-2">
                <div className="input-group">
                  <label>Category</label>
                  <input type="text" value={form.category} onChange={set('category')} />
                </div>
                <div className="input-group">
                  <label>Image URL</label>
                  <input type="text" value={form.imageUrl} onChange={set('imageUrl')} />
                </div>
              </div>
              <button type="submit" className="btn btn-primary" disabled={saving}>
                {saving ? 'Saving...' : editId ? 'Update Product' : 'Add Product'}
              </button>
            </form>
          </div>
        </div>
      )}

      {/* Product Table */}
      {products.length === 0 ? (
        <div className="empty-state">
          <span className="icon"><Icon name="package" size={32} /></span>
          <p>No products yet. Add your first product!</p>
        </div>
      ) : (
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Product</th>
                <th>Price</th>
                <th>Stock</th>
                <th>Category</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {products.map(p => (
                <tr key={p.id}>
                  <td style={{ fontWeight: 600 }}>{p.name}</td>
                  <td className="price">₹{p.price?.toLocaleString()}</td>
                  <td>
                    <span className={`badge ${p.stock > 5 ? 'badge-paid' : p.stock > 0 ? 'badge-pending' : 'badge-cancelled'}`}>
                      {p.stock}
                    </span>
                  </td>
                  <td className="text-secondary">{p.category || '—'}</td>
                  <td>
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                      <button className="btn btn-ghost btn-sm" onClick={() => openEdit(p)}><Icon name="edit" size={16} /></button>
                      <button className="btn btn-ghost btn-sm" onClick={() => handleDelete(p.id)}><Icon name="trash" size={16} /></button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
