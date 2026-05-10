import { useEffect, useState } from 'react';
import { Plus, Pencil, Trash2, Save, X, ImageIcon } from 'lucide-react';
import { CategoriesApi } from '../../api/client.js';
import { useToast } from '../../context/ToastContext.jsx';

export default function Categories() {
  const { addToast } = useToast();
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState({ name: '', description: '' });
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    fetchCats();
  }, []);

  const fetchCats = async () => {
    try {
      const data = await CategoriesApi.list();
      setCategories(Array.isArray(data) ? data : []);
    } catch {
      addToast('Failed to load categories', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    if (!form.name.trim()) return;
    setSaving(true);
    try {
      if (editingId) {
        await CategoriesApi.update(editingId, form);
        addToast('Category updated', 'success');
      } else {
        await CategoriesApi.create(form);
        addToast('Category created', 'success');
      }
      setForm({ name: '', description: '' });
      setEditingId(null);
      fetchCats();
    } catch {
      addToast('Save failed', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleEdit = (cat) => {
    setEditingId(cat.id);
    setForm({ name: cat.name || '', description: cat.description || '' });
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this category?')) return;
    try {
      await CategoriesApi.delete(id);
      setCategories((prev) => prev.filter((c) => c.id !== id));
      addToast('Category deleted', 'success');
    } catch {
      addToast('Delete failed', 'error');
    }
  };

  const cancelEdit = () => {
    setEditingId(null);
    setForm({ name: '', description: '' });
  };

  return (
    <div className="space-y-5">
      <h1 className="text-2xl font-bold text-ink-900 dark:text-white">Categories</h1>

      <div className="card p-5 space-y-4 max-w-xl">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-lg bg-ink-100 dark:bg-ink-700 flex items-center justify-center">
            <ImageIcon size={18} className="text-ink-400" />
          </div>
          <h2 className="text-sm font-semibold text-ink-900 dark:text-white">
            {editingId ? 'Edit Category' : 'New Category'}
          </h2>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <input
            placeholder="Category name"
            value={form.name}
            onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
            className="input"
          />
          <input
            placeholder="Description"
            value={form.description}
            onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
            className="input"
          />
        </div>
        <div className="flex items-center gap-2">
          <button onClick={handleSave} disabled={saving} className="btn-primary">
            <Save size={14} /> {saving ? 'Saving…' : editingId ? 'Update' : 'Create'}
          </button>
          {editingId && (
            <button onClick={cancelEdit} className="btn-ghost">
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
                <th className="px-5 py-3 font-semibold text-[11px] uppercase tracking-wider text-ink-400">Name</th>
                <th className="px-5 py-3 font-semibold text-[11px] uppercase tracking-wider text-ink-400">Description</th>
                <th className="px-5 py-3 font-semibold text-[11px] uppercase tracking-wider text-ink-400 w-24"></th>
              </tr>
            </thead>
            <tbody className="divide-y divide-ink-200 dark:divide-ink-700">
              {categories.length === 0 ? (
                <tr><td colSpan={3} className="px-5 py-8 text-center text-ink-400">No categories</td></tr>
              ) : (
                categories.map((c) => (
                  <tr key={c.id} className="hover:bg-ink-50 dark:hover:bg-ink-700/50 transition">
                    <td className="px-5 py-3 text-ink-900 dark:text-ink-100 font-medium">{c.name}</td>
                    <td className="px-5 py-3 text-ink-600 dark:text-ink-300">{c.description || '—'}</td>
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
