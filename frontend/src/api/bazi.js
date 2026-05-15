import request from '../utils/request'

export function analyzeBazi(data) {
  return request.post('/bazi/analyze', data)
}

