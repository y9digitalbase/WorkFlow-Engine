package net.risesoft.api.itemadmin;

import javax.validation.constraints.NotBlank;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import net.risesoft.model.itemadmin.ActRuDetailModel;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;

/**
 * 办结接口
 *
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
public interface ItemDoneApi {

    /**
     * 查询办结任务数量
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @param systemName 系统名称
     * @return {@code Y9Result<Integer>} 通用请求返回对象 - data 是办结任务数量
     * @throws Exception Exception
     */
    @GetMapping("/countByUserIdAndSystemName")
    Y9Result<Integer> countByUserIdAndSystemName(@RequestParam("tenantId") String tenantId,
        @RequestParam("userId") String userId, @RequestParam("systemName") @NotBlank String systemName)
        throws Exception;

    /**
     * 获取监控办结列表
     *
     * @param tenantId 租户id
     * @param systemName 系统名称
     * @param page page
     * @param rows rows
     * @return {@code Y9Page<ActRuDetailModel>} 通用分页请求返回对象 - rows 是监控办结列表
     * @throws Exception Exception
     */
    @GetMapping("/findBySystemName")
    Y9Page<ActRuDetailModel> findBySystemName(@RequestParam("tenantId") String tenantId,
        @RequestParam("systemName") String systemName, @RequestParam("page") Integer page,
        @RequestParam("rows") Integer rows) throws Exception;

    /**
     * 获取个人办结列表
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @param systemName 系统名称
     * @param page page
     * @param rows rows
     * @return {@code Y9Page<ActRuDetailModel>} 通用分页请求返回对象 - rows 是个人办结列表
     * @throws Exception Exception
     */
    @GetMapping("/findByUserIdAndSystemName")
    Y9Page<ActRuDetailModel> findByUserIdAndSystemName(@RequestParam("tenantId") String tenantId,
        @RequestParam("userId") String userId, @RequestParam("systemName") String systemName,
        @RequestParam("page") Integer page, @RequestParam("rows") Integer rows) throws Exception;

    /**
     * 监控办结列表搜索
     *
     * @param tenantId 租户id
     * @param systemName 系统名称
     * @param tableName 表名称
     * @param searchMapStr 搜索内容
     * @param page page
     * @param rows rows
     * @return {@code Y9Page<ActRuDetailModel>} 通用分页请求返回对象 - rows 是监控办结列表
     * @throws Exception Exception
     */
    @GetMapping("/searchBySystemName")
    Y9Page<ActRuDetailModel> searchBySystemName(@RequestParam("tenantId") String tenantId,
        @RequestParam("systemName") String systemName, @RequestParam(value = "tableName") String tableName,
        @RequestParam(value = "searchMapStr", required = false) String searchMapStr, @RequestParam("page") Integer page,
        @RequestParam("rows") Integer rows) throws Exception;

    /**
     * 个人办结列表搜索
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @param systemName 系统名称
     * @param tableName 表名称
     * @param searchMapStr 搜索内容
     * @param page page
     * @param rows rows
     * @return {@code Y9Page<ActRuDetailModel>} 通用分页请求返回对象 - rows 是个人办结列表
     * @throws Exception Exception
     */
    @GetMapping("/searchByUserIdAndSystemName")
    Y9Page<ActRuDetailModel> searchByUserIdAndSystemName(@RequestParam("tenantId") String tenantId,
        @RequestParam("userId") String userId, @RequestParam("systemName") String systemName,
        @RequestParam(value = "tableName") String tableName,
        @RequestParam(value = "searchMapStr", required = false) String searchMapStr, @RequestParam("page") Integer page,
        @RequestParam("rows") Integer rows) throws Exception;
}
