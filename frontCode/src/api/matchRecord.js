import request from '@/utils/request'

// 获取匹配历史记录
export function getMatchRecords(params) {
    return request({
        url: '/api/match-record',
        method: 'get',
        params
    })
}

// 获取匹配记录详情
export function getMatchRecordDetail(id) {
    return request({
        url: `/api/match-record/${id}`,
        method: 'get'
    })
}

// 保存匹配记录
export function saveMatchRecord(data) {
    return request({
        url: '/api/match-record',
        method: 'post',
        data
    })
}

// 删除匹配记录
export function deleteMatchRecord(id) {
    return request({
        url: `/api/match-record/${id}`,
        method: 'delete'
    })
} 