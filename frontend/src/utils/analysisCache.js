export function saveAnalysisCache(key, payload) {
  localStorage.setItem(key, JSON.stringify({ ...payload, savedAt: Date.now() }))
}

export function readAnalysisCache(key) {
  try {
    return JSON.parse(localStorage.getItem(key) || 'null')
  } catch {
    return null
  }
}

export function clearAnalysisCache(key) {
  localStorage.removeItem(key)
}

export function readPendingAnalysis(key, ttlMs = 10 * 60 * 1000) {
  const pending = readAnalysisCache(key)
  if (!pending) return null
  const startedAt = Number(pending.startedAt || pending.savedAt || 0)
  if (!startedAt || Date.now() - startedAt > ttlMs) {
    clearAnalysisCache(key)
    return null
  }
  return pending
}

export function startPendingAnalysis(key, payload) {
  saveAnalysisCache(key, { ...payload, startedAt: Date.now() })
}

export function finishPendingAnalysis(key) {
  clearAnalysisCache(key)
}
