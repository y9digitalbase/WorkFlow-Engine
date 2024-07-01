package y9.client.rest.processadmin;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import net.risesoft.api.processadmin.HistoricActivityApi;
import net.risesoft.model.processadmin.HistoricActivityInstanceModel;
import net.risesoft.pojo.Y9Result;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/19
 */
@FeignClient(contextId = "HistoricActivityApiClient", name = "${y9.service.processAdmin.name:processAdmin}",
    url = "${y9.service.processAdmin.directUrl:}",
    path = "/${y9.service.processAdmin.name:processAdmin}/services/rest/historicActivity")
public interface HistoricActivityApiClient extends HistoricActivityApi {

    /**
     * 根据流程实例获取历史节点实例
     *
     * @param tenantId 租户id
     * @param processInstanceId 流程实例id
     * @return {@code Y9Result<List<HistoricActivityInstanceModel>>}
     */
    @Override
    @GetMapping("/getByProcessInstanceId")
    Y9Result<List<HistoricActivityInstanceModel>> getByProcessInstanceId(@RequestParam("tenantId") String tenantId,
        @RequestParam("processInstanceId") String processInstanceId);

    /**
     *
     * @param tenantId 租户id
     * @param processInstanceId 流程实例id
     * @param year 年度
     * @return {@code Y9Result<List<HistoricActivityInstanceModel>>}
     */
    @Override
    @GetMapping("/getByProcessInstanceIdAndYear")
    Y9Result<List<HistoricActivityInstanceModel>> getByProcessInstanceIdAndYear(
        @RequestParam("tenantId") String tenantId, @RequestParam("processInstanceId") String processInstanceId,
        @RequestParam("year") String year);
}
