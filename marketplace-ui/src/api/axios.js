import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_URL
  ? import.meta.env.VITE_API_URL.replace(/\/$/, '')
  : 'http://localhost:8080/api/v1';

const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
});

// Request interceptor — attach JWT token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor — handle 401 and extract data
api.interceptors.response.use(
  (response) => response.data,
  async (error) => {
    if (error.response?.status === 401) {
      // Try token refresh
      const token = localStorage.getItem('token');
      if (token && !error.config._retry) {
        error.config._retry = true;
        try {
          const res = await axios.post(`${API_BASE}/auth/refresh`, null, {
            headers: { Authorization: `Bearer ${token}` },
          });
          const newToken = res.data?.data?.token;
          if (newToken) {
            localStorage.setItem('token', newToken);
            error.config.headers.Authorization = `Bearer ${newToken}`;
            return api(error.config);
          }
        } catch {
          // Refresh failed — force logout
          localStorage.clear();
          window.location.href = '/login';
        }
      } else {
        localStorage.clear();
        window.location.href = '/login';
      }
    }
    const message = error.response?.data?.message || error.message || 'Something went wrong';
    return Promise.reject({ message, status: error.response?.status, data: error.response?.data });
  }
);

export default api;
