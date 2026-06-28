import { useScroll } from '@vueuse/core'
import { computed, type Ref } from 'vue'

/** 阅读进度 — 基于页面滚动计算百分比 */
export function useReadingProgress(target: Ref<HTMLElement | null>) {
  const { y } = useScroll(window)

  const progress = computed(() => {
    const el = target.value
    if (!el) return 0

    const rect = el.getBoundingClientRect()
    const elTop = el.offsetTop
    const elHeight = el.offsetHeight
    const viewportHeight = window.innerHeight
    const scrollTop = y.value

    if (elHeight <= viewportHeight) return 100

    const scrolled = scrollTop - elTop + viewportHeight
    const total = elHeight
    const pct = Math.min(100, Math.max(0, (scrolled / total) * 100))
    return Math.round(pct)
  })

  return { progress }
}
