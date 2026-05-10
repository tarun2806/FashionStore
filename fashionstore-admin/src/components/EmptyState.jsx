import { Package, Users, ShoppingCart, FileText, Search, Plus } from 'lucide-react';

const ICONS = {
  products: Package,
  users: Users,
  orders: ShoppingCart,
  default: FileText,
  search: Search,
};

export default function EmptyState({ 
  icon = 'default', 
  title = 'No data found', 
  description = 'There are no items to display at this time.',
  action,
  actionLabel = 'Add new item',
  onAction,
}) {
  const Icon = ICONS[icon] || ICONS.default;

  return (
    <div className="flex flex-col items-center justify-center py-16 px-6 text-center">
      <div className="relative">
        <div className="w-20 h-20 rounded-2xl bg-ink-100 dark:bg-ink-700 flex items-center justify-center mb-6">
          <Icon size={32} strokeWidth={1.5} className="text-ink-400 dark:text-ink-500" />
        </div>
        <div className="absolute -top-2 -right-2 w-6 h-6 bg-ink-200 dark:bg-ink-600 rounded-full opacity-50" />
        <div className="absolute -bottom-3 -left-3 w-8 h-8 bg-ink-200 dark:bg-ink-600 rounded-full opacity-30" />
      </div>
      
      <h3 className="text-lg font-semibold text-ink-900 dark:text-white mb-2">
        {title}
      </h3>
      
      <p className="text-sm text-ink-500 dark:text-ink-400 max-w-sm mb-6 leading-relaxed">
        {description}
      </p>
      
      {action || onAction ? (
        <button
          onClick={onAction || action}
          className="inline-flex items-center gap-2 px-5 py-2.5 bg-ink-900 hover:bg-ink-800 text-white dark:bg-white dark:hover:bg-ink-100 dark:text-ink-900 rounded-xl text-sm font-semibold transition-all duration-200 hover:shadow-lg hover:-translate-y-0.5"
        >
          <Plus size={16} strokeWidth={2} />
          {actionLabel}
        </button>
      ) : null}
    </div>
  );
}
