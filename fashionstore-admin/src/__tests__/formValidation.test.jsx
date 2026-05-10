import { describe, it, expect, vi } from 'vitest';

describe('Form Validation', () => {
  describe('Email Validation', () => {
    it('validates correct email format', () => {
      const validEmails = [
        'admin@example.com',
        'user.name@domain.co.uk',
        'test+tag@example.org',
      ];

      validEmails.forEach(email => {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        expect(emailRegex.test(email)).toBe(true);
      });
    });

    it('rejects invalid email format', () => {
      const invalidEmails = [
        'invalid',
        '@example.com',
        'user@',
        'user@domain',
        'user name@example.com',
      ];

      invalidEmails.forEach(email => {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        expect(emailRegex.test(email)).toBe(false);
      });
    });
  });

  describe('Password Validation', () => {
    it('validates strong password', () => {
      const strongPassword = 'SecurePass123!';
      const hasMinLength = strongPassword.length >= 8;
      const hasUpperCase = /[A-Z]/.test(strongPassword);
      const hasLowerCase = /[a-z]/.test(strongPassword);
      const hasNumber = /[0-9]/.test(strongPassword);
      const hasSpecialChar = /[!@#$%^&*]/.test(strongPassword);

      expect(hasMinLength).toBe(true);
      expect(hasUpperCase).toBe(true);
      expect(hasLowerCase).toBe(true);
      expect(hasNumber).toBe(true);
      expect(hasSpecialChar).toBe(true);
    });

    it('rejects weak passwords', () => {
      const weakPasswords = [
        '123',
        'password',
        'Password',
        '12345678',
      ];

      weakPasswords.forEach(password => {
        const hasMinLength = password.length >= 8;
        const hasUpperCase = /[A-Z]/.test(password);
        const hasLowerCase = /[a-z]/.test(password);
        const hasNumber = /[0-9]/.test(password);
        
        expect(hasMinLength && hasUpperCase && hasLowerCase && hasNumber).toBe(false);
      });
    });
  });

  describe('Product Form Validation', () => {
    it('validates required product fields', () => {
      const product = {
        name: '',
        price: '',
        stock: '',
      };

      const isValid = product.name.trim() !== '' && 
                     product.price !== '' && 
                     product.stock !== '';

      expect(isValid).toBe(false);
    });

    it('validates product price is positive number', () => {
      const prices = [99.99, 0.01, 1000];
      
      prices.forEach(price => {
        const isValid = price > 0 && !isNaN(price);
        expect(isValid).toBe(true);
      });
    });

    it('rejects invalid product prices', () => {
      const invalidPrices = [-10, 0, 'abc', NaN];
      
      invalidPrices.forEach(price => {
        const isValid = price > 0 && !isNaN(price);
        expect(isValid).toBe(false);
      });
    });

    it('validates product stock is non-negative integer', () => {
      const stockValues = [0, 10, 100];
      
      stockValues.forEach(stock => {
        const isValid = stock >= 0 && Number.isInteger(Number(stock));
        expect(isValid).toBe(true);
      });
    });

    it('validates discount percentage range', () => {
      const discounts = [0, 10, 50, 100];
      
      discounts.forEach(discount => {
        const isValid = discount >= 0 && discount <= 100;
        expect(isValid).toBe(true);
      });
    });

    it('rejects invalid discount percentages', () => {
      const invalidDiscounts = [-10, 101, 150];
      
      invalidDiscounts.forEach(discount => {
        const isValid = discount >= 0 && discount <= 100;
        expect(isValid).toBe(false);
      });
    });

    it('validates SKU format', () => {
      const validSkus = ['SKU-001', 'PROD123', 'ITEM-XYZ-99'];
      
      validSkus.forEach(sku => {
        const skuRegex = /^[A-Z0-9\-]+$/i;
        expect(skuRegex.test(sku)).toBe(true);
      });
    });

    it('validates comma-separated tags', () => {
      const tags = 'summer,new,sale,featured';
      const tagArray = tags.split(',').map(t => t.trim()).filter(Boolean);
      
      expect(tagArray).toHaveLength(4);
      expect(tagArray).toContain('summer');
      expect(tagArray).toContain('new');
      expect(tagArray).toContain('sale');
      expect(tagArray).toContain('featured');
    });

    it('validates comma-separated sizes', () => {
      const sizes = 'S,M,L,XL,XXL';
      const sizeArray = sizes.split(',').map(s => s.trim()).filter(Boolean);
      
      expect(sizeArray).toHaveLength(5);
      expect(sizeArray).toContain('S');
      expect(sizeArray).toContain('M');
      expect(sizeArray).toContain('L');
      expect(sizeArray).toContain('XL');
      expect(sizeArray).toContain('XXL');
    });
  });

  describe('User Form Validation', () => {
    it('validates required user fields', () => {
      const user = {
        fullName: '',
        email: '',
        phone: '',
      };

      const isValid = user.fullName.trim() !== '' && 
                     user.email.trim() !== '' && 
                     user.phone.trim() !== '';

      expect(isValid).toBe(false);
    });

    it('validates phone number format', () => {
      const validPhones = ['1234567890', '987-654-3210', '(123) 456-7890'];
      const phoneRegex = /^[\d\-\(\)\s]+$/;
      
      validPhones.forEach(phone => {
        expect(phoneRegex.test(phone)).toBe(true);
      });
    });

    it('validates name has minimum length', () => {
      const names = ['John Doe', 'Jane Smith', 'Bob Johnson'];
      
      names.forEach(name => {
        const isValid = name.trim().length >= 2;
        expect(isValid).toBe(true);
      });
    });

    it('rejects names that are too short', () => {
      const shortNames = ['J', 'A', ''];
      
      shortNames.forEach(name => {
        const isValid = name.trim().length >= 2;
        expect(isValid).toBe(false);
      });
    });
  });

  describe('Address Form Validation', () => {
    it('validates required address fields', () => {
      const address = {
        addressLine1: '',
        city: '',
        state: '',
        zip: '',
        country: '',
      };

      const isValid = address.addressLine1.trim() !== '' && 
                     address.city.trim() !== '' && 
                     address.state.trim() !== '' && 
                     address.zip.trim() !== '' && 
                     address.country.trim() !== '';

      expect(isValid).toBe(false);
    });

    it('validates postal code format', () => {
      const validZips = ['12345', '12345-6789', 'SW1A 1AA'];
      
      validZips.forEach(zip => {
        const isValid = zip.trim().length >= 5;
        expect(isValid).toBe(true);
      });
    });

    it('validates state code format', () => {
      const validStates = ['CA', 'NY', 'TX', 'FL'];
      const stateRegex = /^[A-Z]{2}$/;
      
      validStates.forEach(state => {
        expect(stateRegex.test(state)).toBe(true);
      });
    });
  });

  describe('Numeric Validation', () => {
    it('validates positive integers', () => {
      const values = [1, 10, 100, 1000];
      
      values.forEach(value => {
        const isValid = Number.isInteger(value) && value > 0;
        expect(isValid).toBe(true);
      });
    });

    it('validates non-negative integers', () => {
      const values = [0, 1, 10, 100];
      
      values.forEach(value => {
        const isValid = Number.isInteger(value) && value >= 0;
        expect(isValid).toBe(true);
      });
    });

    it('validates decimal numbers', () => {
      const values = [0.01, 99.99, 1000.50];
      
      values.forEach(value => {
        const isValid = !isNaN(value) && value > 0;
        expect(isValid).toBe(true);
      });
    });

    it('rejects negative numbers', () => {
      const values = [-1, -10, -100];
      
      values.forEach(value => {
        const isValid = value >= 0;
        expect(isValid).toBe(false);
      });
    });
  });

  describe('String Validation', () => {
    it('validates string length within bounds', () => {
      const str = 'Test String';
      const min = 3;
      const max = 50;
      
      const isValid = str.length >= min && str.length <= max;
      expect(isValid).toBe(true);
    });

    it('rejects strings exceeding max length', () => {
      const str = 'a'.repeat(101);
      const max = 100;
      
      const isValid = str.length <= max;
      expect(isValid).toBe(false);
    });

    it('rejects strings below min length', () => {
      const str = 'ab';
      const min = 3;
      
      const isValid = str.length >= min;
      expect(isValid).toBe(false);
    });

    it('trims whitespace from strings', () => {
      const str = '  test string  ';
      const trimmed = str.trim();
      
      expect(trimmed).toBe('test string');
      expect(trimmed.length).toBe(11);
    });
  });

  describe('URL Validation', () => {
    it('validates correct URL format', () => {
      const validUrls = [
        'https://example.com',
        'http://example.com',
        'https://example.com/image.jpg',
        'https://cdn.example.com/products/123.jpg',
      ];
      
      validUrls.forEach(url => {
        try {
          new URL(url);
          expect(true).toBe(true);
        } catch {
          expect(false).toBe(true);
        }
      });
    });

    it('rejects invalid URL format', () => {
      const invalidUrls = [
        'not-a-url',
        'example.com',
        'http://',
        'https://',
      ];
      
      invalidUrls.forEach(url => {
        try {
          new URL(url);
          expect(false).toBe(true);
        } catch {
          expect(true).toBe(true);
        }
      });
    });
  });
});
