import request from '../utils/request'

export function getQuota() {
  return request.get('/quota')
}

export function rechargeQuota(data) {
  return request.post('/quota/recharge', data)
}

export function createAlipayOrder(data) {
  return request.post('/quota/alipay/create', data)
}
