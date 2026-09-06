import { useEffect, useRef, useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Icon from './Icon';
import './Navbar.css';

export default function Navbar() {
  const { user, isAuthenticated, isAdmin, isSeller, logout } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const menuRef = useRef(null);

  useEffect(() => {
    const onClick = (e) => {
      if (menuRef.current && !menuRef.current.contains(e.target)) setOpen(false);
    };
    document.addEventListener('mousedown', onClick);
    return () => document.removeEventListener('mousedown', onClick);
  }, []);

  const handleLogout = () => {
    logout();
    setOpen(false);
    navigate('/');
  };

  const roleLabel = isAdmin ? 'Admin' : isSeller ? 'Seller' : 'Buyer';
  const dashboardPath = isAdmin ? '/admin' : isSeller ? '/seller/dashboard' : '/dashboard/orders';

  return (
    <header className="navbar">
      <div className="navbar-inner container">
        <Link to="/" className="navbar-brand" aria-label="Uday home">
          <span className="brand-mark" aria-hidden="true">
            <Icon name="store" size={20} strokeWidth={2} />
          </span>
          <span className="brand-text">Uday</span>
        </Link>

        <nav className="navbar-links" aria-label="Primary">
          <NavLink to="/catalog" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
            Browse
          </NavLink>
          <NavLink to="/seller/register" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
            Sell
          </NavLink>
          <NavLink to="/cart" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
            <span className="nav-cart">
              <Icon name="cart" size={18} />
              <span className="nav-cart-label">Cart</span>
            </span>
          </NavLink>
        </nav>

        <div className="navbar-actions">
          {isAuthenticated ? (
            <>
              <Link to={dashboardPath} className="btn btn-secondary btn-sm nav-dashboard">
                <Icon name="dashboard" size={16} />
                <span>{roleLabel} Dashboard</span>
              </Link>
              <div className="account-menu" ref={menuRef}>
                <button className="account-trigger" onClick={() => setOpen(o => !o)} aria-expanded={open} aria-haspopup="menu">
                  <span className="account-avatar" aria-hidden="true">
                    {(user.username || 'U').charAt(0).toUpperCase()}
                  </span>
                  <span className="account-name">{user.username}</span>
                  <Icon name="chevronDown" size={16} className="account-chevron" />
                </button>
                {open && (
                  <div className="account-dropdown" role="menu">
                    <div className="account-dropdown-head">
                      <span className="account-dropdown-name">{user.username}</span>
                      <span className="account-dropdown-email">{user.email}</span>
                      <span className={`badge ${isAdmin ? 'badge-info' : isSeller ? 'badge-neutral' : 'badge-success'}`}>{roleLabel}</span>
                    </div>
                    <Link to="/" className="account-item" onClick={() => setOpen(false)}>
                      <Icon name="eye" size={16} /> View storefront
                    </Link>
                    <button className="account-item" onClick={handleLogout} role="menuitem">
                      <Icon name="logout" size={16} /> Sign out
                    </button>
                  </div>
                )}
              </div>
            </>
          ) : (
            <>
              <Link to="/login" className="btn btn-ghost btn-sm">Sign in</Link>
              <Link to="/register" className="btn btn-primary btn-sm">Create account</Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
