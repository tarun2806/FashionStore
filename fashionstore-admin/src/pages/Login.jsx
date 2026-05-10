import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';

export default function Login() {
  const { login, user } = useAuth();
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  // If already authenticated, redirect to dashboard
  if (user) {
    navigate('/dashboard', { replace: true });
    return null;
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!email || !password) {
      setError('Please enter both email and password.');
      return;
    }
    setSubmitting(true);
    try {
      const result = await login(email, password);
      if (result.ok) {
        navigate('/dashboard', { replace: true });
      } else {
        setError(result.message || 'Invalid credentials.');
      }
    } catch {
      setError('Something went wrong. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-ink-50 dark:bg-ink-900 px-4">
      <div className="w-full max-w-sm bg-white dark:bg-ink-800 rounded-xl shadow-lg border border-ink-200 dark:border-ink-700 p-8">
        <h1 className="text-2xl font-bold text-center text-ink-900 dark:text-ink-50 mb-6">
          Admin Login
        </h1>

        {error && (
          <div className="mb-4 p-3 rounded-lg bg-red-50 dark:bg-red-900/20 text-red-700 dark:text-red-400 text-sm border border-red-200 dark:border-red-800">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="email" className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-1">
              Email
            </label>
            <input
              id="email"
              type="email"
              autoComplete="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full px-3 py-2 rounded-lg border border-ink-300 dark:border-ink-600 bg-white dark:bg-ink-900 text-ink-900 dark:text-ink-50 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
              placeholder="admin@example.com"
              disabled={submitting}
            />
          </div>

          <div>
            <label htmlFor="password" className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-1">
              Password
            </label>
            <input
              id="password"
              type="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-3 py-2 rounded-lg border border-ink-300 dark:border-ink-600 bg-white dark:bg-ink-900 text-ink-900 dark:text-ink-50 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
              placeholder="••••••••"
              disabled={submitting}
            />
          </div>

          <button
            type="submit"
            disabled={submitting}
            className="w-full py-2.5 px-4 rounded-lg bg-primary text-white font-medium hover:bg-primary/90 focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 dark:focus:ring-offset-ink-800 disabled:opacity-60 disabled:cursor-not-allowed transition-colors"
          >
            {submitting ? 'Signing in…' : 'Sign In'}
          </button>
        </form>

        <p className="text-center text-sm text-ink-600 dark:text-ink-400 mt-6">
          Need an admin account?{' '}
          <a href="/register" className="text-primary hover:underline">
            Register as admin
          </a>
        </p>
      </div>
    </div>
  );
}
