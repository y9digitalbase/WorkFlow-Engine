package net.risesoft.api;

import lombok.RequiredArgsConstructor;
import net.risesoft.api.itemadmin.PrintApi;
import net.risesoft.api.platform.org.PersonApi;
import net.risesoft.entity.ItemPrintTemplateBind;
import net.risesoft.model.platform.Person;
import net.risesoft.repository.jpa.PrintTemplateItemBindRepository;
import net.risesoft.y9.Y9LoginUserHolder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 打印模板接口
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/services/rest/print")
public class PrintApiImpl implements PrintApi {

    private final PrintTemplateItemBindRepository printTemplateItemBindRepository;

    private final PersonApi personManager;

    /**
     * 打开打印模板
     * @param tenantId 租户id
     * @param userId 人员id
     * @param itemId 事项id
     * @return String
     */
    @Override
    @GetMapping(value = "/openDocument", produces = MediaType.APPLICATION_JSON_VALUE)
    public String openDocument(String tenantId, String userId, String itemId) {
        Person person = personManager.get(tenantId, userId).getData();
        Y9LoginUserHolder.setPerson(person);
        Y9LoginUserHolder.setTenantId(tenantId);
        ItemPrintTemplateBind bind = printTemplateItemBindRepository.findByItemId(itemId);
        String fileStoreId = bind.getTemplateUrl();
        return fileStoreId;
    }

}
