import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080'

export const api = axios.create({
  baseURL: API_BASE_URL,
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('authToken')
  if (token) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export interface LoginStepOneRequest {
  username: string
  password: string
}

export interface LoginStepOneResponse {
  message: string
  email: string
}

export interface LoginStepTwoRequest {
  email: string
  otp: string
}

export interface LoginStepTwoResponse {
  Message: string
  Token: string
}

export interface UserDto {
  id: number
  username: string
  email: string
  accountVisibility: 'PUBLIC' | 'PRIVATE'
  profilePicture?: string
}

export interface PostDto {
  id: number
  title: string
  content: string
  createdAt?: string
  user?: UserDto
  comments?: CommentDto[]
  categoryId?: number
  categoryName?: string
  profilePicture?: string
  likes?: unknown[]
  noOfComments?: number
  noOfLikes?: number
  postImage?: string
}

export interface CommentDto {
  id: number
  text: string
  createdAt?: string
  postId?: number
  user?: UserDto
  parentCommentId?: number
  likesCount?: number
}

export interface ProfileDto {
  userId?: number
  username: string
  name?: string
  website?: string
  link2?: string
  bio: string
  profilePicture: string
  noOfPosts: number
  posts: PostDto[]
  isPrivate: boolean
  /** True when the current user is following this profile's user. */
  isFollowing?: boolean
  /** True when the current user has a pending follow request sent to this user. */
  isFollowRequested?: boolean
  followersCount?: number
  followingCount?: number
  /** At most 2 mutual followers for "Followed by X, Y and more" (when viewing someone else's profile). */
  mutualPreview?: UserPreview[]
  /** Total mutual count (so we can show "and N others"). */
  mutualCount?: number
  /** Current viewer's user id (when viewing someone else's profile). */
  currentUserId?: number
}

export interface UserPreview {
  id: number
  username: string
  profilePicture?: string
  /** Optional full name for richer UI. */
  name?: string
  /** Optional followers count for search cards. */
  followersCount?: number
  /** Optional text like "Followed by X and N others". */
  followedBySummary?: string
  /** True when the current viewer follows this user (for search). */
  following?: boolean
}

/** User preview with flag for whether the current user follows them (for others' follow lists). */
export interface UserPreviewWithFollowStatus extends UserPreview {
  following: boolean
}

// Notifications
export type NotificationType =
  | 'LIKE'
  | 'COMMENT'
  | 'FOLLOW_REQUEST'
  | 'FOLLOWED'
  | 'FOLLOW_REQUEST_ACCEPTED'
  | 'COMMENT_REPLY'
  | 'COMMENT_LIKE'

export interface NotificationDtoApi {
  id: number
  type: NotificationType
  read: boolean
  createdAt: string

  actorId?: number
  actorUsername?: string
  actorProfilePicture?: string

  postId?: number
  postTitle?: string
  commentText?: string

  canFollowBack: boolean
}

export interface PaginatedNotificationsResponse {
  content: NotificationDtoApi[]
  totalPages: number
  totalElements: number
  number: number
  size: number
  first: boolean
  last: boolean
  numberOfElements: number
}

// Messaging
export interface MessageDtoApi {
  id: number
  conversationId: number
  senderId: number
  senderUsername: string
  content: string
  createdAt: string
  deliveredAt: string | null
  readAt: string | null
}

export interface ConversationDtoApi {
  id: number
  otherUser: UserPreview
  lastMessage: string | null
  lastMessageAt: string | null
  unreadCount: number
}

export interface PaginatedResponse<T> {
  content: T[]
  totalPages: number
  totalElements: number
  number: number
  size: number
  first: boolean
  last: boolean
  numberOfElements: number
}

export const authApi = {
  register(user: Record<string, unknown>) {
    return api.post<string>('/api/auth/register', user)
  },
  oauth2Register(payload: { email: string; username: string; password?: string }) {
    return api.post<{ message: string; token: string; username: string; email: string }>('/api/auth/oauth2-register', payload)
  },
  verifyRegisterOtp(email: string, otp: string) {
    return api.post('/api/auth/verifyOtp', { email, otp })
  },
  loginStepOne(payload: LoginStepOneRequest) {
    return api.post<LoginStepOneResponse>('/api/auth/login', payload)
  },
  loginStepTwo(payload: LoginStepTwoRequest) {
    return api.post<LoginStepTwoResponse>('/api/auth/verifyLoginOtp', payload)
  },
  forgotPassword(email: string) {
    return api.post<MessageResponse>('/api/auth/forgot-password', { email })
  },
  resetPassword(token: string, newPassword: string) {
    return api.post<MessageResponse>('/api/auth/reset-password', { token, newPassword })
  },
}

/** Paginated feed response from GET /api/posts/ */
export interface PaginatedPostsResponse {
  content: PostDto[]
  totalPages: number
  totalElements: number
  number: number
  size: number
  first: boolean
  last: boolean
  numberOfElements: number
}

export const postApi = {
  /** Get feed (paginated). Defaults: page=0, size=15. Pass seed for stable random order. */
  getAll(page = 0, size = 15, seed = 'default') {
    return api.get<PaginatedPostsResponse>('/api/posts/', { params: { page, size, seed } })
  },
  getById(id: number) {
    return api.get<PostDto>(`/api/posts/${id}`)
  },
  create(post: { title: string; content: string; categoryId: number }) {
    return api.post<PostDto>('/api/posts', post)
  },
  update(postId: number, post: { title?: string; content?: string; categoryId?: number }) {
    return api.put<PostDto>(`/api/posts/${postId}`, post)
  },
  getComments(postId: number) {
    return api.get<CommentDto[]>(`/api/comments/post/${postId}`)
  },
  addComment(postId: number, text: string, parentCommentId?: number) {
    return api.post<CommentDto>(`/api/comments/post/${postId}`, parentCommentId ? { text, parentCommentId } : { text })
  },
  like(postId: number) {
    return api.post(`/api/likes/posts/${postId}/like`)
  },
  unlike(postId: number) {
    return api.delete(`/api/likes/posts/${postId}/like`)
  },
  hasLiked(postId: number) {
    return api.get<boolean>(`/api/likes/posts/${postId}/likes/hasLiked`)
  },
  likedUsers(postId: number) {
    return api.get<UserDto[]>(`/api/likes/posts/${postId}/likes/users`)
  },
  updatePostImage(postId: number, file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return api.post<PostDto>(`/api/posts/postImage/${postId}`, formData)
  },
}

export const commentApi = {
  delete(commentId: number) {
    return api.delete<string>(`/api/comments/${commentId}`)
  },
  like(commentId: number) {
    return api.post<void>(`/api/comments/${commentId}/like`)
  },
  unlike(commentId: number) {
    return api.delete<void>(`/api/comments/${commentId}/like`)
  },
}

export const profileApi = {
  me() {
    return api.get<ProfileDto>('/api/profile/me')
  },
  byUserId(userId: number) {
    return api.get<ProfileDto>(`/api/profile/${userId}`)
  },
  updateBio(bio: string) {
    return api.put<ProfileDto>('/api/profile/bio', { bio })
  },
  updateName(name: string) {
    return api.put<ProfileDto>('/api/profile/name', { name })
  },
  updateWebsite(website: string) {
    return api.put<ProfileDto>('/api/profile/website', { website })
  },
  updateLink2(link2: string) {
    return api.put<ProfileDto>('/api/profile/link2', { link2 })
  },
  updateProfilePicture(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return api.post<ProfileDto>('/api/profile/profile-picture', formData)
  },
}

export interface MessageResponse {
  message: string
}

export const followApi = {
  follow(userId: number) {
    return api.post<MessageResponse>(`/api/follow-system/follow/${userId}`)
  },
  unfollow(userId: number) {
    return api.delete<MessageResponse>(`/api/follow-system/unfollow/${userId}`)
  },
  removeFollower(followerId: number) {
    return api.delete<MessageResponse>(`/api/follow-system/removeFollower/${followerId}`)
  },
  cancelRequest(userId: number) {
    return api.delete<MessageResponse>(`/api/follow-system/cancelFollowRequest/${userId}`)
  },
  myFollowers() {
    return api.get<UserPreview[]>('/api/follow-system/followers')
  },
  myFollowing() {
    return api.get<UserPreview[]>('/api/follow-system/following')
  },
  followersOfUser(userId: number) {
    return api.get<UserPreviewWithFollowStatus[]>(`/api/follow-system/users/${userId}/followers`)
  },
  followingOfUser(userId: number) {
    return api.get<UserPreviewWithFollowStatus[]>(`/api/follow-system/users/${userId}/following`)
  },
  mutualFollowers(userId: number) {
    return api.get<UserPreviewWithFollowStatus[]>(`/api/follow-system/users/${userId}/mutual-followers`)
  },
  acceptRequest(userId: number) {
    return api.post<MessageResponse>(`/api/follow-system/acceptFollowRequest/${userId}`)
  },
  rejectRequest(userId: number) {
    return api.post<MessageResponse>(`/api/follow-system/rejectFollowRequest/${userId}`)
  },
}

export const imageApi = {
  upload(file: File) {
    const formData = new FormData()
    formData.append('image', file)
    return api.post<string>('/api/images/upload', formData)
  },
}

export interface Category {
  id: number
  name: string
}

export const categoryApi = {
  getAll() {
    return api.get<Category[]>('/api/categories')
  },
}

/** Search users by username (partial match). */
export const searchApi = {
  searchUsers(q: string) {
    return api.get<UserPreview[]>('/api/users/search', { params: { q: q.trim() } })
  },
}

/** Current user account (id, username, email). For settings. */
export const userApi = {
  getById(userId: number) {
    return api.get<UserDto>(`/api/users/${userId}`)
  },
  update(userId: number, data: { username?: string; email?: string; password?: string }) {
    return api.put<UserDto>(`/api/users/${userId}`, data)
  },
  usernameAvailable(username: string) {
    return api.get<{ available: boolean }>(`/api/users/username-available`, { params: { username } })
  },
}

export const accountApi = {
  makePrivate() {
    return api.put<MessageResponse>('/api/account/private')
  },
  makePublic() {
    return api.put<MessageResponse>('/api/account/public')
  },
}

export const notificationApi = {
  list(page = 0, size = 20) {
    return api.get<PaginatedNotificationsResponse>('/api/notifications', {
      params: { page, size },
    })
  },
  unreadCount() {
    return api.get<number>('/api/notifications/unread-count')
  },
  markRead(id: number) {
    return api.post<MessageResponse>(`/api/notifications/${id}/read`)
  },
  markAllRead() {
    return api.post<MessageResponse>('/api/notifications/read-all')
  },
}

export const messageApi = {
  listConversations(page = 0, size = 30) {
    return api.get<PaginatedResponse<ConversationDtoApi>>('/api/messages/conversations', {
      params: { page, size },
    })
  },
  listMessages(conversationId: number, page = 0, size = 50) {
    return api.get<PaginatedResponse<MessageDtoApi>>(`/api/messages/conversations/${conversationId}`, {
      params: { page, size },
    })
  },
  sendToUser(userId: number, content: string) {
    return api.post<MessageDtoApi>(`/api/messages/to/${userId}`, { content })
  },
  markConversationRead(conversationId: number) {
    return api.post<MessageResponse>(`/api/messages/conversations/${conversationId}/read`)
  },

  deleteConversation(conversationId: number) {
    return api.delete<MessageResponse>(`/api/messages/conversations/${conversationId}`)
  },
}

