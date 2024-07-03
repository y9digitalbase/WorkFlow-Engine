package net.risesoft.api.processadmin;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import net.risesoft.pojo.Y9Result;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/19
 */
public interface ProcessDataCopyApi {

    /**
     * 复制拷贝流程定义数据
     *
     * @param sourceTenantId 源租户Id
     * @param targetTenantId 目标租户Id
     * @param modelKey 流程定义key
     * @return Y9Result<Object>
     */
    @PostMapping("/copyModel")
    Y9Result<Object> copyModel(@RequestParam("sourceTenantId") String sourceTenantId,
        @RequestParam("targetTenantId") String targetTenantId, @RequestParam("modelKey") String modelKey);

}
