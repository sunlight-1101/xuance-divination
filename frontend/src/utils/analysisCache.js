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
