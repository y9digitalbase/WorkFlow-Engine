package net.risesoft.api;

import lombok.RequiredArgsConstructor;
import net.risesoft.api.itemadmin.OrganWordApi;
import net.risesoft.api.platform.org.PersonApi;
import net.risesoft.api.platform.org.PositionApi;
import net.risesoft.model.platform.Person;
import net.risesoft.model.platform.Position;
import net.risesoft.service.OrganWordService;
import net.risesoft.y9.Y9LoginUserHolder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 编号接口
 *
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/services/rest/organWord")
public class OrganWordApiImpl implements OrganWordApi {

    private final OrganWordService organWordService;

    private final PositionApi positionApi;

    private final PersonApi personApi;

    /**
     * 检查编号是否已经被使用了
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param characterValue 机关代字
     * @param custom 机关代字标志
     * @param year 文号年份
     * @param numberTemp 编号
     * @param itemId 事项id
     * @param common common
     * @param processSerialNumber 流程编号
     * @return Integer
     */
    @Override
    @GetMapping(value = "/checkNumberStr", produces = MediaType.APPLICATION_JSON_VALUE)
    public Integer checkNumberStr(String tenantId, String userId, String characterValue, String custom, Integer year,
        Integer numberTemp, String itemId, Integer common, String processSerialNumber){
        Person person = personApi.get(tenantId, userId).getData();
        Y9LoginUserHolder.setPerson(person);
        Y9LoginUserHolder.setTenantId(tenantId);
        return organWordService.checkNumberStr(characterValue, custom, year, numberTemp, itemId, common,
            processSerialNumber);
    }

    /**
     * 判断机构代字custom在某个流程实例中是否已经编号,没有编号的话就查找有权限的编号的机关代字
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param custom 机关代字标志
     * @param processSerialNumber 流程编号
     * @param processInstanceId 流程实例id
     * @param itembox 办件状态，todo（待办），doing（在办），done（办结）
     * @return Map&lt;String, Object&gt;
     */
    @Override
    @GetMapping(value = "/exist", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> exist(String tenantId, String userId, String custom, String processSerialNumber,
        String processInstanceId, String itembox){
        Person person = personApi.get(tenantId, userId).getData();
        Y9LoginUserHolder.setPerson(person);
        Y9LoginUserHolder.setTenantId(tenantId);
        return organWordService.exist(custom, processSerialNumber, processInstanceId, itembox);
    }

    /**
     * 查找有权限的机构代字
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param custom 机关代字标志
     * @param itemId 事项id
     * @param processDefinitionId 流程定义id
     * @param taskDefKey 任务定义key
     * @return List&lt;Map&lt;String, Object&gt;&gt;
     */
    @Override
    @GetMapping(value = "/findByCustom", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> findByCustom(String tenantId, String userId, String custom, String itemId,
        String processDefinitionId, String taskDefKey){
        Position position = positionApi.get(tenantId, userId).getData();
        Y9LoginUserHolder.setPosition(position);
        Y9LoginUserHolder.setTenantId(tenantId);
        return organWordService.findByCustom(itemId, processDefinitionId, taskDefKey, custom);
    }

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
     * @return Map&lt;String, Object&gt;
     */
    @Override
    @GetMapping(value = "/getNumber", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getNumber(String tenantId, String userId, String custom, String characterValue,
        Integer year, Integer common, String itemId){
        Person person = personApi.get(tenantId, userId).getData();
        Y9LoginUserHolder.setPerson(person);
        Y9LoginUserHolder.setTenantId(tenantId);
        return organWordService.getNumber(custom, characterValue, year, common, itemId);
    }

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
     * @return Integer
     */
    @Override
    @GetMapping(value = "/getNumberOnly", produces = MediaType.APPLICATION_JSON_VALUE)
    public Integer getNumberOnly(String tenantId, String userId, String custom, String characterValue, Integer year,
        Integer common, String itemId){
        Person person = personApi.get(tenantId, userId).getData();
        Y9LoginUserHolder.setPerson(person);
        Y9LoginUserHolder.setTenantId(tenantId);
        return organWordService.getNumberOnly(custom, characterValue, year, common, itemId);
    }
}
