import { onMounted, onUnmounted, ref, type Ref } from 'vue'
import type { TocItem } from '@/types'

/** TOC 锚点高亮 — 滚动时激活当前可见标题 */
export function useTocHighlight(tocItems: Ref<TocItem[]>) {
  const activeId = ref('')

  function updateActiveId() {
    const headings = tocItems.value
      .map((item) => document.getElementById(item.id))
      .filter(Boolean) as HTMLElement[]

    if (!headings.length) return

    const scrollTop = window.scrollY + 120
    let current = headings[0]!.id

    for (const heading of headings) {
      if (heading.offsetTop <= scrollTop) {
        current = heading.id
      }
    }

    activeId.value = current
  }

  onMounted(() => {
    window.addEventListener('scroll', updateActiveId, { passive: true })
    updateActiveId()
  })

  onUnmounted(() => {
    window.removeEventListener('scroll', updateActiveId)
  })

  function scrollToHeading(id: string) {
    const el = document.getElementById(id)
    if (el) {
      const top = el.offsetTop - 80
      window.scrollTo({ top, behavior: 'smooth' })
    }
  }

  return { activeId, scrollToHeading }
}
