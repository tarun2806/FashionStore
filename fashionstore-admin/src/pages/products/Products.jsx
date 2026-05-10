import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Search, Pencil, Trash2, Filter } from 'lucide-react';
import DataTable from '../../components/DataTable.jsx';
import { ProductsApi } from '../../api/client.js';
import { useToast } from '../../context/ToastContext.jsx';

const STATUS_OPTIONS = ['all', 'active', 'inactive', 'out_of_stock'];

export default function Products() {
  const navigate = useNavigate();
  const { addToast } = useToast();

  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [deletingId, setDeletingId] = useState(null);

  useEffect(() => {
    (async () => {
      try {
        const data = await ProductsApi.list();
        setProducts(Array.isArray(data) ? data : []);
      } catch {
        addToast('Failed to load products', 'error');
      } finally {
        setLoading(false);
      }
    })();
  }, [addToast]);

  const filtered = useMemo(() => {
    let rows = products;
    if (search.trim()) {
      const q = search.toLowerCase();
      rows = rows.filter((r) =>
        (r.name || '').toLowerCase().includes(q) ||
        (r.sku || '').toLowerCase().includes(q) ||
        (r.category || '').toLowerCase().includes(q)
      );
    }
    if (statusFilter !== 'all') {
      rows = rows.filter((r) => (r.status || 'active') === statusFilter);
    }
    return rows;
  }, [products, search, statusFilter]);

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this product?')) return;
    setDeletingId(id);
    try {
      await ProductsApi.delete?.(id);
      setProducts((prev) => prev.filter((p) => p.id !== id));
      addToast('Product deleted', 'success');
    } catch {
      addToast('Delete failed', 'error');
    } finally {
      setDeletingId(null);
    }
  };

  const columns = [
    {
      key: 'image',
      header: 'Image',
      width: '64px',
      render: (r) => (
        <div className="w-10 h-10 rounded-lg bg-ink-100 dark:bg-ink-700 flex items-center justify-center overflow-hidden">
          {r.imageUrl ? (
            <img src={r.imageUrl} alt="" className="w-full h-full object-cover" />
          ) : (
            <span className="text-xs text-ink-400">—</span>
          )}
        </div>
      ),
    },
    { key: 'name', header: 'Product Name' },
    { key: 'category', header: 'Category', width: '140px' },
    { key: 'price', header: 'Price', width: '100px', render: (r) => `$${(r.price || 0).toFixed(2)}` },
    {
      key: 'stock',
      header: 'Stock',
      width: '90px',
      render: (r) => (
        <span className={`text-xs font-semibold ${(r.stock || 0) <= 5 ? 'text-rose-600' : 'text-emerald-600'}`}>
          {r.stock || 0}
        </span>
      ),
    },
    {
      key: 'status',
      header: 'Status',
      width: '100px',
      render: (r) => {
        const s = r.status || 'active';
        const cls = s === 'active' ? 'pill-success' : s === 'inactive' ? 'pill-warning' : 'pill-danger';
        return <span className={`pill ${cls}`}>{s}</span>;
      },
    },
    {
      key: 'actions',
      header: '',
      width: '100px',
      render: (r) => (
        <div className="flex items-center gap-2">
          <button
            onClick={() => navigate(`/products/${r.id}/edit`)}
            className="p-1.5 rounded-md hover:bg-ink-100 dark:hover:bg-ink-700 text-ink-500 dark:text-ink-300"
            title="Edit"
          >
            <Pencil size={14} />
          </button>
          <button
            onClick={() => handleDelete(r.id)}
            disabled={deletingId === r.id}
            className="p-1.5 rounded-md hover:bg-rose-50 dark:hover:bg-rose-900/30 text-rose-500 disabled:opacity-50"
            title="Delete"
          >
            <Trash2 size={14} />
          </button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-5">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <h1 className="text-2xl font-bold text-ink-900 dark:text-white">Products</h1>
        <button
          onClick={() => navigate('/products/new')}
          className="btn-primary self-start"
        >
          <Plus size={16} /> Add Product
        </button>
      </div>

      <div className="card p-4 flex flex-col sm:flex-row gap-3">
        <div className="flex items-center gap-2 px-3 h-9 rounded-lg border border-ink-200 dark:border-ink-700 bg-ink-50 dark:bg-ink-900 text-ink-400 flex-1">
          <Search size={16} />
          <input
            type="search"
            placeholder="Search products by name, SKU, category…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="flex-1 bg-transparent text-sm text-ink-900 dark:text-ink-100 placeholder:text-ink-400 focus:outline-none"
          />
        </div>
        <div className="flex items-center gap-2">
          <Filter size={16} className="text-ink-400" />
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="h-9 px-3 rounded-lg border border-ink-200 dark:border-ink-700 bg-white dark:bg-ink-900 text-sm text-ink-900 dark:text-ink-100 focus:outline-none"
          >
            {STATUS_OPTIONS.map((s) => (
              <option key={s} value={s}>{s === 'all' ? 'All Status' : s.replace('_', ' ')}</option>
            ))}
          </select>
        </div>
      </div>

      <div className="card">
        {loading ? (
          <div className="p-8 space-y-3">
            {[1, 2, 3, 4, 5].map((i) => (
              <div key={i} className="h-10 rounded bg-ink-100 dark:bg-ink-800 animate-pulse" />
            ))}
          </div>
        ) : (
          <DataTable
            columns={columns}
            rows={filtered}
            empty="No products found"
            getRowKey={(r) => r.id}
          />
        )}
      </div>
    </div>
  );
}
