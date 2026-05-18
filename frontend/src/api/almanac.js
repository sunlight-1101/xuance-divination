import request from '../utils/request'

export function getAlmanacDay(date) {
  return request.get('/almanac/day', { params: { date } })
}
