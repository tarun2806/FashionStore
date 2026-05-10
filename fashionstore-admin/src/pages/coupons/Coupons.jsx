import { useEffect, useState } from 'react';
import { Plus, Pencil, Trash2, Save, X, Percent } from 'lucide-react';
import { CouponsApi } from '../../api/client.js';
import { useToast } from '../../context/ToastContext.jsx';

export default function Coupons() {
  const { addToast } = useToast();
  const [coupons, setCoupons] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState({
    code: '',
    discountType: 'percentage',
    discountValue: '',
    minOrder: '',
    maxUses: '',
    expiresAt: '',
  });
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    fetchCoupons();
  }, []);

  const fetchCoupons = async () => {
    try {
      const data = await CouponsApi.list();
      setCoupons(Array.isArray(data) ? data : []);
    } catch {
      addToast('Failed to load coupons', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    if (!form.code.trim() || !form.discountValue) return;
    setSaving(true);
    try {
      const payload = {
        ...form,
        discountValue: Number(form.discountValue),
        minOrder: Number(form.minOrder) || 0,
        maxUses: Number(form.maxUses) || 0,
      };
      if (editingId) {
        await CouponsApi.update(editingId, payload);
        addToast('Coupon updated', 'success');
      } else {
        await CouponsApi.create(payload);
        addToast('Coupon created', 'success');
      }
      setForm({ code: '', discountType: 'percentage', discountValue: '', minOrder: '', maxUses: '', expiresAt: '' });
      setEditingId(null);
      fetchCoupons();
    } catch {
      addToast('Save failed', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleEdit = (c) => {
    setEditingId(c.id);
    setForm({
      code: c.code || '',
      discountType: c.discountType || 'percentage',
      discountValue: c.discountValue ?? '',
      minOrder: c.minOrder ?? '',
      maxUses: c.maxUses ?? '',
      expiresAt: c.expiresAt ? c.expiresAt.slice(0, 10) : '',
    });
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this coupon?')) return;
    try {
      await CouponsApi.delete(id);
      setCoupons((prev) => prev.filter((c) => c.id !== id));
      addToast('Coupon deleted', 'success');
    } catch {
      addToast('Delete failed', 'error');
    }
  };

  return (
    <div className="space-y-5">
      <h1 className="text-2xl font-bold text-ink-900 dark:text-white">Coupons</h1>

      <div className="card p-5 space-y-4 max-w-2xl">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-lg bg-ink-100 dark:bg-ink-700 flex items-center justify-center">
            <Percent size={18} className="text-ink-400" />
          </div>
          <h2 className="text-sm font-semibold text-ink-900 dark:text-white">
            {editingId ? 'Edit Coupon' : 'New Coupon'}
          </h2>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          <input placeholder="Code (e.g. SAVE20)" value={form.code} onChange={(e) => setForm((f) => ({ ...f, code: e.target.value.toUpperCase() }))} className="input" />
          <select value={form.discountType} onChange={(e) => setForm((f) => ({ ...f, discountType: e.target.value }))} className="input">
            <option value="percentage">Percentage %</option>
            <option value="fixed">Fixed Amount</option>
          </select>
          <input type="number" placeholder="Discount value" value={form.discountValue} onChange={(e) => setForm((f) => ({ ...f, discountValue: e.target.value }))} className="input" />
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          <input type="number" placeholder="Min order ($)" value={form.minOrder} onChange={(e) => setForm((f) => ({ ...f, minOrder: e.target.value }))} className="input" />
          <input type="number" placeholder="Max uses" value={form.maxUses} onChange={(e) => setForm((f) => ({ ...f, maxUses: e.target.value }))} className="input" />
          <input type="date" value={form.expiresAt} onChange={(e) => setForm((f) => ({ ...f, expiresAt: e.target.value }))} className="input" />
        </div>
        <div className="flex items-center gap-2">
          <button onClick={handleSave} disabled={saving} className="btn-primary">
            <Save size={14} /> {saving ? 'Saving…' : editingId ? 'Update' : 'Create'}
          </button>
          {editingId && (
            <button onClick={() => { setEditingId(null); setForm({ code: '', discountType: 'percentage', discountValue: '', minOrder: '', maxUses: '', expiresAt: '' }); }} className="btn-ghost">
              <X size={14} /> Cancel
            </button>
          )}
        </div>
      </div>

      <div className="card">
        {loading ? (
          <div className="p-8 space-y-3">
            {[1, 2, 3].map((i) => (
              <div key={i} className="h-10 rounded bg-ink-100 dark:bg-ink-800 animate-pulse" />
            ))}
          </div>
        ) : (
          <table className="w-full text-sm text-left">
            <thead>
              <tr className="border-b border-ink-200 dark:border-ink-700 bg-ink-50/50 dark:bg-ink-900/50">
                <th className="px-5 py-3 font-semibold text-[11px] uppercase tracking-wider text-ink-400">Code</th>
                <th className="px-5 py-3 font-semibold text-[11px] uppercase tracking-wider text-ink-400">Type</th>
                <th className="px-5 py-3 font-semibold text-[11px] uppercase tracking-wider text-ink-400">Value</th>
                <th className="px-5 py-3 font-semibold text-[11px] uppercase tracking-wider text-ink-400">Expires</th>
                <th className="px-5 py-3 font-semibold text-[11px] uppercase tracking-wider text-ink-400 w-24"></th>
              </tr>
            </thead>
            <tbody className="divide-y divide-ink-200 dark:divide-ink-700">
              {coupons.length === 0 ? (
                <tr><td colSpan={5} className="px-5 py-8 text-center text-ink-400">No coupons</td></tr>
              ) : (
                coupons.map((c) => (
                  <tr key={c.id} className="hover:bg-ink-50 dark:hover:bg-ink-700/50 transition">
                    <td className="px-5 py-3 font-mono font-semibold text-ink-900 dark:text-ink-100">{c.code}</td>
                    <td className="px-5 py-3 text-ink-600 dark:text-ink-300 capitalize">{c.discountType}</td>
                    <td className="px-5 py-3 text-ink-600 dark:text-ink-300">{c.discountType === 'percentage' ? `${c.discountValue}%` : `$${c.discountValue}`}</td>
                    <td className="px-5 py-3 text-ink-600 dark:text-ink-300">{c.expiresAt ? new Date(c.expiresAt).toLocaleDateString() : '—'}</td>
                    <td className="px-5 py-3">
                      <div className="flex items-center gap-1">
                        <button onClick={() => handleEdit(c)} className="p-1.5 rounded hover:bg-ink-100 dark:hover:bg-ink-700 text-ink-500"><Pencil size={14} /></button>
                        <button onClick={() => handleDelete(c.id)} className="p-1.5 rounded hover:bg-rose-50 dark:hover:bg-rose-900/30 text-rose-500"><Trash2 size={14} /></button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
