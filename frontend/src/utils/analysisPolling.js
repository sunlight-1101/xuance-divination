import { getRecord } from '../api/record'

const wait = ms => new Promise(resolve => setTimeout(resolve, ms))

export async function waitForAnalysisRecord(recordId, options = {}) {
  const intervalMs = options.intervalMs || 3000
  const timeoutMs = options.timeoutMs || 300000
  const startedAt = Date.now()

  while (Date.now() - startedAt < timeoutMs) {
    const record = await getRecord(recordId)
    options.onTick?.(record)
    if (record?.status === 'DONE' && record.resultJson) {
      return record
    }
    if (record?.status === 'FAILED') {
      const error = new Error('分析失败，请稍后重试。')
      error.record = record
      throw error
    }
    await wait(intervalMs)
  }

  const error = new Error('分析仍在进行中，可稍后到历史记录查看。')
  error.timeout = true
  throw error
}
