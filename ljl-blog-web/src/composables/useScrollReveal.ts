import { nextTick, onMounted, onUnmounted, watch, type Ref } from 'vue'
import { useRoute } from 'vue-router'

const REVEAL_OBSERVER_OPTIONS: IntersectionObserverInit = {
  threshold: 0.12,
  rootMargin: '0px 0px -32px 0px',
}

let sharedObserver: IntersectionObserver | null = null
const observedRoots = new WeakSet<Element>()

const REVEAL_SELECTOR = '.reveal, .reveal-scale'

function prefersReducedMotion(): boolean {
  return window.matchMedia('(prefers-reduced-motion: reduce)').matches
}

function ensureObserver(): IntersectionObserver {
  if (sharedObserver) return sharedObserver

  sharedObserver = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        entry.target.classList.add('is-visible')
        sharedObserver?.unobserve(entry.target)
      }
    })
  }, REVEAL_OBSERVER_OPTIONS)

  return sharedObserver
}

export function revealAllImmediately(root: HTMLElement | null): void {
  if (!root) return
  root.querySelectorAll(REVEAL_SELECTOR).forEach((el) => {
    el.classList.add('is-visible')
  })
}

/** 扫描容器内 .reveal 元素并绑定 IntersectionObserver */
export function initScrollReveal(root: HTMLElement | null): void {
  if (!root) return

  if (prefersReducedMotion()) {
    revealAllImmediately(root)
    return
  }

  const observer = ensureObserver()

  root.querySelectorAll(`${REVEAL_SELECTOR}:not(.is-visible)`).forEach((el) => {
    if (observedRoots.has(el)) return
    observedRoots.add(el)
    observer.observe(el)

    const rect = el.getBoundingClientRect()
    if (rect.top < window.innerHeight && rect.bottom > 0) {
      el.classList.add('is-visible')
      observer.unobserve(el)
      observedRoots.delete(el)
    }
  })
}

/** 重置容器内 reveal 状态 — 仅页面卸载时使用，路由切换时不调用 */
export function resetScrollReveal(root: HTMLElement | null): void {
  if (!root) return
  root.querySelectorAll(REVEAL_SELECTOR).forEach((el) => {
    el.classList.remove('is-visible')
    sharedObserver?.unobserve(el)
    observedRoots.delete(el)
  })
}

/** 强制显示尚未触发动画的 reveal 元素 */
export function ensureRevealVisible(root: HTMLElement | null): void {
  if (!root) return
  root.querySelectorAll(`${REVEAL_SELECTOR}:not(.is-visible)`).forEach((el) => {
    el.classList.add('is-visible')
  })
}

/** 布局级 scroll reveal — 在 MainLayout 使用 */
export function useLayoutScrollReveal(rootRef: Ref<HTMLElement | null>) {
  const route = useRoute()
  let revealTimer: ReturnType<typeof setTimeout> | null = null

  /** 路由切换后刷新 reveal — 不 reset，避免内容被隐藏 */
  function refreshReveal() {
    nextTick(() => {
      initScrollReveal(rootRef.value)
      ensureRevealVisible(rootRef.value)
    })
  }

  /** 多次兜底，覆盖异步列表渲染 */
  function scheduleRevealRefresh() {
    refreshReveal()
    if (revealTimer) clearTimeout(revealTimer)
    revealTimer = setTimeout(() => {
      initScrollReveal(rootRef.value)
      revealAllImmediately(rootRef.value)
    }, 500)
  }

  onMounted(scheduleRevealRefresh)
  watch(() => route.name, scheduleRevealRefresh)
  onUnmounted(() => {
    if (revealTimer) clearTimeout(revealTimer)
    resetScrollReveal(rootRef.value)
  })

  return { refreshReveal: scheduleRevealRefresh }
}

/** 页面级 scroll reveal — 单页容器（兼容旧用法） */
export function useScrollReveal(targetRef: Ref<HTMLElement | null>) {
  const route = useRoute()

  function refresh() {
    nextTick(() => initScrollReveal(targetRef.value))
  }

  onMounted(refresh)
  watch(() => route.fullPath, refresh)
  onUnmounted(() => resetScrollReveal(targetRef.value))
}

export const REVEAL_REFRESH_KEY = Symbol('revealRefresh')
