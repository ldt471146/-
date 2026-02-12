import http from './http'

export const fetchCommunityPosts = (params) => http.get('/api/community/posts', { params })
export const fetchCommunityPostDetail = (id) => http.get(`/api/community/posts/${id}`)
export const createCommunityPost = (data) => http.post('/api/community/posts', data)
export const createCommunityReply = (postId, data) => http.post(`/api/community/posts/${postId}/replies`, data)
export const markCommunityBest = (postId, replyId) =>
  http.post(`/api/community/posts/${postId}/best/${replyId}`)
