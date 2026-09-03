import { NavLink } from 'react-router-dom';
import './Sidebar.css';

export default function Sidebar({ links }) {
  return (
    <aside className="sidebar">
      <nav className="sidebar-nav">
        {links.map(link => (
          <NavLink
            key={link.path}
            to={link.path}
            className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
          >
            <span className="sidebar-icon">{link.icon}</span>
            <span>{link.label}</span>
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
