package net.risesoft.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.api.itemadmin.ItemViewConfApi;
import net.risesoft.entity.ItemViewConf;
import net.risesoft.model.itemadmin.ItemViewConfModel;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.ItemViewConfService;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.util.Y9BeanUtil;

/**
 * 列表视图接口
 *
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/services/rest/itemViewConf", produces = MediaType.APPLICATION_JSON_VALUE)
public class ItemViewConfApiImpl implements ItemViewConfApi {

    private final ItemViewConfService itemViewConfService;

    /**
     * 获取事项视图配置列表
     *
     * @param tenantId 租户id
     * @param itemId 事项id
     * @param viewType 视图类型
     * @return List<ItemViewConfModel>
     */
    @Override
    public Y9Result<List<ItemViewConfModel>> findByItemIdAndViewType(String tenantId, String itemId, String viewType) {
        Y9LoginUserHolder.setTenantId(tenantId);
        List<ItemViewConfModel> modelList = new ArrayList<>();
        List<ItemViewConf> list = itemViewConfService.findByItemIdAndViewType(itemId, viewType);
        ItemViewConfModel model;
        for (ItemViewConf itemViewConf : list) {
            model = new ItemViewConfModel();
            Y9BeanUtil.copyProperties(itemViewConf, model);
            modelList.add(model);
        }
        return Y9Result.success(modelList);
    }
}
