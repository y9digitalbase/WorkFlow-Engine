package net.risesoft.service;

import java.util.Map;

import net.risesoft.pojo.Y9Page;

public interface MonitorService {

    /**
     * 单位所有件
     *
     * @param itemId
     * @param searchName
     * @param userName
     * @param state
     * @param year
     * @param page
     * @param rows
     * @return
     */
    Y9Page<Map<String, Object>> deptList(String itemId, String searchName, String userName, String state, String year, Integer page, Integer rows);

    /**
     * 监控办件
     *
     * @param searchName
     * @param itemId
     * @param userName
     * @param state
     * @param year
     * @param page
     * @param rows
     * @return
     */
    Y9Page<Map<String, Object>> monitorBanjianList(String searchName, String itemId, String userName, String state, String year, Integer page, Integer rows);

    /**
     * 监控阅件列表
     *
     * @param searchName
     * @param itemId
     * @param senderName
     * @param userName
     * @param state
     * @param year
     * @param page
     * @param rows
     * @return
     */
    Y9Page<Map<String, Object>> monitorChaosongList(String searchName, String itemId, String senderName, String userName, String state, String year, Integer page, Integer rows);

    /**
     * 监控在办列表
     *
     * @param processDefinitionKey
     * @param searchTerm 搜索词
     * @param page
     * @param rows
     * @return
     */
    Y9Page<Map<String, Object>> monitorDoingList(String processDefinitionKey, String searchTerm, Integer page, Integer rows);

    /**
     * 获取监控办结列表
     *
     * @param itemId
     * @param searchTerm 搜索词
     * @param page
     * @param rows
     * @return
     */
    Y9Page<Map<String, Object>> monitorDoneList(String processDefinitionKey, String searchTerm, Integer page, Integer rows);

    /**
     * 获取回收列表
     *
     * @param itemId
     * @param searchTerm 搜索词
     * @param page
     * @param rows
     * @return
     */
    Map<String, Object> monitorRecycleList(String processDefinitionKey, String searchTerm, Integer page, Integer rows);

}
