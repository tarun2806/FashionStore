import { createContext, useContext, useCallback, useState } from 'react';
import { CheckCircle, XCircle, AlertTriangle, Info, X } from 'lucide-react';

const ToastContext = createContext(null);

const STYLES = {
  success: {
    bg: 'bg-emerald-50 dark:bg-emerald-900/40',
    text: 'text-emerald-800 dark:text-emerald-200',
    border: 'border-emerald-200 dark:border-emerald-800',
    icon: CheckCircle,
    iconColor: 'text-emerald-600 dark:text-emerald-400',
  },
  error: {
    bg: 'bg-rose-50 dark:bg-rose-900/40',
    text: 'text-rose-800 dark:text-rose-200',
    border: 'border-rose-200 dark:border-rose-800',
    icon: XCircle,
    iconColor: 'text-rose-600 dark:text-rose-400',
  },
  warning: {
    bg: 'bg-amber-50 dark:bg-amber-900/40',
    text: 'text-amber-800 dark:text-amber-200',
    border: 'border-amber-200 dark:border-amber-800',
    icon: AlertTriangle,
    iconColor: 'text-amber-600 dark:text-amber-400',
  },
  info: {
    bg: 'bg-white dark:bg-ink-800',
    text: 'text-ink-800 dark:text-ink-100',
    border: 'border-ink-200 dark:border-ink-700',
    icon: Info,
    iconColor: 'text-blue-600 dark:text-blue-400',
  },
};

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const addToast = useCallback((message, type = 'info', duration = 4000) => {
    const id = Date.now() + Math.random();
    setToasts((prev) => [...prev, { id, message, type }]);
    
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, duration);
  }, []);

  const removeToast = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  return (
    <ToastContext.Provider value={{ addToast, toasts, removeToast }}>
      {children}
      <div className="fixed top-4 right-4 z-[60] space-y-3 pointer-events-none">
        {toasts.map((t) => {
          const style = STYLES[t.type] || STYLES.info;
          const Icon = style.icon;
          
          return (
            <div
              key={t.id}
              className={[
                'pointer-events-auto flex items-start gap-3 px-4 py-4 rounded-xl shadow-xl border min-w-[320px] max-w-md',
                'animate-in slide-in-from-right-full duration-300 ease-out',
                'hover:shadow-2xl transition-all duration-200',
                style.bg,
                style.text,
                style.border,
              ].join(' ')}
            >
              <div className={`flex-shrink-0 mt-0.5 ${style.iconColor}`}>
                <Icon size={18} strokeWidth={2} />
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium leading-relaxed">{t.message}</p>
              </div>
              <button
                onClick={() => removeToast(t.id)}
                className="flex-shrink-0 p-1 rounded-md hover:bg-black/5 dark:hover:bg-white/10 transition-colors"
                aria-label="Dismiss"
              >
                <X size={16} strokeWidth={2} className="opacity-60 hover:opacity-100" />
              </button>
            </div>
          );
        })}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast must be used inside <ToastProvider>');
  return ctx;
}
