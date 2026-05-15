import request from '../utils/request'

export function getQuota(userId) {
  return request.get('/quota', { params: { userId } })
}

export function rechargeQuota(data) {
  return request.post('/quota/recharge', data)
}

export function createAlipayOrder(data) {
  return request.post('/quota/alipay/create', data)
}
