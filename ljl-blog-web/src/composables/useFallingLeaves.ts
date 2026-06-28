export type LeafVariant = 'green' | 'yellow' | 'orange' | 'brown'

export interface FallingLeafConfig {
  id: string
  left: number
  delay: number
  duration: number
  sway: number
  spin: number
  opacity: number
  scale: number
  variant: LeafVariant
}

const VARIANTS: LeafVariant[] = ['green', 'yellow', 'orange', 'brown']

function randomVariant(): LeafVariant {
  return VARIANTS[Math.floor(Math.random() * VARIANTS.length)] ?? 'green'
}

function randomId(): string {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`
}

/** 生成一片随机参数的落叶 */
export function createRandomLeaf(): FallingLeafConfig {
  return {
    id: randomId(),
    left: Math.random() * 100,
    delay: Math.random() * 16,
    duration: 10 + Math.random() * 16,
    sway: (20 + Math.random() * 60) * (Math.random() > 0.5 ? 1 : -1),
    spin: 120 + Math.random() * 600,
    opacity: 0.35 + Math.random() * 0.4,
    scale: 0.7 + Math.random() * 0.7,
    variant: randomVariant(),
  }
}

/** 批量生成随机落叶 */
export function createFallingLeaves(count: number): FallingLeafConfig[] {
  return Array.from({ length: count }, () => createRandomLeaf())
}
