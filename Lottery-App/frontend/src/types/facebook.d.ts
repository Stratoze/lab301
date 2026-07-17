interface Window {
  FB: {
    init: (options: Record<string, any>) => void;
    login: (callback: (response: any) => void, options?: Record<string, any>) => void;
  };
  fbAsyncInit: () => void;
}