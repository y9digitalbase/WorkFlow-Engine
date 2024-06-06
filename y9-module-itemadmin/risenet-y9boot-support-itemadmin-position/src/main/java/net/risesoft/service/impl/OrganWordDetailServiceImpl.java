package net.risesoft.service.impl;

import lombok.RequiredArgsConstructor;
import net.risesoft.entity.OrganWordDetail;
import net.risesoft.repository.jpa.OrganWordDetailRepository;
import net.risesoft.service.OrganWordDetailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
@Service
@RequiredArgsConstructor
@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public class OrganWordDetailServiceImpl implements OrganWordDetailService {

    private final OrganWordDetailRepository organWordDetailRepository;

    @Override
    public OrganWordDetail findByCustomAndCharacterValueAndYearAndItemId(String custom, String characterValue,
        Integer year, String itemId) {
        return organWordDetailRepository.findByCustomAndCharacterValueAndYearAndItemId(custom, characterValue, year,
            itemId);
    }

    @Override
    @Transactional
    public OrganWordDetail save(OrganWordDetail organWordDetail) {
        return organWordDetailRepository.save(organWordDetail);
    }

    @Override
    @Transactional
    public OrganWordDetail saveOrUpdate(OrganWordDetail organWordDetail) {
        // TODO Auto-generated method stub
        return null;
    }

}
