import { Link } from 'react-router-dom';
import Icon from './Icon';
import './Footer.css';

export default function Footer() {
  return (
    <footer className="footer">
      <div className="container">
        <div className="footer-grid">
          <div className="footer-brand">
            <Link to="/" className="footer-logo">
              <span className="brand-mark" aria-hidden="true"><Icon name="store" size={20} strokeWidth={2} /></span>
              <span className="brand-text">MarketHub</span>
            </Link>
            <p className="footer-tagline">
              India's multi-seller marketplace — buy and sell with confidence.
            </p>
          </div>
          <div className="footer-col">
            <span className="footer-heading">Shop</span>
            <Link to="/catalog" className="footer-link">Browse products</Link>
            <Link to="/catalog?q=Electronics" className="footer-link">Electronics</Link>
            <Link to="/catalog?q=Home" className="footer-link">Home &amp; Office</Link>
          </div>
          <div className="footer-col">
            <span className="footer-heading">Sell</span>
            <Link to="/seller/register" className="footer-link">Open a store</Link>
            <Link to="/seller/register" className="footer-link">Selling fees</Link>
          </div>
          <div className="footer-col">
            <span className="footer-heading">Account</span>
            <Link to="/login" className="footer-link">Sign in</Link>
            <Link to="/register" className="footer-link">Create account</Link>
          </div>
        </div>
        <div className="footer-bottom">
          <span>© {new Date().getFullYear()} MarketHub. All rights reserved.</span>
          <span className="footer-made">Made for learning &amp; demo purposes</span>
        </div>
      </div>
    </footer>
  );
}
