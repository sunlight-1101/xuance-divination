import request from '../utils/request'

export function buildZiweiChart(data) {
  return request.post('/ziwei/chart', data)
}

export function analyzeZiwei(data) {
  return request.post('/ziwei/analyze', data)
}
