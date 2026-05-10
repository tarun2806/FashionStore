import { describe, it, expect, vi, beforeEach } from 'vitest';
import { ProductsApi, OrdersApi, AuthApi, CategoriesApi } from '../client.js';

describe('API Integration', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('AuthApi', () => {
    it('login sends correct request', async () => {
      const mockAxios = await import('axios');
      mockAxios.default.post.mockResolvedValue({
        data: { success: true, user: { id: 1, email: 'admin@example.com', role: 'admin' } }
      });

      const result = await AuthApi.login('admin@example.com', 'password123');
      
      expect(mockAxios.default.post).toHaveBeenCalledWith(
        expect.stringContaining('/login'),
        { email: 'admin@example.com', password: 'password123' }
      );
      expect(result.data.success).toBe(true);
    });

    it('logout sends correct request', async () => {
      const mockAxios = await import('axios');
      mockAxios.default.post.mockResolvedValue({ data: { success: true } });

      await AuthApi.logout();
      
      expect(mockAxios.default.post).toHaveBeenCalledWith(
        expect.stringContaining('/logout')
      );
    });

    it('me sends correct request', async () => {
      const mockAxios = await import('axios');
      mockAxios.default.get.mockResolvedValue({
        data: { success: true, user: { id: 1, email: 'admin@example.com' } }
      });

      const result = await AuthApi.me();
      
      expect(mockAxios.default.get).toHaveBeenCalledWith(expect.stringContaining('/me'));
      expect(result.data.user).toBeDefined();
    });
  });

  describe('ProductsApi', () => {
    it('list sends correct request', async () => {
      const mockAxios = await import('axios');
      mockAxios.default.get.mockResolvedValue({
        data: [
          { id: 1, name: 'Product 1', price: 99.99 },
          { id: 2, name: 'Product 2', price: 149.99 }
        ]
      });

      const result = await ProductsApi.list(10);
      
      expect(mockAxios.default.get).toHaveBeenCalledWith(
        expect.stringContaining('/products'),
        expect.any(Object)
      );
      expect(result.data).toHaveLength(2);
    });

    it('get sends correct request', async () => {
      const mockAxios = await import('axios');
      mockAxios.default.get.mockResolvedValue({
        data: { id: 1, name: 'Product 1', price: 99.99 }
      });

      const result = await ProductsApi.get(1);
      
      expect(mockAxios.default.get).toHaveBeenCalledWith(
        expect.stringContaining('/products/1')
      );
      expect(result.data.id).toBe(1);
    });

    it('create sends correct request', async () => {
      const mockAxios = await import('axios');
      mockAxios.default.post.mockResolvedValue({
        data: { success: true, id: 1, name: 'New Product' }
      });

      const productData = {
        name: 'New Product',
        price: 99.99,
        stock: 100,
        description: 'Test description'
      };

      const result = await ProductsApi.create(productData);
      
      expect(mockAxios.default.post).toHaveBeenCalledWith(
        expect.stringContaining('/products'),
        productData
      );
      expect(result.data.success).toBe(true);
    });

    it('update sends correct request', async () => {
      const mockAxios = await import('axios');
      mockAxios.default.put.mockResolvedValue({
        data: { success: true, id: 1, name: 'Updated Product' }
      });

      const productData = {
        name: 'Updated Product',
        price: 149.99,
        stock: 50
      };

      const result = await ProductsApi.update(1, productData);
      
      expect(mockAxios.default.put).toHaveBeenCalledWith(
        expect.stringContaining('/products/1'),
        productData
      );
      expect(result.data.success).toBe(true);
    });

    it('delete sends correct request', async () => {
      const mockAxios = await import('axios');
      mockAxios.default.delete.mockResolvedValue({
        data: { success: true }
      });

      const result = await ProductsApi.delete(1);
      
      expect(mockAxios.default.delete).toHaveBeenCalledWith(
        expect.stringContaining('/products/1')
      );
      expect(result.data.success).toBe(true);
    });
  });

  describe('OrdersApi', () => {
    it('list sends correct request', async () => {
      const mockAxios = await import('axios');
      mockAxios.default.get.mockResolvedValue({
        data: [
          { id: 1, customerName: 'John Doe', total: 99.99, status: 'pending' },
          { id: 2, customerName: 'Jane Smith', total: 149.99, status: 'shipped' }
        ]
      });

      const result = await OrdersApi.list(50);
      
      expect(mockAxios.default.get).toHaveBeenCalledWith(
        expect.stringContaining('/orders'),
        expect.any(Object)
      );
      expect(result.data).toHaveLength(2);
    });

    it('get sends correct request', async () => {
      const mockAxios = await import('axios');
      mockAxios.default.get.mockResolvedValue({
        data: { id: 1, customerName: 'John Doe', total: 99.99, status: 'pending' }
      });

      const result = await OrdersApi.get(1);
      
      expect(mockAxios.default.get).toHaveBeenCalledWith(
        expect.stringContaining('/orders/1')
      );
      expect(result.data.id).toBe(1);
    });

    it('approve sends correct request', async () => {
      const mockAxios = await import('axios');
      mockAxios.default.post.mockResolvedValue({
        data: { success: true, status: 'processing' }
      });

      const result = await OrdersApi.approve(1);
      
      expect(mockAxios.default.post).toHaveBeenCalledWith(
        expect.stringContaining('/orders/1/approve')
      );
      expect(result.data.success).toBe(true);
    });

    it('cancel sends correct request', async () => {
      const mockAxios = await import('axios');
      mockAxios.default.post.mockResolvedValue({
        data: { success: true, status: 'cancelled' }
      });

      const result = await OrdersApi.cancel(1);
      
      expect(mockAxios.default.post).toHaveBeenCalledWith(
        expect.stringContaining('/orders/1/cancel')
      );
      expect(result.data.success).toBe(true);
    });

    it('ship sends correct request', async () => {
      const mockAxios = await import('axios');
      mockAxios.default.post.mockResolvedValue({
        data: { success: true, status: 'shipped' }
      });

      const result = await OrdersApi.ship(1);
      
      expect(mockAxios.default.post).toHaveBeenCalledWith(
        expect.stringContaining('/orders/1/ship')
      );
      expect(result.data.success).toBe(true);
    });

    it('deliver sends correct request', async () => {
      const mockAxios = await import('axios');
      mockAxios.default.post.mockResolvedValue({
        data: { success: true, status: 'delivered' }
      });

      const result = await OrdersApi.deliver(1);
      
      expect(mockAxios.default.post).toHaveBeenCalledWith(
        expect.stringContaining('/orders/1/deliver')
      );
      expect(result.data.success).toBe(true);
    });

    it('refund sends correct request', async () => {
      const mockAxios = await import('axios');
      mockAxios.default.post.mockResolvedValue({
        data: { success: true }
      });

      const result = await OrdersApi.refund(1);
      
      expect(mockAxios.default.post).toHaveBeenCalledWith(
        expect.stringContaining('/orders/1/refund')
      );
      expect(result.data.success).toBe(true);
    });
  });

  describe('CategoriesApi', () => {
    it('list sends correct request', async () => {
      const mockAxios = await import('axios');
      mockAxios.default.get.mockResolvedValue({
        data: [
          { id: 1, name: 'Clothing' },
          { id: 2, name: 'Accessories' }
        ]
      });

      const result = await CategoriesApi.list();
      
      expect(mockAxios.default.get).toHaveBeenCalledWith(
        expect.stringContaining('/categories')
      );
      expect(result.data).toHaveLength(2);
    });
  });

  describe('Error handling', () => {
    it('handles network errors', async () => {
      const mockAxios = await import('axios');
      mockAxios.default.get.mockRejectedValue(new Error('Network Error'));

      await expect(ProductsApi.list()).rejects.toThrow('Network Error');
    });

    it('handles 404 errors', async () => {
      const mockAxios = await import('axios');
      mockAxios.default.get.mockRejectedValue({
        response: { status: 404, data: { message: 'Not found' } }
      });

      await expect(ProductsApi.get(999)).rejects.toThrow();
    });

    it('handles 500 errors', async () => {
      const mockAxios = await import('axios');
      mockAxios.default.post.mockRejectedValue({
        response: { status: 500, data: { message: 'Internal server error' } }
      });

      await expect(ProductsApi.create({})).rejects.toThrow();
    });
  });
});
