import request from '../utils/request'

export function listClassicBooks() {
  return request.get('/classics/books')
}

export function listClassicChapters(params) {
  return request.get('/classics/chapters', { params })
}
