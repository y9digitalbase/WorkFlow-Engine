package net.risesoft.service;

import net.risesoft.pojo.Y9Page;


import java.util.Map;

public interface TodoService {

    /**
     * 获取待办列表
     *
     * @param itemId     事项Id
     * @param searchTerm 搜索词
     * @param page       页数
     * @param rows       行数
     * @return Map<String, Object>
     */
    Map<String, Object> list(String itemId, String searchTerm, Integer page, Integer rows);

    /**
     * 获取待办列表
     *
     * @param itemId     事项Id
     * @param searchTerm 搜索词
     * @param page       页数
     * @param rows       行数
     * @return Y9Page<Map < String, Object>>
     */
    Y9Page<Map<String, Object>> listNew(String itemId, String searchTerm, Integer page, Integer rows);

    /**
     * 待办列表
     *
     * @param itemId       事项Id
     * @param tableName    表名
     * @param searchMapStr 搜索条件
     * @param page         页数
     * @param rows         条数
     * @return Y9Page<Map < String, Object>>
     */
    Y9Page<Map<String, Object>> searchList(String itemId, String tableName, String searchMapStr, Integer page,
                                           Integer rows);
}
