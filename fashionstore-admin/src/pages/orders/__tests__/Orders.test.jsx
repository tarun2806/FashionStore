import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Orders from '../Orders.jsx';

const mockAddToast = vi.fn();

vi.mock('../../context/ToastContext.jsx', () => ({
  useToast: () => ({ addToast: mockAddToast }),
}));

vi.mock('../../api/client.js', () => ({
  OrdersApi: {
    list: vi.fn(),
    approve: vi.fn(),
    cancel: vi.fn(),
    ship: vi.fn(),
    deliver: vi.fn(),
    refund: vi.fn(),
  },
}));

vi.mock('../../components/DataTable.jsx', () => ({
  default: ({ columns, rows, empty }) => (
    <div data-testid="datatable">
      {rows.length === 0 ? <div>{empty}</div> : rows.map((row) => <div key={row.id}>{row.id}</div>)}
    </div>
  ),
}));

describe('Orders', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders orders page', async () => {
    const { OrdersApi } = await import('../../api/client.js');
    OrdersApi.list.mockResolvedValue([
      { id: 1, customerName: 'John Doe', status: 'pending', total: 99.99, createdAt: '2024-01-01' },
      { id: 2, customerName: 'Jane Smith', status: 'shipped', total: 149.99, createdAt: '2024-01-02' },
    ]);

    render(
      <BrowserRouter>
        <Orders />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Orders')).toBeInTheDocument();
      expect(screen.getByTestId('datatable')).toBeInTheDocument();
    });
  });

  it('shows loading state initially', () => {
    const { OrdersApi } = require('../../api/client.js');
    OrdersApi.list.mockImplementation(() => new Promise(() => {}));

    render(
      <BrowserRouter>
        <Orders />
      </BrowserRouter>
    );

    expect(screen.queryByText('Orders')).toBeInTheDocument();
  });

  it('filters orders by status tabs', async () => {
    const { OrdersApi } = await import('../../api/client.js');
    OrdersApi.list.mockResolvedValue([
      { id: 1, customerName: 'John Doe', status: 'pending', total: 99.99, createdAt: '2024-01-01' },
      { id: 2, customerName: 'Jane Smith', status: 'shipped', total: 149.99, createdAt: '2024-01-02' },
      { id: 3, customerName: 'Bob Johnson', status: 'pending', total: 79.99, createdAt: '2024-01-03' },
    ]);

    render(
      <BrowserRouter>
        <Orders />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Pending')).toBeInTheDocument();
    });

    const pendingTab = screen.getByText('Pending');
    fireEvent.click(pendingTab);

    await waitFor(() => {
      expect(pendingTab).toHaveClass('bg-ink-900');
    });
  });

  it('calls approve action', async () => {
    const { OrdersApi } = await import('../../api/client.js');
    OrdersApi.list.mockResolvedValue([
      { id: 1, customerName: 'John Doe', status: 'pending', total: 99.99, createdAt: '2024-01-01' },
    ]);
    OrdersApi.approve.mockResolvedValue({ success: true });

    render(
      <BrowserRouter>
        <Orders />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByTestId('datatable')).toBeInTheDocument();
    });

    const approveButton = screen.getByTitle('Approve');
    fireEvent.click(approveButton);

    await waitFor(() => {
      expect(OrdersApi.approve).toHaveBeenCalledWith(1);
      expect(mockAddToast).toHaveBeenCalledWith('Order approved', 'success');
    });
  });

  it('calls cancel action', async () => {
    const { OrdersApi } = await import('../../api/client.js');
    OrdersApi.list.mockResolvedValue([
      { id: 1, customerName: 'John Doe', status: 'pending', total: 99.99, createdAt: '2024-01-01' },
    ]);
    OrdersApi.cancel.mockResolvedValue({ success: true });

    render(
      <BrowserRouter>
        <Orders />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByTestId('datatable')).toBeInTheDocument();
    });

    const cancelButton = screen.getByTitle('Cancel');
    fireEvent.click(cancelButton);

    await waitFor(() => {
      expect(OrdersApi.cancel).toHaveBeenCalledWith(1);
      expect(mockAddToast).toHaveBeenCalledWith('Order cancelled', 'success');
    });
  });

  it('calls ship action', async () => {
    const { OrdersApi } = await import('../../api/client.js');
    OrdersApi.list.mockResolvedValue([
      { id: 1, customerName: 'John Doe', status: 'processing', total: 99.99, createdAt: '2024-01-01' },
    ]);
    OrdersApi.ship.mockResolvedValue({ success: true });

    render(
      <BrowserRouter>
        <Orders />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByTestId('datatable')).toBeInTheDocument();
    });

    const shipButton = screen.getByTitle('Ship');
    fireEvent.click(shipButton);

    await waitFor(() => {
      expect(OrdersApi.ship).toHaveBeenCalledWith(1);
      expect(mockAddToast).toHaveBeenCalledWith('Order shipped', 'success');
    });
  });

  it('calls deliver action', async () => {
    const { OrdersApi } = await import('../../api/client.js');
    OrdersApi.list.mockResolvedValue([
      { id: 1, customerName: 'John Doe', status: 'shipped', total: 99.99, createdAt: '2024-01-01' },
    ]);
    OrdersApi.deliver.mockResolvedValue({ success: true });

    render(
      <BrowserRouter>
        <Orders />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByTestId('datatable')).toBeInTheDocument();
    });

    const deliverButton = screen.getByTitle('Deliver');
    fireEvent.click(deliverButton);

    await waitFor(() => {
      expect(OrdersApi.deliver).toHaveBeenCalledWith(1);
      expect(mockAddToast).toHaveBeenCalledWith('Order delivered', 'success');
    });
  });

  it('calls refund action', async () => {
    const { OrdersApi } = await import('../../api/client.js');
    OrdersApi.list.mockResolvedValue([
      { id: 1, customerName: 'John Doe', status: 'delivered', total: 99.99, createdAt: '2024-01-01' },
    ]);
    OrdersApi.refund.mockResolvedValue({ success: true });

    render(
      <BrowserRouter>
        <Orders />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByTestId('datatable')).toBeInTheDocument();
    });

    const refundButton = screen.getByTitle('Refund');
    fireEvent.click(refundButton);

    await waitFor(() => {
      expect(OrdersApi.refund).toHaveBeenCalledWith(1);
      expect(mockAddToast).toHaveBeenCalledWith('Order refunded', 'success');
    });
  });

  it('shows error when action fails', async () => {
    const { OrdersApi } = await import('../../api/client.js');
    OrdersApi.list.mockResolvedValue([
      { id: 1, customerName: 'John Doe', status: 'pending', total: 99.99, createdAt: '2024-01-01' },
    ]);
    OrdersApi.approve.mockRejectedValue(new Error('Network error'));

    render(
      <BrowserRouter>
        <Orders />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByTestId('datatable')).toBeInTheDocument();
    });

    const approveButton = screen.getByTitle('Approve');
    fireEvent.click(approveButton);

    await waitFor(() => {
      expect(mockAddToast).toHaveBeenCalledWith('Action failed', 'error');
    });
  });

  it('opens order detail modal', async () => {
    const { OrdersApi } = await import('../../api/client.js');
    OrdersApi.list.mockResolvedValue([
      { 
        id: 1, 
        customerName: 'John Doe', 
        customerEmail: 'john@example.com',
        status: 'pending', 
        total: 99.99, 
        createdAt: '2024-01-01',
        items: [
          { name: 'Product 1', price: 49.99, quantity: 2 },
        ]
      },
    ]);

    render(
      <BrowserRouter>
        <Orders />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByTestId('datatable')).toBeInTheDocument();
    });

    const viewButton = screen.getByTitle('View');
    fireEvent.click(viewButton);

    await waitFor(() => {
      expect(screen.getByText('Order #1')).toBeInTheDocument();
      expect(screen.getByText('John Doe')).toBeInTheDocument();
      expect(screen.getByText('john@example.com')).toBeInTheDocument();
    });
  });

  it('closes order detail modal', async () => {
    const { OrdersApi } = await import('../../api/client.js');
    OrdersApi.list.mockResolvedValue([
      { id: 1, customerName: 'John Doe', status: 'pending', total: 99.99, createdAt: '2024-01-01' },
    ]);

    render(
      <BrowserRouter>
        <Orders />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByTestId('datatable')).toBeInTheDocument();
    });

    const viewButton = screen.getByTitle('View');
    fireEvent.click(viewButton);

    await waitFor(() => {
      expect(screen.getByText('Order #1')).toBeInTheDocument();
    });

    const closeButton = screen.getByText('×');
    fireEvent.click(closeButton);

    await waitFor(() => {
      expect(screen.queryByText('Order #1')).not.toBeInTheDocument();
    });
  });
});
