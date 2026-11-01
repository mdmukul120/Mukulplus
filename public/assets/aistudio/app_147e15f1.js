/* ==========================================================================
   NetStream Web — App (store, router, views, banner, player)
   ========================================================================== */
(() => {
  const C = window.CONFIG;
  const app = document.getElementById("app");

  /* ---------- Movie store (keeps full objects for detail/player) --------- */
  const store = {};
  (function restore() {
    try { Object.assign(store, JSON.parse(sessionStorage.getItem("ns_store") || "{}")); } catch (e) {}
  })();
  function remember(list) {
    list.forEach((m) => { if (m && m.id != null) store[m.id] = m; });
    try { sessionStorage.setItem("ns_store", JSON.stringify(store)); } catch (e) {}
  }
  const getMovie = (id) => store[id];

  /* ---------- Small helpers --------------------------------------------- */
  const esc = (s) =>
    String(s == null ? "" : s).replace(/[&<>"']/g, (c) =>
      ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));

  const PLACEHOLDER =
    "data:image/svg+xml;utf8," +
    encodeURIComponent(
      `<svg xmlns='http://www.w3.org/2000/svg' width='300' height='450'>
        <rect width='100%' height='100%' fill='#1a1a1d'/>
        <text x='50%' y='50%' fill='#3a3a40' font-family='sans-serif' font-size='22'
          text-anchor='middle' dominant-baseline='middle'>NetStream</text>
      </svg>`
    );

  const posterOf = (m) => (m.poster && m.poster.startsWith("http") ? m.poster : PLACEHOLDER);
  const onImgErr = `onerror="this.onerror=null;this.src='${PLACEHOLDER}'"`;

  const ICONS = {
    play: '<svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>',
    star: '<svg width="13" height="13" viewBox="0 0 24 24" fill="currentColor"><path d="M12 17.3 6.2 21l1.6-6.6L2.5 9.8l6.8-.6L12 3l2.7 6.2 6.8.6-5.3 4.6L17.8 21z"/></svg>',
    search: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="11" cy="11" r="7"/><path d="m21 21-4.3-4.3"/></svg>',
    back: '<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M15 18l-6-6 6-6"/></svg>',
    chevL: '<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><path d="M15 18l-6-6 6-6"/></svg>',
    chevR: '<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><path d="M9 18l6-6-6-6"/></svg>',
    arrow: '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14M13 6l6 6-6 6"/></svg>',
    home: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 10l9-7 9 7v10a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><path d="M9 22V12h6v10"/></svg>',
    film: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M7 3v18M17 3v18M3 7.5h4M3 12h18M3 16.5h4M17 7.5h4M17 16.5h4"/></svg>',
    globe: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><path d="M3 12h18M12 3c2.5 2.7 2.5 15.3 0 18M12 3c-2.5 2.7-2.5 15.3 0 18"/></svg>',
    tv: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="13" rx="2"/><path d="m7 3 5 4 5-4"/></svg>',
    warn: '<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><path d="M12 9v4M12 17h.01M10.3 3.9 1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0z"/></svg>',
    empty: '<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="7"/><path d="m21 21-4.3-4.3"/></svg>',
    download: '<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3v12M7 10l5 5 5-5M5 21h14"/></svg>',
    telegram: '<svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor"><path d="M21.9 4.3 3.2 11.5c-1 .4-1 1.8.1 2.1l4.7 1.5 1.8 5.6c.3.8 1.3 1 1.9.3l2.6-2.6 4.7 3.5c.7.5 1.7.1 1.9-.7l3-14.4c.2-1-.8-1.9-1.9-1.5zM9.6 14.9l-.4 4-1.3-4.1 9-6.2z"/></svg>',
  };

  const metaLine = (m) => {
    const parts = [];
    if (m.year) parts.push(m.year);
    if (m.duration) { const h = (m.duration / 60) | 0, mi = m.duration % 60; parts.push(h ? `${h}h ${mi}m` : `${mi} min`); }
    if (m.languages && m.languages.length) parts.push(m.languages.slice(0, 2).join(", "));
    return parts.join("  •  ");
  };

  /* ---------- Reusable card markup -------------------------------------- */
  function cardHTML(m) {
    return `
      <article class="card" data-nav="#/detail/${m.id}">
        <div class="card-poster-wrap">
          ${m.imdb > 0 ? `<span class="card-rating">${ICONS.star}${m.imdb.toFixed(1)}</span>` : ""}
          <span class="card-type">${m.isSerial ? "Series" : "Movie"}</span>
          <img class="card-poster" loading="lazy" src="${posterOf(m)}" alt="${esc(m.title)}" ${onImgErr}>
        </div>
        <div class="card-body">
          <div class="card-title">${esc(m.title)}</div>
          <div class="card-sub">${m.year || ""}${m.genres && m.genres.length ? " · " + esc(m.genres[0]) : ""}</div>
        </div>
      </article>`;
  }

  const skeletonGrid = (n = 12) =>
    `<div class="grid">${Array.from({ length: n }).map(() => `<div class="skel skel-card"></div>`).join("")}</div>`;

  function errorState(msg, retryHash) {
    return `<div class="state">${ICONS.warn}<h3>Something went wrong</h3>
      <p>${esc(msg || "Could not load content. Check your connection and try again.")}</p>
      <button class="btn btn-primary" data-retry="${retryHash || location.hash}">Try again</button></div>`;
  }

  /* ====================================================================== */
  /*  HEADER + NAV                                                          */
  /* ====================================================================== */
  function renderChrome() {
    const activeKey = currentTabKey();
    const navLinks = C.TABS.map(
      (t) => `<a data-nav="#/${t.key === "home" ? "" : t.key}" class="${t.key === activeKey ? "active" : ""}">${t.label}</a>`
    ).join("");
    const bnav = C.TABS.map(
      (t) => `<a data-nav="#/${t.key === "home" ? "" : t.key}" class="${t.key === activeKey ? "active" : ""}">
        ${ICONS[t.icon]}<span>${t.label}</span></a>`
    ).join("");

    document.getElementById("chrome").innerHTML = `
      <header class="header">
        <a class="brand" data-nav="#/"><span class="b-net">NET</span><span class="b-stream">STREAM</span><span class="dot"></span></a>
        <nav class="nav-desktop">${navLinks}</nav>
        <form class="header-search" id="deskSearch">
          ${ICONS.search}
          <input id="deskSearchInput" type="search" placeholder="Search movies & series…" autocomplete="off">
        </form>
        <button class="search-btn-mobile" id="openMobileSearch" aria-label="Search">${ICONS.search}</button>
      </header>
      <nav class="bottom-nav">${bnav}</nav>
      <div class="mobile-search" id="mobileSearch">
        <div class="ms-bar">${ICONS.search}
          <input id="mobileSearchInput" type="search" placeholder="Search movies & series…" autocomplete="off">
          <button class="ms-close" id="closeMobileSearch">Cancel</button>
        </div>
      </div>`;

    // desktop search submit
    document.getElementById("deskSearch").addEventListener("submit", (e) => {
      e.preventDefault();
      const q = document.getElementById("deskSearchInput").value.trim();
      if (q) location.hash = "#/search/" + encodeURIComponent(q);
    });
    // mobile search
    const ms = document.getElementById("mobileSearch");
    document.getElementById("openMobileSearch").addEventListener("click", () => {
      ms.classList.add("open");
      document.getElementById("mobileSearchInput").focus();
    });
    document.getElementById("closeMobileSearch").addEventListener("click", () => ms.classList.remove("open"));
    document.getElementById("mobileSearchInput").addEventListener("keydown", (e) => {
      if (e.key === "Enter") {
        const q = e.target.value.trim();
        if (q) { ms.classList.remove("open"); location.hash = "#/search/" + encodeURIComponent(q); }
      }
    });
  }

  function currentTabKey() {
    const h = location.hash.replace(/^#\//, "");
    if (h === "" ) return "home";
    if (["bollywood", "hollywood", "serials"].includes(h)) return h;
    return "";
  }

  /* ====================================================================== */
  /*  HOME                                                                  */
  /* ====================================================================== */
  let bannerTimer = null;

  async function viewHome() {
    if (bannerTimer) { clearInterval(bannerTimer); bannerTimer = null; }
    app.innerHTML = `<div class="view">
      <div class="hero skel" style="border-radius:0"></div>
      <div class="container">${["Popular", "Trending"].map(() => `
        <section class="section"><div class="section-head"><div class="skel" style="height:22px;width:180px;border-radius:8px"></div></div>
        <div class="row-scroll">${Array.from({ length: 7 }).map(() => `<div class="skel skel-card" style="flex:0 0 auto;width:158px"></div>`).join("")}</div></section>`).join("")}
      </div></div>`;

    let categories;
    try {
      categories = await API.getCatalog();
    } catch (e) {
      app.innerHTML = `<div class="view container">${errorState(
        "Couldn't reach the catalog. If this keeps happening, the source API may be blocking browser (CORS) requests — see the README for the free proxy setup.",
        "#/"
      )}</div>`;
      return;
    }

    // remember all movies for detail/player lookups
    categories.forEach((cat) => remember(cat.movies));

    // Build "popular" pool for the banner — best-rated titles with a poster + description
    const pool = [];
    const seen = new Set();
    categories.forEach((c) =>
      c.movies.forEach((m) => {
        if (m.id != null && !seen.has(m.id) && posterOf(m) !== PLACEHOLDER && m.description) {
          seen.add(m.id); pool.push(m);
        }
      })
    );
    pool.sort((a, b) => b.imdb - a.imdb);
    const banner = pool.slice(0, C.BANNER_COUNT);

    const rows = categories
      .map(
        (cat) => `
      <section class="section">
        <div class="section-head">
          <h2 class="section-title">${esc(cat.name)}</h2>
          ${categoryHashFor(cat.name) ? `<a class="see-all" data-nav="${categoryHashFor(cat.name)}">See all ${ICONS.arrow}</a>` : ""}
        </div>
        <div class="row-scroll">${cat.movies.map(cardHTML).join("")}</div>
      </section>`
      )
      .join("");

    app.innerHTML = `<div class="view">
      <div class="container">${promoHTML()}</div>
      ${bannerHTML(banner)}
      <div class="container">${rows}</div>
    </div>`;
    if (banner.length) initBanner(banner.length);
  }

  function promoHTML() {
    return `
      <div class="promo">
        <a class="promo-item app" href="${esc(C.DOWNLOAD_URL)}" target="_blank" rel="noopener">
          <span class="promo-ico">${ICONS.download}</span>
          <span class="promo-text">
            <b>Download the app</b>
            <span>For full access to the collection</span>
          </span>
          <span class="promo-cta">Download now</span>
        </a>
        <a class="promo-item tg" href="${esc(C.TELEGRAM_URL)}" target="_blank" rel="noopener">
          <span class="promo-ico">${ICONS.telegram}</span>
          <span class="promo-text">
            <b>Join our Telegram</b>
            <span><span class="imp">Important!</span> Updates & new releases</span>
          </span>
          <span class="promo-cta">Join now</span>
        </a>
      </div>`;
  }

  function categoryHashFor(name) {
    const n = name.toLowerCase();
    if (n.includes("bollywood")) return "#/bollywood";
    if (n.includes("hollywood")) return "#/hollywood";
    if (n.includes("serial") || n.includes("series")) return "#/serials";
    return "";
  }

  /* ---------- Banner carousel ------------------------------------------- */
  function bannerHTML(items) {
    if (!items.length) return "";
    const slides = items
      .map(
        (m, i) => `
      <div class="hero-slide ${i === 0 ? "active" : ""}" data-slide="${i}">
        <div class="hero-bg" style="background-image:url('${posterOf(m)}')"></div>
        <div class="hero-scrim"></div>
        <div class="hero-inner">
          <img class="hero-poster" src="${posterOf(m)}" alt="${esc(m.title)}" ${onImgErr}>
          <div class="hero-text">
            <div class="hero-badges">
              <span class="badge badge-type">${m.isSerial ? "Series" : "Movie"}</span>
              ${m.imdb > 0 ? `<span class="badge badge-rating">${ICONS.star}${m.imdb.toFixed(1)}</span>` : ""}
              ${m.age ? `<span class="badge badge-muted">${esc(m.age)}</span>` : ""}
            </div>
            <h1 class="hero-title">${esc(m.title)}</h1>
            <div class="hero-meta">${esc(metaLine(m) || (m.genres || []).join("  •  "))}</div>
            <p class="hero-desc">${esc(m.description)}</p>
            <div class="hero-actions">
              <button class="btn btn-primary" data-nav="#/watch/${m.id}/0">${ICONS.play} Play now</button>
              <button class="btn btn-ghost" data-nav="#/detail/${m.id}">More info</button>
            </div>
          </div>
        </div>
      </div>`
      )
      .join("");

    const dots = items.map((_, i) => `<button data-dot="${i}" class="${i === 0 ? "active" : ""}"></button>`).join("");

    return `
      <div class="hero" id="hero">
        <div class="hero-track">${slides}</div>
        <button class="hero-nav hero-prev" data-hero="prev" aria-label="Previous">${ICONS.chevL}</button>
        <button class="hero-nav hero-next" data-hero="next" aria-label="Next">${ICONS.chevR}</button>
        <div class="hero-dots">${dots}</div>
      </div>`;
  }

  function initBanner(count) {
    let idx = 0;
    const hero = document.getElementById("hero");
    if (!hero) return;
    const slides = hero.querySelectorAll(".hero-slide");
    const dots = hero.querySelectorAll(".hero-dots button");

    const go = (n) => {
      idx = (n + count) % count;
      slides.forEach((s, i) => s.classList.toggle("active", i === idx));
      dots.forEach((d, i) => d.classList.toggle("active", i === idx));
    };
    const start = () => { stop(); bannerTimer = setInterval(() => go(idx + 1), C.BANNER_INTERVAL); };
    const stop = () => { if (bannerTimer) clearInterval(bannerTimer); bannerTimer = null; };

    hero.addEventListener("click", (e) => {
      const b = e.target.closest("[data-hero]");
      const dot = e.target.closest("[data-dot]");
      if (b) { go(idx + (b.dataset.hero === "next" ? 1 : -1)); start(); }
      if (dot) { go(+dot.dataset.dot); start(); }
    });
    hero.addEventListener("mouseenter", stop);
    hero.addEventListener("mouseleave", start);

    // swipe on touch
    let sx = 0;
    hero.addEventListener("touchstart", (e) => (sx = e.touches[0].clientX), { passive: true });
    hero.addEventListener("touchend", (e) => {
      const dx = e.changedTouches[0].clientX - sx;
      if (Math.abs(dx) > 45) { go(idx + (dx < 0 ? 1 : -1)); start(); }
    }, { passive: true });

    start();
  }

  /* ====================================================================== */
  /*  CATEGORY (paginated + infinite scroll)                                */
  /* ====================================================================== */
  async function viewCategory(type) {
    const titles = { bollywood: "Bollywood", hollywood: "Hollywood", serials: "Series" };
    app.innerHTML = `<div class="view container">
      <section class="section"><div class="section-head"><h2 class="section-title">${titles[type] || type}</h2></div>
      ${skeletonGrid(15)}</section></div>`;

    let page = 1, hasMore = true, loading = false;
    const seen = new Set();
    const all = [];

    async function load(p) {
      loading = true;
      const data = await API.getCategory(type, p, C.PAGE_SIZE);
      const fresh = data.movies.filter((m) => m.id != null && !seen.has(m.id));
      fresh.forEach((m) => seen.add(m.id));
      remember(fresh);
      all.push(...fresh);
      hasMore = data.pagination.hasMore;
      page = data.pagination.page;
      loading = false;
      return fresh;
    }

    try {
      await load(1);
    } catch (e) {
      app.innerHTML = `<div class="view container">${errorState("Couldn't load this category.", location.hash)}</div>`;
      return;
    }

    app.innerHTML = `<div class="view container">
      <section class="section">
        <div class="section-head"><h2 class="section-title">${titles[type] || type}</h2></div>
        <div class="grid" id="catGrid">${all.map(cardHTML).join("")}</div>
        <div class="loadmore-sentinel" id="sentinel">${hasMore ? '<div class="spinner"></div>' : ""}</div>
      </section></div>`;

    const grid = document.getElementById("catGrid");
    const sentinel = document.getElementById("sentinel");
    const io = new IntersectionObserver(async (entries) => {
      if (entries[0].isIntersecting && hasMore && !loading) {
        try {
          const fresh = await load(page + 1);
          grid.insertAdjacentHTML("beforeend", fresh.map(cardHTML).join(""));
          if (!hasMore) { sentinel.innerHTML = ""; io.disconnect(); }
        } catch (e) { sentinel.innerHTML = ""; }
      }
    }, { rootMargin: "600px" });
    io.observe(sentinel);
  }

  /* ====================================================================== */
  /*  SEARCH (new API)                                                      */
  /* ====================================================================== */
  async function viewSearch(query) {
    const q = decodeURIComponent(query || "");
    // keep search boxes in sync
    const di = document.getElementById("deskSearchInput");
    if (di) di.value = q;

    app.innerHTML = `<div class="view container">
      <section class="section"><div class="section-head">
        <h2 class="section-title">Results for “${esc(q)}”</h2></div>${skeletonGrid(12)}</section></div>`;

    let data;
    try {
      data = await API.search(q, 1, C.PAGE_SIZE);
    } catch (e) {
      app.innerHTML = `<div class="view container">${errorState("Search failed. Please try again.", location.hash)}</div>`;
      return;
    }
    remember(data.movies);

    if (!data.movies.length) {
      app.innerHTML = `<div class="view container"><div class="state">${ICONS.empty}
        <h3>No results for “${esc(q)}”</h3><p>Try a different title or spelling.</p></div></div>`;
      return;
    }

    // paginated search with infinite scroll
    let page = data.pagination.page, hasMore = data.pagination.hasMore, loading = false;
    const seen = new Set(data.movies.map((m) => m.id));

    app.innerHTML = `<div class="view container">
      <section class="section">
        <div class="section-head"><h2 class="section-title">Results for “${esc(q)}”</h2></div>
        <div class="grid" id="searchGrid">${data.movies.map(cardHTML).join("")}</div>
        <div class="loadmore-sentinel" id="sentinel">${hasMore ? '<div class="spinner"></div>' : ""}</div>
      </section></div>`;

    const grid = document.getElementById("searchGrid");
    const sentinel = document.getElementById("sentinel");
    const io = new IntersectionObserver(async (entries) => {
      if (entries[0].isIntersecting && hasMore && !loading) {
        loading = true;
        try {
          const next = await API.search(q, page + 1, C.PAGE_SIZE);
          const fresh = next.movies.filter((m) => m.id != null && !seen.has(m.id));
          fresh.forEach((m) => seen.add(m.id));
          remember(fresh);
          grid.insertAdjacentHTML("beforeend", fresh.map(cardHTML).join(""));
          page = next.pagination.page;
          hasMore = next.pagination.hasMore;
          if (!hasMore) { sentinel.innerHTML = ""; io.disconnect(); }
        } catch (e) { sentinel.innerHTML = ""; }
        loading = false;
      }
    }, { rootMargin: "600px" });
    io.observe(sentinel);
  }

  /* ====================================================================== */
  /*  DETAIL                                                                */
  /* ====================================================================== */
  function viewDetail(id) {
    const m = getMovie(id);
    if (!m) {
      app.innerHTML = `<div class="view container"><div class="state">${ICONS.warn}
        <h3>Title not available</h3><p>Open it from the home or search page.</p>
        <button class="btn btn-primary" data-nav="#/">Back to home</button></div></div>`;
      return;
    }

    const servers = m.players || [];
    const serverBtns = servers
      .map(
        (p, i) => `<button class="server-btn ${i === 0 ? "active" : ""}" data-server="${i}">
          <span>${esc(p.translator || "Server " + (i + 1))}</span>
          ${p.quality ? `<small>${esc(p.quality)}</small>` : ""}
        </button>`
      )
      .join("");

    const facts = [];
    if (m.countries && m.countries.length) facts.push(`<div><b>Country:</b> ${esc(m.countries.join(", "))}</div>`);
    if (m.genres && m.genres.length) facts.push(`<div><b>Genre:</b> ${esc(m.genres.join(", "))}</div>`);
    if (m.languages && m.languages.length) facts.push(`<div><b>Language:</b> ${esc(m.languages.join(", "))}</div>`);

    app.innerHTML = `<div class="view detail-hero">
      <div class="detail-backdrop" style="background-image:url('${posterOf(m)}')"></div>
      <div class="container">
        <div class="detail-body">
          <img class="detail-poster" src="${posterOf(m)}" alt="${esc(m.title)}" ${onImgErr}>
          <div class="detail-info">
            <h1 class="detail-title">${esc(m.title)}</h1>
            <div class="detail-meta">
              <span class="chip">${m.isSerial ? "Series" : "Movie"}</span>
              ${m.year ? `<span class="chip">${m.year}</span>` : ""}
              ${m.imdb > 0 ? `<span class="chip chip-rating">${ICONS.star}${m.imdb.toFixed(1)} IMDb</span>` : ""}
              ${m.duration ? `<span class="chip">${((m.duration / 60) | 0)}h ${m.duration % 60}m</span>` : ""}
              ${(m.genres || []).slice(0, 3).map((g) => `<span class="chip chip-genre">${esc(g)}</span>`).join("")}
            </div>

            <div class="detail-actions">
              ${servers.length ? `<button class="btn btn-primary" id="playBtn">${ICONS.play} Play now</button>` : ""}
            </div>

            <div class="detail-section-label">Overview</div>
            <p class="detail-desc">${esc(m.description || "No description available.")}</p>
            ${facts.length ? `<div class="detail-facts">${facts.join("")}</div>` : ""}

            ${servers.length
              ? `<div class="detail-section-label">Choose a server</div>
                 <div class="server-list" id="serverList">${serverBtns}</div>`
              : `<div class="detail-section-label">Playback</div>
                 <p class="detail-desc">No streaming servers are available for this title.</p>`}
          </div>
        </div>
      </div>
    </div>`;

    let selected = 0;
    const list = document.getElementById("serverList");
    if (list) {
      list.addEventListener("click", (e) => {
        const b = e.target.closest("[data-server]");
        if (!b) return;
        selected = +b.dataset.server;
        list.querySelectorAll(".server-btn").forEach((x) => x.classList.remove("active"));
        b.classList.add("active");
      });
    }
    const play = document.getElementById("playBtn");
    if (play) play.addEventListener("click", () => (location.hash = `#/watch/${m.id}/${selected}`));
    window.scrollTo(0, 0);
  }

  /* ====================================================================== */
  /*  WATCH / PLAYER                                                        */
  /* ====================================================================== */
  function loadHls(video, url) {
    const boot = () => {
      if (window.Hls && window.Hls.isSupported()) {
        const hls = new window.Hls();
        hls.loadSource(url);
        hls.attachMedia(video);
      } else if (video.canPlayType("application/vnd.apple.mpegurl")) {
        video.src = url; // Safari
      }
    };
    if (window.Hls) return boot();
    const s = document.createElement("script");
    s.src = "https://cdn.jsdelivr.net/npm/hls.js@1.5.13/dist/hls.min.js";
    s.onload = boot;
    s.onerror = boot;
    document.head.appendChild(s);
  }

  function viewWatch(id, serverIdx) {
    const m = getMovie(id);
    if (!m || !m.players || !m.players.length) {
      app.innerHTML = `<div class="view container"><div class="state">${ICONS.warn}
        <h3>Nothing to play</h3><p>This title has no available servers, or it wasn't loaded yet.</p>
        <button class="btn btn-primary" data-nav="#/">Back to home</button></div></div>`;
      return;
    }
    let idx = Math.max(0, Math.min(+serverIdx || 0, m.players.length - 1));
    const p = m.players[idx];

    const serverBtns = m.players
      .map(
        (pl, i) => `<button class="server-btn ${i === idx ? "active" : ""}" data-server="${i}">
          <span>${esc(pl.translator || "Server " + (i + 1))}</span>
          ${pl.quality ? `<small>${esc(pl.quality)}</small>` : ""}
        </button>`
      )
      .join("");

    const frame =
      p.source === "m3u8"
        ? `<video id="hlsVideo" controls autoplay playsinline></video>`
        : `<iframe src="${esc(p.url)}" allow="autoplay; encrypted-media; fullscreen; picture-in-picture"
             allowfullscreen referrerpolicy="no-referrer" sandbox="allow-scripts allow-same-origin allow-forms allow-presentation allow-popups"></iframe>`;

    app.innerHTML = `<div class="view player-page">
      <div class="player-top">
        <button class="icon-btn" data-nav="#/detail/${m.id}" aria-label="Back">${ICONS.back}</button>
        <div class="player-title">${esc(m.title)}</div>
      </div>
      <div class="player-frame" id="playerFrame">${frame}</div>
      <div class="player-servers">
        <div class="detail-section-label">Servers</div>
        <div class="server-list" id="watchServers">${serverBtns}</div>
      </div>
    </div>`;

    if (p.source === "m3u8") loadHls(document.getElementById("hlsVideo"), p.url);

    document.getElementById("watchServers").addEventListener("click", (e) => {
      const b = e.target.closest("[data-server]");
      if (!b) return;
      const n = +b.dataset.server;
      if (n !== idx) location.hash = `#/watch/${m.id}/${n}`;
    });
    window.scrollTo(0, 0);
  }

  /* ====================================================================== */
  /*  ROUTER                                                                */
  /* ====================================================================== */
  function route() {
    const hash = location.hash.replace(/^#\/?/, "");
    const parts = hash.split("/").filter(Boolean);

    renderChrome();
    document.getElementById("mobileSearch")?.classList.remove("open");

    if (parts.length === 0) return viewHome();
    switch (parts[0]) {
      case "bollywood":
      case "hollywood":
      case "serials":
        return viewCategory(parts[0]);
      case "search":
        return viewSearch(parts.slice(1).join("/"));
      case "detail":
        return viewDetail(parts[1]);
      case "watch":
        return viewWatch(parts[1], parts[2]);
      default:
        return viewHome();
    }
  }

  /* ---------- Global click delegation (data-nav / data-retry) ----------- */
  document.addEventListener("click", (e) => {
    const nav = e.target.closest("[data-nav]");
    if (nav) { e.preventDefault(); location.hash = nav.dataset.nav; return; }
    const retry = e.target.closest("[data-retry]");
    if (retry) { e.preventDefault(); route(); }
  });

  window.addEventListener("hashchange", () => { window.scrollTo(0, 0); route(); });
  window.addEventListener("DOMContentLoaded", route);
  route();
})();
