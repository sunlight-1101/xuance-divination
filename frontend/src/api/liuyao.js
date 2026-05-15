import request from '../utils/request'

export function analyzeLiuyao(data) {
  return request.post('/liuyao/analyze', data)
}

