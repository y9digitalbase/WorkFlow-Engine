package net.risesoft.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.risesoft.api.itemadmin.position.Attachment4PositionApi;
import net.risesoft.api.platform.org.PersonApi;
import net.risesoft.api.platform.org.PositionApi;
import net.risesoft.entity.TransactionFile;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.itemadmin.AttachmentModel;
import net.risesoft.model.platform.Person;
import net.risesoft.model.platform.Position;
import net.risesoft.repository.jpa.TransactionFileRepository;
import net.risesoft.service.TransactionFileService;
import net.risesoft.util.ItemAdminModelConvertUtil;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.json.Y9JsonUtil;
import net.risesoft.y9.util.Y9BeanUtil;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 附件接口
 *
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/services/rest/attachment4Position")
public class AttachmentApiImpl implements Attachment4PositionApi {

    private final TransactionFileService transactionFileService;

    private final TransactionFileRepository transactionFileRepository;

    private final PositionApi positionManager;

    private final PersonApi personManager;

    /**
     * 附件下载
     *
     * @param tenantId 租户id
     * @param id       附件id
     * @return Map&lt;String, Object&gt;
     */
    @Override
    @GetMapping(value = "/download", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> attachmentDownload(String tenantId, String id) {
        Y9LoginUserHolder.setTenantId(tenantId);
        return transactionFileService.download(id);
    }

    /**
     * 根据流程编号删除附件
     *
     * @param tenantId             租户id
     * @param processSerialNumbers 流程编号
     */
    @Override
    @PostMapping(value = "/delByProcessSerialNumbers", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public void delBatchByProcessSerialNumbers(String tenantId, @RequestBody List<String> processSerialNumbers) {
        Y9LoginUserHolder.setTenantId(tenantId);
        transactionFileService.delBatchByProcessSerialNumbers(processSerialNumbers);
    }

    /**
     * 删除附件
     *
     * @param tenantId 租户id
     * @param ids      附件ids
     * @return Map&lt;String, Object&gt;
     */
    @Override
    @PostMapping(value = "/delFile", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> delFile(String tenantId, String ids) {
        Y9LoginUserHolder.setTenantId(tenantId);
        return transactionFileService.delFile(ids);
    }

    /**
     * 获取附件数
     *
     * @param tenantId            租户id
     * @param processSerialNumber 流程编号
     * @return Integer Integer
     */
    @Override
    @GetMapping(value = "/fileCounts", produces = MediaType.APPLICATION_JSON_VALUE)
    public Integer fileCounts(String tenantId, String processSerialNumber) {
        Y9LoginUserHolder.setTenantId(tenantId);
        return transactionFileService.fileCounts(processSerialNumber);
    }

    /**
     * 获取附件数
     *
     * @param tenantId            租户id
     * @param processSerialNumber 流程编号
     * @param fileSource          附件来源
     * @param fileType            文件类型
     * @return int
     */
    @Override
    @GetMapping(value = "/getAttachmentCount", produces = MediaType.APPLICATION_JSON_VALUE)
    public int getAttachmentCount(String tenantId, String processSerialNumber, String fileSource, String fileType) {
        Y9LoginUserHolder.setTenantId(tenantId);
        fileType = fileType.toLowerCase();
        return transactionFileService.getTransactionFileCount(processSerialNumber, fileSource, fileType);
    }

    /**
     * 获取附件列表
     *
     * @param tenantId            租户id
     * @param processSerialNumber 流程编号
     * @param fileSource          附件来源
     * @param page                页码
     * @param rows                行数
     * @return Map&lt;String, Object&gt;
     */
    @Override
    @GetMapping(value = "/getAttachmentList", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getAttachmentList(String tenantId, String processSerialNumber, String fileSource,
                                                 int page, int rows) {
        Y9LoginUserHolder.setTenantId(tenantId);
        return transactionFileService.getAttachmentList(processSerialNumber, fileSource, page, rows);
    }

    /**
     * 获取附件列表(model)
     *
     * @param tenantId            租户id
     * @param processSerialNumber 流程编号
     * @param fileSource          附件来源
     * @return List<AttachmentModel>
     */
    @Override
    @GetMapping(value = "/getAttachmentModelList", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AttachmentModel> getAttachmentModelList(String tenantId, String processSerialNumber,
                                                        String fileSource) {
        Y9LoginUserHolder.setTenantId(tenantId);
        List<TransactionFile> transactionFileList =
                transactionFileService.getAttachmentModelList(processSerialNumber, fileSource);
        return ItemAdminModelConvertUtil.attachmentList2ModelList(transactionFileList);
    }

    /**
     * 获取附件信息
     *
     * @param tenantId 租户id
     * @param fileId   附件id
     * @return AttachmentModel
     */
    @Override
    @GetMapping(value = "/getFile", produces = MediaType.APPLICATION_JSON_VALUE)
    public AttachmentModel getFile(String tenantId, String fileId) {
        Y9LoginUserHolder.setTenantId(tenantId);
        TransactionFile file = transactionFileRepository.findById(fileId).orElse(null);
        AttachmentModel model = null;
        if (file != null) {
            model = new AttachmentModel();
            Y9BeanUtil.copyProperties(file, model);
        }
        return model;
    }

    /**
     * 保存附件信息
     *
     * @param tenantId            租户id
     * @param positionId          岗位id
     * @param attachjson          附件信息
     * @param processSerialNumber 流程编号
     * @return Boolean 是否保存成功
     */
    @SuppressWarnings("unchecked")
    @Override
    @PostMapping(value = "/saveAttachment", produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean saveAttachment(String tenantId, String positionId, String attachjson, String processSerialNumber) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Position position = positionManager.get(tenantId, positionId).getData();
        Y9LoginUserHolder.setPosition(position);
        boolean checkSave = false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Map<String, Object> attachmentJson = Y9JsonUtil.readValue(attachjson, Map.class);
            assert attachmentJson != null;
            List<Map<String, Object>> attachmentList = (List<Map<String, Object>>) attachmentJson.get("attachment");
            for (Map<String, Object> map : attachmentList) {
                TransactionFile file = new TransactionFile();
                file.setDescribes(map.get("describes") == null ? "" : map.get("describes").toString());
                file.setFileStoreId(map.get("filePath").toString());
                file.setFileSize(map.get("fileSize") == null ? "" : map.get("fileSize").toString());
                file.setFileSource(map.get("fileSource") == null ? "" : map.get("fileSource").toString());
                file.setFileType(map.get("fileType") == null ? "" : map.get("fileType").toString());
                file.setId(map.get("id").toString());
                file.setName(map.get("fileName").toString());
                file.setPersonId(map.get("personId") == null ? "" : map.get("personId").toString());
                file.setPersonName(map.get("personName") == null ? "" : map.get("personName").toString());
                file.setProcessSerialNumber(processSerialNumber);
                file.setUploadTime(sdf.format(new Date()));
                transactionFileService.save(file);
                checkSave = true;
            }
        } catch (Exception e) {
            LOGGER.error("saveAttachment error", e);
            checkSave = false;
        }
        return checkSave;
    }

    /**
     * 保存附件信息
     *
     * @param tenantId            租户id
     * @param positionId          岗位id
     * @param fileName            文件名称
     * @param fileType            文件类型
     * @param fileSizeString      文件大小
     * @param fileSource          附件来源
     * @param processInstanceId   流程实例id
     * @param processSerialNumber 流程编号
     * @param taskId              任务id
     * @param y9FileStoreId       附件上传id
     * @return String String
     */
    @Override
    @PostMapping(value = "/saveOrUpdateUploadInfo", produces = MediaType.APPLICATION_JSON_VALUE)
    public String saveOrUpdateUploadInfo(String tenantId, String positionId, String fileName, String fileType,
                                         String fileSizeString, String fileSource, String processInstanceId, String processSerialNumber, String taskId,
                                         String y9FileStoreId) {
        String msg;
        Y9LoginUserHolder.setTenantId(tenantId);
        Position position = positionManager.get(tenantId, positionId).getData();
        Y9LoginUserHolder.setPosition(position);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            TransactionFile attachment = transactionFileService.getFileInfoByFileName(fileName, processSerialNumber);
            if (null != attachment) {
                attachment.setFileStoreId(y9FileStoreId);
                attachment.setName(fileName);
                attachment.setFileSize(fileSizeString);
                attachment.setTaskId(taskId);
                attachment.setPersonId(positionId);
                attachment.setPersonName(position.getName());
                attachment.setUploadTime(sdf.format(new Date()));
                transactionFileRepository.save(attachment);
            } else {
                TransactionFile fileAttachment = new TransactionFile();
                fileAttachment.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
                fileAttachment.setFileStoreId(y9FileStoreId);
                fileAttachment.setName(fileName);
                fileAttachment.setFileSize(fileSizeString);
                fileAttachment.setFileType(fileType);
                fileAttachment.setUploadTime(sdf.format(new Date()));
                fileAttachment.setPersonId(positionId);
                fileAttachment.setPersonName(position.getName());
                fileAttachment.setProcessInstanceId(processInstanceId);
                fileAttachment.setProcessSerialNumber(processSerialNumber);
                fileAttachment.setTaskId(taskId);
                fileAttachment.setFileSource(fileSource);
                fileAttachment.setTabIndex(transactionFileService.fileCounts(processSerialNumber) + 1);
                transactionFileRepository.save(fileAttachment);
            }
            msg = "success:true";
        } catch (Exception e) {
            LOGGER.error("saveOrUpdateUploadInfo error", e);
            msg = "success:false";
        }
        return msg;
    }

    /**
     * 更新附件信息
     *
     * @param tenantId       租户id
     * @param userId         人员id
     * @param positionId     岗位id
     * @param fileId         文件id
     * @param fileSizeString 文件大小
     * @param taskId         任务id
     * @param y9FileStoreId  附件上传id
     * @return String
     */
    @Override
    @PostMapping(value = "/updateFile", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateFile(String tenantId, String userId, String positionId, String fileId, String fileSizeString,
                             String taskId, String y9FileStoreId) {
        String msg;
        Y9LoginUserHolder.setTenantId(tenantId);
        Position position = positionManager.get(tenantId, positionId).getData();
        Y9LoginUserHolder.setPosition(position);
        Person person = personManager.get(tenantId, userId).getData();
        Y9LoginUserHolder.setPerson(person);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            TransactionFile attachment = transactionFileRepository.findById(fileId).orElse(null);
            if (null != attachment) {
                attachment.setFileStoreId(y9FileStoreId);
                attachment.setFileSize(fileSizeString);
                attachment.setUploadTime(sdf.format(new Date()));
                transactionFileRepository.save(attachment);
            }
            msg = "success:true";
        } catch (Exception e) {
            LOGGER.error("updateFile error", e);
            msg = "success:false";
        }
        return msg;
    }

    /**
     * 上传附件
     *
     * @param tenantId            租户id
     * @param userId              人员id
     * @param positionId          岗位id
     * @param fileName            文件名
     * @param fileSize            文件大小
     * @param processInstanceId   流程实例id
     * @param taskId              任务id
     * @param describes           描述
     * @param processSerialNumber 流程编号
     * @param fileSource          附件来源
     * @param y9FileStoreId       附件上传id
     * @return Map&lt;String, Object&gt;
     */
    @Override
    @PostMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> upload(String tenantId, String userId, String positionId, String fileName,
                                      String fileSize, String processInstanceId, String taskId, String describes, String processSerialNumber,
                                      String fileSource, String y9FileStoreId) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Position position = positionManager.get(tenantId, positionId).getData();
        Y9LoginUserHolder.setPosition(position);
        Person person = personManager.get(tenantId, userId).getData();
        Y9LoginUserHolder.setPerson(person);
        return transactionFileService.uploadRest(fileName, fileSize, processInstanceId, taskId, processSerialNumber,
                describes, fileSource, y9FileStoreId);
    }

    /**
     * 上传附件(model)
     *
     * @param tenantId        租户id
     * @param positionId      岗位id
     * @param attachmentModel 附件实体信息
     * @return boolean
     */
    @Override
    @PostMapping(value = "/uploadModel", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public boolean uploadModel(String tenantId, String positionId, @RequestBody AttachmentModel attachmentModel) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Position position = positionManager.get(tenantId, positionId).getData();
        Y9LoginUserHolder.setPosition(position);
        boolean success = false;
        TransactionFile transactionFile = ItemAdminModelConvertUtil.attachmentModel2TransactionFile(attachmentModel);
        try {
            transactionFileService.uploadRestModel(transactionFile);
            success = true;
        } catch (ParseException e) {
            LOGGER.error("uploadModel error", e);
        }
        return success;
    }
}
