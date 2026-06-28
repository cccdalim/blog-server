import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import type { TocItem } from '@/types'
import { normalizeMarkdown } from '@/utils/normalizeMarkdown'

let mdInstance: MarkdownIt | null = null

function createMarkdownIt(): MarkdownIt {
  const md = new MarkdownIt({
    html: false,
    linkify: true,
    typographer: true,
    breaks: true,
    highlight(str, lang) {
      if (lang && hljs.getLanguage(lang)) {
        try {
          return hljs.highlight(str, { language: lang }).value
        } catch {
          // fallback below
        }
      }
      return hljs.highlightAuto(str).value
    },
  })

  // 自定义 fence 渲染 — 添加复制按钮
  md.renderer.rules.fence = (tokens, idx, options) => {
    const token = tokens[idx]
    if (!token) return ''
    const code = token.content
    const lang = token.info.trim() || 'text'
    const highlighted = options.highlight?.(code, lang, '') ?? md.utils.escapeHtml(code)
    const encoded = encodeURIComponent(code)

    return `<div class="code-block" data-lang="${lang}">
      <div class="code-block__header">
        <span class="code-block__lang">${lang}</span>
        <button type="button" class="code-block__copy" data-code="${encoded}">复制</button>
      </div>
      <pre class="code-block__pre"><code class="hljs language-${lang}">${highlighted}</code></pre>
    </div>`
  }

  // 自定义 heading 渲染 — 添加 id 供 TOC 锚点
  md.renderer.rules.heading_open = (tokens, idx, options, env, self) => {
    const token = tokens[idx]
    if (!token) return ''
    const nextToken = tokens[idx + 1]
    const text = nextToken?.type === 'inline' ? nextToken.content : ''
    const id = slugify(text)
    token.attrSet('id', id)
    return self.renderToken(tokens, idx, options)
  }

  return md
}

function slugify(text: string): string {
  return text
    .toLowerCase()
    .replace(/[^\w\u4e00-\u9fff\s-]/g, '')
    .replace(/\s+/g, '-')
    .replace(/-+/g, '-')
    .trim()
}

export function useMarkdownRenderer() {
  if (!mdInstance) {
    mdInstance = createMarkdownIt()
  }

  function render(content: string): string {
    return mdInstance!.render(normalizeMarkdown(content))
  }

  function extractToc(content: string): TocItem[] {
    const md = mdInstance ?? createMarkdownIt()
    const tokens = md.parse(normalizeMarkdown(content), {})
    const toc: TocItem[] = []

    for (let i = 0; i < tokens.length; i++) {
      const token = tokens[i]
      if (!token || token.type !== 'heading_open') continue
      const level = parseInt(token.tag.slice(1), 10)
      const inline = tokens[i + 1]
      if (inline?.type === 'inline') {
        toc.push({
          id: slugify(inline.content),
          text: inline.content,
          level,
        })
      }
    }

    return toc
  }

  return { render, extractToc }
}
