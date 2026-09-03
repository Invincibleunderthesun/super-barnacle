import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { authAPI } from '../../api';

export default function Login() {
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await authAPI.login(form);
      login(res.data);
      const role = res.data.role;
      if (role === 'ROLE_ADMIN') navigate('/admin');
      else if (role === 'ROLE_SELLER') navigate('/seller/dashboard');
      else navigate('/dashboard/orders');
    } catch (err) {
      setError(err.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ minHeight: 'calc(100vh - var(--navbar-height))', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 'var(--space-lg)' }}>
      <div className="card" style={{ width: '100%', maxWidth: '420px' }}>
        <h2 style={{ marginBottom: '0.25rem' }}>Welcome back</h2>
        <p className="text-secondary text-sm" style={{ marginBottom: 'var(--space-lg)' }}>
          Sign in to your account
        </p>

        {error && <div className="alert alert-error" style={{ marginBottom: 'var(--space-md)' }}>{error}</div>}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-md)' }}>
          <div className="input-group">
            <label htmlFor="email">Email</label>
            <input id="email" type="email" placeholder="you@example.com" required
              value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} />
          </div>
          <div className="input-group">
            <label htmlFor="password">Password</label>
            <input id="password" type="password" placeholder="••••••••" required
              value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} />
          </div>
          <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
            <Link to="/forgot-password" className="text-sm" style={{ color: 'var(--brand)' }}>
              Forgot password?
            </Link>
          </div>
          <button type="submit" className="btn btn-primary btn-lg" disabled={loading}
            style={{ width: '100%' }}>
            {loading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>

        <div style={{ marginTop: 'var(--space-lg)', textAlign: 'center' }}>
          <span className="text-secondary text-sm">Don't have an account? </span>
          <Link to="/register" className="text-sm" style={{ fontWeight: 600 }}>Sign up</Link>
          <span className="text-secondary text-sm"> or </span>
          <Link to="/seller/register" className="text-sm" style={{ fontWeight: 600 }}>Sell on MarketHub</Link>
        </div>
      </div>
    </div>
  );
}
