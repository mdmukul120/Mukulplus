/* ==========================================================================
   NetStream Web — API layer
   Handles CORS fallback + normalizes every response into a common Movie shape.
   ========================================================================== */
window.API = (() => {
  const C = window.CONFIG;
  let workingProxy = null; // index of the proxy that last succeeded
  const mem = {};          // in-session memo (fastest tab switches)

  /* ----- 24h browser cache (localStorage) --------------------------------- */
  function cacheGet(path) {
    if (mem[path]) return mem[path];
    try {
      const raw = localStorage.getItem(C.CACHE_PREFIX + path);
      if (!raw) return null;
      const { t, d } = JSON.parse(raw);
      if (Date.now() - t > C.CACHE_TTL) { localStorage.removeItem(C.CACHE_PREFIX + path); return null; }
      mem[path] = d;
      return d;
    } catch (e) { return null; }
  }
  function cacheSet(path, data) {
    mem[path] = data;
    try {
      localStorage.setItem(C.CACHE_PREFIX + path, JSON.stringify({ t: Date.now(), d: data }));
    } catch (e) {
      pruneCache();
      try { localStorage.setItem(C.CACHE_PREFIX + path, JSON.stringify({ t: Date.now(), d: data })); } catch (_) {}
    }
  }
  // On quota errors, drop the oldest cached entries.
  function pruneCache() {
    try {
      const items = [];
      for (let i = 0; i < localStorage.length; i++) {
        const k = localStorage.key(i);
        if (k && k.startsWith(C.CACHE_PREFIX)) {
          let t = 0; try { t = JSON.parse(localStorage.getItem(k)).t; } catch (_) {}
          items.push([k, t]);
        }
      }
      items.sort((a, b) => a[1] - b[1]);
      items.slice(0, Math.ceil(items.length / 2)).forEach(([k]) => localStorage.removeItem(k));
    } catch (e) {}
  }
  function clearCache() {
    Object.keys(mem).forEach((k) => delete mem[k]);
    try {
      const keys = [];
      for (let i = 0; i < localStorage.length; i++) {
        const k = localStorage.key(i);
        if (k && k.startsWith(C.CACHE_PREFIX)) keys.push(k);
      }
      keys.forEach((k) => localStorage.removeItem(k));
    } catch (e) {}
  }

  async function tryFetch(fullUrl, timeoutMs = 10000) {
    const ctrl = new AbortController();
    const timer = setTimeout(() => ctrl.abort(), timeoutMs);
    try {
      const res = await fetch(fullUrl, {
        method: "GET",
        headers: { Accept: "application/json" },
        signal: ctrl.signal,
      });
      if (!res.ok) throw new Error("HTTP " + res.status);
      const text = await res.text();
      // Some proxies / servers send a leading BOM or wrap JSON — parse leniently.
      return JSON.parse(text.replace(/^\uFEFF/, "").trim());
    } finally {
      clearTimeout(timer);
    }
  }

  // Fetches a path relative to BASE_URL, trying direct then proxy fallbacks.
  // Cache-first: returns a fresh (<24h) cached copy without any network call.
  async function apiGet(path) {
    const cached = cacheGet(path);
    if (cached) return cached;

    const target = C.BASE_URL + path;

    // If we already know a proxy works, try it first.
    const order = [];
    if (workingProxy !== null) order.push(workingProxy);
    C.CORS_PROXIES.forEach((_, i) => {
      if (i !== workingProxy) order.push(i);
    });

    let lastErr;
    for (const i of order) {
      try {
        const data = await tryFetch(C.CORS_PROXIES[i](target));
        workingProxy = i;
        cacheSet(path, data);
        return data;
      } catch (e) {
        console.warn(`[proxy ${i}] failed:`, e.message);
        lastErr = e;
      }
    }
    // All proxies failed — reset so next call retries from scratch.
    workingProxy = null;
    throw lastErr || new Error("All CORS proxies failed — see worker-proxy.js for a permanent fix.");
  }

  /* ----- Normalization ---------------------------------------------------- */

  function str(v) {
    return v === null || v === undefined ? "" : String(v);
  }

  function normalizeMovie(raw) {
    if (!raw || typeof raw !== "object") return null;

    const titleEn = raw.title_en || raw.titleEn || "";
    const titleRu = raw.title_ru || raw.titleRu || "";
    const title = titleEn || titleRu || raw.title || raw.name || "Untitled";
    const id = raw.kinopoisk_id ?? raw.id ?? raw.movie_id ?? null;

    // ratings can be nested { imdb: { rating, votes } }
    let imdb = 0;
    const r = raw.ratings || {};
    if (r.imdb && typeof r.imdb === "object") imdb = Number(r.imdb.rating) || 0;
    else if (typeof raw.imdb === "number") imdb = raw.imdb;
    else if (typeof raw.rating === "number") imdb = raw.rating;

    const list = (arr, k) =>
      Array.isArray(arr) ? arr.map((x) => (typeof x === "string" ? x : str(x[k] || x.name))).filter(Boolean) : [];

    const players = Array.isArray(raw.player)
      ? raw.player
          .map((p) => ({
            url: str(p.url),
            translator: str(p.translator),
            quality: str(p.quality),
            source: str(p.source) || "iframe",
            server: p.server ?? 0,
          }))
          .filter((p) => p.url)
      : [];

    return {
      id,
      type: str(raw.type),
      isSerial: raw.type === "serial",
      year: Number(raw.year) || 0,
      title,
      titleRu,
      description: str(raw.description),
      poster: str(raw.poster),
      duration: raw.duration ? Number(raw.duration) : 0,
      age: str(raw.age),
      imdb,
      genres: list(raw.genres, "name"),
      countries: list(raw.countries, "name"),
      languages: list(raw.languages, "name"),
      players,
    };
  }

  // Pull an array of movie-ish objects out of any response envelope.
  function extractList(json) {
    if (!json) return [];
    if (Array.isArray(json)) return json;
    if (Array.isArray(json.results)) return json.results;
    if (Array.isArray(json.data)) return json.data;
    if (json.data && Array.isArray(json.data.results)) return json.data.results;
    if (json.result && Array.isArray(json.result.results)) return json.result.results;
    if (json.movies && Array.isArray(json.movies)) return json.movies;
    return [];
  }

  function extractPagination(json) {
    const p = (json && (json.pagination || (json.data && json.data.pagination))) || {};
    const page = Number(p.page) || 1;
    const pages = Number(p.pages) || page;
    return { page, pages, hasMore: page < pages };
  }

  /* ----- Public methods --------------------------------------------------- */

  async function getCatalog() {
    const json = await apiGet(C.EP_CATALOG);
    const full = json?.result?.full || json?.full || [];
    return full
      .map((cat) => ({
        id: cat.id,
        name: str(cat.name),
        index: cat.index ?? 0,
        movies: (cat.movies || []).map(normalizeMovie).filter(Boolean),
      }))
      .filter((c) => c.movies.length);
  }

  async function getCategory(type, page = 1, limit = C.PAGE_SIZE) {
    const json = await apiGet(C.EP_CATEGORY(type, page, limit));
    return {
      movies: extractList(json).map(normalizeMovie).filter(Boolean),
      pagination: extractPagination(json),
    };
  }

  async function search(title, page = 1, limit = C.PAGE_SIZE) {
    const json = await apiGet(C.EP_SEARCH(title, page, limit));
    return {
      movies: extractList(json).map(normalizeMovie).filter(Boolean),
      pagination: extractPagination(json),
    };
  }

  return { getCatalog, getCategory, search, normalizeMovie, clearCache };
})();
