import { useEffect, useState } from 'react';
import {
  DollarSign, ShoppingBag, Package, Users, Clock, AlertTriangle,
} from 'lucide-react';
import {
  AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  BarChart, Bar,
} from 'recharts';
import StatCard from '../../components/StatCard.jsx';
import DataTable from '../../components/DataTable.jsx';
import { DashboardApi, OrdersApi, UsersApi, ProductsApi } from '../../api/client.js';
import { useToast } from '../../context/ToastContext.jsx';

const SALES_MOCK = [
  { name: 'Mon', revenue: 1200, orders: 12 },
  { name: 'Tue', revenue: 1900, orders: 18 },
  { name: 'Wed', revenue: 1500, orders: 15 },
  { name: 'Thu', revenue: 2200, orders: 22 },
  { name: 'Fri', revenue: 2800, orders: 28 },
  { name: 'Sat', revenue: 3400, orders: 34 },
  { name: 'Sun', revenue: 2900, orders: 29 },
];

export default function Dashboard() {
  const [stats, setStats] = useState({
    revenue: '$24,500',
    orders: 142,
    products: 0,
    customers: 0,
    pending: 0,
    lowStock: 0,
  });
  const [recentOrders, setRecentOrders] = useState([]);
  const [recentUsers, setRecentUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const { addToast } = useToast();

  useEffect(() => {
    (async () => {
      try {
        const [dash, prodRes, userRes, orderRes] = await Promise.allSettled([
          DashboardApi.fetch(),
          ProductsApi.list(),
          UsersApi.list(),
          OrdersApi.list(5),
        ]);

        const products = prodRes.status === 'fulfilled' ? prodRes.value?.length || 0 : 0;
        const customers = userRes.status === 'fulfilled' ? userRes.value?.length || 0 : 0;
        const orders = orderRes.status === 'fulfilled' ? orderRes.value || [] : [];

        setStats((s) => ({
          ...s,
          products,
          customers,
          pending: orders.filter((o) => (o.status || '').toLowerCase() === 'pending').length,
          lowStock: 0,
        }));
        setRecentOrders(orders.slice(0, 5));
        setRecentUsers(userRes.status === 'fulfilled' ? (userRes.value || []).slice(0, 5) : []);
      } catch {
        addToast('Failed to load dashboard data', 'error');
      } finally {
        setLoading(false);
      }
    })();
  }, [addToast]);

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <div key={i} className="card p-5 h-24 animate-pulse bg-ink-100 dark:bg-ink-800" />
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <StatCard icon={DollarSign} label="Total Revenue" value={stats.revenue} delta={12.5} accent="success" />
        <StatCard icon={ShoppingBag} label="Total Orders" value={stats.orders} delta={8.2} accent="primary" />
        <StatCard icon={Package} label="Products" value={stats.products} accent="info" />
        <StatCard icon={Users} label="Customers" value={stats.customers} accent="neutral" />
        <StatCard icon={Clock} label="Pending Orders" value={stats.pending} accent="warning" />
        <StatCard icon={AlertTriangle} label="Low Stock Items" value={stats.lowStock} accent="danger" />
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white dark:bg-ink-800 rounded-2xl border border-ink-200 dark:border-ink-700 p-6 hover:shadow-xl transition-shadow duration-300">
          <div className="flex items-center justify-between mb-6">
            <h3 className="text-sm font-semibold text-ink-900 dark:text-white uppercase tracking-wider">Revenue Trend</h3>
            <div className="flex items-center gap-2">
              <div className="w-2 h-2 rounded-full bg-emerald-500" />
              <span className="text-xs text-ink-500 dark:text-ink-400">This week</span>
            </div>
          </div>
          <div className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={SALES_MOCK} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
                <defs>
                  <linearGradient id="colorRev" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#10b981" stopOpacity={0.4} />
                    <stop offset="95%" stopColor="#10b981" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="currentColor" className="stroke-ink-200 dark:stroke-ink-700" opacity={0.5} />
                <XAxis 
                  dataKey="name" 
                  stroke="currentColor" 
                  className="text-ink-400 dark:text-ink-500"
                  fontSize={12}
                  tickLine={false}
                  axisLine={false}
                />
                <YAxis 
                  stroke="currentColor" 
                  className="text-ink-400 dark:text-ink-500"
                  fontSize={12}
                  tickLine={false}
                  axisLine={false}
                  tickFormatter={(value) => `$${value}`}
                />
                <Tooltip 
                  contentStyle={{ 
                    borderRadius: 12, 
                    border: 'none', 
                    boxShadow: '0 10px 40px -10px rgba(0, 0, 0, 0.15)',
                    background: 'white',
                    color: '#101010',
                    padding: '12px 16px',
                  }}
                  formatter={(value) => [`$${value}`, 'Revenue']}
                />
                <Area 
                  type="monotone" 
                  dataKey="revenue" 
                  stroke="#10b981" 
                  strokeWidth={2.5}
                  fillOpacity={1} 
                  fill="url(#colorRev)" 
                  animationDuration={1000}
                  animationEasing="ease-out"
                />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="bg-white dark:bg-ink-800 rounded-2xl border border-ink-200 dark:border-ink-700 p-6 hover:shadow-xl transition-shadow duration-300">
          <div className="flex items-center justify-between mb-6">
            <h3 className="text-sm font-semibold text-ink-900 dark:text-white uppercase tracking-wider">Orders Trend</h3>
            <div className="flex items-center gap-2">
              <div className="w-2 h-2 rounded-full bg-indigo-500" />
              <span className="text-xs text-ink-500 dark:text-ink-400">This week</span>
            </div>
          </div>
          <div className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={SALES_MOCK} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="currentColor" className="stroke-ink-200 dark:stroke-ink-700" opacity={0.5} />
                <XAxis 
                  dataKey="name" 
                  stroke="currentColor" 
                  className="text-ink-400 dark:text-ink-500"
                  fontSize={12}
                  tickLine={false}
                  axisLine={false}
                />
                <YAxis 
                  stroke="currentColor" 
                  className="text-ink-400 dark:text-ink-500"
                  fontSize={12}
                  tickLine={false}
                  axisLine={false}
                />
                <Tooltip 
                  contentStyle={{ 
                    borderRadius: 12, 
                    border: 'none', 
                    boxShadow: '0 10px 40px -10px rgba(0, 0, 0, 0.15)',
                    background: 'white',
                    color: '#101010',
                    padding: '12px 16px',
                  }}
                  formatter={(value) => [value, 'Orders']}
                />
                <Bar 
                  dataKey="orders" 
                  fill="#6366f1" 
                  radius={[6, 6, 0, 0]}
                  animationDuration={800}
                  animationEasing="ease-out"
                />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      {/* Recent Activity */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="card">
          <div className="card-head">
            <h2>Recent Orders</h2>
          </div>
          <div className="card-body">
            <DataTable
              columns={[
                { key: 'id', header: 'Order ID', width: '100px' },
                { key: 'customer', header: 'Customer' },
                { key: 'total', header: 'Total', width: '80px', render: (r) => `$${(r.total || 0).toFixed(2)}` },
                { key: 'status', header: 'Status', width: '100px', render: (r) => <StatusBadge status={r.status} /> },
              ]}
              rows={recentOrders}
              empty="No recent orders"
              getRowKey={(r) => r.id}
            />
          </div>
        </div>

        <div className="card">
          <div className="card-head">
            <h2>Recent Users</h2>
          </div>
          <div className="card-body">
            <DataTable
              columns={[
                { key: 'name', header: 'Name', render: (r) => r.fullName || r.name || r.email },
                { key: 'email', header: 'Email' },
                { key: 'role', header: 'Role', width: '80px', render: (r) => <span className="pill">{r.role || 'user'}</span> },
              ]}
              rows={recentUsers}
              empty="No recent users"
              getRowKey={(r) => r.id}
            />
          </div>
        </div>
      </div>
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
