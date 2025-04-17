// @ts-ignore
/* eslint-disable */
import request from '@/request.ts'

/** getCommits GET /api/git/commits */
export async function getCommitsUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResultListGitHubCommitInfo_>('/api/git/commits', {
    method: 'GET',
    ...(options || {}),
  })
}
