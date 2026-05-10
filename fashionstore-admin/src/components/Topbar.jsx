import { LogOut, Search, Bell, Sun, Moon, Menu } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';
import { useTheme } from '../auth/ThemeContext.jsx';

export default function Topbar({ onMenuClick }) {
  const { user, logout } = useAuth();
  const { theme, toggle } = useTheme();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  const initials = (user?.fullName || user?.email || '?')
    .split(' ')
    .map((p) => p[0])
    .join('')
    .slice(0, 2)
    .toUpperCase();

  return (
    <header className="sticky top-0 z-20 h-16 bg-white/90 dark:bg-ink-800/90 backdrop-blur border-b border-ink-200 dark:border-ink-700 flex items-center gap-4 px-4 sm:px-6">
      <button
        onClick={onMenuClick}
        className="lg:hidden p-2 -ml-2 rounded-lg text-ink-600 hover:bg-ink-100 dark:text-ink-300 dark:hover:bg-ink-700"
        aria-label="Open menu"
      >
        <Menu size={20} />
      </button>

      <div className="hidden sm:flex items-center gap-2 px-3 h-9 w-full max-w-md rounded-full border border-ink-200 bg-ink-50 dark:bg-ink-900 dark:border-ink-700 text-ink-400">
        <Search size={16} />
        <input
          type="search"
          placeholder="Search products, orders, users…"
          aria-label="Search"
          className="flex-1 bg-transparent text-sm text-ink-900 dark:text-ink-100 placeholder:text-ink-400 focus:outline-none"
        />
      </div>

      <div className="ml-auto flex items-center gap-3">
        <button
          onClick={toggle}
          className="w-9 h-9 rounded-full border border-ink-200 dark:border-ink-700 bg-white dark:bg-ink-900 text-ink-600 dark:text-ink-300 hover:bg-ink-50 dark:hover:bg-ink-700 flex items-center justify-center transition"
          aria-label={theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}
          title={theme === 'dark' ? 'Light mode' : 'Dark mode'}
        >
          {theme === 'dark' ? <Sun size={16} /> : <Moon size={16} />}
        </button>

        <button
          className="relative w-9 h-9 rounded-full border border-ink-200 dark:border-ink-700 bg-white dark:bg-ink-900 text-ink-600 dark:text-ink-300 hover:bg-ink-50 dark:hover:bg-ink-700 flex items-center justify-center transition"
          aria-label="Notifications"
        >
          <Bell size={16} />
          <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-rose-500 ring-2 ring-white dark:ring-ink-900" />
        </button>

        <div className="hidden md:flex items-center gap-2 pr-3 border-r border-ink-200 dark:border-ink-700">
          <div className="w-8 h-8 rounded-full bg-ink-900 text-white dark:bg-white dark:text-ink-900 flex items-center justify-center text-xs font-bold">
            {initials}
          </div>
          <div className="flex flex-col leading-tight">
            <span className="text-sm font-semibold text-ink-900 dark:text-white">{user?.fullName || 'Admin'}</span>
            <span className="text-[10px] uppercase tracking-wider text-ink-400">{user?.role || 'admin'}</span>
          </div>
        </div>

        <button onClick={handleLogout} className="btn-ghost">
          <LogOut size={14} />
          <span className="hidden sm:inline">Sign out</span>
        </button>
      </div>
    </header>
  );
}
