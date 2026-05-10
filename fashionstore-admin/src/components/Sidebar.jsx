import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Package,
  Boxes,
  ShoppingCart,
  Users,
  ShieldCheck,
  X,
  Tags,
  TicketPercent,
  Settings,
  LogOut,
  ChevronRight,
} from 'lucide-react';

const NAV = [
  { to: '/dashboard',  label: 'Dashboard',  Icon: LayoutDashboard },
  { to: '/products',   label: 'Products',   Icon: Package },
  { to: '/inventory',  label: 'Inventory',  Icon: Boxes },
  { to: '/orders',     label: 'Orders',     Icon: ShoppingCart },
  { to: '/users',      label: 'Users',      Icon: Users },
  { to: '/categories', label: 'Categories', Icon: Tags },
  { to: '/coupons',    label: 'Coupons',    Icon: TicketPercent },
  { to: '/settings',   label: 'Settings',   Icon: Settings },
];

export default function Sidebar({ mobileOpen, onClose }) {
  return (
    <aside
      aria-label="Primary navigation"
      className={[
        'fixed inset-y-0 left-0 z-40 w-64 bg-white dark:bg-ink-800',
        'border-r border-ink-200 dark:border-ink-700',
        'flex flex-col px-4 py-5 transition-transform duration-300 ease-out',
        mobileOpen ? 'translate-x-0' : '-translate-x-full',
        'lg:translate-x-0',
        'shadow-xl lg:shadow-none',
      ].join(' ')}
    >
      <div className="flex items-center gap-3 px-2 pb-6 mb-4 border-b border-ink-200 dark:border-ink-700">
        <div className="relative">
          <ShieldCheck size={24} strokeWidth={2.2} className="text-ink-900 dark:text-white" />
          <div className="absolute -bottom-0.5 -right-0.5 w-2 h-2 bg-emerald-500 rounded-full border-2 border-white dark:border-ink-800" />
        </div>
        <div className="flex-1">
          <span className="block font-bold tracking-tight text-lg text-ink-900 dark:text-white">
            FashionStore
          </span>
          <span className="block text-[10px] font-semibold uppercase tracking-wider text-ink-400 dark:text-ink-500">
            Admin Panel
          </span>
        </div>
        <button
          onClick={onClose}
          className="lg:hidden p-1.5 rounded-lg hover:bg-ink-100 dark:hover:bg-ink-700 text-ink-400 hover:text-ink-700 dark:hover:text-ink-100 transition-colors"
          aria-label="Close sidebar"
        >
          <X size={20} />
        </button>
      </div>

      <nav className="flex flex-col gap-1 flex-1 overflow-y-auto">
        {NAV.map(({ to, label, Icon }) => (
          <NavLink
            key={to}
            to={to}
            onClick={onClose}
            end={to === '/dashboard'}
            className={({ isActive }) =>
              [
                'group flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all duration-200',
                'hover:shadow-sm',
                isActive
                  ? 'bg-ink-900 text-white dark:bg-white dark:text-ink-900 shadow-md'
                  : 'text-ink-600 hover:bg-ink-100 hover:text-ink-900 dark:text-ink-300 dark:hover:bg-ink-700 dark:hover:text-white',
              ].join(' ')
            }
          >
            <Icon size={18} strokeWidth={2} className="transition-transform group-hover:scale-110" />
            <span className="flex-1">{label}</span>
            <ChevronRight 
              size={14} 
              strokeWidth={2.5} 
              className={[
                'opacity-0 -translate-x-1 transition-all duration-200',
                'group-hover:opacity-100 group-hover:translate-x-0',
              ].join(' ')}
            />
          </NavLink>
        ))}
      </nav>

      <div className="mt-auto pt-4 border-t border-ink-200 dark:border-ink-700">
        <NavLink
          to="/logout"
          className="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium text-ink-600 hover:bg-red-50 hover:text-red-600 dark:text-ink-300 dark:hover:bg-red-900/20 dark:hover:text-red-400 transition-all duration-200"
        >
          <LogOut size={18} strokeWidth={2} />
          <span>Logout</span>
        </NavLink>
        <div className="mt-4 px-3 py-2 text-[11px] uppercase tracking-wider text-ink-400 dark:text-ink-500 font-medium">
          <div className="flex items-center justify-between">
            <span>Version</span>
            <span className="text-ink-900 dark:text-white font-semibold">1.0.0</span>
          </div>
        </div>
      </div>
    </aside>
  );
}
