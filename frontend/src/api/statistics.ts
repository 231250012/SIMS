import apiClient from './index'

export interface ApiStatResponse {
  id: number
  endpoint: string
  callCount: number
  redisCallCount: number
  lastUpdate: string
}

export interface SystemOverviewResponse {
  totalStudents: number
  totalClasses: number
  totalUsers: number
  totalApiCalls: number
  totalApiCallsRedis: number
  totalApiEndpoints: number
  averageApiCalls: number
  averageScore: number
  topPerformingClass: string
  topApiEndpoints: ApiStatResponse[]
  lastUpdateTime: string
}

export interface ScoreDistributionResponse {
  name: string
  count: number
}

export const statisticsApi = {
  // Get API call statistics
  getApiStatistics(): Promise<ApiStatResponse[]> {
    return apiClient.get('/statistics/api-calls').then(res => res.data)
  },

  // Get top API statistics
  getTopApiStatistics(limit: number = 10): Promise<ApiStatResponse[]> {
    return apiClient.get('/statistics/api-calls/top', { params: { limit } }).then(res => res.data)
  },

  // Get total API calls
  getTotalApiCalls(): Promise<{
    databaseTotal: number
    redisTotal: number
    averagePerEndpoint: number
    difference: number
  }> {
    return apiClient.get('/statistics/api-calls/total').then(res => res.data)
  },

  // Get system overview
  getSystemOverview(): Promise<SystemOverviewResponse> {
    return apiClient.get('/statistics/overview').then(res => res.data)
  },

  // Get score distribution
  getScoreDistribution(): Promise<ScoreDistributionResponse[]> {
    return apiClient.get('/statistics/score-distribution').then(res => res.data)
  }
}