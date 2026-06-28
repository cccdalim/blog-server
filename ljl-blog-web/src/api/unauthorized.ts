type UnauthorizedHandler = () => void

let handler: UnauthorizedHandler | null = null

export function onUnauthorized(fn: UnauthorizedHandler) {
  handler = fn
}

export function triggerUnauthorized() {
  handler?.()
}
