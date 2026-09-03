import { useState, useEffect } from 'react';
import { addressAPI } from '../../api';
import { useAuth } from '../../context/AuthContext';

export default function MyAddresses() {
  const { user } = useAuth();
  const [addresses, setAddresses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editId, setEditId] = useState(null);
  const [form, setForm] = useState({ fullName: '', phone: '', addressLine1: '', addressLine2: '', city: '', state: '', pincode: '' });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const fetchAddresses = () => {
    setLoading(true);
    addressAPI.getAll(user.id)
      .then(res => setAddresses(res.data || []))
      .catch(() => setAddresses([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => { if (user) fetchAddresses(); }, [user]);

  const openNew = () => { setEditId(null); setForm({ fullName: '', phone: '', addressLine1: '', addressLine2: '', city: '', state: '', pincode: '' }); setShowForm(true); };
  const openEdit = (a) => { setEditId(a.id); setForm({ fullName: a.fullName, phone: a.phone, addressLine1: a.addressLine1, addressLine2: a.addressLine2 || '', city: a.city, state: a.state, pincode: a.pincode }); setShowForm(true); };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true); setError('');
    try {
      if (editId) await addressAPI.update(editId, form);
      else await addressAPI.create(user.id, form);
      setShowForm(false); fetchAddresses();
    } catch (err) { setError(err.message || 'Failed to save'); }
    finally { setSaving(false); }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this address?')) return;
    try { await addressAPI.delete(id); fetchAddresses(); } catch {}
  };

  const handleSetDefault = async (id) => {
    try { await addressAPI.setDefault(user.id, id); fetchAddresses(); } catch {}
  };

  const set = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  if (loading) return <div className="loading-screen"><div className="spinner" /></div>;

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--space-lg)' }}>
        <h2>My Addresses</h2>
        <button className="btn btn-primary" onClick={openNew}>+ Add Address</button>
      </div>

      {showForm && (
        <div className="modal-overlay" onClick={() => setShowForm(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>{editId ? 'Edit Address' : 'Add Address'}</h3>
              <button className="modal-close" onClick={() => setShowForm(false)}>×</button>
            </div>
            {error && <div className="alert alert-error mb-md">{error}</div>}
            <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-md)' }}>
              <div className="grid-2">
                <div className="input-group"><label>Full Name *</label><input required value={form.fullName} onChange={set('fullName')} /></div>
                <div className="input-group"><label>Phone *</label><input required value={form.phone} onChange={set('phone')} placeholder="10-digit mobile" /></div>
              </div>
              <div className="input-group"><label>Address Line 1 *</label><input required value={form.addressLine1} onChange={set('addressLine1')} /></div>
              <div className="input-group"><label>Address Line 2</label><input value={form.addressLine2} onChange={set('addressLine2')} /></div>
              <div className="grid-2">
                <div className="input-group"><label>City *</label><input required value={form.city} onChange={set('city')} /></div>
                <div className="input-group"><label>State *</label><input required value={form.state} onChange={set('state')} /></div>
              </div>
              <div className="input-group"><label>Pincode *</label><input required value={form.pincode} onChange={set('pincode')} placeholder="6-digit" /></div>
              <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving...' : editId ? 'Update' : 'Add Address'}</button>
            </form>
          </div>
        </div>
      )}

      {addresses.length === 0 ? (
        <div className="empty-state"><div className="icon">📍</div><p>No addresses saved</p></div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-md)' }}>
          {addresses.map(a => (
            <div key={a.id} className="card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 'var(--space-md)' }}>
              <div style={{ flex: 1 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <p style={{ fontWeight: 600 }}>{a.fullName}</p>
                  {a.default && <span className="badge badge-paid">Default</span>}
                </div>
                <p className="text-secondary text-sm">{a.addressLine1}{a.addressLine2 ? `, ${a.addressLine2}` : ''}, {a.city}, {a.state} - {a.pincode}</p>
                <p className="text-muted text-sm">📞 {a.phone}</p>
              </div>
              <div style={{ display: 'flex', gap: '0.5rem', flexShrink: 0 }}>
                {!a.default && <button className="btn btn-ghost btn-sm" onClick={() => handleSetDefault(a.id)}>Set Default</button>}
                <button className="btn btn-ghost btn-sm" onClick={() => openEdit(a)}>✏️</button>
                <button className="btn btn-ghost btn-sm" onClick={() => handleDelete(a.id)}>🗑️</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
