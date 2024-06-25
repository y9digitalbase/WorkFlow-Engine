package net.risesoft.service;

import net.risesoft.entity.DraftEntity;
import org.springframework.data.domain.Page;

import java.util.Map;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
public interface DraftEntityService {

    /**
     * 根据流程实例删除草稿
     *
     * @param processSerialNumber
     */
    void deleteByProcessSerialNumber(String processSerialNumber);

    /**
     * 彻底删除草稿
     *
     * @param ids
     * @return
     */
    Map<String, Object> deleteDraft(String ids);

    /**
     * 获取草稿列表
     *
     * @param userid
     * @param page
     * @param rows
     * @param title
     * @param itemId
     * @param delFlag
     * @return
     */
    Page<DraftEntity> getDraftList(String itemId, String userid, int page, int rows, String title, boolean delFlag);

    /**
     * 
     * Description: 获取草稿列表
     * 
     * @param systemName
     * @param userId
     * @param page
     * @param rows
     * @param title
     * @param delFlag
     * @return
     */
    Page<DraftEntity> getDraftListBySystemName(String systemName, String userId, int page, int rows, String title,
        boolean delFlag);

    /**
     * 
     * Description: 打开草稿
     * 
     * @param processSerialNumber
     * @param itemId
     * @param mobile
     * @return
     */
    Map<String, Object> openDraft(String processSerialNumber, String itemId, boolean mobile);

    /**
     * 还原草稿
     *
     * @param ids
     * @return
     */
    Map<String, Object> reduction(String ids);

    /**
     * 删除草稿
     *
     * @param ids
     * @return
     */
    Map<String, Object> removeDraft(String ids);

    /**
     * 
     * Description: 保存草稿
     * 
     * @param itemId
     * @param processSerialNumber
     * @param processDefinitionKey
     * @param number
     * @param level
     * @param title
     * @param type
     * @return
     */
    Map<String, Object> saveDraft(String itemId, String processSerialNumber, String processDefinitionKey, String number,
        String level, String title, String type);

    /**
     * 
     * Description: 保存草稿
     * 
     * @param itemId
     * @param processSerialNumber
     * @param processDefinitionKey
     * @param number
     * @param level
     * @param jijian
     * @param title
     * @param type
     * @return
     */
    Map<String, Object> saveDraft(String itemId, String processSerialNumber, String processDefinitionKey, String number,
        String level, String jijian, String title, String type);
}
