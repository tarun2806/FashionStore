// Modern Commerce UX System
const contextPath = window.contextPath || '';
window.FashionStore = {
    // Performance Optimization - DOM Query Cache
    cache: {
        get: function(selector) {
            if (!this._cache) this._cache = {};
            if (!this._cache[selector]) {
                this._cache[selector] = document.querySelector(selector);
            }
            return this._cache[selector];
        },
        getAll: function(selector) {
            if (!this._cacheAll) this._cacheAll = {};
            if (!this._cacheAll[selector]) {
                this._cacheAll[selector] = document.querySelectorAll(selector);
            }
            return this._cacheAll[selector];
        },
        clear: function() {
            this._cache = {};
            this._cacheAll = {};
        }
    },
    
    // Event Delegation - Prevent Listener Duplication
    events: {
        listeners: new Map(),
        
        on: function(element, event, handler, options = {}) {
            const key = `${element}_${event}`;
            if (this.listeners.has(key)) {
                element.removeEventListener(event, this.listeners.get(key));
            }
            element.addEventListener(event, handler, options);
            this.listeners.set(key, handler);
        },
        
        off: function(element, event) {
            const key = `${element}_${event}`;
            if (this.listeners.has(key)) {
                element.removeEventListener(event, this.listeners.get(key));
                this.listeners.delete(key);
            }
        }
    },
    
    // Request Animation Frame Throttling
    rafThrottle: function(callback) {
        let rafId = null;
        return function(...args) {
            if (rafId) return;
            rafId = requestAnimationFrame(() => {
                callback.apply(this, args);
                rafId = null;
            });
        };
    },
    
    // Debounce Function
    debounce: function(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func.apply(this, args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    },
    
    // Search Suggestions System
    search: {
        debounceTimer: null,
        suggestionsContainer: null,
        selectedIndex: -1,
        
        // Trending searches (can be loaded from backend)
        trendingSearches: [
            'Summer Collection',
            'Casual Shirts',
            'Denim Jeans',
            'Sneakers',
            'Blazers'
        ],
        
        init: function() {
            const searchInput = document.querySelector('.nav-search input, .catalog-search input');
            if (!searchInput) return;
            
            this.suggestionsContainer = document.createElement('div');
            this.suggestionsContainer.className = 'search-suggestions';
            this.suggestionsContainer.style.display = 'none';
            searchInput.parentNode.appendChild(this.suggestionsContainer);
            
            searchInput.addEventListener('input', (e) => this.handleInput(e));
            searchInput.addEventListener('focus', () => this.showTrendingAndRecent());
            searchInput.addEventListener('blur', () => {
                setTimeout(() => this.hideSuggestions(), 200);
            });
            searchInput.addEventListener('keydown', (e) => this.handleKeyboard(e));
            
            // Mobile-specific optimizations
            if (window.innerWidth <= 768) {
                searchInput.setAttribute('autocomplete', 'off');
                searchInput.setAttribute('autocorrect', 'off');
                searchInput.setAttribute('autocapitalize', 'off');
            }
        },
        
        handleInput: function(e) {
            const query = e.target.value.trim();
            clearTimeout(this.debounceTimer);
            this.selectedIndex = -1;
            
            if (query.length < 2) {
                this.showTrendingAndRecent();
                return;
            }
            
            // Optimized debounce: faster for short queries, slower for long queries
            const debounceTime = query.length < 4 ? 150 : 300;
            this.debounceTimer = setTimeout(() => this.fetchSuggestions(query), debounceTime);
        },
        
        fetchSuggestions: function(query) {
            fetch(`${contextPath}/search/suggestions?q=${encodeURIComponent(query)}`)
                .then(res => res.json())
                .then(suggestions => this.displaySuggestions(suggestions, query))
                .catch(err => {
                    console.error('Error fetching suggestions:', err);
                    this.displayEmptyState(query);
                });
        },
        
        displaySuggestions: function(suggestions, query) {
            if (!this.suggestionsContainer) return;
            
            let html = '';
            
            if (suggestions.length === 0) {
                this.displayEmptyState(query);
                return;
            }
            
            // Add "See all results" option
            html += `
                <div class="suggestion-item search-all" data-value="${this.escapeHtml(query)}" data-type="search">
                    <span class="suggestion-icon">🔍</span>
                    <span class="suggestion-text">Search for "${this.escapeHtml(query)}"</span>
                </div>
            `;
            
            suggestions.forEach((s, index) => {
                const icon = s.type === 'brand' ? '🏷️' : '👕';
                html += `
                    <div class="suggestion-item ${index === 0 ? 'selected' : ''}" 
                         data-value="${this.escapeHtml(s.value)}" 
                         data-type="${s.type}"
                         data-index="${index}">
                        <span class="suggestion-icon">${icon}</span>
                        <span class="suggestion-text">${this.highlightMatch(s.value, query)}</span>
                    </div>
                `;
            });
            
            this.suggestionsContainer.innerHTML = html;
            this.suggestionsContainer.style.display = 'block';
            this.selectedIndex = suggestions.length > 0 ? 0 : -1;
            
            // Add click handlers
            this.suggestionsContainer.querySelectorAll('.suggestion-item').forEach(item => {
                item.addEventListener('click', (e) => {
                    const searchInput = document.querySelector('.nav-search input, .catalog-search input');
                    if (searchInput) {
                        searchInput.value = item.dataset.value;
                        this.saveSearch(item.dataset.value);
                        searchInput.closest('form').submit();
                    }
                });
            });
        },
        
        showTrendingAndRecent: function() {
            if (!this.suggestionsContainer) return;
            
            const recentSearches = JSON.parse(sessionStorage.getItem('recentSearches') || '[]');
            let html = '';
            
            // Show trending searches
            if (this.trendingSearches.length > 0) {
                html += '<div class="suggestion-header">Trending Searches</div>';
                html += '<div class="suggestion-row trending-row">';
                this.trendingSearches.slice(0, 4).forEach((search, index) => {
                    html += `
                        <div class="suggestion-chip" data-value="${this.escapeHtml(search)}">
                            <span class="trend-rank">${index + 1}</span>
                            <span class="trend-text">${this.escapeHtml(search)}</span>
                        </div>
                    `;
                });
                html += '</div>';
            }
            
            // Show recent searches
            if (recentSearches.length > 0) {
                html += '<div class="suggestion-header">Recent Searches</div>';
                recentSearches.slice(0, 5).forEach(search => {
                    html += `
                        <div class="suggestion-item" data-value="${this.escapeHtml(search)}">
                            <span class="suggestion-icon">🕐</span>
                            <span class="suggestion-text">${this.escapeHtml(search)}</span>
                            <span class="suggestion-remove" data-search="${this.escapeHtml(search)}">✕</span>
                        </div>
                    `;
                });
            }
            
            if (html === '') {
                this.suggestionsContainer.style.display = 'none';
                return;
            }
            
            this.suggestionsContainer.innerHTML = html;
            this.suggestionsContainer.style.display = 'block';
            
            // Add click handlers
            this.suggestionsContainer.querySelectorAll('.suggestion-item').forEach(item => {
                item.addEventListener('click', (e) => {
                    if (e.target.classList.contains('suggestion-remove')) {
                        e.stopPropagation();
                        this.removeRecentSearch(e.target.dataset.search);
                        return;
                    }
                    
                    const searchInput = document.querySelector('.nav-search input, .catalog-search input');
                    if (searchInput) {
                        searchInput.value = item.dataset.value;
                        this.saveSearch(item.dataset.value);
                        searchInput.closest('form').submit();
                    }
                });
            });
            
            // Add click handlers for trending chips
            this.suggestionsContainer.querySelectorAll('.suggestion-chip').forEach(chip => {
                chip.addEventListener('click', () => {
                    const searchInput = document.querySelector('.nav-search input, .catalog-search input');
                    if (searchInput) {
                        searchInput.value = chip.dataset.value;
                        this.saveSearch(chip.dataset.value);
                        searchInput.closest('form').submit();
                    }
                });
            });
        },
        
        displayEmptyState: function(query) {
            if (!this.suggestionsContainer) return;
            
            let html = `
                <div class="suggestion-empty">
                    <div class="empty-icon">🔍</div>
                    <div class="empty-text">No results found for "${this.escapeHtml(query)}"</div>
                    <div class="empty-hint">Try different keywords or browse our categories</div>
                </div>
                <div class="suggestion-item search-all" data-value="${this.escapeHtml(query)}" data-type="search">
                    <span class="suggestion-icon">🔍</span>
                    <span class="suggestion-text">See all products matching "${this.escapeHtml(query)}"</span>
                </div>
            `;
            
            this.suggestionsContainer.innerHTML = html;
            this.suggestionsContainer.style.display = 'block';
            
            // Add click handler for search all
            this.suggestionsContainer.querySelector('.search-all').addEventListener('click', () => {
                const searchInput = document.querySelector('.nav-search input, .catalog-search input');
                if (searchInput) {
                    searchInput.value = query;
                    searchInput.closest('form').submit();
                }
            });
        },
        
        saveSearch: function(search) {
            let recentSearches = JSON.parse(sessionStorage.getItem('recentSearches') || '[]');
            
            // Remove if already exists (to move to top)
            recentSearches = recentSearches.filter(s => s !== search);
            
            // Add to front
            recentSearches.unshift(search);
            
            // Keep only last 10
            recentSearches = recentSearches.slice(0, 10);
            
            sessionStorage.setItem('recentSearches', JSON.stringify(recentSearches));
        },
        
        removeRecentSearch: function(search) {
            let recentSearches = JSON.parse(sessionStorage.getItem('recentSearches') || '[]');
            recentSearches = recentSearches.filter(s => s !== search);
            sessionStorage.setItem('recentSearches', JSON.stringify(recentSearches));
            this.showTrendingAndRecent();
        },
        
        hideSuggestions: function() {
            if (this.suggestionsContainer) {
                this.suggestionsContainer.style.display = 'none';
            }
        },
        
        highlightMatch: function(text, query) {
            const regex = new RegExp(`(${this.escapeRegex(query)})`, 'gi');
            return text.replace(regex, '<mark>$1</mark>');
        },
        
        escapeHtml: function(text) {
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        },
        
        escapeRegex: function(text) {
            return text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
        },
        
        handleKeyboard: function(e) {
            const items = this.suggestionsContainer?.querySelectorAll('.suggestion-item');
            if (!items || items.length === 0) return;
            
            if (e.key === 'ArrowDown') {
                e.preventDefault();
                this.selectedIndex = this.selectedIndex < items.length - 1 ? this.selectedIndex + 1 : 0;
                this.selectItem(items, this.selectedIndex);
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                this.selectedIndex = this.selectedIndex > 0 ? this.selectedIndex - 1 : items.length - 1;
                this.selectItem(items, this.selectedIndex);
            } else if (e.key === 'Enter') {
                if (this.selectedIndex >= 0) {
                    e.preventDefault();
                    items[this.selectedIndex].click();
                }
            } else if (e.key === 'Escape') {
                this.hideSuggestions();
            }
        },
        
        selectItem: function(items, index) {
            items.forEach((item, i) => {
                item.classList.toggle('selected', i === index);
            });
            
            // Scroll selected item into view
            if (items[index]) {
                items[index].scrollIntoView({ block: 'nearest' });
            }
        }
    },
    // Dark Mode Management
    darkMode: {
        STORAGE_KEY: 'fashionstore-theme',
        DARK_CLASS: 'dark-mode',
        
        init: function() {
            // Check localStorage first
            const stored = localStorage.getItem(this.STORAGE_KEY);
            if (stored) {
                if (stored === 'dark') {
                    document.documentElement.classList.add(this.DARK_CLASS);
                    document.documentElement.setAttribute('data-theme', 'dark');
                } else {
                    document.documentElement.removeAttribute('data-theme');
                }
                return;
            }
            
            // Fall back to system preference
            if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
                document.documentElement.classList.add(this.DARK_CLASS);
                document.documentElement.setAttribute('data-theme', 'dark');
            } else {
                document.documentElement.removeAttribute('data-theme');
            }
        },
        
        toggle: function() {
            const html = document.documentElement;
            const isDark = html.classList.toggle(this.DARK_CLASS);
            if (isDark) html.setAttribute('data-theme', 'dark');
            else html.removeAttribute('data-theme');
            localStorage.setItem(this.STORAGE_KEY, isDark ? 'dark' : 'light');
            return isDark;
        },
        
        isDark: function() {
            return document.documentElement.classList.contains(this.DARK_CLASS);
        }
    },

    // Toast System
    showToast: function(message, type = 'success', duration = 3000) {
        const container = document.getElementById('toast-container');
        if (!container) return;

        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.setAttribute('role', 'alert');
        toast.setAttribute('aria-live', 'polite');
        
        const icons = {
            success: '✓',
            error: '✕',
            info: 'ℹ',
            warning: '⚠'
        };
        
        toast.innerHTML = `
            <div class="toast-icon">${icons[type] || icons.info}</div>
            <div class="toast-message">${message}</div>
            <button class="toast-close" aria-label="Close notification">✕</button>
        `;
        
        container.appendChild(toast);

        // Trigger reflow for transition
        setTimeout(() => toast.classList.add('show'), 10);

        // Auto dismiss
        const dismissTimer = setTimeout(() => {
            this.dismissToast(toast);
        }, duration);

        // Manual dismiss
        toast.querySelector('.toast-close').addEventListener('click', () => {
            clearTimeout(dismissTimer);
            this.dismissToast(toast);
        });
    },
    
    dismissToast: function(toast) {
        if (!toast) return;
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    },

    // Loading States
    showLoading: function(element, text = 'Loading...') {
        if (!element) return;
        
        const originalContent = element.innerHTML;
        element.dataset.originalContent = originalContent;
        
        element.innerHTML = `
            <div class="loading-spinner">
                <div class="spinner"></div>
                <span>${text}</span>
            </div>
        `;
        element.disabled = true;
    },

    hideLoading: function(element) {
        if (!element || !element.dataset.originalContent) return;
        
        element.innerHTML = element.dataset.originalContent;
        element.disabled = false;
        delete element.dataset.originalContent;
    },

    // Skeleton Loaders
    showSkeleton: function(container, count = 3) {
        if (!container) return;
        
        let skeletonHTML = '';
        for (let i = 0; i < count; i++) {
            skeletonHTML += `
                <div class="skeleton-card">
                    <div class="skeleton skeleton-image"></div>
                    <div class="skeleton skeleton-title"></div>
                    <div class="skeleton skeleton-text"></div>
                    <div class="skeleton skeleton-price"></div>
                </div>
            `;
        }
        
        container.innerHTML = skeletonHTML;
    },

    hideSkeleton: function(container, content) {
        if (!container) return;
        container.innerHTML = content || '';
    },

    // Smooth Animations
    animateValue: function(element, start, end, duration = 300) {
        const range = end - start;
        const startTime = performance.now();
        
        const animate = (currentTime) => {
            const elapsed = currentTime - startTime;
            const progress = Math.min(elapsed / duration, 1);
            
            const easeOutQuart = 1 - Math.pow(1 - progress, 4);
            const current = start + (range * easeOutQuart);
            
            element.textContent = current.toFixed(2);
            
            if (progress < 1) {
                requestAnimationFrame(animate);
            }
        };
        
        requestAnimationFrame(animate);
    },

    // Enhanced Cart Operations
    addToCart: function(productId, size = 'M', quantity = 1) {
        if (!productId) {
            console.error('addToCart: productId is required');
            FashionStore.showToast('Failed to add to cart - missing product ID', 'error');
            return Promise.reject('Missing productId');
        }
        
        const button = (typeof event !== 'undefined' && event && event.target) ? event.target : null;
        if (button) {
            FashionStore.showLoading(button, 'Adding...');
        }

        return fetch(`${contextPath}/cart`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Requested-With': 'XMLHttpRequest',
                'X-CSRF-Token': window.csrfToken || ''
            },
            body: new URLSearchParams({
                action: 'add',
                productId: productId,
                size: size,
                quantity: quantity
            })
        })
        .then(res => {
            if(res.redirected) {
                window.location.href = res.url;
                return;
            }
            // 401 from AuthFilter still returns a JSON body with a redirect URL.
            // Parse it instead of throwing so unauthenticated users go to /login
            // gracefully rather than seeing a generic toast error.
            return res.json().then(data => {
                if (res.status === 401 && data && data.redirect) {
                    FashionStore.showToast(data.message || 'Please login to continue.', 'info');
                    setTimeout(() => { window.location.href = data.redirect; }, 600);
                    return null;
                }
                if (!res.ok) {
                    throw new Error((data && data.message) || `HTTP error! status: ${res.status}`);
                }
                return data;
            });
        })
        .then(data => {
            if (!data) return; // already handled (e.g. 401 redirect or 302 redirected response)
            if(data.success || data.status === 'success') {
                FashionStore.updateMiniCartUI(data);
                FashionStore.showToast("Added to cart", 'success');
                
                // Update navbar badge
                const badgeEl = document.getElementById('nav-cart-badge');
                if (badgeEl && data.cartCount !== undefined) {
                    badgeEl.innerText = data.cartCount;
                }
                
                // Open drawer after a short delay
                setTimeout(() => {
                    document.getElementById('mini-cart-overlay')?.classList.add('active');
                    document.getElementById('mini-cart-drawer')?.classList.add('active');
                }, 300);
            } else {
                FashionStore.showToast(data.message || "Failed to add to cart", 'error');
            }
        })
        .catch(err => {
            console.error("Error adding to cart:", err);
            FashionStore.showToast("Failed to add to cart: " + err.message, 'error');
        })
        .finally(() => {
            if (button) {
                FashionStore.hideLoading(button);
            }
        });
    },

    // Enhanced Wishlist - Global function for inline handlers
    toggleWishlist: function(productId, button) {
        if (!productId) {
            console.error('toggleWishlist: productId is required');
            return Promise.reject('Missing productId');
        }
        
        if (button) {
            FashionStore.showLoading(button, '');
        }

        return fetch(`${contextPath}/wishlist`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Requested-With': 'XMLHttpRequest',
                'X-CSRF-Token': window.csrfToken || ''
            },
            body: new URLSearchParams({
                action: 'toggle',
                productId: productId
            })
        })
        .then(res => {
            // 401 from AuthFilter ships a JSON body with redirect URL.
            // Don't throw on 401 — honor the redirect so guests are sent to /login.
            return res.json().then(data => {
                if (res.status === 401 && data && data.redirect) {
                    FashionStore.showToast(data.message || 'Please login to continue.', 'info');
                    setTimeout(() => { window.location.href = data.redirect; }, 600);
                    return null;
                }
                if (!res.ok) {
                    throw new Error((data && data.message) || `HTTP error! status: ${res.status}`);
                }
                return data;
            });
        })
        .then(data => {
            if (!data) return; // already handled (e.g. 401 redirect)
            if(data.redirect) {
                window.location.href = data.redirect;
                return;
            }
            
            if(data.success) {
                FashionStore.showToast(data.message, 'success');
                
                // Update button state
                if (button) {
                    const icon = button.querySelector('.wishlist-icon');
                    if (icon) {
                        icon.classList.toggle('active', data.isFavorite);
                    }
                }
            } else {
                FashionStore.showToast(data.message || "Failed to update wishlist", 'error');
            }
        })
        .catch(err => {
            console.error("Error toggling wishlist:", err);
            FashionStore.showToast("Failed to update wishlist: " + err.message, 'error');
        })
        .finally(() => {
            if (button) {
                FashionStore.hideLoading(button);
            }
        });
    },

    // Product Reviews
    submitReview: function(productId, rating, comment) {
        const submitButton = document.querySelector('.submit-review-btn');
        if (submitButton) {
            FashionStore.showLoading(submitButton, 'Submitting...');
        }

        return fetch(`${contextPath}/review`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Requested-With': 'XMLHttpRequest',
                'X-CSRF-Token': window.csrfToken || ''
            },
            body: new URLSearchParams({
                action: 'add',
                productId: productId,
                rating: rating,
                comment: comment
            })
        })
        .then(res => res.json())
        .then(data => {
            if(data.success) {
                FashionStore.showToast("Review submitted successfully", 'success');
                
                // Update reviews section
                const reviewsContainer = document.querySelector('.reviews-list');
                if (reviewsContainer && data.review) {
                    const reviewHTML = FashionStore.createReviewHTML(data.review);
                    reviewsContainer.insertAdjacentHTML('afterbegin', reviewHTML);
                }
                
                // Clear form
                const ratingInput = document.querySelector('#reviewRating');
                const commentInput = document.querySelector('#reviewComment');
                if (ratingInput) ratingInput.value = '5';
                if (commentInput) commentInput.value = '';
            } else {
                FashionStore.showToast(data.message || "Failed to submit review", 'error');
            }
        })
        .catch(err => {
            console.error("Error submitting review:", err);
            FashionStore.showToast("Failed to submit review", 'error');
        })
        .finally(() => {
            if (submitButton) {
                FashionStore.hideLoading(submitButton);
            }
        });
    },

    createReviewHTML: function(review) {
        return `
            <div class="review-card">
                <div class="review-header">
                    <div class="review-author">${review.userName}</div>
                    <div class="review-rating">
                        ${'★'.repeat(review.rating)}${'☆'.repeat(5 - review.rating)}
                    </div>
                </div>
                <div class="review-comment">${review.comment}</div>
                <div class="review-date">${new Date(review.createdAt).toLocaleDateString()}</div>
            </div>
        `;
    },

    // Mini Cart UI Update
    updateMiniCartUI: function(data) {
        const itemsContainer = document.getElementById('mini-cart-items');
        const totalPriceEl = document.getElementById('mini-cart-total-price');
        const badgeEl = document.getElementById('nav-cart-badge');

        if (!data.cartItems || data.cartItems.length === 0) {
            itemsContainer.innerHTML = `
                <div class="mini-cart-empty">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                        <circle cx="9" cy="21" r="1"/>
                        <circle cx="20" cy="21" r="1"/>
                        <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
                    </svg>
                    <h4>Your cart is empty</h4>
                    <p>Add items to get started</p>
                </div>
            `;
        } else {
            let html = '';
            data.cartItems.forEach(item => {
                const itemTotal = item.price * item.quantity;
                html += `
                    <div class="mini-cart-item" data-id="${item.cartItemId}">
                        <a href="${contextPath}/product?id=${item.productId}" class="mini-cart-item-image">
                            <img src="${item.imageUrl || ''}" alt="${item.productName}" onerror="this.src='${contextPath}/assets/images/placeholder-product.jpg'">
                        </a>
                        <div class="mini-cart-item-details">
                            <h4><a href="${contextPath}/product?id=${item.productId}">${item.productName || 'Product'}</a></h4>
                            <div class="mini-cart-item-meta">
                                <span><span class="mini-cart-item-meta-label">Size</span>${item.sizeLabel || 'M'}</span>
                                <span><span class="mini-cart-item-meta-label">Price</span>₹${item.price.toFixed(2)}</span>
                            </div>
                            <div class="mini-cart-item-qty">
                                <button type="button" class="mini-cart-qty-btn mini-cart-qty-decrease" data-id="${item.cartItemId}" data-qty="${item.quantity}" ${item.quantity <= 1 ? 'disabled' : ''}>
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
                                        <line x1="5" y1="12" x2="19" y2="12"/>
                                    </svg>
                                </button>
                                <span class="mini-cart-qty-value">${item.quantity}</span>
                                <button type="button" class="mini-cart-qty-btn mini-cart-qty-increase" data-id="${item.cartItemId}" data-qty="${item.quantity}">
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
                                        <line x1="12" y1="5" x2="12" y2="19"/>
                                        <line x1="5" y1="12" x2="19" y2="12"/>
                                    </svg>
                                </button>
                            </div>
                        </div>
                        <div class="mini-cart-item-actions">
                            <span class="mini-cart-item-price">₹${itemTotal.toFixed(2)}</span>
                            <button type="button" class="mini-cart-remove-btn" data-id="${item.cartItemId}">Remove</button>
                        </div>
                    </div>
                `;
            });
            itemsContainer.innerHTML = html;
            
            // Attach event listeners to new buttons
            FashionStore.attachMiniCartListeners();
        }

        if (totalPriceEl) {
            totalPriceEl.innerText = '₹' + (data.cartTotal ? data.cartTotal.toFixed(2) : '0.00');
        }
        if (badgeEl) {
            badgeEl.innerText = data.cartCount || 0;
        }
    },
    
    // Attach event listeners to mini cart buttons
    attachMiniCartListeners: function() {
        // Quantity decrease buttons
        document.querySelectorAll('.mini-cart-qty-decrease').forEach(btn => {
            btn.addEventListener('click', function(e) {
                e.preventDefault();
                const cartItemId = this.getAttribute('data-id');
                const currentQty = parseInt(this.getAttribute('data-qty'));
                FashionStore.updateMiniCartQty(cartItemId, currentQty - 1);
            });
        });
        
        // Quantity increase buttons
        document.querySelectorAll('.mini-cart-qty-increase').forEach(btn => {
            btn.addEventListener('click', function(e) {
                e.preventDefault();
                const cartItemId = this.getAttribute('data-id');
                const currentQty = parseInt(this.getAttribute('data-qty'));
                FashionStore.updateMiniCartQty(cartItemId, currentQty + 1);
            });
        });
        
        // Remove buttons
        document.querySelectorAll('.mini-cart-remove-btn').forEach(btn => {
            btn.addEventListener('click', function(e) {
                e.preventDefault();
                const cartItemId = this.getAttribute('data-id');
                FashionStore.removeMiniCartItem(cartItemId);
            });
        });
    },
    
    // Update mini cart quantity
    updateMiniCartQty: function(cartItemId, newQty) {
        const params = new URLSearchParams();
        params.append('action', 'update');
        params.append('cartItemId', cartItemId);
        params.append('currentQty', newQty);

        fetch(`${contextPath}/cart`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Requested-With': 'XMLHttpRequest',
                'X-CSRF-Token': window.csrfToken || ''
            },
            body: params
        })
        .then(res => res.json())
        .then(data => {
            if (data.success || data.status === 'success') {
                FashionStore.updateMiniCartUI(data);
                FashionStore.showToast('Cart updated', 'success');
            } else {
                FashionStore.showToast(data.message || 'Failed to update cart', 'error');
            }
        })
        .catch(err => {
            console.error('Mini cart update error:', err);
            FashionStore.showToast('Failed to update cart', 'error');
        });
    },
    
    // Remove item from mini cart
    removeMiniCartItem: function(cartItemId) {
        const itemEl = document.querySelector(`.mini-cart-item[data-id="${cartItemId}"]`);
        if (itemEl) {
            itemEl.classList.add('removing');
        }
        
        const params = new URLSearchParams();
        params.append('action', 'remove');
        params.append('cartItemId', cartItemId);
        params.append('currentQty', 0);

        fetch(`${contextPath}/cart`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Requested-With': 'XMLHttpRequest',
                'X-CSRF-Token': window.csrfToken || ''
            },
            body: params
        })
        .then(res => res.json())
        .then(data => {
            if (data.success || data.status === 'success') {
                FashionStore.updateMiniCartUI(data);
                FashionStore.showToast('Item removed', 'success');
            } else {
                FashionStore.showToast(data.message || 'Failed to remove item', 'error');
                if (itemEl) itemEl.classList.remove('removing');
            }
        })
        .catch(err => {
            console.error('Mini cart remove error:', err);
            FashionStore.showToast('Failed to remove item', 'error');
            if (itemEl) itemEl.classList.remove('removing');
        });
    },

    // Fetch Cart Data
    fetchCart: function() {
        return fetch(`${contextPath}/cart`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Requested-With': 'XMLHttpRequest',
                'X-CSRF-Token': window.csrfToken || ''
            },
            body: new URLSearchParams({ action: 'get' })
        })
        .then(res => {
            if (!res.ok) {
                if(res.redirected) window.location.href = res.url;
                throw new Error("Not logged in or error");
            }
            return res.json();
        })
        .then(data => {
            FashionStore.updateMiniCartUI(data);
        })
        .catch(err => console.error("Error fetching cart:", err));
    },

    // Quick View Modal
    openQuickView: function(productId) {
        const modal = document.getElementById('quickViewModal');
        const modalContent = document.getElementById('modalContent');
        
        if (!modal || !modalContent) return;
        
        // Show loading state
        modalContent.innerHTML = '<div class="skeleton skeleton-image"></div><div class="skeleton skeleton-text"></div><div class="skeleton skeleton-text-short"></div><div class="skeleton skeleton-price"></div>';
        modal.classList.add('active');
        modal.setAttribute('aria-hidden', 'false');
        document.body.style.overflow = 'hidden';
        
        // Fetch product details
        fetch(`${contextPath}/product?id=${productId}`)
            .then(response => response.text())
            .then(html => {
                // Parse the HTML to extract product details
                const parser = new DOMParser();
                const doc = parser.parseFromString(html, 'text/html');
                
                const productName = doc.querySelector('.product-title')?.textContent || '';
                const productPrice = doc.querySelector('.price')?.textContent || '';
                const productDescription = doc.querySelector('.description')?.textContent || '';
                const productImage = doc.querySelector('.main-image img')?.src || '';
                const sizes = Array.from(doc.querySelectorAll('.size-btn')).map(btn => btn.textContent.trim());
                
                // Build modal content
                modalContent.innerHTML = `
                    <div>
                        <img src="${productImage}" alt="${productName}" class="modal-image">
                    </div>
                    <div class="modal-details">
                        <h2 class="modal-title">${productName}</h2>
                        <div class="modal-price">${productPrice}</div>
                        <p class="modal-description">${productDescription}</p>
                        <div class="modal-sizes">
                            ${sizes.map(size => `<button class="modal-size-btn" onclick="this.classList.toggle('selected')">${size}</button>`).join('')}
                        </div>
                        <div class="modal-actions">
                            <button class="btn-primary" onclick="FashionStore.addToCart(${productId})" style="flex: 1;">Add to Cart</button>
                            <button class="btn-secondary" onclick="FashionStore.closeQuickView(); window.location.href='${contextPath}/product?id=${productId}'">View Details</button>
                        </div>
                    </div>
                `;
            })
            .catch(error => {
                console.error('Error loading product details:', error);
                modalContent.innerHTML = '<p style="text-align: center; color: var(--color-secondary);">Failed to load product details. Please try again.</p>';
            });
    },
    
    closeQuickView: function() {
        const modal = document.getElementById('quickViewModal');
        if (modal) {
            modal.classList.remove('active');
            modal.setAttribute('aria-hidden', 'true');
            document.body.style.overflow = '';
        }
    },

    // Coupon Application
    applyCoupon: function() {
        const couponInput = document.getElementById('couponCode');
        const couponMessage = document.getElementById('couponMessage');
        const discountRow = document.getElementById('discountRow');
        const discountSpan = document.getElementById('discount');
        const totalSpan = document.getElementById('summary-total');
        
        if (!couponInput || !couponInput.value.trim()) {
            if (couponMessage) {
                couponMessage.textContent = 'Please enter a coupon code';
                couponMessage.className = 'coupon-message error';
            }
            return;
        }
        
        const couponCode = couponInput.value.trim().toUpperCase();
        
        // Get current cart total
        const subtotalElement = document.getElementById('summary-subtotal');
        const currentSubtotal = subtotalElement ? parseFloat(subtotalElement.textContent.replace(/[₹,]/g, '')) : 0;
        
        // Show loading state
        if (couponMessage) {
            couponMessage.innerHTML = '<span class="loading-spinner"></span> Validating...';
            couponMessage.className = 'coupon-message';
        }
        
        fetch(`${contextPath}/cart`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Requested-With': 'XMLHttpRequest',
                'X-CSRF-Token': window.csrfToken || ''
            },
            body: new URLSearchParams({
                action: 'applyCoupon',
                couponCode: couponCode,
                cartTotal: currentSubtotal
            })
        })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                if (couponMessage) {
                    couponMessage.textContent = `Coupon applied! You saved ₹${data.discount.toFixed(2)}`;
                    couponMessage.className = 'coupon-message success';
                }
                
                // Update discount display
                if (discountRow && discountSpan) {
                    discountRow.style.display = 'flex';
                    discountSpan.textContent = `-₹${data.discount.toFixed(2)}`;
                }
                
                // Update total
                if (totalSpan) {
                    totalSpan.textContent = data.total.toFixed(2);
                }
                
                // Store applied coupon
                window.appliedCoupon = couponCode;
                window.appliedDiscount = data.discount;
                
                FashionStore.showToast('Coupon applied successfully!', 'success');
            } else {
                if (couponMessage) {
                    couponMessage.textContent = data.message || 'Invalid coupon code';
                    couponMessage.className = 'coupon-message error';
                }
                
                // Hide discount row if coupon failed
                if (discountRow) {
                    discountRow.style.display = 'none';
                }
                
                FashionStore.showToast(data.message || 'Invalid coupon code', 'error');
            }
        })
        .catch(err => {
            console.error('Error applying coupon:', err);
            if (couponMessage) {
                couponMessage.textContent = 'Failed to apply coupon. Please try again.';
                couponMessage.className = 'coupon-message error';
            }
            FashionStore.showToast('Failed to apply coupon', 'error');
        });
    },

    // Shipping Estimation
    estimateShipping: function() {
        const shippingSpan = document.getElementById('shipping');
        if (!shippingSpan) return;
        
        const cartItems = document.querySelectorAll('.cart-card');
        if (cartItems.length === 0) {
            shippingSpan.textContent = 'FREE';
            return;
        }
        
        // Simple shipping logic: free for orders above ₹999, otherwise ₹49
        const subtotalElement = document.getElementById('summary-subtotal');
        const subtotal = subtotalElement ? parseFloat(subtotalElement.textContent.replace(/[₹,]/g, '')) : 0;
        
        if (subtotal >= 999) {
            shippingSpan.textContent = 'FREE';
            shippingSpan.className = 'free-tag';
        } else {
            shippingSpan.textContent = '₹49';
            shippingSpan.className = '';
        }
    },

    // Checkout Step Navigation
    goToCheckoutStep: function(stepNumber) {
        const steps = document.querySelectorAll('.progress-step');
        const sections = document.querySelectorAll('.checkout-step-section');
        
        // Update progress indicator
        steps.forEach((step, index) => {
            const stepNum = index + 1;
            step.classList.remove('active', 'completed');
            if (stepNum < stepNumber) {
                step.classList.add('completed');
            } else if (stepNum === stepNumber) {
                step.classList.add('active');
            }
        });
        
        sections.forEach((section, index) => {
            const sectionNum = index + 1;
            section.classList.toggle('is-active', sectionNum === stepNumber);
        });
    },

    // Address Validation
    validateAddress: function(form) {
        const fullName = form.querySelector('[name="fullName"]');
        const address = form.querySelector('[name="address"]');
        const city = form.querySelector('[name="city"]');
        const state = form.querySelector('[name="state"]');
        const zip = form.querySelector('[name="zip"]');
        const phone = form.querySelector('[name="phone"]');
        
        const errors = [];
        
        if (!fullName || !fullName.value.trim()) {
            errors.push('Full name is required');
        }
        
        if (!address || !address.value.trim()) {
            errors.push('Address is required');
        }
        
        if (!city || !city.value.trim()) {
            errors.push('City is required');
        }
        
        if (!state || !state.value.trim()) {
            errors.push('State is required');
        }
        
        if (!zip || !zip.value.trim() || !/^\d{6}$/.test(zip.value.trim())) {
            errors.push('Valid 6-digit ZIP code is required');
        }
        
        if (!phone || !phone.value.trim() || !/^[6-9]\d{9}$/.test(phone.value.trim())) {
            errors.push('Valid 10-digit phone number is required');
        }
        
        return errors;
    },

    // Payment Selection Handler
    selectPaymentMethod: function(method) {
        const paymentCards = document.querySelectorAll('.payment-card');
        paymentCards.forEach(card => {
            const input = card.querySelector('input[type="radio"]');
            if (input && input.value === method) {
                input.checked = true;
                card.classList.add('selected');
            } else {
                card.classList.remove('selected');
            }
        });
    },

    // Validate address and proceed to payment step
    validateAndProceedToPayment: function() {
        const form = document.getElementById('checkoutForm');
        if (!form) return;
        
        const errors = FashionStore.validateAddress(form);
        
        if (errors.length > 0) {
            FashionStore.showToast(errors[0], 'error');
            return;
        }
        
        FashionStore.goToCheckoutStep(2);
    },

    // Review order before final submission
    reviewOrder: function() {
        const form = document.getElementById('checkoutForm');
        if (!form) return;
        
        const paymentMethod = form.querySelector('input[name="paymentMethod"]:checked');
        if (!paymentMethod) {
            FashionStore.showToast('Please select a payment method', 'error');
            return;
        }
        
        FashionStore.goToCheckoutStep(3);
    },

    // Save for Later functionality
    saveForLater: function(cartItemId) {
        const cartItem = document.getElementById(`cart-item-${cartItemId}`);
        if (!cartItem) return;
        
        FashionStore.showLoading(cartItem, 'Saving...');
        
        const contextPath = document.querySelector('.cart-page')?.getAttribute('data-context-path') || window.contextPath || '';
        
        fetch(`${contextPath}/cart`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Requested-With': 'XMLHttpRequest',
                'X-CSRF-Token': window.csrfToken || ''
            },
            body: new URLSearchParams({
                action: 'saveForLater',
                cartItemId: cartItemId
            })
        })
        .then(res => res.json())
        .then(data => {
            if (data.success || data.status === 'success') {
                FashionStore.showToast('Item saved for later', 'success');
                // Remove item from cart with animation
                cartItem.style.opacity = '0';
                cartItem.style.transform = 'translateX(-20px)';
                setTimeout(() => {
                    cartItem.remove();
                    FashionStore.fetchCart(); // Refresh cart
                }, 300);
            } else {
                FashionStore.showToast('Failed to save item', 'error');
            }
        })
        .catch(err => {
            console.error('Error saving item:', err);
            FashionStore.showToast('Failed to save item', 'error');
        })
        .finally(() => {
            FashionStore.hideLoading(cartItem);
        });
    },
};

// Mini Cart Drawer
function toggleMiniCart(event) {
    if (event) event.preventDefault();
    const overlay = document.getElementById('mini-cart-overlay');
    const drawer = document.getElementById('mini-cart-drawer');
    
    if (overlay?.classList.contains('active')) {
        overlay.classList.remove('active');
        drawer.classList.remove('active');
    } else {
        overlay.classList.add('active');
        drawer.classList.add('active');
        FashionStore.fetchCart();
    }
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    // Shared image fallback to prevent broken media rendering across pages.
    document.querySelectorAll('img').forEach((img) => {
        img.addEventListener('error', () => {
            if (img.dataset.fallbackApplied === 'true') return;
            img.dataset.fallbackApplied = 'true';
            img.src = `${window.contextPath || ''}/assets/images/placeholder-product.jpg`;
        }, { once: true });
    });

    // Initialize search suggestions
    FashionStore.search.init();
    
    // Initialize dark mode
    FashionStore.darkMode.init();
    
    // Clear DOM cache on page unload
    window.addEventListener('beforeunload', () => {
        FashionStore.cache.clear();
    });

    // Dark mode toggle handler
    const darkModeToggle = document.getElementById('dark-mode-toggle');
    if (darkModeToggle) {
        FashionStore.events.on(darkModeToggle, 'click', () => {
            const isDark = FashionStore.darkMode.toggle();
            darkModeToggle.setAttribute('aria-label', isDark ? 'Switch to light mode' : 'Switch to dark mode');
        });
    }

    const badge = document.getElementById('nav-cart-badge');
    if (badge && parseInt(badge.innerText, 10) > 0) {
        FashionStore.fetchCart();
    }

    // PDP gallery: thumbnail click swaps main image with fade
    const galleryMain = document.querySelector('.product-gallery-main');
    const mainImg = galleryMain?.querySelector('img');
    const thumbs = document.querySelectorAll('.product-gallery-thumbs .gallery-thumb');
    if (galleryMain && mainImg && thumbs.length) {
        thumbs.forEach((btn) => {
            btn.addEventListener('click', () => {
                const img = btn.querySelector('img');
                const nextSrc = img?.getAttribute('src');
                if (!nextSrc || nextSrc === mainImg.getAttribute('src')) return;

                thumbs.forEach((t) => t.classList.remove('active'));
                btn.classList.add('active');

                galleryMain.classList.add('is-switching');
                window.setTimeout(() => {
                    mainImg.setAttribute('src', nextSrc);
                    mainImg.setAttribute('loading', 'eager');
                }, 140);
                window.setTimeout(() => {
                    galleryMain.classList.remove('is-switching');
                }, 260);
            });
        });
    }

    // Mobile drawer stability: close navigation after selecting a destination.
    document.querySelectorAll('#nav-actions a').forEach((link) => {
        link.addEventListener('click', () => {
            const navActions = document.getElementById('nav-actions');
            const overlay = document.getElementById('mobile-nav-overlay');
            const trigger = document.getElementById('mobile-menu-btn');
            navActions?.classList.remove('open');
            overlay?.classList.remove('active');
            document.body.classList.remove('nav-drawer-open');
            if (trigger) trigger.setAttribute('aria-expanded', 'false');
        });
    });

    function closeAdminSidebar() {
        const sidebar = document.querySelector('.admin-sidebar');
        const backdrop = document.querySelector('.admin-sidebar-backdrop');
        sidebar?.classList.remove('is-open');
        backdrop?.classList.remove('is-open');
        document.body.style.overflow = '';
    }

    document.body.addEventListener('click', (e) => {
        if (e.target.closest('[data-admin-menu-open]')) {
            const sidebar = document.querySelector('.admin-sidebar');
            const backdrop = document.querySelector('.admin-sidebar-backdrop');
            if (sidebar?.classList.contains('is-open')) {
                closeAdminSidebar();
            } else {
                sidebar?.classList.add('is-open');
                backdrop?.classList.add('is-open');
                document.body.style.overflow = 'hidden';
            }
            return;
        }
        if (e.target.closest('[data-admin-menu-close]')) {
            closeAdminSidebar();
        }
    });

    document.querySelectorAll('.admin-sidebar a').forEach((link) => {
        link.addEventListener('click', () => {
            if (window.innerWidth <= 1024) closeAdminSidebar();
        });
    });
});

// Backward compatibility
window.showToast = FashionStore.showToast;
window.addToCart = FashionStore.addToCart;
window.toggleWishlist = FashionStore.toggleWishlist;
