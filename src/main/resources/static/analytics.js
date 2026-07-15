const JujeAnalytics = {
    visitorId: null,
    sessionId: null,

    init(forceNew = false) {
        // Visitor ID (persistent in localStorage)
        this.visitorId = localStorage.getItem('juje-visitor-id');
        if (!this.visitorId) {
            this.visitorId = 'v_' + Math.random().toString(36).substr(2, 9) + '_' + Date.now();
            localStorage.setItem('juje-visitor-id', this.visitorId);
        }

        // Session ID (per visit in sessionStorage)
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
                    visitorId: this.visitorId,
                    sessionId: this.sessionId,
                    context: context
                })
            });
        } catch (e) { console.error('Analytics error:', e); }
    },

    async trackPlanGenerated(success, errorMessage, products, categories, context) {
        this.init();
        try {
            await fetch('/api/v1/analytics/plan-generated', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    visitorId: this.visitorId,
                    sessionId: this.sessionId,
                    success,
                    errorMessage,
                    products: products || [],
                    categories: categories || [],
                    context: context
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
                    visitorId: this.visitorId,
                    sessionId: this.sessionId,
                    productId,
                    productName
                })
            });
            this.trackStep('Product Clicked', null);
        } catch (e) { console.error('Analytics error:', e); }
    },

    async trackPrintClick() {
        this.init();
        try {
            await fetch('/api/v1/analytics/print-click', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ 
                    visitorId: this.visitorId,
                    sessionId: this.sessionId 
                })
            });
        } catch (e) { console.error('Analytics error:', e); }
    },

    async trackShareClick() {
        this.init();
        try {
            await fetch('/api/v1/analytics/share-click', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ 
                    visitorId: this.visitorId,
                    sessionId: this.sessionId 
                })
            });
        } catch (e) { console.error('Analytics error:', e); }
    },

    async trackStep(stepName, context) {
        this.init();
        try {
            await fetch('/api/v1/analytics/track-step', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    visitorId: this.visitorId,
                    sessionId: this.sessionId,
                    stepName,
                    context
                })
            });
        } catch (e) { console.error('Analytics error:', e); }
    }
};

JujeAnalytics.init();
