const JujeAnalytics = {
    sessionId: null,

    init(forceNew = false) {
        this.sessionId = sessionStorage.getItem('juje_session_id');
        if (!this.sessionId || forceNew) {
            this.sessionId = 's_' + Math.random().toString(36).substr(2, 9) + '_' + Date.now();
            sessionStorage.setItem('juje_session_id', this.sessionId);
        }
    },

    async trackSessionStart(context) {
        this.init();
        try {
            await fetch('/api/v1/analytics/session-start', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    sessionId: this.sessionId,
                    context: context
                })
            });
        } catch (e) { console.error('Analytics error:', e); }
    },

    async trackPlanGenerated(success, errorMessage, products, categories) {
        this.init();
        try {
            await fetch('/api/v1/analytics/plan-generated', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    sessionId: this.sessionId,
                    success,
                    errorMessage,
                    products: products || [],
                    categories: categories || []
                })
            });
        } catch (e) { console.error('Analytics error:', e); }
    },

    async trackProductClick(productId, productName) {
        this.init();
        try {
            await fetch('/api/v1/analytics/product-click', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    sessionId: this.sessionId,
                    productId,
                    productName
                })
            });
        } catch (e) { console.error('Analytics error:', e); }
    },

    async trackPrintClick() {
        this.init();
        try {
            await fetch('/api/v1/analytics/print-click', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ sessionId: this.sessionId })
            });
        } catch (e) { console.error('Analytics error:', e); }
    },

    async trackShareClick() {
        this.init();
        try {
            await fetch('/api/v1/analytics/share-click', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ sessionId: this.sessionId })
            });
        } catch (e) { console.error('Analytics error:', e); }
    }
};

JujeAnalytics.init();
