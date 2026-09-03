import { useState } from 'react';
import { Link } from 'react-router-dom';
import { authAPI } from '../../api';
import Icon from '../../components/Icon';

export default function ForgotPassword() {
  const [email, setEmail] = useState('');
  const [sent, setSent] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await authAPI.forgotPassword({ email });
      setSent(true);
    } catch (err) {
      setError(err.message || 'Failed to send reset email');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ minHeight: 'calc(100vh - var(--navbar-height))', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 'var(--space-lg)' }}>
      <div className="card" style={{ width: '100%', maxWidth: '420px' }}>
        <h2 style={{ marginBottom: '0.25rem' }}>Reset Password</h2>
        <p className="text-secondary text-sm" style={{ marginBottom: 'var(--space-lg)' }}>
          Enter your email and we'll send you a reset link
        </p>

        {sent ? (
          <div className="alert alert-success">
            <Icon name="check" size={16} /> Password reset email sent! Check your inbox.
          </div>
        ) : (
          <>
            {error && <div className="alert alert-error mb-md"><Icon name="info" size={16} /> {error}</div>}
            <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-md)' }}>
              <div className="input-group">
                <label>Email</label>
                <input type="email" required value={email} onChange={e => setEmail(e.target.value)} placeholder="you@example.com" />
              </div>
              <button type="submit" className="btn btn-primary btn-lg" disabled={loading} style={{ width: '100%' }}>
                {loading ? 'Sending...' : 'Send Reset Link'}
              </button>
            </form>
          </>
        )}

        <div style={{ marginTop: 'var(--space-lg)', textAlign: 'center' }}>
          <Link to="/login" className="text-sm">← Back to login</Link>
        </div>
      </div>
    </div>
  );
}
