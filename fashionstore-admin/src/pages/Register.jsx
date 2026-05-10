import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthApi } from '../api/client.js';

export default function Register() {
  const navigate = useNavigate();

  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [adminKey, setAdminKey] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!fullName || !email || !phone || !password || !confirmPassword || !adminKey) {
      setError('Please fill in all fields.');
      return;
    }

    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    if (password.length < 8) {
      setError('Password must be at least 8 characters.');
      return;
    }

    setSubmitting(true);
    try {
      const result = await AuthApi.register({
        fullName,
        email,
        phone,
        password,
        confirmPassword,
        adminKey,
      });

      if (result.data?.success) {
        navigate('/login', { replace: true, state: { message: 'Admin account created successfully. Please login.' } });
      } else {
        setError(result.data?.message || 'Registration failed.');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Something went wrong. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-ink-50 dark:bg-ink-900 px-4">
      <div className="w-full max-w-sm bg-white dark:bg-ink-800 rounded-xl shadow-lg border border-ink-200 dark:border-ink-700 p-8">
        <h1 className="text-2xl font-bold text-center text-ink-900 dark:text-ink-50 mb-2">
          Admin Registration
        </h1>
        <p className="text-sm text-center text-ink-600 dark:text-ink-400 mb-6">
          Create a new admin account
        </p>

        {error && (
          <div className="mb-4 p-3 rounded-lg bg-red-50 dark:bg-red-900/20 text-red-700 dark:text-red-400 text-sm border border-red-200 dark:border-red-800">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="fullName" className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-1">
              Full Name
            </label>
            <input
              id="fullName"
              type="text"
              autoComplete="name"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              className="w-full px-3 py-2 rounded-lg border border-ink-300 dark:border-ink-600 bg-white dark:bg-ink-900 text-ink-900 dark:text-ink-50 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
              placeholder="John Doe"
              disabled={submitting}
            />
          </div>

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
            <label htmlFor="phone" className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-1">
              Phone
            </label>
            <input
              id="phone"
              type="tel"
              autoComplete="tel"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              className="w-full px-3 py-2 rounded-lg border border-ink-300 dark:border-ink-600 bg-white dark:bg-ink-900 text-ink-900 dark:text-ink-50 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
              placeholder="9876543210"
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
              autoComplete="new-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-3 py-2 rounded-lg border border-ink-300 dark:border-ink-600 bg-white dark:bg-ink-900 text-ink-900 dark:text-ink-50 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
              placeholder="••••••••"
              disabled={submitting}
              minLength={8}
            />
          </div>

          <div>
            <label htmlFor="confirmPassword" className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-1">
              Confirm Password
            </label>
            <input
              id="confirmPassword"
              type="password"
              autoComplete="new-password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              className="w-full px-3 py-2 rounded-lg border border-ink-300 dark:border-ink-600 bg-white dark:bg-ink-900 text-ink-900 dark:text-ink-50 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
              placeholder="••••••••"
              disabled={submitting}
              minLength={8}
            />
          </div>

          <div>
            <label htmlFor="adminKey" className="block text-sm font-medium text-ink-700 dark:text-ink-300 mb-1">
              Admin Secret Key
            </label>
            <input
              id="adminKey"
              type="password"
              value={adminKey}
              onChange={(e) => setAdminKey(e.target.value)}
              className="w-full px-3 py-2 rounded-lg border border-ink-300 dark:border-ink-600 bg-white dark:bg-ink-900 text-ink-900 dark:text-ink-50 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
              placeholder="Enter admin secret key"
              disabled={submitting}
            />
            <p className="text-xs text-ink-500 dark:text-ink-400 mt-1">
              Contact the system owner for the admin secret key.
            </p>
          </div>

          <button
            type="submit"
            disabled={submitting}
            className="w-full py-2.5 px-4 rounded-lg bg-primary text-white font-medium hover:bg-primary/90 focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 dark:focus:ring-offset-ink-800 disabled:opacity-60 disabled:cursor-not-allowed transition-colors"
          >
            {submitting ? 'Creating account…' : 'Create Admin Account'}
          </button>
        </form>

        <p className="text-center text-sm text-ink-600 dark:text-ink-400 mt-6">
          Already have an account?{' '}
          <a href="/login" className="text-primary hover:underline">
            Sign in
          </a>
        </p>
      </div>
    </div>
  );
}
