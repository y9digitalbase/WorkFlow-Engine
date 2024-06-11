package net.risesoft.controller.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.risesoft.api.itemadmin.ProcessParamApi;
import net.risesoft.api.itemadmin.TransactionWordApi;
import net.risesoft.api.itemadmin.position.Attachment4PositionApi;
import net.risesoft.api.itemadmin.position.Draft4PositionApi;
import net.risesoft.api.platform.org.OrgUnitApi;
import net.risesoft.api.platform.org.PersonApi;
import net.risesoft.api.platform.org.PositionApi;
import net.risesoft.model.itemadmin.AttachmentModel;
import net.risesoft.model.itemadmin.ProcessParamModel;
import net.risesoft.model.platform.OrgUnit;
import net.risesoft.model.platform.Person;
import net.risesoft.model.platform.Position;
import net.risesoft.pojo.Y9Result;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.configuration.Y9Properties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


import java.util.HashMap;
import java.util.Map;

@Validated
@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping(value = "/services/vueNtko")
public class VueNTKOController {

    private final ProcessParamApi processParamApi;

    private final Draft4PositionApi draft4PositionApi;

    private final TransactionWordApi transactionWordApi;

    private final PersonApi personApi;

    private final PositionApi positionApi;

    private final Attachment4PositionApi attachment4PositionApi;

    private final Y9Properties y9Config;

    private final OrgUnitApi orgUnitApi;

    /**
     * 获取附件信息
     *
     * @param processSerialNumber 流程编号
     * @param itembox             状态
     * @param taskId              任务id
     * @param browser             浏览器类型
     * @param fileId              文件id
     * @param tenantId            租户id
     * @param userId              人员id
     * @param positionId          岗位id
     * @return Y9Result<Map < String, Object>>
     */
    @RequestMapping("/showFile")
    @ResponseBody
    public Y9Result<Map<String, Object>> showFile(@RequestParam String processSerialNumber, @RequestParam String itembox, @RequestParam String taskId, @RequestParam String browser, @RequestParam String fileId, @RequestParam String tenantId, @RequestParam String userId,
                                                  @RequestParam String positionId) {
        try {
            Map<String, Object> map = new HashMap<>();
            Person person = personApi.get(tenantId, userId).getData();
            Y9LoginUserHolder.setPerson(person);
            AttachmentModel file = attachment4PositionApi.getFile(tenantId, fileId);
            String downloadUrl = y9Config.getCommon().getItemAdminBaseUrl() + "/s/" + file.getFileStoreId() + "." + file.getFileType();
            map.put("fileName", file.getName());
            map.put("browser", browser);
            map.put("fileUrl", downloadUrl);
            map.put("tenantId", tenantId);
            map.put("userId", userId);
            map.put("taskId", taskId);
            map.put("positionId", positionId);
            map.put("itembox", itembox);
            map.put("fileId", fileId);
            map.put("userName", person.getName());
            map.put("processSerialNumber", processSerialNumber);
            return Y9Result.success(map, "获取信息成功");
        } catch (Exception e) {
            LOGGER.error("获取信息失败", e);
        }
        return Y9Result.failure("获取信息失败");
    }

    /**
     * 获取正文
     *
     * @param processSerialNumber 流程编号
     * @param processInstanceId   流程实例id
     * @param itemId              事项id
     * @param itembox             状态
     * @param taskId              任务id
     * @param browser             浏览器类型
     * @param positionId          岗位id
     * @param tenantId            租户id
     * @param userId              人员id
     * @return Y9Result<Map < String, Object>>
     */
    @RequestMapping("/showWord")
    @ResponseBody
    public Y9Result<Map<String, Object>> showWord(@RequestParam String processSerialNumber, @RequestParam String processInstanceId, @RequestParam String itemId, @RequestParam String itembox, @RequestParam String taskId, @RequestParam String browser, @RequestParam String positionId,
                                                  @RequestParam String tenantId, @RequestParam String userId, Model model) {
        try {
            Map<String, Object> map = transactionWordApi.showWord(tenantId, userId, processSerialNumber, itemId, itembox, taskId);
            Object documentTitle;
            if (StringUtils.isBlank(processInstanceId)) {
                Map<String, Object> retMap = draft4PositionApi.getDraftByProcessSerialNumber(tenantId, processSerialNumber);
                documentTitle = retMap.get("title");
            } else {
                String[] pInstanceId = processInstanceId.split(",");
                ProcessParamModel processModel = processParamApi.findByProcessInstanceId(tenantId, pInstanceId[0]);
                documentTitle = processModel.getTitle();
                processInstanceId = pInstanceId[0];
            }
            map.put("documentTitle", documentTitle != null ? documentTitle : "正文");
            map.put("browser", browser);
            map.put("processInstanceId", processInstanceId);
            map.put("tenantId", tenantId);
            map.put("userId", userId);
            map.put("positionId", positionId);
            Position position = positionApi.get(tenantId, positionId).getData();
            OrgUnit currentBureau = orgUnitApi.getBureau(tenantId, position.getParentId()).getData();
            model.addAttribute("currentBureauGuid", currentBureau != null ? currentBureau.getId() : "");
            return Y9Result.success(map, "获取信息成功");
        } catch (Exception e) {
            LOGGER.error("获取信息失败", e);
        }
        return Y9Result.failure("获取信息失败");
    }

}
