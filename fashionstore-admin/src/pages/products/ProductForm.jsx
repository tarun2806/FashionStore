import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, Save, Upload } from 'lucide-react';
import { ProductsApi, CategoriesApi } from '../../api/client.js';
import { useToast } from '../../context/ToastContext.jsx';

export default function ProductForm() {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEdit = Boolean(id);
  const { addToast } = useToast();

  const [form, setForm] = useState({
    name: '',
    description: '',
    category: '',
    price: '',
    stock: '',
    discount: '',
    status: 'active',
    sku: '',
    tags: '',
    sizes: '',
  });
  const [imagePreview, setImagePreview] = useState('');
  const [categories, setCategories] = useState([]);
  const [saving, setSaving] = useState(false);
  const [loading, setLoading] = useState(isEdit);

  useEffect(() => {
    (async () => {
      try {
        const data = await CategoriesApi.list();
        setCategories(Array.isArray(data) ? data : []);
      } catch {
        // ignore
      }
    })();
  }, []);

  useEffect(() => {
    if (!isEdit) return;
    (async () => {
      try {
        const data = await ProductsApi.get?.(id);
        if (data) {
          setForm({
            name: data.name || '',
            description: data.description || '',
            category: data.category || data.categoryId || '',
            price: data.price ?? '',
            stock: data.stock ?? '',
            discount: data.discount ?? '',
            status: data.status || 'active',
            sku: data.sku || '',
            tags: Array.isArray(data.tags) ? data.tags.join(', ') : data.tags || '',
            sizes: Array.isArray(data.sizes) ? data.sizes.join(', ') : data.sizes || '',
          });
          setImagePreview(data.imageUrl || '');
        }
      } catch {
        addToast('Failed to load product', 'error');
      } finally {
        setLoading(false);
      }
    })();
  }, [id, isEdit, addToast]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((f) => ({ ...f, [name]: value }));
  };

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      const url = URL.createObjectURL(file);
      setImagePreview(url);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      const payload = {
        ...form,
        price: Number(form.price),
        stock: Number(form.stock),
        discount: Number(form.discount) || 0,
        tags: form.tags.split(',').map((t) => t.trim()).filter(Boolean),
        sizes: form.sizes.split(',').map((t) => t.trim()).filter(Boolean),
      };
      if (isEdit) {
        await ProductsApi.update?.(id, payload);
        addToast('Product updated', 'success');
      } else {
        await ProductsApi.create?.(payload);
        addToast('Product created', 'success');
      }
      navigate('/products');
    } catch {
      addToast(isEdit ? 'Update failed' : 'Create failed', 'error');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="space-y-4">
        <div className="h-8 w-40 bg-ink-100 dark:bg-ink-800 animate-pulse rounded" />
        <div className="card p-6 space-y-4">
          {[1, 2, 3, 4, 5].map((i) => (
            <div key={i} className="h-10 bg-ink-100 dark:bg-ink-800 animate-pulse rounded" />
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-5">
      <div className="flex items-center gap-3">
        <button onClick={() => navigate('/products')} className="btn-ghost px-2">
          <ArrowLeft size={16} />
        </button>
        <h1 className="text-2xl font-bold text-ink-900 dark:text-white">
          {isEdit ? 'Edit Product' : 'Add Product'}
        </h1>
      </div>

      <form onSubmit={handleSubmit} className="card p-6 space-y-5 max-w-3xl">
        {/* Image Upload */}
        <div>
          <label className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-2">Product Image</label>
          <div className="flex items-center gap-4">
            <div className="w-24 h-24 rounded-xl border border-dashed border-ink-300 dark:border-ink-600 bg-ink-50 dark:bg-ink-900 flex items-center justify-center overflow-hidden">
              {imagePreview ? (
                <img src={imagePreview} alt="Preview" className="w-full h-full object-cover" />
              ) : (
                <Upload size={20} className="text-ink-400" />
              )}
            </div>
            <label className="btn-ghost cursor-pointer">
              <Upload size={14} /> Upload Image
              <input type="file" accept="image/*" className="hidden" onChange={handleImageChange} />
            </label>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-1">Name *</label>
            <input name="name" value={form.name} onChange={handleChange} required className="input w-full" />
          </div>
          <div>
            <label className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-1">SKU</label>
            <input name="sku" value={form.sku} onChange={handleChange} className="input w-full" />
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-1">Description</label>
          <textarea name="description" value={form.description} onChange={handleChange} rows={3} className="input w-full" />
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-1">Category</label>
            <select name="category" value={form.category} onChange={handleChange} className="input w-full">
              <option value="">Select category</option>
              {categories.map((c) => (
                <option key={c.id || c.name} value={c.name || c.id}>{c.name}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-1">Status</label>
            <select name="status" value={form.status} onChange={handleChange} className="input w-full">
              <option value="active">Active</option>
              <option value="inactive">Inactive</option>
              <option value="out_of_stock">Out of Stock</option>
            </select>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div>
            <label className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-1">Price ($)</label>
            <input name="price" type="number" min="0" step="0.01" value={form.price} onChange={handleChange} required className="input w-full" />
          </div>
          <div>
            <label className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-1">Stock</label>
            <input name="stock" type="number" min="0" value={form.stock} onChange={handleChange} required className="input w-full" />
          </div>
          <div>
            <label className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-1">Discount (%)</label>
            <input name="discount" type="number" min="0" max="100" value={form.discount} onChange={handleChange} className="input w-full" />
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-1">Sizes (comma separated)</label>
            <input name="sizes" value={form.sizes} onChange={handleChange} placeholder="S, M, L, XL" className="input w-full" />
          </div>
          <div>
            <label className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-1">Tags (comma separated)</label>
            <input name="tags" value={form.tags} onChange={handleChange} placeholder="summer, new, sale" className="input w-full" />
          </div>
        </div>

        <div className="flex items-center gap-3 pt-2">
          <button type="submit" disabled={saving} className="btn-primary">
            <Save size={16} /> {saving ? 'Saving…' : isEdit ? 'Update Product' : 'Create Product'}
          </button>
          <button type="button" onClick={() => navigate('/products')} className="btn-ghost">
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}
