<template>
  <div class="video-player-mock">
    <div class="player-content">
      <div class="play-btn"></div>
      <div class="pulse-ring"></div>
      <p class="now-playing">Streaming: {{ title }}</p>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true,
  },
});
</script>
