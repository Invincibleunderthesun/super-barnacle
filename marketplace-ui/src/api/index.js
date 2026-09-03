import api from './axios';

export const authAPI = {
  login: (data) => api.post('/auth/login', data),
  register: (data) => api.post('/auth/register', data),
  registerSeller: (data) => api.post('/sellers/register', data),
  forgotPassword: (data) => api.post('/auth/forgot-password', data),
  resetPassword: (data) => api.post('/auth/reset-password', data),
  refresh: () => api.post('/auth/refresh'),
};

export const productsAPI = {
  getAll: (page = 0, size = 12, sortBy = 'id', sortDir = 'desc') =>
    api.get(`/products?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}`),
  getById: (id) => api.get(`/products/${id}`),
  search: (query, page = 0, size = 12) =>
    api.get(`/products/search?query=${query}&page=${page}&size=${size}`),
  filter: (minPrice, maxPrice, page = 0, size = 12) =>
    api.get(`/products/filter?minPrice=${minPrice || ''}&maxPrice=${maxPrice || ''}&page=${page}&size=${size}`),
  create: (data) => api.post('/products', data),
  update: (id, data) => api.put(`/products/${id}`, data),
  delete: (id) => api.delete(`/products/${id}`),
  lowStock: (threshold = 5) => api.get(`/products/low-stock?threshold=${threshold}`),
};

export const cartAPI = {
  get: (userId) => api.get(`/cart/user/${userId}`),
  add: (userId, productId, quantity = 1) =>
    api.post(`/cart/user/${userId}/add?productId=${productId}&quantity=${quantity}`),
  remove: (userId, productId) =>
    api.delete(`/cart/user/${userId}/remove?productId=${productId}`),
  updateQty: (userId, productId, quantity) =>
    api.put(`/cart/user/${userId}/update?productId=${productId}&quantity=${quantity}`),
  clear: (userId) => api.delete(`/cart/user/${userId}/clear`),
};

export const ordersAPI = {
  checkout: (userId, addressId) =>
    api.post(`/orders/checkout/user/${userId}?addressId=${addressId}`),
  getByUser: (userId) => api.get(`/orders/user/${userId}`),
  getById: (id) => api.get(`/orders/${id}`),
  cancel: (orderId, userId) => api.delete(`/orders/${orderId}/cancel/user/${userId}`),
  getStats: () => api.get('/orders/stats'),
  getAll: (page = 0, size = 10) => api.get(`/orders?page=${page}&size=${size}`),
};

export const sellersAPI = {
  getProfile: () => api.get('/sellers/me'),
  updateProfile: (data) => api.put('/sellers/me', data),
  getProducts: (page = 0, size = 10) =>
    api.get(`/sellers/me/products?page=${page}&size=${size}`),
  addProduct: (data) => api.post('/sellers/me/products', data),
  updateProduct: (id, data) => api.put(`/sellers/me/products/${id}`, data),
  deleteProduct: (id) => api.delete(`/sellers/me/products/${id}`),
  getStats: () => api.get('/sellers/me/stats'),
  getPublicProfile: (id) => api.get(`/sellers/${id}`),
  getPublicProducts: (id, page = 0, size = 12) =>
    api.get(`/sellers/${id}/products?page=${page}&size=${size}`),
  // Admin
  getAll: (page = 0, size = 10) => api.get(`/sellers?page=${page}&size=${size}`),
  verify: (id, verified) => api.put(`/sellers/${id}/verify?verified=${verified}`),
  setCommission: (id, rate) => api.put(`/sellers/${id}/commission?rate=${rate}`),
};

export const addressAPI = {
  getAll: (userId) => api.get(`/addresses/user/${userId}`),
  create: (userId, data) => api.post(`/addresses/user/${userId}`, data),
  update: (id, data) => api.put(`/addresses/${id}`, data),
  delete: (id) => api.delete(`/addresses/${id}`),
  setDefault: (userId, id) => api.put(`/addresses/user/${userId}/default/${id}`),
};

export const paymentsAPI = {
  create: (orderId) => api.post(`/payments/create/${orderId}`),
  verify: (data) => api.post('/payments/verify', data),
};

export const usersAPI = {
  getAll: (page = 0, size = 10) => api.get(`/users?page=${page}&size=${size}`),
  getProfile: (id) => api.get(`/users/${id}`),
  updateProfile: (id, data) => api.put(`/users/${id}`, data),
};
