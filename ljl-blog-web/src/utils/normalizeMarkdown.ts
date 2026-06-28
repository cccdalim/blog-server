/**
 * 规范化 Markdown 正文 — 渲染/保存前统一格式，使排版更整齐
 */
export function normalizeMarkdown(content: string): string {
  if (!content?.trim()) return ''

  let text = content.replace(/\r\n/g, '\n').trim()

  // 块级元素前补空行（标题、列表、代码块、引用、表格）
  text = text.replace(/([^\n])\n(#{1,6}\s)/g, '$1\n\n$2')
  text = text.replace(/([^\n])\n([-*+]\s)/g, '$1\n\n$2')
  text = text.replace(/([^\n])\n(\d+\.\s)/g, '$1\n\n$2')
  text = text.replace(/([^\n])\n(```)/g, '$1\n\n$2')
  text = text.replace(/([^\n])\n(>\s)/g, '$1\n\n$2')
  text = text.replace(/([^\n])\n(\|.+\|)\n/g, '$1\n\n$2\n')

  // 代码块结束符后补空行
  text = text.replace(/(```)\n([^\n`])/g, '$1\n\n$2')

  // 合并多余空行（最多保留一个空行）
  text = text.replace(/\n{3,}/g, '\n\n')

  return text
}

/** 新建文章时的 Markdown 结构模板 */
export const MARKDOWN_ARTICLE_TEMPLATE = `## 背景

在这里介绍问题背景或写作动机。

## 核心内容

### 要点一

正文段落，建议每段 3～5 行，段与段之间空一行。

### 要点二

- 列表项一
- 列表项二

\`\`\`js
// 代码示例
console.log('Hello Blog')
\`\`\`

> 引用：补充说明或重要结论。

## 总结

简要总结全文要点。`

/** 新建学习文档时的 Markdown 结构模板 */
export const MARKDOWN_DOC_TEMPLATE = `## 概述

简要说明本教程/文档的目标与适用读者。

## 前置知识

- 知识点一
- 知识点二

## 正文

### 第一节

正文内容，配合代码示例说明。

\`\`\`js
// 示例代码
console.log('Hello Docs')
\`\`\`

### 第二节

- 步骤一
- 步骤二

> 提示：重要概念或注意事项。

## 小结

总结本章节要点，可附延伸阅读链接。`
