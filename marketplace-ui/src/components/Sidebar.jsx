import { NavLink } from 'react-router-dom';
import Icon from './Icon';
import './Sidebar.css';

export default function Sidebar({ links }) {
  return (
    <aside className="sidebar">
      <nav className="sidebar-nav" aria-label="Dashboard">
        {links.map(link => (
          <NavLink
            key={link.path}
            to={link.path}
            end
            className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
          >
            <Icon name={link.icon} size={18} className="sidebar-icon" />
            <span>{link.label}</span>
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
