import request from '../utils/request'

export function listRecords(params) {
  return request.get('/records', { params })
}

export function getRecord(id) {
  return request.get(`/records/${id}`)
}

export function deleteRecord(id) {
  return request.delete(`/records/${id}`)
}

