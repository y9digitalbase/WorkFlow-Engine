package net.risesoft.api.itemadmin;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import net.risesoft.model.itemadmin.OrganWordModel;
import net.risesoft.model.itemadmin.OrganWordPropertyModel;
import net.risesoft.pojo.Y9Result;

/**
 * 编号
 * 
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/19
 */
public interface OrganWordApi {

    /**
     * 检查编号是否已经被使用了
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param characterValue 机关代字
     * @param custom 机关代字标志
     * @param year 文号年份
     * @param numberTemp numberTemp
     * @param itemId 事项id
     * @param common common
     * @param processSerialNumber 流程编号
     * @return @return {@code Y9Result<Integer>} 通用请求返回对象
     */
    @GetMapping("/checkNumberStr")
    Y9Result<Integer> checkNumberStr(@RequestParam("tenantId") String tenantId, @RequestParam("userId") String userId,
        @RequestParam("characterValue") String characterValue, @RequestParam("custom") String custom,
        @RequestParam("year") Integer year, @RequestParam("numberTemp") Integer numberTemp,
        @RequestParam("itemId") String itemId, @RequestParam("common") Integer common,
        @RequestParam("processSerialNumber") String processSerialNumber);

    /**
     * 判断机构代字custom在某个流程实例中是否已经编号,没有编号的话就查找有权限的编号的机关代字
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param custom 机关代字标志
     * @param processSerialNumber 流程编号
     * @param processInstanceId 流程实例id
     * @param itembox 办件状态:todo(待办),doing(在办),done(办结)
     * @return {@code Y9Result<OrganWordModel>} 通用请求返回对象 -data是数据字典列表
     */
    @GetMapping("/exist")
    Y9Result<OrganWordModel> exist(@RequestParam("tenantId") String tenantId, @RequestParam("userId") String userId,
        @RequestParam("custom") String custom, @RequestParam("processSerialNumber") String processSerialNumber,
        @RequestParam(value = "processInstanceId", required = false) String processInstanceId,
        @RequestParam("itembox") String itembox);

    /**
     *
     * Description: 查找有权限的机构代字
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param custom 机关代字标志
     * @param itemId 事项id
     * @param processDefinitionId 流程定义id
     * @param taskDefKey 任务定义key
     * @return {@code Y9Result<List<OrganWordPropertyModel>>} 通用请求返回对象 -data是数据字典列表
     */
    @GetMapping("/findByCustom")
    Y9Result<List<OrganWordPropertyModel>> findByCustom(@RequestParam("tenantId") String tenantId,
        @RequestParam("userId") String userId, @RequestParam("custom") String custom,
        @RequestParam("itemId") String itemId, @RequestParam("processDefinitionId") String processDefinitionId,
        @RequestParam(value = "taskDefKey", required = false) String taskDefKey);

    /**
     * 获取编号
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param custom 机关代字标志
     * @param characterValue 机关代字
     * @param year 文号年份
     * @param common common
     * @param itemId 事项id
     * @return {@code Y9Result<Integer>} 通用请求返回对象 -data是数据字典列表
     */
    @GetMapping("/getNumber")
    Y9Result<Integer> getNumber(@RequestParam("tenantId") String tenantId, @RequestParam("userId") String userId,
        @RequestParam("custom") String custom, @RequestParam("characterValue") String characterValue,
        @RequestParam("year") Integer year, @RequestParam("common") Integer common,
        @RequestParam("itemId") String itemId);

    /**
     * 获取编号的数字
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param custom 机关代字标志
     * @param characterValue 机关代字
     * @param year 文号年份
     * @param common common
     * @param itemId 事项id
     * @return {@code Y9Result<Integer>} 通用请求返回对象 -data是编号的数字
     * 
     */
    @GetMapping("/getNumberOnly")
    Y9Result<Integer> getNumberOnly(@RequestParam("tenantId") String tenantId, @RequestParam("userId") String userId,
        @RequestParam("custom") String custom, @RequestParam("characterValue") String characterValue,
        @RequestParam("year") Integer year, @RequestParam("common") Integer common,
        @RequestParam("itemId") String itemId);

}
