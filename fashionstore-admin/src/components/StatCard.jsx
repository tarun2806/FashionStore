import { TrendingUp, TrendingDown, ArrowUpRight, ArrowDownRight } from 'lucide-react';

const ACCENTS = {
  primary: 'bg-gradient-to-br from-ink-900 to-ink-800 text-white dark:from-white dark:to-ink-100 dark:text-ink-900',
  success: 'bg-gradient-to-br from-emerald-500 to-emerald-600 text-white dark:from-emerald-600 dark:to-emerald-500',
  warning: 'bg-gradient-to-br from-amber-500 to-orange-500 text-white dark:from-amber-600 dark:to-orange-600',
  info:    'bg-gradient-to-br from-blue-500 to-blue-600 text-white dark:from-blue-600 dark:to-blue-500',
  neutral: 'bg-gradient-to-br from-ink-100 to-ink-200 text-ink-700 dark:from-ink-700 dark:to-ink-600 dark:text-ink-200',
};

export default function StatCard({ icon: Icon, label, value, delta, accent = 'neutral' }) {
  const accentClass = ACCENTS[accent] || ACCENTS.neutral;
  const deltaPositive = typeof delta === 'number' ? delta >= 0 : null;

  return (
    <div className="group relative p-6 bg-white dark:bg-ink-800 rounded-2xl border border-ink-200 dark:border-ink-700 hover:shadow-xl hover:shadow-ink-900/5 dark:hover:shadow-white/5 transition-all duration-300 hover:-translate-y-1">
      <div className="flex items-start justify-between gap-4">
        <div className={`w-12 h-12 rounded-xl flex items-center justify-center shadow-lg ${accentClass} group-hover:scale-110 transition-transform duration-300`}>
          {Icon ? <Icon size={20} strokeWidth={2.2} /> : null}
        </div>
        {delta != null && (
          <div className={`flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold ${
            deltaPositive 
              ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-300' 
              : 'bg-rose-50 text-rose-700 dark:bg-rose-900/30 dark:text-rose-300'
          }`}>
            {deltaPositive ? <TrendingUp size={12} strokeWidth={2.5} /> : <TrendingDown size={12} strokeWidth={2.5} />}
            <span>{Math.abs(delta)}%</span>
          </div>
        )}
      </div>
      
      <div className="mt-4">
        <div className="text-[11px] font-semibold uppercase tracking-wider text-ink-400 dark:text-ink-500 mb-1">
          {label}
        </div>
        <div className="text-2xl font-bold tracking-tight text-ink-900 dark:text-white flex items-baseline gap-2">
          {value}
          <span className="text-sm font-normal text-ink-400 dark:text-ink-500">
            {deltaPositive ? <ArrowUpRight size={14} className="text-emerald-500" /> : <ArrowDownRight size={14} className="text-rose-500" />}
          </span>
        </div>
      </div>

      <div className="absolute inset-0 rounded-2xl bg-gradient-to-br from-ink-900/0 via-transparent to-ink-900/0 dark:from-white/0 dark:to-white/0 opacity-0 group-hover:opacity-100 transition-opacity duration-300 pointer-events-none" />
    </div>
  );
}
