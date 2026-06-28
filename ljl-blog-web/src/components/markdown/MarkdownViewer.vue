<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useMarkdownRenderer } from '@/composables/useMarkdown'

interface Props {
  content: string
}

const props = defineProps<Props>()

const containerRef = ref<HTMLElement | null>(null)
const { render } = useMarkdownRenderer()

const html = computed(() => render(props.content))

async function copyToClipboard(text: string): Promise<boolean> {
  if (navigator.clipboard?.writeText) {
    try {
      await navigator.clipboard.writeText(text)
      return true
    } catch {
      // 非 HTTPS 等场景下 clipboard API 可能不可用，走 fallback
    }
  }

  try {
    const textarea = document.createElement('textarea')
    textarea.value = text
    textarea.setAttribute('readonly', '')
    textarea.style.position = 'fixed'
    textarea.style.left = '-9999px'
    document.body.appendChild(textarea)
    textarea.select()
    const ok = document.execCommand('copy')
    document.body.removeChild(textarea)
    return ok
  } catch {
    return false
  }
}

async function handleCopyClick(event: MouseEvent) {
  const target = event.target as HTMLElement
  if (!target.classList.contains('code-block__copy')) return

  const encoded = target.getAttribute('data-code')
  if (!encoded) return

  const code = decodeURIComponent(encoded)
  const ok = await copyToClipboard(code)
  if (ok) {
    ElMessage.success('代码已复制')
  } else {
    ElMessage.error('复制失败，请手动选择代码复制')
  }
}

onMounted(() => {
  containerRef.value?.addEventListener('click', handleCopyClick)
})
</script>

<template>
  <article ref="containerRef" class="markdown-viewer" v-html="html" />
</template>

<style lang="scss">
@use '@/styles/markdown.scss';
</style>
