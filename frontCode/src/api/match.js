import request from '@/utils/request'

// 计算星座匹配度
export function calculateMatch(data) {
    return request({
        url: '/starMatchAI/match/calculate',
        method: 'post',
        data
    })
}

// 获取历史匹配记录
export function getMatchHistory(data) {
    return request({
        url: '/starMatchAI/match/history',
        method: 'post',
        data
    })
}

// 流式匹配分析
export function calculateMatchStream(data, onProgress) {
    return new Promise((resolve, reject) => {
        let buffer = '';

        fetch(import.meta.env.VITE_API_BASE_URL + '/starMatchAI/match/calculate/stream', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        }).then(response => {
            if (!response.ok) {
                throw new Error(response.statusText);
            }

            const reader = response.body.getReader();
            const decoder = new TextDecoder();

            function processChunk(chunk) {
                buffer += decoder.decode(chunk, { stream: true });

                let newlineIndex;
                while ((newlineIndex = buffer.indexOf('\n')) !== -1) {
                    const line = buffer.slice(0, newlineIndex);
                    buffer = buffer.slice(newlineIndex + 1);
                    if (line.trim().startsWith('data:')) {
                        try {
                            // 提取 data: 后面的内容
                            const eventData = line.slice(5).trim();
                            const parsedData = JSON.parse(eventData);
                            // 确保回调被调用
                            if (onProgress && typeof onProgress === 'function') {
                                onProgress(parsedData);
                            } else {
                                console.warn('onProgress is not a function:', onProgress);
                            }
                        } catch (e) {
                            console.error('Parse error:', e);
                            console.error('Problematic line:', line);
                        }
                    } else {
                        console.log('Skipping non-data line:', line);
                    }
                }
            }

            async function readStream() {
                try {
                    console.log('Starting to read stream');
                    while (true) {
                        const { done, value } = await reader.read();

                        if (done) {
                            console.log('Stream complete');
                            if (buffer.length > 0) {
                                console.log('Processing remaining buffer');
                                processChunk(new Uint8Array());
                            }
                            resolve();
                            break;
                        }

                        console.log('Received chunk');
                        processChunk(value);
                    }
                } catch (error) {
                    console.error('Stream reading error:', error);
                    reject(error);
                }
            }

            readStream();
        }).catch(error => {
            console.error('Fetch error:', error);
            reject(error);
        });
    });
}