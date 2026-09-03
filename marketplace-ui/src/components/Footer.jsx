import { Link } from 'react-router-dom';

export default function Footer() {
  return (
    <footer style={{
      borderTop: '1px solid var(--border-color)',
      padding: '2rem 0',
      marginTop: 'auto',
      background: 'var(--bg-secondary)',
    }}>
      <div className="container" style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        flexWrap: 'wrap',
        gap: '1rem',
      }}>
        <div>
          <div style={{ fontWeight: 700, fontSize: '1rem', marginBottom: '0.25rem' }}>
            🛒 <span style={{
              background: 'var(--accent-gradient)',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
            }}>MarketHub</span>
          </div>
          <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
            Multi-seller marketplace — Buy & Sell with confidence
          </p>
        </div>
        <div style={{ display: 'flex', gap: '1.5rem' }}>
          <Link to="/catalog" style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Catalog</Link>
          <Link to="/seller/register" style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Sell on MarketHub</Link>
          <Link to="/login" style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Login</Link>
        </div>
        <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', width: '100%', textAlign: 'center', marginTop: '0.5rem' }}>
          © {new Date().getFullYear()} MarketHub. All rights reserved.
        </p>
      </div>
    </footer>
  );
}
