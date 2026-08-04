<script setup>
defineProps({
  label: String,
  value: [String, Number],
  hint: String
})
</script>

<template>
  <div class="stat-tile">
    <span>{{ label }}</span>
    <strong>{{ value }}</strong>
    <small>{{ hint }}</small>
  </div>
</template>
