<template>
  <div class="app-container">
    <!-- Navbar / Header -->
    <header class="app-header">
      <div class="logo-section">
        <div class="logo-pulse"></div>
        <h1>Live Analytics Counter</h1>
      </div>
      <div class="status-section">
        <span
          class="status-indicator"
          :class="{ connected: wsConnected }"
        ></span>
        <span class="status-text">{{
          wsConnected
            ? "Connected (Real-time Active)"
            : "Connecting to Gateway..."
        }}</span>
      </div>
    </header>

    <!-- Main Content -->
    <main class="app-main">
      <!-- Left Column: Video List -->
      <section class="video-list-section">
        <h2 class="section-title">📺 Active Broadcasts</h2>
        <div class="video-grid">
          <VideoCard
            v-for="video in videos"
            :key="video.id"
            :video="video"
            :active="selectedVideo?.id === video.id"
            @click="selectVideo(video)"
          />
        </div>
      </section>

      <!-- Right Column: Detail & Simulation -->
      <section class="video-detail-section">
        <div v-if="selectedVideo" class="detail-container glass-panel">
          <VideoPlayerMock :title="selectedVideo.title" />

          <div class="detail-header">
            <h2 class="detail-title">{{ selectedVideo.title }}</h2>
            <p class="video-id">
              ID: <code>{{ selectedVideo.id }}</code>
            </p>
          </div>

          <!-- Counters Display -->
          <div class="stats-showcase">
            <div class="stat-box views-box">
              <div class="stat-box-bg"></div>
              <span class="stat-label">Views Counter</span>
              <span
                class="stat-value animated-number"
                :key="selectedVideo.views"
              >
                {{ formatNumber(selectedVideo.views) }}
              </span>
            </div>

            <div class="stat-box likes-box">
              <div class="stat-box-bg"></div>
              <span class="stat-label">Likes Counter</span>
              <span
                class="stat-value animated-number"
                :key="selectedVideo.likes"
              >
                {{ formatNumber(selectedVideo.likes) }}
              </span>
            </div>
          </div>

          <!-- Interaction Actions -->
          <div class="actions-panel">
            <button
              type="button"
              class="action-btn view-btn"
              @click="triggerView"
            >
              <span class="btn-icon">👁️</span> Sim View (+1)
            </button>
            <button
              type="button"
              class="action-btn like-btn"
              @click="triggerLike"
            >
              <span class="btn-icon">❤️</span> Like Video (+1)
            </button>
          </div>

          <!-- High Load Simulator Panel -->
          <SimulatorConsole
            :active="simulationActive"
            v-model:views-per-sec="simViewsPerSec"
            v-model:likes-per-sec="simLikesPerSec"
            @toggle="toggleSimulation"
          />
        </div>
        <div v-else class="select-prompt glass-panel">
          <div class="prompt-icon">👈</div>
          <h3>Select a video broadcast to begin</h3>
          <p>
            Subscribe to a real-time room to observe live analytics updates.
          </p>
        </div>
      </section>
    </main>

    <!-- Footer -->
    <footer class="app-footer">
      <p>
        Live Analytics Counter - Microservices & Real-Time Write-Back
        Architecture Demo
      </p>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from "vue";
import "./app.css";

// Thiết lập SEO Meta Data
useHead({
  title:
    "Live Analytics Counter - Hệ thống đếm lượt xem, lượt thích video trực tuyến",
  meta: [
    {
      name: "description",
      content: "Dịch vụ đếm lượt xem, lượt thích video trực tuyến tốc độ cao.",
    },
    {
      name: "keywords",
      content:
        "live analytics counter, hệ thống đếm lượt xem, hệ thống đếm lượt thích, live streaming analytics, real-time analytics, video analytics",
    },
    {
      property: "og:title",
      content:
        "Live Analytics Counter - Hệ thống đếm lượt xem, lượt thích video trực tuyến",
    },
    {
      property: "og:description",
      content: "Dịch vụ đếm lượt xem, lượt thích video trực tuyến tốc độ cao.",
    },
    { property: "og:type", content: "website" },
  ],
  htmlAttrs: {
    lang: "vi",
  },
});

const videos = ref([]);
const selectedVideo = ref(null);
const wsConnected = ref(false);
const simViewsPerSec = ref(10);
const simLikesPerSec = ref(2);
const simulationActive = ref(false);

let ws = null;
let simInterval = null;

const fetchVideos = async () => {
  try {
    const res = await fetch("/api/videos");
    if (res.ok) {
      const data = await res.json();
      videos.value = data;
      // Keep selected video updated
      if (selectedVideo.value) {
        const updated = data.find((v) => v.id === selectedVideo.value.id);
        if (updated) {
          selectedVideo.value.views = updated.views;
          selectedVideo.value.likes = updated.likes;
        }
      }
    }
  } catch (err) {
    console.error("Error fetching videos:", err);
  }
};

const selectVideo = (video) => {
  selectedVideo.value = video;
  subscribeToRoom(video.id);
};

const triggerView = async () => {
  if (!selectedVideo.value) return;
  try {
    await fetch(`/api/videos/${selectedVideo.value.id}/view`, {
      method: "POST",
    });
  } catch (err) {
    console.error("Error triggering view:", err);
  }
};

const triggerLike = async () => {
  if (!selectedVideo.value) return;
  try {
    await fetch(`/api/videos/${selectedVideo.value.id}/like`, {
      method: "POST",
    });
  } catch (err) {
    console.error("Error triggering like:", err);
  }
};

const subscribeToRoom = (videoId) => {
  if (ws && ws.readyState === WebSocket.OPEN) {
    const message = JSON.stringify({
      action: "subscribe",
      video_id: videoId,
    });
    ws.send(message);
  }
};

const connectWebSocket = () => {
  const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
  const wsUrl = `${protocol}//${window.location.host}/ws/video`;

  ws = new WebSocket(wsUrl);

  ws.onopen = () => {
    wsConnected.value = true;
    console.log("WebSocket Connected");
    if (selectedVideo.value) {
      subscribeToRoom(selectedVideo.value.id);
    }
  };

  ws.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data);
      if (data.type === "stats_update" && data.video_id) {
        // Update selected video stats
        if (selectedVideo.value && selectedVideo.value.id === data.video_id) {
          selectedVideo.value.views = data.views;
          selectedVideo.value.likes = data.likes;
        }
        // Update list stats
        const listVideo = videos.value.find((v) => v.id === data.video_id);
        if (listVideo) {
          listVideo.views = data.views;
          listVideo.likes = data.likes;
        }
      }
    } catch (err) {
      console.error("Error handling WS message:", err);
    }
  };

  ws.onclose = () => {
    wsConnected.value = false;
    console.log("WebSocket Disconnected, reconnecting...");
    setTimeout(connectWebSocket, 3000);
  };
};

const toggleSimulation = () => {
  if (simulationActive.value) {
    clearInterval(simInterval);
    simulationActive.value = false;
  } else {
    if (!selectedVideo.value) return;
    simulationActive.value = true;
    const viewDelay = 1000 / simViewsPerSec.value;
    const likeDelay = 1000 / simLikesPerSec.value;

    let lastViewTime = Date.now();
    let lastLikeTime = Date.now();

    simInterval = setInterval(() => {
      const now = Date.now();
      if (now - lastViewTime >= viewDelay) {
        triggerView();
        lastViewTime = now;
      }
      if (now - lastLikeTime >= likeDelay) {
        triggerLike();
        lastLikeTime = now;
      }
    }, 50);
  }
};

const formatNumber = (num) => {
  if (num === undefined || num === null) return "0";
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
};

onMounted(() => {
  fetchVideos();
  connectWebSocket();
  // Poll video list every 5s just in case, but real-time WS is primary
  setInterval(fetchVideos, 5000);
});

onUnmounted(() => {
  if (ws) ws.close();
  if (simInterval) clearInterval(simInterval);
});
</script>
