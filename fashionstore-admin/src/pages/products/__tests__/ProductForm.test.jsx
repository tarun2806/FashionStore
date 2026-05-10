import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter, Router } from 'react-router-dom';
import ProductForm from '../ProductForm.jsx';

const mockNavigate = vi.fn();
const mockAddToast = vi.fn();

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
    useParams: () => ({}),
  };
});

vi.mock('../../context/ToastContext.jsx', () => ({
  useToast: () => ({ addToast: mockAddToast }),
}));

vi.mock('../../api/client.js', () => ({
  ProductsApi: {
    create: vi.fn(),
    update: vi.fn(),
    get: vi.fn(),
  },
  CategoriesApi: {
    list: vi.fn(),
  },
}));

describe('ProductForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders add product form', async () => {
    const { CategoriesApi } = await import('../../api/client.js');
    CategoriesApi.list.mockResolvedValue([
      { id: 1, name: 'Clothing' },
      { id: 2, name: 'Accessories' },
    ]);

    render(
      <BrowserRouter>
        <ProductForm />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Add Product')).toBeInTheDocument();
      expect(screen.getByLabelText('Name *')).toBeInTheDocument();
      expect(screen.getByLabelText('Price ($)')).toBeInTheDocument();
      expect(screen.getByLabelText('Stock')).toBeInTheDocument();
    });
  });

  it('renders edit product form', async () => {
    const { useParams } = await import('react-router-dom');
    useParams.mockReturnValue({ id: '123' });

    const { CategoriesApi, ProductsApi } = await import('../../api/client.js');
    CategoriesApi.list.mockResolvedValue([{ id: 1, name: 'Clothing' }]);
    ProductsApi.get.mockResolvedValue({
      name: 'Test Product',
      description: 'Test Description',
      category: 'Clothing',
      price: 99.99,
      stock: 100,
      discount: 10,
      status: 'active',
      sku: 'SKU123',
      tags: 'summer,new',
      sizes: 'S,M,L',
      imageUrl: 'test.jpg',
    });

    render(
      <BrowserRouter>
        <ProductForm />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Edit Product')).toBeInTheDocument();
    });
  });

  it('updates form fields on change', async () => {
    const { CategoriesApi } = await import('../../api/client.js');
    CategoriesApi.list.mockResolvedValue([{ id: 1, name: 'Clothing' }]);

    render(
      <BrowserRouter>
        <ProductForm />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByLabelText('Name *')).toBeInTheDocument();
    });

    const nameInput = screen.getByLabelText('Name *');
    fireEvent.change(nameInput, { target: { value: 'New Product' } });
    expect(nameInput.value).toBe('New Product');
  });

  it('submits create product form', async () => {
    const { CategoriesApi, ProductsApi } = await import('../../api/client.js');
    CategoriesApi.list.mockResolvedValue([{ id: 1, name: 'Clothing' }]);
    ProductsApi.create.mockResolvedValue({ success: true });

    render(
      <BrowserRouter>
        <ProductForm />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByLabelText('Name *')).toBeInTheDocument();
    });

    fireEvent.change(screen.getByLabelText('Name *'), { target: { value: 'Test Product' } });
    fireEvent.change(screen.getByLabelText('Price ($)'), { target: { value: '99.99' } });
    fireEvent.change(screen.getByLabelText('Stock'), { target: { value: '100' } });

    const submitButton = screen.getByRole('button', { name: /Create Product/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(ProductsApi.create).toHaveBeenCalled();
      expect(mockAddToast).toHaveBeenCalledWith('Product created', 'success');
      expect(mockNavigate).toHaveBeenCalledWith('/products');
    });
  });

  it('submits update product form', async () => {
    const { useParams } = await import('react-router-dom');
    useParams.mockReturnValue({ id: '123' });

    const { CategoriesApi, ProductsApi } = await import('../../api/client.js');
    CategoriesApi.list.mockResolvedValue([{ id: 1, name: 'Clothing' }]);
    ProductsApi.get.mockResolvedValue({
      name: 'Test Product',
      description: 'Test Description',
      category: 'Clothing',
      price: 99.99,
      stock: 100,
      discount: 10,
      status: 'active',
    });
    ProductsApi.update.mockResolvedValue({ success: true });

    render(
      <BrowserRouter>
        <ProductForm />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByLabelText('Name *')).toBeInTheDocument();
    });

    const submitButton = screen.getByRole('button', { name: /Update Product/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(ProductsApi.update).toHaveBeenCalled();
      expect(mockAddToast).toHaveBeenCalledWith('Product updated', 'success');
    });
  });

  it('shows error on submission failure', async () => {
    const { CategoriesApi, ProductsApi } = await import('../../api/client.js');
    CategoriesApi.list.mockResolvedValue([{ id: 1, name: 'Clothing' }]);
    ProductsApi.create.mockRejectedValue(new Error('Network error'));

    render(
      <BrowserRouter>
        <ProductForm />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByLabelText('Name *')).toBeInTheDocument();
    });

    fireEvent.change(screen.getByLabelText('Name *'), { target: { value: 'Test Product' } });
    fireEvent.change(screen.getByLabelText('Price ($)'), { target: { value: '99.99' } });
    fireEvent.change(screen.getByLabelText('Stock'), { target: { value: '100' } });

    const submitButton = screen.getByRole('button', { name: /Create Product/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(mockAddToast).toHaveBeenCalledWith('Create failed', 'error');
    });
  });

  it('navigates back on cancel', async () => {
    const { CategoriesApi } = await import('../../api/client.js');
    CategoriesApi.list.mockResolvedValue([{ id: 1, name: 'Clothing' }]);

    render(
      <BrowserRouter>
        <ProductForm />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Add Product')).toBeInTheDocument();
    });

    const cancelButton = screen.getByRole('button', { name: 'Cancel' });
    fireEvent.click(cancelButton);

    expect(mockNavigate).toHaveBeenCalledWith('/products');
  });

  it('disables submit button while saving', async () => {
    const { CategoriesApi, ProductsApi } = await import('../../api/client.js');
    CategoriesApi.list.mockResolvedValue([{ id: 1, name: 'Clothing' }]);
    ProductsApi.create.mockImplementation(() => new Promise(() => {}));

    render(
      <BrowserRouter>
        <ProductForm />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByLabelText('Name *')).toBeInTheDocument();
    });

    fireEvent.change(screen.getByLabelText('Name *'), { target: { value: 'Test Product' } });
    fireEvent.change(screen.getByLabelText('Price ($)'), { target: { value: '99.99' } });
    fireEvent.change(screen.getByLabelText('Stock'), { target: { value: '100' } });

    const submitButton = screen.getByRole('button', { name: /Create Product/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(submitButton).toBeDisabled();
      expect(screen.getByText('Saving…')).toBeInTheDocument();
    });
  });
});
