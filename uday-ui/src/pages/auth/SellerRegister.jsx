import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { authAPI } from '../../api';
import Icon from '../../components/Icon';

export default function SellerRegister() {
  const [form, setForm] = useState({
    username: '', email: '', password: '',
    storeName: '', storeDescription: '', gstNumber: '', phone: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await authAPI.registerSeller(form);
      login(res.data);
      navigate('/seller/dashboard');
    } catch (err) {
      setError(err.message || 'Seller registration failed');
    } finally {
      setLoading(false);
    }
  };

  const set = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  return (
    <div style={{ minHeight: 'calc(100vh - var(--navbar-height))', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 'var(--space-xl)' }}>
      <div className="card" style={{ width: '100%', maxWidth: '520px' }}>
        <h2 style={{ marginBottom: '0.25rem' }}><Icon name="store" size={20} /> Become a Seller</h2>
        <p className="text-secondary text-sm" style={{ marginBottom: 'var(--space-lg)' }}>
          Set up your store and start selling on Uday
        </p>

        {error && <div className="alert alert-error" style={{ marginBottom: 'var(--space-md)' }}><Icon name="info" size={16} /> {error}</div>}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-md)' }}>
          <h4 style={{ color: 'var(--text-secondary)', marginTop: 'var(--space-sm)' }}>Account Info</h4>
          <div className="grid-2">
            <div className="input-group">
              <label>Username</label>
              <input type="text" required value={form.username} onChange={set('username')} placeholder="Your name" />
            </div>
            <div className="input-group">
              <label>Email</label>
              <input type="email" required value={form.email} onChange={set('email')} placeholder="you@example.com" />
            </div>
          </div>
          <div className="input-group">
            <label>Password</label>
            <input type="password" required value={form.password} onChange={set('password')} placeholder="Min 6 characters" />
          </div>

          <h4 style={{ color: 'var(--text-secondary)', marginTop: 'var(--space-sm)' }}>Store Details</h4>
          <div className="input-group">
            <label>Store Name *</label>
            <input type="text" required value={form.storeName} onChange={set('storeName')} placeholder="Your store name" />
          </div>
          <div className="input-group">
            <label>Store Description</label>
            <textarea value={form.storeDescription} onChange={set('storeDescription')} placeholder="Tell buyers what you sell..." />
          </div>
          <div className="grid-2">
            <div className="input-group">
              <label>GST Number</label>
              <input type="text" value={form.gstNumber} onChange={set('gstNumber')} placeholder="Optional" />
            </div>
            <div className="input-group">
              <label>Phone</label>
              <input type="tel" value={form.phone} onChange={set('phone')} placeholder="Optional" />
            </div>
          </div>

          <button type="submit" className="btn btn-primary btn-lg" disabled={loading}
            style={{ width: '100%', marginTop: 'var(--space-sm)' }}>
            {loading ? 'Creating store...' : 'Create My Store'}
          </button>
        </form>

        <div style={{ marginTop: 'var(--space-lg)', textAlign: 'center' }}>
          <span className="text-secondary text-sm">Already have an account? </span>
          <Link to="/login" className="text-sm" style={{ fontWeight: 600 }}>Sign in</Link>
        </div>
      </div>
    </div>
  );
}
