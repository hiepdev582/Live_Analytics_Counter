<template>
  <div class="video-card" :class="{ active: active }">
    <div class="video-thumbnail">
      <span class="live-badge">LIVE</span>
      <div class="thumbnail-glow"></div>
    </div>
    <div class="video-info">
      <h3 class="video-title">{{ video.title }}</h3>
      <div class="video-stats-row">
        <span class="stat-item">
          <i class="icon">👁️</i> {{ formatNumber(video.views) }} views
        </span>
        <span class="stat-item">
          <i class="icon">❤️</i> {{ formatNumber(video.likes) }} likes
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  video: {
    type: Object,
    required: true,
  },
  active: {
    type: Boolean,
    default: false,
  },
});

const formatNumber = (num) => {
  if (num === undefined || num === null) return "0";
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
};
</script>
