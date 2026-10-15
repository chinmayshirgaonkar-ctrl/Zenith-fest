"""
Web Portal for Zenith '26 College Fest Management System
Provides a modern, responsive HTML interface for students and evaluators visiting the root URL.
"""

PORTAL_HTML = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Zenith '26 - College Fest Management System</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;600&display=swap" rel="stylesheet">
  <style>
    :root {
      --bg: #0B0F19;
      --card-bg: #151D2F;
      --card-border: #1E293B;
      --card-hover: #1E2942;
      --primary: #7C3AED;
      --primary-hover: #6D28D9;
      --accent: #06B6D4;
      --accent-glow: rgba(6, 182, 212, 0.15);
      --text: #F8FAFC;
      --text-muted: #94A3B8;
      --text-dim: #64748B;
      --success: #10B981;
      --danger: #EF4444;
      --badge-tech: #38BDF8;
      --badge-cult: #F43F5E;
      --badge-sport: #F59E0B;
      --badge-game: #A855F7;
      --badge-work: #10B981;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      font-family: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, sans-serif;
      background-color: var(--bg);
      color: var(--text);
      line-height: 1.5;
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      overflow-x: hidden;
      background-image: 
        radial-gradient(circle at 15% 20%, rgba(124, 58, 237, 0.12) 0%, transparent 40%),
        radial-gradient(circle at 85% 60%, rgba(6, 182, 212, 0.10) 0%, transparent 40%);
    }
    /* Navbar */
    header {
      position: sticky;
      top: 0;
      z-index: 50;
      backdrop-filter: blur(16px);
      background: rgba(11, 15, 25, 0.85);
      border-bottom: 1px solid var(--card-border);
      padding: 0.9rem 1.5rem;
    }
    .nav-container {
      max-width: 1200px;
      margin: 0 auto;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
    .brand {
      display: flex;
      align-items: center;
      gap: 0.65rem;
      text-decoration: none;
      color: var(--text);
      font-weight: 800;
      font-size: 1.25rem;
      letter-spacing: -0.02em;
    }
    .brand-logo {
      width: 34px;
      height: 34px;
      border-radius: 9px;
      background: linear-gradient(135deg, var(--primary), var(--accent));
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.1rem;
      box-shadow: 0 0 16px rgba(124, 58, 237, 0.5);
    }
    .nav-actions {
      display: flex;
      align-items: center;
      gap: 0.75rem;
    }
    .btn {
      display: inline-flex;
      align-items: center;
      gap: 0.45rem;
      padding: 0.55rem 1.05rem;
      border-radius: 8px;
      font-size: 0.875rem;
      font-weight: 600;
      text-decoration: none;
      cursor: pointer;
      transition: all 0.2s ease;
      border: 1px solid transparent;
    }
    .btn-primary {
      background: linear-gradient(135deg, var(--primary), #9333EA);
      color: #FFF;
      box-shadow: 0 4px 14px rgba(124, 58, 237, 0.35);
    }
    .btn-primary:hover {
      background: linear-gradient(135deg, var(--primary-hover), #7E22CE);
      transform: translateY(-1px);
    }
    .btn-outline {
      background: rgba(30, 41, 59, 0.5);
      color: var(--text);
      border-color: var(--card-border);
    }
    .btn-outline:hover {
      background: rgba(30, 41, 59, 0.9);
      border-color: var(--primary);
    }

    /* Hero Section */
    .hero {
      text-align: center;
      padding: 3.5rem 1.5rem 2rem;
      max-width: 900px;
      margin: 0 auto;
    }
    .live-pill {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.35rem 0.85rem;
      border-radius: 9999px;
      font-size: 0.75rem;
      font-weight: 700;
      letter-spacing: 0.05em;
      text-transform: uppercase;
      background: rgba(16, 185, 129, 0.12);
      color: var(--success);
      border: 1px solid rgba(16, 185, 129, 0.25);
      margin-bottom: 1.25rem;
    }
    .live-dot {
      width: 7px;
      height: 7px;
      border-radius: 50%;
      background: var(--success);
      box-shadow: 0 0 8px var(--success);
      animation: pulse 2s infinite;
    }
    @keyframes pulse {
      0%, 100% { opacity: 1; }
      50% { opacity: 0.4; }
    }
    .hero h1 {
      font-size: clamp(2.4rem, 5vw, 3.8rem);
      font-weight: 800;
      line-height: 1.1;
      letter-spacing: -0.03em;
      margin-bottom: 1rem;
      background: linear-gradient(135deg, #FFFFFF 30%, #CBD5E1 60%, #A855F7 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
    .hero p {
      color: var(--text-muted);
      font-size: 1.1rem;
      max-width: 680px;
      margin: 0 auto 1.75rem;
    }
    .hero-meta {
      display: flex;
      flex-wrap: wrap;
      justify-content: center;
      gap: 1.5rem;
      color: var(--text-dim);
      font-size: 0.9rem;
      margin-bottom: 2rem;
    }
    .hero-meta span {
      display: flex;
      align-items: center;
      gap: 0.4rem;
      color: var(--text-muted);
    }

    /* Stats Grid */
    .stats-container {
      max-width: 1200px;
      margin: 0 auto 2.5rem;
      padding: 0 1.5rem;
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 1rem;
    }
    .stat-card {
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      border-radius: 12px;
      padding: 1.25rem;
      display: flex;
      flex-direction: column;
      gap: 0.35rem;
      transition: all 0.2s ease;
    }
    .stat-card:hover {
      border-color: rgba(124, 58, 237, 0.4);
      transform: translateY(-2px);
    }
    .stat-label {
      font-size: 0.8rem;
      color: var(--text-dim);
      text-transform: uppercase;
      letter-spacing: 0.05em;
      font-weight: 600;
    }
    .stat-val {
      font-size: 1.85rem;
      font-weight: 800;
      color: var(--text);
    }
    .stat-sub {
      font-size: 0.8rem;
      color: var(--text-muted);
    }

    /* Filter & Search Bar */
    .controls-container {
      max-width: 1200px;
      margin: 0 auto 1.75rem;
      padding: 0 1.5rem;
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }
    @media (min-width: 768px) {
      .controls-container {
        flex-direction: row;
        justify-content: space-between;
        align-items: center;
      }
    }
    .category-pills {
      display: flex;
      gap: 0.5rem;
      overflow-x: auto;
      padding-bottom: 0.25rem;
    }
    .pill-btn {
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      color: var(--text-muted);
      padding: 0.45rem 0.95rem;
      border-radius: 9999px;
      font-size: 0.85rem;
      font-weight: 600;
      cursor: pointer;
      white-space: nowrap;
      transition: all 0.2s ease;
    }
    .pill-btn:hover {
      border-color: var(--primary);
      color: var(--text);
    }
    .pill-btn.active {
      background: var(--primary);
      border-color: var(--primary);
      color: #FFF;
      box-shadow: 0 2px 10px rgba(124, 58, 237, 0.4);
    }
    .search-input {
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      color: var(--text);
      padding: 0.55rem 1rem;
      border-radius: 8px;
      font-size: 0.875rem;
      min-width: 260px;
      outline: none;
      transition: border-color 0.2s ease;
    }
    .search-input:focus {
      border-color: var(--primary);
    }

    /* Events Grid */
    .events-container {
      max-width: 1200px;
      margin: 0 auto 3rem;
      padding: 0 1.5rem;
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
      gap: 1.25rem;
      flex: 1;
    }
    .event-card {
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      border-radius: 14px;
      padding: 1.4rem;
      display: flex;
      flex-direction: column;
      gap: 1rem;
      transition: all 0.25s ease;
      position: relative;
    }
    .event-card:hover {
      border-color: rgba(124, 58, 237, 0.5);
      background: var(--card-hover);
      transform: translateY(-3px);
      box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.4);
    }
    .card-top {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 0.75rem;
    }
    .event-icon {
      font-size: 2.2rem;
      line-height: 1;
      padding: 0.5rem;
      background: rgba(30, 41, 59, 0.6);
      border-radius: 10px;
      border: 1px solid var(--card-border);
    }
    .badge {
      font-size: 0.7rem;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      padding: 0.25rem 0.6rem;
      border-radius: 6px;
    }
    .badge-Technical { background: rgba(56, 189, 248, 0.15); color: var(--badge-tech); border: 1px solid rgba(56, 189, 248, 0.3); }
    .badge-Cultural { background: rgba(244, 63, 94, 0.15); color: var(--badge-cult); border: 1px solid rgba(244, 63, 94, 0.3); }
    .badge-Sports { background: rgba(245, 158, 11, 0.15); color: var(--badge-sport); border: 1px solid rgba(245, 158, 11, 0.3); }
    .badge-Gaming { background: rgba(168, 85, 247, 0.15); color: var(--badge-game); border: 1px solid rgba(168, 85, 247, 0.3); }
    .badge-Workshop { background: rgba(16, 185, 129, 0.15); color: var(--badge-work); border: 1px solid rgba(16, 185, 129, 0.3); }
    .event-title {
      font-size: 1.2rem;
      font-weight: 700;
      color: #FFF;
      line-height: 1.3;
    }
    .event-desc {
      font-size: 0.875rem;
      color: var(--text-muted);
      line-height: 1.45;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
    .event-details {
      display: flex;
      flex-direction: column;
      gap: 0.4rem;
      font-size: 0.825rem;
      color: var(--text-muted);
      padding: 0.75rem 0;
      border-top: 1px solid rgba(255, 255, 255, 0.05);
      border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    }
    .detail-row {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }
    .card-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: auto;
      padding-top: 0.25rem;
    }
    .seat-info {
      display: flex;
      flex-direction: column;
    }
    .seat-count {
      font-size: 0.85rem;
      font-weight: 700;
      color: var(--accent);
    }
    .fee-badge {
      font-size: 0.75rem;
      font-weight: 600;
      color: var(--text-dim);
    }

    /* Registration Modal */
    .modal-backdrop {
      position: fixed;
      inset: 0;
      background: rgba(0, 0, 0, 0.75);
      backdrop-filter: blur(8px);
      display: none;
      align-items: center;
      justify-content: center;
      z-index: 100;
      padding: 1.25rem;
    }
    .modal {
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      border-radius: 16px;
      max-width: 480px;
      width: 100%;
      padding: 1.75rem;
      display: flex;
      flex-direction: column;
      gap: 1.25rem;
      box-shadow: 0 20px 50px rgba(0, 0, 0, 0.5);
      animation: modalIn 0.2s ease;
    }
    @keyframes modalIn {
      from { opacity: 0; transform: scale(0.96); }
      to { opacity: 1; transform: scale(1); }
    }
    .modal-head {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .modal-title {
      font-size: 1.25rem;
      font-weight: 700;
      color: #FFF;
    }
    .close-btn {
      background: transparent;
      border: none;
      color: var(--text-dim);
      font-size: 1.5rem;
      cursor: pointer;
      line-height: 1;
    }
    .close-btn:hover { color: var(--text); }
    .form-group {
      display: flex;
      flex-direction: column;
      gap: 0.4rem;
    }
    .form-label {
      font-size: 0.8rem;
      font-weight: 600;
      color: var(--text-muted);
    }
    .form-input {
      background: rgba(11, 15, 25, 0.8);
      border: 1px solid var(--card-border);
      border-radius: 8px;
      padding: 0.65rem 0.85rem;
      color: var(--text);
      font-size: 0.9rem;
      outline: none;
      transition: border-color 0.2s ease;
    }
    .form-input:focus { border-color: var(--primary); }
    .modal-alert {
      padding: 0.75rem;
      border-radius: 8px;
      font-size: 0.85rem;
      display: none;
    }
    .alert-success { background: rgba(16, 185, 129, 0.15); color: #34D399; border: 1px solid rgba(16, 185, 129, 0.3); }
    .alert-error { background: rgba(239, 68, 68, 0.15); color: #F87171; border: 1px solid rgba(239, 68, 68, 0.3); }

    /* Footer */
    footer {
      border-top: 1px solid var(--card-border);
      padding: 2rem 1.5rem;
      background: rgba(11, 15, 25, 0.5);
      margin-top: auto;
    }
    .footer-inner {
      max-width: 1200px;
      margin: 0 auto;
      display: flex;
      flex-wrap: wrap;
      justify-content: space-between;
      align-items: center;
      gap: 1rem;
      color: var(--text-dim);
      font-size: 0.85rem;
    }
    .footer-links {
      display: flex;
      gap: 1.5rem;
    }
    .footer-links a {
      color: var(--text-muted);
      text-decoration: none;
      transition: color 0.2s ease;
    }
    .footer-links a:hover { color: var(--primary); }
  </style>
</head>
<body>

  <!-- Header -->
  <header>
    <div class="nav-container">
      <a href="/" class="brand">
        <div class="brand-logo">⚡</div>
        <span>ZENITH '26</span>
      </a>
      <div class="nav-actions">
        <a href="/docs" class="btn btn-outline" target="_blank">📖 Swagger Docs</a>
        <a href="https://github.com/chinmayshirgaonkar-ctrl/Zenith-fest/raw/main/zenith26-fest.apk" class="btn btn-primary">📱 Download APK</a>
      </div>
    </div>
  </header>

  <!-- Hero -->
  <section class="hero">
    <div class="live-pill">
      <span class="live-dot"></span>
      College Fest Management System
    </div>
    <h1>Ignite the Campus at Zenith '26</h1>
    <p>The annual national-level technical, cultural, and sports extravaganza. Explore 14 premier competitions, register online, or connect via our native Android app.</p>
    <div class="hero-meta">
      <span>📅 November 14 - 15, 2026</span>
      <span>📍 Main Campus Arena</span>
      <span>⚡ 14 Premier Events</span>
      <span>🏆 Trophy & Cash Prizes</span>
    </div>
  </section>

  <!-- Stats Grid -->
  <section class="stats-container" id="stats-section">
    <div class="stat-card">
      <span class="stat-label">Total Events</span>
      <span class="stat-val" id="stat-events">14</span>
      <span class="stat-sub">Across 5 Categories</span>
    </div>
    <div class="stat-card">
      <span class="stat-label">Total Registrations</span>
      <span class="stat-val" id="stat-regs" style="color: var(--accent);">0</span>
      <span class="stat-sub">Live student signups</span>
    </div>
    <div class="stat-card">
      <span class="stat-label">Total Capacity</span>
      <span class="stat-val" id="stat-cap">1,507</span>
      <span class="stat-sub">Across all arenas</span>
    </div>
    <div class="stat-card">
      <span class="stat-label">System Status</span>
      <span class="stat-val" style="color: var(--success); font-size: 1.4rem;">ONLINE 🟢</span>
      <span class="stat-sub">FastAPI + Vercel Serverless</span>
    </div>
  </section>

  <!-- Controls -->
  <section class="controls-container">
    <div class="category-pills" id="category-pills">
      <button class="pill-btn active" onclick="filterCategory('All')">All Events</button>
      <button class="pill-btn" onclick="filterCategory('Technical')">💻 Technical</button>
      <button class="pill-btn" onclick="filterCategory('Cultural')">🎭 Cultural</button>
      <button class="pill-btn" onclick="filterCategory('Sports')">⚽ Sports</button>
      <button class="pill-btn" onclick="filterCategory('Gaming')">🎮 Gaming</button>
      <button class="pill-btn" onclick="filterCategory('Workshop')">📐 Workshop</button>
    </div>
    <input type="text" id="search-input" class="search-input" placeholder="🔍 Search events by name..." oninput="filterEvents()">
  </section>

  <!-- Events Grid -->
  <main class="events-container" id="events-grid">
    <div style="grid-column: 1/-1; text-align: center; color: var(--text-dim); padding: 3rem 0;">
      Loading events...
    </div>
  </main>

  <!-- Registration Modal -->
  <div class="modal-backdrop" id="reg-modal">
    <div class="modal">
      <div class="modal-head">
        <div>
          <div class="modal-title" id="modal-event-title">Register for Event</div>
          <div style="font-size: 0.8rem; color: var(--text-muted);" id="modal-event-meta">Details</div>
        </div>
        <button class="close-btn" onclick="closeModal()">&times;</button>
      </div>

      <div class="modal-alert alert-success" id="alert-success"></div>
      <div class="modal-alert alert-error" id="alert-error"></div>

      <form id="reg-form" onsubmit="submitRegistration(event)" style="display: flex; flex-direction: column; gap: 0.9rem;">
        <input type="hidden" id="reg-event-id">
        <div class="form-group">
          <label class="form-label">Student Full Name *</label>
          <input type="text" id="reg-name" class="form-input" required placeholder="e.g. Chinmay Shirgaonkar">
        </div>
        <div class="form-group">
          <label class="form-label">Roll Number / Student ID *</label>
          <input type="text" id="reg-roll" class="form-input" required placeholder="e.g. 21CS045">
        </div>
        <div class="form-group">
          <label class="form-label">College Email *</label>
          <input type="email" id="reg-email" class="form-input" required placeholder="e.g. chinmay@college.edu">
        </div>
        <div class="form-group">
          <label class="form-label">Phone Number (Optional)</label>
          <input type="tel" id="reg-phone" class="form-input" placeholder="e.g. 9876543210">
        </div>
        <button type="submit" class="btn btn-primary" id="btn-submit" style="justify-content: center; padding: 0.75rem; margin-top: 0.5rem;">
          Confirm Registration
        </button>
      </form>
    </div>
  </div>

  <!-- Footer -->
  <footer>
    <div class="footer-inner">
      <div>
        <strong>Zenith '26 Management System</strong> • Built with FastAPI, SQLite, Kotlin Android & Jetpack Compose
      </div>
      <div class="footer-links">
        <a href="/api/events" target="_blank">Events JSON</a>
        <a href="/api/stats" target="_blank">Stats JSON</a>
        <a href="/docs" target="_blank">Swagger API</a>
        <a href="https://github.com/chinmayshirgaonkar-ctrl/Zenith-fest" target="_blank">GitHub</a>
      </div>
    </div>
  </footer>

  <script>
    let allEvents = [];
    let currentCategory = 'All';

    async function loadStats() {
      try {
        const res = await fetch('/api/stats');
        if (res.ok) {
          const stats = await res.json();
          document.getElementById('stat-events').innerText = stats.total_events;
          document.getElementById('stat-regs').innerText = stats.total_registrations;
          document.getElementById('stat-cap').innerText = stats.total_capacity.toLocaleString();
        }
      } catch (e) {
        console.error('Error fetching stats:', e);
      }
    }

    async function loadEvents() {
      try {
        const res = await fetch('/api/events');
        if (res.ok) {
          allEvents = await res.json();
          renderEvents();
        }
      } catch (e) {
        document.getElementById('events-grid').innerHTML = '<div style="grid-column: 1/-1; text-align: center; color: var(--danger);">Failed to load events.</div>';
      }
    }

    function filterCategory(cat) {
      currentCategory = cat;
      document.querySelectorAll('.pill-btn').forEach(btn => {
        btn.classList.toggle('active', btn.innerText.includes(cat) || (cat === 'All' && btn.innerText.includes('All')));
      });
      renderEvents();
    }

    function filterEvents() {
      renderEvents();
    }

    function renderEvents() {
      const search = (document.getElementById('search-input').value || '').toLowerCase().trim();
      const grid = document.getElementById('events-grid');
      
      const filtered = allEvents.filter(ev => {
        const matchesCat = currentCategory === 'All' || ev.category.toLowerCase() === currentCategory.toLowerCase();
        const matchesSearch = !search || ev.title.toLowerCase().includes(search) || ev.description.toLowerCase().includes(search);
        return matchesCat && matchesSearch;
      });

      if (filtered.length === 0) {
        grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; color: var(--text-dim); padding: 3rem 0;">No matching events found.</div>';
        return;
      }

      grid.innerHTML = filtered.map(ev => {
        const feeText = ev.fee > 0 ? `₹${ev.fee} Entry` : 'Free Entry';
        const isFull = ev.available_seats <= 0;
        return `
          <div class="event-card">
            <div class="card-top">
              <div class="event-icon">${ev.icon || '📅'}</div>
              <span class="badge badge-${ev.category}">${ev.category}</span>
            </div>
            <div>
              <h3 class="event-title">${escapeHtml(ev.title)}</h3>
              <p class="event-desc">${escapeHtml(ev.description || 'Join us for this premier Zenith event!')}</p>
            </div>
            <div class="event-details">
              <div class="detail-row">
                <span>📅</span>
                <span>${ev.event_date} • ${ev.event_time}</span>
              </div>
              <div class="detail-row">
                <span>📍</span>
                <span>${escapeHtml(ev.venue)}</span>
              </div>
            </div>
            <div class="card-footer">
              <div class="seat-info">
                <span class="seat-count">${ev.available_seats} seats left</span>
                <span class="fee-badge">${feeText}</span>
              </div>
              <button 
                class="btn ${isFull ? 'btn-outline' : 'btn-primary'}" 
                ${isFull ? 'disabled' : ''} 
                onclick="openModal(${ev.id}, '${escapeHtml(ev.title)}', '${ev.event_date}', '${escapeHtml(ev.venue)}')">
                ${isFull ? 'Event Full' : 'Register Now'}
              </button>
            </div>
          </div>
        `;
      }).join('');
    }

    function escapeHtml(str) {
      if (!str) return '';
      return str.replace(/[&<>"']/g, m => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[m]));
    }

    function openModal(id, title, date, venue) {
      document.getElementById('reg-event-id').value = id;
      document.getElementById('modal-event-title').innerText = title;
      document.getElementById('modal-event-meta').innerText = `${date} • ${venue}`;
      document.getElementById('alert-success').style.display = 'none';
      document.getElementById('alert-error').style.display = 'none';
      document.getElementById('reg-form').style.display = 'flex';
      document.getElementById('reg-modal').style.display = 'flex';
    }

    function closeModal() {
      document.getElementById('reg-modal').style.display = 'none';
      document.getElementById('reg-form').reset();
    }

    async function submitRegistration(e) {
      e.preventDefault();
      const eventId = document.getElementById('reg-event-id').value;
      const btn = document.getElementById('btn-submit');
      const alertSuccess = document.getElementById('alert-success');
      const alertError = document.getElementById('alert-error');

      alertSuccess.style.display = 'none';
      alertError.style.display = 'none';
      btn.disabled = true;
      btn.innerText = 'Registering...';

      const payload = {
        student_name: document.getElementById('reg-name').value.trim(),
        roll_number: document.getElementById('reg-roll').value.trim(),
        email: document.getElementById('reg-email').value.trim(),
        phone: document.getElementById('reg-phone').value.trim() || null
      };

      try {
        const res = await fetch(`/api/events/${eventId}/register`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });

        const data = await res.json();
        if (res.ok) {
          alertSuccess.innerHTML = `<strong>Registration Confirmed!</strong><br>Registration ID: #${data.id}<br>Confirmation sent to ${payload.email}`;
          alertSuccess.style.display = 'block';
          document.getElementById('reg-form').style.display = 'none';
          loadStats();
          loadEvents();
          setTimeout(() => closeModal(), 3500);
        } else {
          alertError.innerText = data.detail || 'Registration failed. Please try again.';
          alertError.style.display = 'block';
        }
      } catch (err) {
        alertError.innerText = 'Network error. Please check your connection.';
        alertError.style.display = 'block';
      } finally {
        btn.disabled = false;
        btn.innerText = 'Confirm Registration';
      }
    }

    // Close modal on backdrop click
    window.onclick = function(event) {
      const modal = document.getElementById('reg-modal');
      if (event.target === modal) {
        closeModal();
      }
    };

    // Initial load
    loadStats();
    loadEvents();
  </script>
</body>
</html>
"""
