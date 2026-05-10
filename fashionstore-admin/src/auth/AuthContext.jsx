import { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { AuthApi } from '../api/client.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // On mount, check whether an admin session already exists (page refresh case).
  useEffect(() => {
    (async () => {
      try {
        const { data } = await AuthApi.me();
        if (data?.success && data.user) setUser(data.user);
      } catch {
        // 401 -> not authenticated
        setUser(null);
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const login = useCallback(async (email, password) => {
    const { data } = await AuthApi.login(email, password);
    if (data?.success && data.user) {
      setUser(data.user);
      return { ok: true };
    }
    return { ok: false, message: data?.message || 'Login failed' };
  }, []);

  const logout = useCallback(async () => {
    try {
      await AuthApi.logout();
    } finally {
      setUser(null);
    }
  }, []);

  return (
    <AuthContext.Provider value={{ user, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside <AuthProvider>');
  return ctx;
}
