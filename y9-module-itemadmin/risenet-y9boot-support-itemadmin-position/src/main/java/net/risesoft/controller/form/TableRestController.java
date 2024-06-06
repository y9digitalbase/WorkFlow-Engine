package net.risesoft.controller.form;

import lombok.extern.slf4j.Slf4j;
import net.risesoft.consts.UtilConsts;
import net.risesoft.entity.form.Y9Table;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.form.Y9TableService;
import net.risesoft.util.form.DbMetaDataUtil;
import net.risesoft.y9.json.Y9JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
@Slf4j
@RestController
@RequestMapping(value = "/vue/y9form/table")
public class TableRestController {
    private final JdbcTemplate jdbcTemplate4Tenant;

    private final Y9TableService y9TableService;

    public TableRestController(@Qualifier("jdbcTemplate4Tenant") JdbcTemplate jdbcTemplate4Tenant, Y9TableService y9TableService) {
        this.jdbcTemplate4Tenant = jdbcTemplate4Tenant;
        this.y9TableService = y9TableService;
    }

    /**
     * 添加数据库表
     *
     * @param tableName    表名称
     * @param systemName   系统名称
     * @param systemCnName 系统中文名称
     * @return
     */
    @RequestMapping(value = "/addDataBaseTable", method = RequestMethod.POST, produces = "application/json")
    public Y9Result<String> addDataBaseTable(@RequestParam String tableName,
                                             @RequestParam String systemName, @RequestParam String systemCnName) {
        Map<String, Object> map = y9TableService.addDataBaseTable(tableName, systemName, systemCnName);
        if ((boolean) map.get(UtilConsts.SUCCESS)) {
            return Y9Result.successMsg((String) map.get("msg"));
        }
        return Y9Result.failure((String) map.get("msg"));
    }

    /**
     * 新生成表，创建数据库表
     *
     * @param tables 表信息
     * @param fields 字段信息
     * @return
     */
    @RequestMapping(value = "/buildTable", method = RequestMethod.POST, produces = "application/json")
    public Y9Result<String> buildTable(@RequestParam String tables,
                                       @RequestParam String fields) {
        Map<String, Object> map;
        Y9Table table = Y9JsonUtil.readValue(tables, Y9Table.class);
        List<Map<String, Object>> listMap = Y9JsonUtil.readListOfMap(fields, String.class, Object.class);
        map = y9TableService.buildTable(table, listMap);
        if ((boolean) map.get(UtilConsts.SUCCESS)) {
            return Y9Result.successMsg((String) map.get("msg"));
        }
        return Y9Result.failure((String) map.get("msg"));
    }

    /**
     * 数据库是否存在该表
     *
     * @param tableName 表名
     * @return
     */
    @RequestMapping(value = "/checkTableExist", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<String> checkTableExist(@RequestParam String tableName) {
        DbMetaDataUtil dbMetaDataUtil = new DbMetaDataUtil();
        Connection connection = null;
        try {
            connection = Objects.requireNonNull(jdbcTemplate4Tenant.getDataSource()).getConnection();
            boolean msg = dbMetaDataUtil.checkTableExist(connection, tableName);
            return Y9Result.success(msg ? "exist" : "isNotExist", "获取成功");
        } catch (Exception e) {
            LOGGER.error("获取数据库表失败", e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOGGER.error("关闭数据库连接失败", e);
            }
        }
        return Y9Result.failure("获取失败");
    }

    /**
     * 获取数据库表
     *
     * @param name 表名
     * @return
     */
    @RequestMapping(value = "/getAllTables", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<Map<String, Object>> getAllTables(@RequestParam(required = false) String name) {
        Map<String, Object> map = y9TableService.getAllTables(name);
        String tableNames = y9TableService.getAlltableName();
        map.put("tableNames", tableNames);
        return Y9Result.success(map, "获取成功");
    }

    /**
     * 获取app分类
     *
     * @return
     */
    @RequestMapping(value = "/getAppList", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<List<Map<String, Object>>> getAppList() {
        List<Map<String, Object>> list = y9TableService.getAppList();
        return Y9Result.success(list, "获取成功");
    }

    /**
     * 获取业务表列表
     *
     * @param systemName 应用名称
     * @param page       页码
     * @param rows       条数
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getTables", method = RequestMethod.GET, produces = "application/json")
    public Y9Page<Y9Table> getTables(@RequestParam(required = false) String systemName,
                                     @RequestParam int page, @RequestParam int rows) {
        Map<String, Object> map = y9TableService.getTables(systemName, page, rows);
        List<Y9Table> list = (List<Y9Table>) map.get("rows");
        return Y9Page.success(page, Integer.parseInt(map.get("totalpages").toString()),
                Integer.parseInt(map.get("total").toString()), list, "获取列表成功");
    }

    /**
     * 获取新增或修改表数据
     *
     * @param id 表id
     * @return
     */
    @RequestMapping(value = "/newOrModifyTable", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<Map<String, Object>> newOrModifyTable(@RequestParam(required = false) String id) {
        DbMetaDataUtil dbMetaDataUtil = new DbMetaDataUtil();
        Connection connection = null;
        Map<String, Object> map = new HashMap<>(16);
        try {
            connection = Objects.requireNonNull(jdbcTemplate4Tenant.getDataSource()).getConnection();
            String databaseName = dbMetaDataUtil.getDatabaseDialectName(connection);
            map.put("databaseName", databaseName);
            if (StringUtils.isNotBlank(id) && !UtilConsts.NULL.equals(id)) {
                Y9Table y9Table = y9TableService.findById(id);
                map.put("y9Table", y9Table);
            }
            String tableNames = y9TableService.getAlltableName();
            map.put("tableNames", tableNames);
            return Y9Result.success(map, "获取成功");
        } catch (Exception e) {
            LOGGER.error("获取数据库表失败", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.error("关闭数据库连接失败", e);
                }
            }
        }
        return Y9Result.failure("获取失败");
    }

    /**
     * 删除业务表
     *
     * @param ids 表ids
     * @return
     */
    @RequestMapping(value = "/removeTable", method = RequestMethod.POST, produces = "application/json")
    public Y9Result<String> removeTable(@RequestParam String ids) {
        Map<String, Object> map = y9TableService.delete(ids);
        if ((boolean) map.get(UtilConsts.SUCCESS)) {
            return Y9Result.successMsg((String) map.get("msg"));
        }
        return Y9Result.failure((String) map.get("msg"));
    }

    /**
     * 保存业务表
     *
     * @param tables 表信息
     * @param fields 字段信息
     * @return
     */
    @RequestMapping(value = "/saveTable", method = RequestMethod.POST, produces = "application/json")
    public Y9Result<String> saveTable(@RequestParam(required = false) String tables,
                                      @RequestParam(required = false) String fields) {
        List<Map<String, Object>> listMap = Y9JsonUtil.readListOfMap(fields, String.class, Object.class);
        Y9Table table = Y9JsonUtil.readValue(tables, Y9Table.class);
        Map<String, Object> map = y9TableService.updateTable(table, listMap, "save");
        if ((boolean) map.get(UtilConsts.SUCCESS)) {
            return Y9Result.successMsg((String) map.get("msg"));
        }
        return Y9Result.failure((String) map.get("msg"));
    }

    /**
     * 修改表结构，增加字段
     *
     * @param tables 表信息
     * @param fields 字段信息
     * @return Y9Result<String>
     */
    @RequestMapping(value = "/updateTable", method = RequestMethod.POST, produces = "application/json")
    public Y9Result<String> updateTable(@RequestParam String tables,
                                        @RequestParam String fields) {
        Y9Table table = Y9JsonUtil.readValue(tables, Y9Table.class);
        List<Map<String, Object>> listMap = Y9JsonUtil.readListOfMap(fields, String.class, Object.class);
        Map<String, Object> map = y9TableService.updateTable(table, listMap, "");
        if ((boolean) map.get(UtilConsts.SUCCESS)) {
            return Y9Result.successMsg((String) map.get("msg"));
        }
        return Y9Result.failure((String) map.get("msg"));
    }

}
