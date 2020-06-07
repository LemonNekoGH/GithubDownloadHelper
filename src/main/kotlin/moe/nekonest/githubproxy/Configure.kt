package moe.nekonest.githubproxy

import com.alibaba.fastjson.annotation.JSONField

data class Configure(
        @JSONField val oldFileScanningIntervals: Int,
        @JSONField val fileTimedOut: Int
)