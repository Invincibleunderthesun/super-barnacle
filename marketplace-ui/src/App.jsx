import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';

// Layouts
import PublicLayout from './layouts/PublicLayout';
import DashboardLayout from './layouts/DashboardLayout';

// Public Pages
import Home from './pages/public/Home';
import Catalog from './pages/public/Catalog';
import ProductDetail from './pages/public/ProductDetail';
import Cart from './pages/public/Cart';
import Checkout from './pages/public/Checkout';
import SellerStore from './pages/public/SellerStore';

// Auth Pages
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import SellerRegister from './pages/auth/SellerRegister';
import ForgotPassword from './pages/auth/ForgotPassword';

// User Dashboard Pages
import MyOrders from './pages/user/MyOrders';
import MyAddresses from './pages/user/MyAddresses';
import MyProfile from './pages/user/MyProfile';

// Seller Dashboard Pages
import SellerOverview from './pages/seller/SellerOverview';
import SellerProducts from './pages/seller/SellerProducts';
import SellerOrders from './pages/seller/SellerOrders';
import SellerEarnings from './pages/seller/SellerEarnings';

// Admin Dashboard Pages
import AdminOverview from './pages/admin/AdminOverview';
import AdminUsers from './pages/admin/AdminUsers';
import AdminOrders from './pages/admin/AdminOrders';

// Route guards
function ProtectedRoute({ children, allowedRoles }) {
  const { isAuthenticated, user, loading } = useAuth();
  if (loading) return <div className="loading-screen"><div className="spinner" /></div>;
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (allowedRoles && !allowedRoles.includes(user?.role)) return <Navigate to="/" replace />;
  return children;
}

// Sidebar link configs
const userLinks = [
  { path: '/dashboard/orders', label: 'My Orders', icon: '📋' },
  { path: '/dashboard/addresses', label: 'Addresses', icon: '📍' },
  { path: '/dashboard/profile', label: 'Profile', icon: '👤' },
];
const sellerLinks = [
  { path: '/seller/dashboard', label: 'Overview', icon: '📊' },
  { path: '/seller/dashboard/products', label: 'Products', icon: '📦' },
  { path: '/seller/dashboard/orders', label: 'Orders', icon: '📋' },
  { path: '/seller/dashboard/earnings', label: 'Earnings', icon: '💰' },
];
const adminLinks = [
  { path: '/admin', label: 'Overview', icon: '📊' },
  { path: '/admin/users', label: 'Users', icon: '👥' },
  { path: '/admin/orders', label: 'Orders', icon: '📋' },
];

function AppRoutes() {
  return (
    <Routes>
      {/* Public routes */}
      <Route element={<PublicLayout />}>
        <Route path="/" element={<Home />} />
        <Route path="/catalog" element={<Catalog />} />
        <Route path="/product/:id" element={<ProductDetail />} />
        <Route path="/seller/:id" element={<SellerStore />} />
        <Route path="/cart" element={
          <ProtectedRoute allowedRoles={['ROLE_USER']}>
            <Cart />
          </ProtectedRoute>
        } />
        <Route path="/checkout" element={
          <ProtectedRoute allowedRoles={['ROLE_USER']}>
            <Checkout />
          </ProtectedRoute>
        } />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/seller/register" element={<SellerRegister />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
      </Route>

      {/* User Dashboard */}
      <Route element={
        <ProtectedRoute allowedRoles={['ROLE_USER']}>
          <DashboardLayout links={userLinks} />
        </ProtectedRoute>
      }>
        <Route path="/dashboard/orders" element={<MyOrders />} />
        <Route path="/dashboard/addresses" element={<MyAddresses />} />
        <Route path="/dashboard/profile" element={<MyProfile />} />
      </Route>

      {/* Seller Dashboard */}
      <Route element={
        <ProtectedRoute allowedRoles={['ROLE_SELLER']}>
          <DashboardLayout links={sellerLinks} />
        </ProtectedRoute>
      }>
        <Route path="/seller/dashboard" element={<SellerOverview />} />
        <Route path="/seller/dashboard/products" element={<SellerProducts />} />
        <Route path="/seller/dashboard/orders" element={<SellerOrders />} />
        <Route path="/seller/dashboard/earnings" element={<SellerEarnings />} />
      </Route>

      {/* Admin Dashboard */}
      <Route element={
        <ProtectedRoute allowedRoles={['ROLE_ADMIN']}>
          <DashboardLayout links={adminLinks} />
        </ProtectedRoute>
      }>
        <Route path="/admin" element={<AdminOverview />} />
        <Route path="/admin/users" element={<AdminUsers />} />
        <Route path="/admin/orders" element={<AdminOrders />} />
      </Route>

      {/* 404 */}
      <Route path="*" element={
        <PublicLayout>
          <div className="empty-state" style={{ minHeight: '60vh', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
            <div className="icon" style={{ fontSize: '4rem' }}>🔍</div>
            <h2>Page Not Found</h2>
            <p className="text-secondary">The page you&apos;re looking for doesn&apos;t exist.</p>
          </div>
        </PublicLayout>
      } />
    </Routes>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  );
}
