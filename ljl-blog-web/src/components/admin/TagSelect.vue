<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElOption, ElSelect } from 'element-plus'
import { fetchTags } from '@/api/modules/meta'
import type { TagScope } from '@/types'

const model = defineModel<string[]>({ default: () => [] })

const props = withDefaults(
  defineProps<{
    /** content = Blog/Docs，recipe = 菜谱 */
    scope?: TagScope
  }>(),
  { scope: 'content' },
)

const options = ref<string[]>([])
const selected = ref<string[]>([...model.value])

watch(
  model,
  (value) => {
    if (value.join('\0') !== selected.value.join('\0')) {
      selected.value = [...value]
    }
  },
  { deep: true },
)

watch(
  selected,
  (value) => {
    model.value = [...value]
  },
  { deep: true },
)

const mergedOptions = computed(() => {
  const set = new Set([...options.value, ...selected.value])
  return Array.from(set).sort((a, b) => a.localeCompare(b, 'zh-CN'))
})

async function loadOptions() {
  const res = await fetchTags(props.scope)
  if (res.code === 0) {
    options.value = res.data.map((tag) => tag.name)
  }
}

watch(
  () => props.scope,
  () => {
    loadOptions()
  },
)

onMounted(loadOptions)
</script>

<template>
  <ElSelect
    v-model="selected"
    multiple
    filterable
    allow-create
    default-first-option
    collapse-tags
    collapse-tags-tooltip
    :max-collapse-tags="4"
    :placeholder="scope === 'recipe' ? '选择或输入菜谱标签' : '选择已有标签或输入新标签'"
    class="tag-select"
  >
    <ElOption v-for="tag in mergedOptions" :key="tag" :label="tag" :value="tag" />
  </ElSelect>
</template>

<style scoped lang="scss">
.tag-select {
  width: 100%;
}
</style>
