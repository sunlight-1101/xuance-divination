import request from '../utils/request'

export function analyzeBazi(data) {
  return request.post('/bazi/analyze', data)
}

export function analyzeBaziCompatibility(data) {
  return request.post('/bazi/compatibility', data)
}

export function analyzeHeCan(data) {
  return request.post('/bazi/hecan', data)
}
