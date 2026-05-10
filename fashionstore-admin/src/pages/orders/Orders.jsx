import { useEffect, useMemo, useState } from 'react';
import { Eye, PackageCheck, PackageX, Truck, CheckCircle, RotateCcw } from 'lucide-react';
import DataTable from '../../components/DataTable.jsx';
import { OrdersApi } from '../../api/client.js';
import { useToast } from '../../context/ToastContext.jsx';

const STATUS_TABS = ['all', 'pending', 'processing', 'shipped', 'delivered', 'cancelled'];

export default function Orders() {
  const { addToast } = useToast();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('all');
  const [actionId, setActionId] = useState(null);
  const [detailOrder, setDetailOrder] = useState(null);

  useEffect(() => {
    (async () => {
      try {
        const data = await OrdersApi.list(100);
        setOrders(Array.isArray(data) ? data : []);
      } catch {
        addToast('Failed to load orders', 'error');
      } finally {
        setLoading(false);
      }
    })();
  }, [addToast]);

  const filtered = useMemo(() => {
    if (activeTab === 'all') return orders;
    return orders.filter((o) => (o.status || 'pending') === activeTab);
  }, [orders, activeTab]);

  const doAction = async (apiFn, id, successMsg) => {
    setActionId(id);
    try {
      await apiFn(id);
      setOrders((prev) => prev.map((o) => (o.id === id ? { ...o, status: inferStatus(apiFn) } : o)));
      addToast(successMsg, 'success');
    } catch {
      addToast('Action failed', 'error');
    } finally {
      setActionId(null);
    }
  };

  const inferStatus = (fn) => {
    if (fn === OrdersApi.approve) return 'processing';
    if (fn === OrdersApi.cancel) return 'cancelled';
    if (fn === OrdersApi.ship) return 'shipped';
    if (fn === OrdersApi.deliver) return 'delivered';
    if (fn === OrdersApi.refund) return 'cancelled';
    return 'pending';
  };

  const columns = [
    { key: 'id', header: 'Order ID', width: '100px' },
    { key: 'customer', header: 'Customer', render: (r) => r.customerName || r.customer?.name || r.user?.email || '—' },
    { key: 'date', header: 'Date', width: '120px', render: (r) => r.createdAt ? new Date(r.createdAt).toLocaleDateString() : '—' },
    { key: 'total', header: 'Total', width: '90px', render: (r) => `$${(r.total || 0).toFixed(2)}` },
    { key: 'status', header: 'Status', width: '110px', render: (r) => <StatusBadge status={r.status} /> },
    { key: 'payment', header: 'Payment', width: '100px', render: (r) => <span className="pill">{r.paymentStatus || 'pending'}</span> },
    {
      key: 'actions',
      header: '',
      width: '180px',
      render: (r) => (
        <div className="flex items-center gap-1">
          <ActionBtn icon={Eye} title="View" onClick={() => setDetailOrder(r)} />
          {r.status === 'pending' && <ActionBtn icon={PackageCheck} title="Approve" onClick={() => doAction(OrdersApi.approve, r.id, 'Order approved')} disabled={actionId === r.id} />}
          {r.status === 'pending' && <ActionBtn icon={PackageX} title="Cancel" onClick={() => doAction(OrdersApi.cancel, r.id, 'Order cancelled')} disabled={actionId === r.id} danger />}
          {r.status === 'processing' && <ActionBtn icon={Truck} title="Ship" onClick={() => doAction(OrdersApi.ship, r.id, 'Order shipped')} disabled={actionId === r.id} />}
          {r.status === 'shipped' && <ActionBtn icon={CheckCircle} title="Deliver" onClick={() => doAction(OrdersApi.deliver, r.id, 'Order delivered')} disabled={actionId === r.id} />}
          {['delivered', 'completed'].includes(r.status) && <ActionBtn icon={RotateCcw} title="Refund" onClick={() => doAction(OrdersApi.refund, r.id, 'Order refunded')} disabled={actionId === r.id} danger />}
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-5">
      <h1 className="text-2xl font-bold text-ink-900 dark:text-white">Orders</h1>

      <div className="flex flex-wrap gap-2">
        {STATUS_TABS.map((tab) => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            className={[
              'px-3 py-1.5 rounded-lg text-sm font-medium transition',
              activeTab === tab
                ? 'bg-ink-900 text-white dark:bg-white dark:text-ink-900'
                : 'bg-white dark:bg-ink-800 border border-ink-200 dark:border-ink-700 text-ink-600 dark:text-ink-300 hover:bg-ink-50 dark:hover:bg-ink-700',
            ].join(' ')}
          >
            {tab === 'all' ? 'All' : tab.charAt(0).toUpperCase() + tab.slice(1)}
            {tab !== 'all' && (
              <span className="ml-1.5 text-[10px] bg-ink-100 dark:bg-ink-700 px-1.5 py-0.5 rounded-full">
                {orders.filter((o) => (o.status || 'pending') === tab).length}
              </span>
            )}
          </button>
        ))}
      </div>

      <div className="card">
        {loading ? (
          <div className="p-8 space-y-3">
            {[1, 2, 3, 4, 5].map((i) => (
              <div key={i} className="h-10 rounded bg-ink-100 dark:bg-ink-800 animate-pulse" />
            ))}
          </div>
        ) : (
          <DataTable columns={columns} rows={filtered} empty="No orders found" getRowKey={(r) => r.id} />
        )}
      </div>

      {/* Order Detail Modal */}
      {detailOrder && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
          <div className="bg-white dark:bg-ink-800 rounded-xl shadow-xl max-w-lg w-full max-h-[80vh] overflow-auto p-6 border border-ink-200 dark:border-ink-700">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-bold text-ink-900 dark:text-white">Order #{detailOrder.id}</h2>
              <button onClick={() => setDetailOrder(null)} className="text-ink-400 hover:text-ink-600 dark:hover:text-ink-200">×</button>
            </div>
            <div className="space-y-3 text-sm">
              <div className="flex justify-between"><span className="text-ink-500">Customer</span><span className="font-medium text-ink-900 dark:text-white">{detailOrder.customerName || detailOrder.customer?.name || '—'}</span></div>
              <div className="flex justify-between"><span className="text-ink-500">Email</span><span className="font-medium text-ink-900 dark:text-white">{detailOrder.customerEmail || detailOrder.customer?.email || '—'}</span></div>
              <div className="flex justify-between"><span className="text-ink-500">Status</span><StatusBadge status={detailOrder.status} /></div>
              <div className="flex justify-between"><span className="text-ink-500">Total</span><span className="font-medium text-ink-900 dark:text-white">${(detailOrder.total || 0).toFixed(2)}</span></div>
              <div className="flex justify-between"><span className="text-ink-500">Date</span><span className="font-medium text-ink-900 dark:text-white">{detailOrder.createdAt ? new Date(detailOrder.createdAt).toLocaleString() : '—'}</span></div>
              {detailOrder.items?.length > 0 && (
                <div className="pt-3 border-t border-ink-200 dark:border-ink-700">
                  <span className="text-ink-500 mb-2 block">Items</span>
                  <ul className="space-y-1">
                    {detailOrder.items.map((item, i) => (
                      <li key={i} className="flex justify-between text-ink-700 dark:text-ink-200">
                        <span>{item.name || item.productName} x{item.quantity}</span>
                        <span>${((item.price || 0) * (item.quantity || 1)).toFixed(2)}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
            <div className="mt-5 flex justify-end">
              <button onClick={() => setDetailOrder(null)} className="btn-ghost">Close</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function StatusBadge({ status }) {
  const map = {
    pending: 'pill-pending',
    processing: 'pill-info',
    shipped: 'pill-shipped',
    delivered: 'pill-delivered',
    cancelled: 'pill-cancelled',
    completed: 'pill-completed',
  };
  return <span className={`pill ${map[status] || 'pill'}`}>{status}</span>;
}

function ActionBtn({ icon: Icon, onClick, disabled, danger, title }) {
  return (
    <button
      onClick={onClick}
      disabled={disabled}
      title={title}
      className={[
        'p-1.5 rounded-md transition disabled:opacity-40',
        danger
          ? 'hover:bg-rose-50 dark:hover:bg-rose-900/30 text-rose-500'
          : 'hover:bg-ink-100 dark:hover:bg-ink-700 text-ink-500 dark:text-ink-300',
      ].join(' ')}
    >
      <Icon size={14} />
    </button>
  );
}
