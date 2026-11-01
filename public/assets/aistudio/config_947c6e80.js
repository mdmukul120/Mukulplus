/* ==========================================================================
   NetStream Web — Configuration
   ========================================================================== */
window.CONFIG = {
  // Base API (same backend the Android app used)
  BASE_URL: "https://mapi.elochkaigolochla.com/api/v1/",

  // Endpoints
  EP_CATALOG: "catalog",
  EP_CATEGORY: (type, page, limit) => `catalog/${type}?page=${page}&limit=${limit}`,
  // NEW search endpoint (replaces the old local-filter search)
  EP_SEARCH: (title, page, limit) =>
    `new-search/movies?title=${encodeURIComponent(title)}&page=${page}&limit=${limit}`,

  PAGE_SIZE: 20,

  // Browsers enforce CORS. We try the API directly first; if the API does not
  // send CORS headers, we transparently fall back through these public proxies.
  // The one that works is remembered for the rest of the session.
  //
  // ⭐ BEST FIX: deploy the tiny Cloudflare Worker from worker-proxy.js,
  //    paste your worker URL below, and it will always work (free tier).
  CORS_PROXIES: [
    (url) => url, // 1) direct — works if the API allows CORS

    // 2) YOUR OWN Cloudflare Worker (see worker-proxy.js) — uncomment & paste URL:
    (url) => `https://delicate-rice-a82c.ahsanz0987.workers.dev/?url=${encodeURIComponent(url)}`,

    // 3) Public proxies (rotated — these can break at any time)
    (url) => `https://api.codetabs.com/v1/proxy?quest=${encodeURIComponent(url)}`,
    (url) => `https://corsproxy.io/?url=${encodeURIComponent(url)}`,
    (url) => `https://api.allorigins.win/raw?url=${encodeURIComponent(url)}`,
  ],

  // Category tabs (label -> api type)
  TABS: [
    { key: "home", label: "Home", icon: "home" },
    { key: "bollywood", label: "Bollywood", icon: "film" },
    { key: "hollywood", label: "Hollywood", icon: "globe" },
    { key: "serials", label: "Series", icon: "tv" },
  ],

  BANNER_COUNT: 7,
  BANNER_INTERVAL: 6000,

  // Promo box on the home screen.
  // Telegram invite (same as netmovie.site):
  TELEGRAM_URL: "https://t.me/+abIZ9HkPXe9hNTlk",
  // Download link. This is the direct APK from netmovie.site (version-pinned).
  // TIP: to avoid editing this on every release, point it at your landing page
  //   DOWNLOAD_URL: "https://netmovie.site/"
  // which always serves the latest APK via version.json.
  DOWNLOAD_URL: "https://netmovie.site/",

  // Client-side cache: every API response is saved in the browser for 24h,
  // so switching tabs is instant and the app doesn't refetch on revisits.
  CACHE_TTL: 24 * 60 * 60 * 1000,
  CACHE_PREFIX: "ns_cache_",
};
