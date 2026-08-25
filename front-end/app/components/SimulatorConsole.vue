<template>
  <div class="simulator-panel glass-panel">
    <div class="simulator-header">
      <h3>⚡ High-Load Simulator Console</h3>
      <span class="badge" :class="active ? 'badge-active' : 'badge-idle'">
        {{ active ? "RUNNING" : "IDLE" }}
      </span>
    </div>
    <p class="simulator-desc">
      Simulate thousands of concurrent user interactions (views and likes)
      hitting Kafka ingestion pipelines.
    </p>
    <div class="simulator-controls">
      <div class="control-group">
        <label for="views-input">Views/sec:</label>
        <input
          id="views-input"
          type="number"
          min="1"
          max="100"
          :value="viewsPerSec"
          @input="
            $emit('update:viewsPerSec', parseInt($event.target.value) || 0)
          "
        />
      </div>
      <div class="control-group">
        <label for="likes-input">Likes/sec:</label>
        <input
          id="likes-input"
          type="number"
          min="1"
          max="50"
          :value="likesPerSec"
          @input="
            $emit('update:likesPerSec', parseInt($event.target.value) || 0)
          "
        />
      </div>
      <button
        type="button"
        class="simulator-toggle-btn"
        :class="{ stop: active }"
        @click="$emit('toggle')"
      >
        {{ active ? "Stop Ingestion Load" : "Inject Load" }}
      </button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  active: {
    type: Boolean,
    required: true,
  },
  viewsPerSec: {
    type: Number,
    required: true,
  },
  likesPerSec: {
    type: Number,
    required: true,
  },
});

defineEmits(["toggle", "update:viewsPerSec", "update:likesPerSec"]);
</script>
