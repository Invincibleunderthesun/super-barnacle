import { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { usersAPI } from '../../api';
import Icon from '../../components/Icon';

export default function MyProfile() {
  const { user } = useAuth();
  const [form, setForm] = useState({ username: user?.username || '', email: user?.email || '' });
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true); setMessage('');
    try {
      await usersAPI.updateProfile(user.id, form);
      setMessage('Profile updated!');
    } catch (err) {
      setMessage(`${err.message}`);
    } finally { setSaving(false); }
  };

  const set = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  return (
    <div>
      <h2 style={{ marginBottom: 'var(--space-lg)' }}>My Profile</h2>
      <div className="card" style={{ maxWidth: '500px' }}>
        {message && <div className={`alert ${message.includes('updated') ? 'alert-success' : 'alert-error'} mb-md`}>{message.includes('updated') ? <Icon name="check" size={16} /> : <Icon name="info" size={16} />} {message}</div>}
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-md)' }}>
          <div className="input-group"><label>Username</label><input value={form.username} onChange={set('username')} /></div>
          <div className="input-group"><label>Email</label><input type="email" value={form.email} onChange={set('email')} /></div>
          <div className="input-group">
            <label>Role</label>
            <input value={user?.role?.replace('ROLE_', '')} disabled style={{ opacity: 0.5 }} />
          </div>
          <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving...' : 'Update Profile'}</button>
        </form>
      </div>
    </div>
  );
}
