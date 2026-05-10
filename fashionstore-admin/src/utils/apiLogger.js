class ApiLogger {
  constructor() {
    this.logQueue = [];
    this.maxQueueSize = 100;
    this.flushInterval = 5000;
    this.flushTimer = null;
    this.startFlushTimer();
  }

  logApiError(error, request) {
    const errorData = {
      timestamp: new Date().toISOString(),
      url: request?.url || 'unknown',
      method: request?.method || 'unknown',
      status: error?.response?.status || 0,
      statusText: error?.response?.statusText || 'Network Error',
      message: error?.message || 'Unknown error',
      stack: error?.stack,
      userAgent: navigator.userAgent,
      url: window.location.href,
    };

    console.error('API Error:', errorData);

    this.logQueue.push(errorData);
    if (this.logQueue.length > this.maxQueueSize) {
      this.logQueue.shift();
    }
  }

  logApiSuccess(request, duration) {
    const successData = {
      timestamp: new Date().toISOString(),
      url: request?.url || 'unknown',
      method: request?.method || 'unknown',
      status: 200,
      duration: duration,
    };

    console.debug('API Success:', successData);
  }

  startFlushTimer() {
    this.flushTimer = setInterval(() => {
      this.flush();
    }, this.flushInterval);
  }

  async flush() {
    if (this.logQueue.length === 0) return;

    const errorsToLog = [...this.logQueue];
    this.logQueue = [];

    try {
      await fetch('/api/log/errors', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ errors: errorsToLog }),
      });
    } catch (err) {
      console.error('Failed to flush error logs:', err);
      this.logQueue.unshift(...errorsToLog);
    }
  }

  destroy() {
    if (this.flushTimer) {
      clearInterval(this.flushTimer);
    }
    this.flush();
  }
}

const apiLogger = new ApiLogger();

export default apiLogger;
