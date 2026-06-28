import { inject, nextTick, watch, type Ref } from 'vue'
import { REVEAL_REFRESH_KEY } from './useScrollReveal'

/** 异步数据加载完成后重新扫描 reveal 元素 */
export function useRevealAfterLoad(loading: Ref<boolean>) {
  const refreshReveal = inject<(() => void) | undefined>(REVEAL_REFRESH_KEY)

  watch(loading, (val, prev) => {
    if (prev === true && val === false) {
      nextTick(() => refreshReveal?.())
    }
  })
}
