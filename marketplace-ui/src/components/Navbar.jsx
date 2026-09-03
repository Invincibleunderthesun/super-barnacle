import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Navbar.css';

export default function Navbar() {
  const { user, isAuthenticated, isAdmin, isSeller, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <nav className="navbar">
      <div className="navbar-inner container">
        <Link to="/" className="navbar-brand">
          <span className="brand-icon">🛒</span>
          <span className="brand-text">MarketHub</span>
        </Link>

        <div className="navbar-links">
          <Link to="/catalog" className="nav-link">Browse</Link>
          <Link to="/seller/register" className="nav-link">Sell</Link>
          {isAuthenticated && !isSeller && !isAdmin && (
            <Link to="/cart" className="nav-link">Cart</Link>
          )}
        </div>

        <div className="navbar-actions">
          {isAuthenticated ? (
            <>
              <span className="user-greeting">
                {user.username}
                <span className="user-role-badge">
                  {isAdmin ? 'Admin' : isSeller ? 'Seller' : 'Buyer'}
                </span>
              </span>
              {isAdmin && (
                <Link to="/admin" className="btn btn-ghost btn-sm">Dashboard</Link>
              )}
              {isSeller && (
                <Link to="/seller/dashboard" className="btn btn-ghost btn-sm">Dashboard</Link>
              )}
              {!isAdmin && !isSeller && (
                <Link to="/dashboard/orders" className="btn btn-ghost btn-sm">My Orders</Link>
              )}
              <button onClick={handleLogout} className="btn btn-secondary btn-sm">
                Logout
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn btn-ghost btn-sm">Login</Link>
              <Link to="/register" className="btn btn-primary btn-sm">Sign Up</Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}
