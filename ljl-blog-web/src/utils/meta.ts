import { SITE_DESCRIPTION, SITE_NAME } from '@/constants'
import { absoluteUrl } from '@/config/env'

/** 更新页面 Meta — 为公网 SEO / 分享预览预留 */
export function updatePageMeta(title: string, path: string): void {
  const fullTitle = `${title} | ${SITE_NAME}`
  document.title = fullTitle

  setMetaContent('name', 'description', SITE_DESCRIPTION)
  setLinkHref('canonical', absoluteUrl(path))
  setMetaContent('property', 'og:title', fullTitle)
  setMetaContent('property', 'og:description', SITE_DESCRIPTION)
  setMetaContent('property', 'og:url', absoluteUrl(path))
  setMetaContent('property', 'og:site_name', SITE_NAME)
}

function setMetaContent(attr: 'name' | 'property', key: string, content: string): void {
  let el = document.querySelector(`meta[${attr}="${key}"]`)
  if (!el) {
    el = document.createElement('meta')
    el.setAttribute(attr, key)
    document.head.appendChild(el)
  }
  el.setAttribute('content', content)
}

function setLinkHref(rel: string, href: string): void {
  let el = document.querySelector(`link[rel="${rel}"]`)
  if (!el) {
    el = document.createElement('link')
    el.setAttribute('rel', rel)
    document.head.appendChild(el)
  }
  el.setAttribute('href', href)
}
