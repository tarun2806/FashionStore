import { useEffect, useState } from 'react';
import DataTable from '../components/DataTable';
import { ProductsApi } from '../api/client';

export default function Inventory() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    ProductsApi.list()
      .then((res) => setProducts(res.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  const columns = [
    { key: 'id', header: 'ID', width: '80px' },
    { key: 'name', header: 'Product Name' },
    { key: 'sku', header: 'SKU', width: '120px' },
    { 
      key: 'stock', 
      header: 'Stock', 
      width: '100px',
      render: (row) => (
        <span className={`px-2 py-1 rounded-full text-xs font-medium ${
          row.stock > 10 
            ? 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400' 
            : row.stock > 0 
              ? 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-400'
              : 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400'
        }`}>
          {row.stock}
        </span>
      )
    },
    { key: 'price', header: 'Price', width: '100px', render: (row) => `$${row.price.toFixed(2)}` },
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold text-ink-900 dark:text-ink-50">Inventory</h1>
      </div>

      <div className="bg-white dark:bg-ink-800 rounded-lg border border-ink-200 dark:border-ink-700 p-6">
        <DataTable 
          columns={columns} 
          rows={products} 
          empty="No products in inventory"
          getRowKey={(row) => row.id}
        />
      </div>
    </div>
  );
}
