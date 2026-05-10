import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider, useAuth } from '../AuthContext.jsx';

const ProtectedRoute = ({ children }) => {
  const { user, loading } = useAuth();
  
  if (loading) return <div>Loading...</div>;
  if (!user) return <div>Please login</div>;
  return children;
};

const mockAuthApi = {
  me: vi.fn(),
  login: vi.fn(),
  logout: vi.fn(),
};

vi.mock('../api/client.js', () => ({
  AuthApi: mockAuthApi,
}));

describe('ProtectedRoute', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows loading state initially', () => {
    mockAuthApi.me.mockImplementation(() => new Promise(() => {}));

    render(
      <BrowserRouter>
        <AuthProvider>
          <ProtectedRoute>
            <div>Protected Content</div>
          </ProtectedRoute>
        </AuthProvider>
      </BrowserRouter>
    );

    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('shows login prompt when not authenticated', async () => {
    mockAuthApi.me.mockRejectedValue(new Error('Unauthorized'));

    render(
      <BrowserRouter>
        <AuthProvider>
          <ProtectedRoute>
            <div>Protected Content</div>
          </ProtectedRoute>
        </AuthProvider>
      </BrowserRouter>
    );

    await vi.waitFor(() => {
      expect(screen.getByText('Please login')).toBeInTheDocument();
    });
  });

  it('renders protected content when authenticated', async () => {
    mockAuthApi.me.mockResolvedValue({
      data: { success: true, user: { id: 1, email: 'admin@example.com', role: 'admin' } }
    });

    render(
      <BrowserRouter>
        <AuthProvider>
          <ProtectedRoute>
            <div>Protected Content</div>
          </ProtectedRoute>
        </AuthProvider>
      </BrowserRouter>
    );

    await vi.waitFor(() => {
      expect(screen.getByText('Protected Content')).toBeInTheDocument();
    });
  });

  it('redirects to login on logout', async () => {
    mockAuthApi.me.mockResolvedValue({
      data: { success: true, user: { id: 1, email: 'admin@example.com', role: 'admin' } }
    });
    mockAuthApi.logout.mockResolvedValue({});

    const TestComponent = () => {
      const { logout } = useAuth();
      return (
        <button onClick={logout}>Logout</button>
      );
    };

    render(
      <BrowserRouter>
        <AuthProvider>
          <Routes>
            <Route path="/protected" element={
              <ProtectedRoute>
                <TestComponent />
              </ProtectedRoute>
            } />
            <Route path="/login" element={<div>Login Page</div>} />
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    );

    await vi.waitFor(() => {
      expect(screen.getByText('Logout')).toBeInTheDocument();
    });

    const logoutButton = screen.getByText('Logout');
    logoutButton.click();

    await vi.waitFor(() => {
      expect(screen.getByText('Please login')).toBeInTheDocument();
    });
  });
});
