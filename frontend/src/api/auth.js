import request from '../utils/request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function sendEmailCode(data) {
  return request.post('/auth/send-email-code', data)
}

export function sendResetPasswordCode(data) {
  return request.post('/auth/send-reset-code', data)
}

export function logout() {
  return request.post('/auth/logout')
}

export function updateProfile(data) {
  return request.post('/auth/profile', data)
}

export function updateNickname(data) {
  return request.post('/auth/nickname', data)
}

export function changePassword(data) {
  return request.post('/auth/change-password', data)
}

export function resetPassword(data) {
  return request.post('/auth/reset-password', data)
}
