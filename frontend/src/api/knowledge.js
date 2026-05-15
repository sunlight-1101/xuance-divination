import request from '../utils/request'

export function listKnowledge(params) {
  return request.get('/knowledge/list', { params })
}

function withOperator(data = {}) {
  const user = JSON.parse(localStorage.getItem('xuance_user') || 'null')
  return { ...data, operatorUserId: user?.id || null }
}

function operatorParams(params = {}) {
  const user = JSON.parse(localStorage.getItem('xuance_user') || 'null')
  return { ...params, operatorUserId: user?.id || null }
}

export function createKnowledge(data) {
  return request.post('/knowledge/create', withOperator(data))
}

export function updateKnowledge(data) {
  return request.put('/knowledge/update', withOperator(data))
}

export function deleteKnowledge(id) {
  return request.delete(`/knowledge/${id}`, { params: operatorParams() })
}
