package net.risesoft.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.DynamicRole;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.repository.jpa.DynamicRoleRepository;
import net.risesoft.service.DynamicRoleService;
import net.risesoft.y9.Y9LoginUserHolder;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/22
 */
@Service(value = "dynamicRoleService")
@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public class DynamicRoleServiceImpl implements DynamicRoleService {

    @Autowired
    private DynamicRoleRepository dynamicRoleRepository;

    @Override
    public List<DynamicRole> findAll() {
        return dynamicRoleRepository.findAllByOrderByTabIndexAsc();
    }

    @Override
    public DynamicRole findOne(String id) {
        return dynamicRoleRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = false)
    public void removeDynamicRoles(String[] dynamicRoleIds) {
        for (String id : dynamicRoleIds) {
            dynamicRoleRepository.deleteById(id);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public DynamicRole saveOrUpdate(DynamicRole dynamicRole) {
        String id = dynamicRole.getId();
        if (StringUtils.isNotEmpty(id)) {
            DynamicRole olddr = dynamicRoleRepository.findById(id).orElse(null);
            if (null != olddr) {
                olddr.setName(dynamicRole.getName());
                olddr.setDescription(dynamicRole.getDescription());
                olddr.setClassPath(dynamicRole.getClassPath());
                olddr.setUseProcessInstanceId(dynamicRole.isUseProcessInstanceId());
                dynamicRoleRepository.save(olddr);
                return olddr;
            } else {
                return dynamicRoleRepository.save(dynamicRole);
            }
        }

        DynamicRole d = new DynamicRole();
        d.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
        d.setName(dynamicRole.getName());
        d.setTenantId(Y9LoginUserHolder.getTenantId());
        d.setDescription(dynamicRole.getDescription());
        d.setClassPath(dynamicRole.getClassPath());
        d.setUseProcessInstanceId(dynamicRole.isUseProcessInstanceId());

        Integer tabIndex = dynamicRoleRepository.getMaxTabIndex();
        if (tabIndex == null) {
            tabIndex = 0;
        } else {
            tabIndex += 1;
        }
        d.setTabIndex(tabIndex);
        dynamicRoleRepository.save(d);
        return d;
    }
}
