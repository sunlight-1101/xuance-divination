import request from '../utils/request'

export function listKnowledge(params) {
  return request.get('/knowledge/list', { params })
}

export function createKnowledge(data) {
  return request.post('/knowledge/create', data)
}

export function updateKnowledge(data) {
  return request.put('/knowledge/update', data)
}

export function deleteKnowledge(id) {
  return request.delete(`/knowledge/${id}`)
}
