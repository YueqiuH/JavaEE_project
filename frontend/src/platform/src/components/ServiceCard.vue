<template>
  <article class="service-card" :style="cardStyle">
    <button
      class="favorite-button"
      :class="{ 'is-favorite': favorite }"
      type="button"
      :aria-label="favorite ? `取消收藏${service.title}` : `收藏${service.title}`"
      @click.stop="$emit('favorite', service.key)"
    >
      <el-icon><StarFilled v-if="favorite" /><Star v-else /></el-icon>
    </button>

    <button class="service-main" type="button" @click="$emit('open', service)">
      <span class="service-icon">
        <el-icon><component :is="service.icon" /></el-icon>
      </span>
      <span class="service-copy">
        <span class="service-title-row">
          <strong>{{ service.title }}</strong>
          <span v-if="service.ai" class="ai-label">AI</span>
        </span>
        <span class="service-description">{{ service.description }}</span>
        <span class="service-summary">
          <span class="summary-dot"></span>
          {{ service.summary }}
        </span>
      </span>
    </button>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import { Star, StarFilled } from '@element-plus/icons-vue'
import { domainMap } from '@/config/navigation.js'

const props = defineProps({
  service: { type: Object, required: true },
  favorite: { type: Boolean, default: false },
})

defineEmits(['favorite', 'open'])

const cardStyle = computed(() => {
  const domain = domainMap[props.service.domain]
  return {
    '--service-color': domain.color,
    '--service-soft': domain.softColor,
  }
})
</script>

<style scoped>
.service-card {
  position: relative;
  min-width: 0;
  min-height: 132px;
  overflow: hidden;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  transition: border-color 180ms ease, background-color 180ms ease;
}

.service-card:hover,
.service-card:focus-within {
  background: color-mix(in srgb, var(--service-soft) 48%, white);
  border-color: var(--service-color);
}

.service-main {
  display: flex;
  width: 100%;
  min-height: 132px;
  align-items: flex-start;
  gap: 14px;
  padding: 20px 44px 18px 18px;
  color: inherit;
  text-align: left;
  background: transparent;
  border: 0;
  cursor: pointer;
}

.service-main:focus-visible,
.favorite-button:focus-visible {
  outline: 2px solid var(--service-color);
  outline-offset: -2px;
}

.service-icon {
  display: inline-flex;
  width: 44px;
  height: 44px;
  flex: 0 0 44px;
  align-items: center;
  justify-content: center;
  color: var(--service-color);
  background: var(--service-soft);
  border-radius: 8px;
}

.service-icon :deep(svg) {
  width: 24px;
  height: 24px;
}

.service-copy {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
}

.service-title-row {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 8px;
}

.service-title-row strong {
  min-width: 0;
  overflow: hidden;
  font-size: 16px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ai-label {
  padding: 1px 5px;
  color: #7a4f00;
  font-size: 11px;
  font-weight: 700;
  line-height: 18px;
  background: #fff2c9;
  border-radius: 4px;
}

.service-description {
  display: -webkit-box;
  min-height: 42px;
  margin-top: 5px;
  overflow: hidden;
  color: var(--color-text-secondary);
  font-size: 13px;
  line-height: 21px;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.service-summary {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  color: var(--color-text-tertiary);
  font-size: 12px;
}

.summary-dot {
  width: 5px;
  height: 5px;
  background: var(--service-color);
  border-radius: 50%;
}

.favorite-button {
  position: absolute;
  z-index: 1;
  top: 12px;
  right: 12px;
  display: inline-flex;
  width: 32px;
  height: 32px;
  align-items: center;
  justify-content: center;
  color: var(--color-text-tertiary);
  background: transparent;
  border: 0;
  border-radius: 4px;
  cursor: pointer;
}

.favorite-button:hover {
  color: var(--color-accent);
  background: rgba(255, 255, 255, 0.76);
}

.favorite-button.is-favorite {
  color: var(--color-accent);
}
</style>
