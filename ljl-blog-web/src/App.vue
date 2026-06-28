<script setup lang="ts">
import { RouterView } from 'vue-router'
import FallingLeaves from '@/components/layout/FallingLeaves.vue'
import MinecraftLever from '@/components/layout/MinecraftLever.vue'
</script>

<template>
  <MinecraftLever />
  <FallingLeaves />
  <RouterView />
</template>
