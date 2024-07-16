package net.risesoft.service.form;

import java.util.List;

import org.springframework.data.domain.Page;

import net.risesoft.entity.form.Y9FormField;
import net.risesoft.pojo.Y9Result;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
public interface Y9FormFieldService {

    /**
     * 删除表单绑定字段
     *
     * @param id
     * @return
     */
    Y9Result<String> deleteFormFieldBind(String id);

    /**
     * Description:
     *
     * @param id
     * @return
     */
    Y9FormField findById(String id);

    /**
     * 根据表单id获取表单元素绑定信息
     *
     * @param formId
     * @return
     */
    List<Y9FormField> listByFormId(String formId);

    /**
     * 根据tableName查找绑定信息
     *
     * @param tableName
     * @return
     */
    List<Y9FormField> listByTableName(String tableName);

    /**
     * 根据表名和表单id获取绑定字段
     *
     * @param tableName
     * @param formId
     * @return
     */
    List<Y9FormField> listByTableNameAndFormId(String tableName, String formId);

    /**
     * 获取表单绑定的业务表字段
     *
     * @param formId
     * @param page
     * @param rows
     * @return
     */
    Page<Y9FormField> pageByFormId(String formId, Integer page, Integer rows);

    /**
     * Description:
     *
     * @param formField
     * @return
     */
    Y9Result<Y9FormField> saveOrUpdate(Y9FormField formField);

}
