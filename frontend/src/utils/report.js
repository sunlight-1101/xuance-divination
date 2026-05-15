export function buildReportMarkdown(report, rules = [], options = {}) {
  if (!report) return ''
  const lines = []
  lines.push(`# ${options.title || '分析报告'}`)
  lines.push('')
  lines.push(`## 核心结论`)
  lines.push(report.coreConclusion || '-')
  lines.push('')
  lines.push(`信心：${report.confidence || '-'}`)
  lines.push('')

  if (report.keyEvidence?.length) {
    lines.push(`## 关键依据`)
    report.keyEvidence.forEach(item => lines.push(`- ${item}`))
    lines.push('')
  }

  if (report.detailedAnalysis) {
    lines.push(`## 详细分析`)
    Object.entries(report.detailedAnalysis).forEach(([key, value]) => {
      lines.push(`### ${detailLabels[key] || key}`)
      lines.push(value || '-')
      lines.push('')
    })
  }

  if (report.timing?.length) {
    lines.push(`## 时间窗口`)
    report.timing.forEach(item => {
      lines.push(`- ${item.time || '-'}：${item.reason || '-'}`)
    })
    lines.push('')
  }

  if (report.suggestion) {
    lines.push(`## 行动建议`)
    lines.push(report.suggestion)
    lines.push('')
  }

  if (report.missingFields?.length) {
    lines.push(`## 需要补充`)
    report.missingFields.forEach(item => lines.push(`- ${item}`))
    lines.push('')
  }

  if (rules.length) {
    lines.push(`## 引用规则`)
    rules.forEach(rule => {
      lines.push(`- 【${rule.category || '未分类'}】${rule.title}`)
    })
    lines.push('')
  }

  const classicReferences = options.classicReferences || []
  if (classicReferences.length) {
    lines.push('## 引用典籍')
    classicReferences.forEach(item => lines.push(`- ${item}`))
    lines.push('')
  }

  return lines.join('\n').trim() + '\n'
}

export async function copyText(text) {
  if (navigator.clipboard?.writeText) {
    await navigator.clipboard.writeText(text)
    return
  }
  const textarea = document.createElement('textarea')
  textarea.value = text
  textarea.setAttribute('readonly', 'readonly')
  textarea.style.position = 'fixed'
  textarea.style.left = '-9999px'
  document.body.appendChild(textarea)
  textarea.select()
  document.execCommand('copy')
  document.body.removeChild(textarea)
}

export function downloadMarkdown(filename, content) {
  const blob = new Blob([content], { type: 'text/markdown;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

const detailLabels = {
  yongShen: '用神',
  shiYing: '世应',
  usefulGodState: '用神状态',
  movingYao: '动爻',
  hiddenSpirit: '伏神',
  timing: '应期'
}
