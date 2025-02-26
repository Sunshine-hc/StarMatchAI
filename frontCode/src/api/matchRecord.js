import request from '@/utils/request'

// 获取匹配历史记录列表
export function getMatchRecords(params) {
    return request({
        url: '/starMatchAI/match-record/list',
        method: 'get',
        params: {
            ...params,
            _t: new Date().getTime() // 添加时间戳防止缓存
        }
    })
}

// 获取匹配记录详情
export function getMatchRecordDetail(id) {
    return request({
        url: `/starMatchAI/match-record/${id}`,
        method: 'get'
    })
}

// 保存匹配记录
export function saveMatchRecord(data) {
    return request({
        url: '/starMatchAI/match-record',
        method: 'post',
        data
    })
}

// 删除匹配记录
export function deleteMatchRecord(id) {
    return request({
        url: `/starMatchAI/match-record/${id}`,
        method: 'delete'
    })
} 