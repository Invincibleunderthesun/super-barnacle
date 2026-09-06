import { Outlet } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Sidebar from '../components/Sidebar';

export default function DashboardLayout({ links }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <Navbar />
      <div style={{ display: 'flex', flex: 1 }}>
        <Sidebar links={links} />
        <main style={{ flex: 1, padding: 'var(--space-xl)', overflowY: 'auto' }}>
          <Outlet />
        </main>
      </div>
    </div>
  );
}
