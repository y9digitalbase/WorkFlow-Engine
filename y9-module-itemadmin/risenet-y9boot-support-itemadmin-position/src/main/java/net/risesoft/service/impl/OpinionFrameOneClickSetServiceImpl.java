package net.risesoft.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.entity.OpinionFrameOneClickSet;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.itemadmin.OpinionFrameOneClickSetModel;
import net.risesoft.repository.jpa.OpinionFrameOneClickSetRepository;
import net.risesoft.service.OpinionFrameOneClickSetService;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.util.Y9BeanUtil;

@Transactional(value = "rsTenantTransactionManager", readOnly = true)
@Service(value = "opinionFrameOpinionFrameOneClickSetService")
@Slf4j
@RequiredArgsConstructor
public class OpinionFrameOneClickSetServiceImpl implements OpinionFrameOneClickSetService {

    private final OpinionFrameOneClickSetRepository opinionFrameOneClickSetRepository;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(readOnly = false)
    public Map<String, Object> save(OpinionFrameOneClickSet opinionFrameOneClickSet) {
        Map<String, Object> ret_Map = new HashMap<String, Object>();
        try {
            if (StringUtils.isBlank(opinionFrameOneClickSet.getId())) {
                opinionFrameOneClickSet.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
                opinionFrameOneClickSet.setCreateDate(sdf.format(new Date()));
                opinionFrameOneClickSet.setUserId(Y9LoginUserHolder.getPersonId());
                opinionFrameOneClickSetRepository.save(opinionFrameOneClickSet);
            } else {
                OpinionFrameOneClickSet oneClick =
                    opinionFrameOneClickSetRepository.findById(opinionFrameOneClickSet.getId()).orElse(null);
                Y9BeanUtil.copyProperties(opinionFrameOneClickSet, oneClick);
                opinionFrameOneClickSetRepository.save(oneClick);
            }
            ret_Map.put("success", true);
            ret_Map.put("msg", "一键配置成功！");
        } catch (Exception e) {
            e.printStackTrace();
            ret_Map.put("success", false);
            ret_Map.put("msg", "一键配置失败！");
        }
        return ret_Map;
    }

    @Override
    public List<OpinionFrameOneClickSet> findByBindId(String bindId) {
        return opinionFrameOneClickSetRepository.findByBindId(bindId);
    }

    @Override
    public List<OpinionFrameOneClickSetModel> findByBindIdModel(String bindId) {
        List<OpinionFrameOneClickSetModel> opinionFrameOneClickSetModels = new ArrayList<>();
        List<OpinionFrameOneClickSet> opinionFrameOneClickSets = opinionFrameOneClickSetRepository.findByBindId(bindId);
        if (null != opinionFrameOneClickSets && opinionFrameOneClickSets.size() > 0) {
            for (OpinionFrameOneClickSet opinionFrameOneClickSet : opinionFrameOneClickSets) {
                OpinionFrameOneClickSetModel opinionFrameOneClickSetModel = new OpinionFrameOneClickSetModel();
                Y9BeanUtil.copyProperties(opinionFrameOneClickSet, opinionFrameOneClickSetModel);
                opinionFrameOneClickSetModels.add(opinionFrameOneClickSetModel);
            }
            return opinionFrameOneClickSetModels;
        }
        return null;
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(String id) {
        opinionFrameOneClickSetRepository.deleteById(id);
    }
}
