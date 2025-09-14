package com.entity.remote

/**
 * 获取第三方广告开关状态响应数据
 * 
 * @property switch 广告开关状态：1-开，2-关
 * 
 * @author 理想换电开发团队
 * @version 1.0.0
 * @since 2024年
 */
data class GetThirdAdStatusRemote(
    val switch: Int
)
