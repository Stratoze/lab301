import '@testing-library/jest-dom/vitest';

// Suppress React act(...) warnings from antd internal async updates (false positives)
const originalConsoleError = console.error;
console.error = (...args: unknown[]) => {
  const msg = args.map(String).join(' ');
  if (
    msg.includes('inside a test was not wrapped in act(') ||
    msg.includes('An update to')
  ) {
    return;
  }
  originalConsoleError.call(console, ...args);
};

// Mock window.matchMedia for Ant Design responsive components
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: (query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: () => {},
    removeListener: () => {},
    addEventListener: () => {},
    removeEventListener: () => {},
    dispatchEvent: () => false,
  }),
});

// Mock navigator.clipboard for copy-to-clipboard tests
// Must be configurable so @testing-library/user-event can replace it.
Object.defineProperty(navigator, 'clipboard', {
  configurable: true,
  writable: true,
  value: {
    writeText: () => Promise.resolve(),
    readText: () => Promise.resolve(''),
  },
});