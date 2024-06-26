package net.risesoft.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.api.itemadmin.ReceiveDeptAndPersonApi;
import net.risesoft.api.platform.org.DepartmentApi;
import net.risesoft.api.platform.org.OrgUnitApi;
import net.risesoft.api.platform.org.PersonApi;
import net.risesoft.entity.ReceiveDepartment;
import net.risesoft.entity.ReceivePerson;
import net.risesoft.model.itemadmin.ReceiveOrgUnit;
import net.risesoft.model.platform.Department;
import net.risesoft.model.platform.OrgUnit;
import net.risesoft.model.platform.Person;
import net.risesoft.pojo.Y9Result;
import net.risesoft.repository.jpa.ReceiveDepartmentRepository;
import net.risesoft.repository.jpa.ReceivePersonRepository;
import net.risesoft.y9.Y9LoginUserHolder;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/22
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/services/rest/receiveDeptAndPerson", produces = MediaType.APPLICATION_JSON_VALUE)
public class ReceiveDeptAndPersonApiImpl implements ReceiveDeptAndPersonApi {

    private final PersonApi personManager;

    private final DepartmentApi departmentManager;

    private final OrgUnitApi orgUnitApi;

    private final ReceivePersonRepository receivePersonRepository;

    private final ReceiveDepartmentRepository receiveDepartmentRepository;

    @Override
    public Y9Result<List<ReceiveOrgUnit>> findByDeptNameLike(String tenantId, String name) {
        Y9LoginUserHolder.setTenantId(tenantId);
        List<ReceiveOrgUnit> listMap = new ArrayList<>();
        if (StringUtils.isBlank(name)) {
            name = "";
        }
        name = "%" + name + "%";
        List<ReceiveDepartment> list = receiveDepartmentRepository.findByDeptNameLikeOrderByTabIndex(name);
        for (ReceiveDepartment receiveDepartment : list) {
            Department department = departmentManager.get(tenantId, receiveDepartment.getDeptId()).getData();
            if (department == null || department.getId() == null) {
                continue;
            }
            ReceiveOrgUnit orgUnit = new ReceiveOrgUnit();
            orgUnit.setId(receiveDepartment.getDeptId());
            orgUnit.setDisabled(department.getDisabled());
            orgUnit.setParentId(receiveDepartment.getParentId());
            orgUnit.setName(department.getName());
            OrgUnit bureau = orgUnitApi.getBureau(tenantId, department.getId()).getData();
            if (bureau != null && bureau.getId() != null && !bureau.getId().equals(department.getId())) {
                orgUnit.setNameWithBureau(department.getName() + "(" + bureau.getName() + ")");
            }
            orgUnit.setOrgType("Department");
            listMap.add(orgUnit);
        }
        return Y9Result.success(listMap);
    }

    @Override
    public Y9Result<List<ReceiveOrgUnit>> getReceiveDeptTree(String tenantId) {
        Y9LoginUserHolder.setTenantId(tenantId);
        List<ReceiveOrgUnit> listMap = new ArrayList<>();
        List<ReceiveDepartment> list = receiveDepartmentRepository.findAll();
        for (ReceiveDepartment receiveDepartment : list) {
            Department department = departmentManager.get(tenantId, receiveDepartment.getDeptId()).getData();
            if (department == null || department.getId() == null) {
                continue;
            }

            ReceiveOrgUnit orgUnit = new ReceiveOrgUnit();
            orgUnit.setId(receiveDepartment.getDeptId());
            orgUnit.setDisabled(department.getDisabled());
            orgUnit.setParentId(receiveDepartment.getParentId());
            orgUnit.setName(department.getName());
            OrgUnit bureau = orgUnitApi.getBureau(tenantId, department.getId()).getData();
            if (bureau != null && bureau.getId() != null && !bureau.getId().equals(department.getId())) {
                orgUnit.setNameWithBureau(department.getName() + "(" + bureau.getName() + ")");
            }
            orgUnit.setOrgType("Department");
            listMap.add(orgUnit);
        }
        return Y9Result.success(listMap);
    }

    @Override
    public Y9Result<List<ReceiveOrgUnit>> getReceiveDeptTreeById(String tenantId, String orgUnitId, String name) {
        Y9LoginUserHolder.setTenantId(tenantId);
        List<ReceiveOrgUnit> listMap = new ArrayList<>();
        List<ReceiveDepartment> list = null;
        if (StringUtils.isNotBlank(name)) {
            list = receiveDepartmentRepository.findByDeptNameContainingOrderByTabIndex(name);
            for (ReceiveDepartment receiveDepartment : list) {
                Department department = departmentManager.get(tenantId, receiveDepartment.getDeptId()).getData();
                if (department == null || department.getId() == null) {
                    continue;
                }

                ReceiveOrgUnit orgUnit = new ReceiveOrgUnit();
                orgUnit.setId(receiveDepartment.getDeptId());
                orgUnit.setDisabled(department.getDisabled());
                orgUnit.setParentId(receiveDepartment.getParentId());
                orgUnit.setName(department.getName());
                OrgUnit bureau = orgUnitApi.getBureau(tenantId, department.getId()).getData();
                if (bureau != null && bureau.getId() != null && !bureau.getId().equals(department.getId())) {
                    orgUnit.setNameWithBureau(department.getName() + "(" + bureau.getName() + ")");
                }
                Integer count = receiveDepartmentRepository.countByParentId(receiveDepartment.getDeptId());
                orgUnit.setIsParent(count > 0);
                orgUnit.setOrgType("Department");
                if (listMap.contains(orgUnit)) {
                    continue;// 去重
                }
                listMap.add(orgUnit);
            }
        } else {
            if (StringUtils.isBlank(orgUnitId)) {
                list = receiveDepartmentRepository.findAll();
                for (ReceiveDepartment receiveDepartment : list) {
                    Department department = departmentManager.get(tenantId, receiveDepartment.getDeptId()).getData();
                    if (department == null || department.getId() == null) {
                        continue;
                    }
                    ReceiveOrgUnit orgUnit = new ReceiveOrgUnit();
                    orgUnit.setId(receiveDepartment.getDeptId());
                    orgUnit.setDisabled(department.getDisabled());
                    orgUnit.setParentId(receiveDepartment.getParentId());
                    orgUnit.setName(department.getName());
                    Integer count = receiveDepartmentRepository.countByParentId(receiveDepartment.getDeptId());
                    orgUnit.setIsParent(count > 0);
                    orgUnit.setOrgType("Department");
                    if (listMap.contains(orgUnit)) {
                        continue;// 去重
                    }
                    listMap.add(orgUnit);
                }
            } else {
                list = receiveDepartmentRepository.findByParentIdOrderByTabIndex(orgUnitId);
                for (ReceiveDepartment receiveDepartment : list) {
                    Department department = departmentManager.get(tenantId, receiveDepartment.getDeptId()).getData();
                    if (department == null || department.getId() == null) {
                        continue;
                    }
                    Integer count = receiveDepartmentRepository.countByParentId(receiveDepartment.getDeptId());

                    ReceiveOrgUnit orgUnit = new ReceiveOrgUnit();
                    orgUnit.setId(receiveDepartment.getDeptId());
                    orgUnit.setDisabled(department.getDisabled());
                    orgUnit.setParentId(orgUnitId);
                    orgUnit.setName(department.getName());
                    orgUnit.setIsParent(count > 0);
                    orgUnit.setOrgType("Department");
                    if (listMap.contains(orgUnit)) {
                        continue;// 去重
                    }
                    listMap.add(orgUnit);
                }
            }
        }
        return Y9Result.success(listMap);
    }

    @Override
    public Y9Result<List<Person>> getSendReceiveByDeptId(String tenantId, String deptId) {
        Y9LoginUserHolder.setTenantId(tenantId);
        List<ReceivePerson> list = receivePersonRepository.findByDeptId(deptId);
        List<Person> users = new ArrayList<>();
        for (ReceivePerson receivePerson : list) {
            Person person = personManager.get(tenantId, receivePerson.getPersonId()).getData();
            if (person != null && StringUtils.isNotBlank(person.getId())
                && !Boolean.TRUE.equals(person.getDisabled())) {
                users.add(person);
            }
        }
        return Y9Result.success(users);
    }

    @Override
    public Y9Result<List<ReceiveOrgUnit>> getSendReceiveByUserId(String tenantId, String userId) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Person person = personManager.get(tenantId, userId).getData();
        List<ReceiveOrgUnit> listMap = new ArrayList<>();
        Y9LoginUserHolder.setPerson(person);
        if (StringUtils.isBlank(userId)) {
            userId = "";
        }
        userId = "%" + userId + "%";
        List<ReceivePerson> list = receivePersonRepository.findByPersonId(userId);
        if (!list.isEmpty()) {
            for (ReceivePerson receivePerson : list) {
                Department department = departmentManager.get(tenantId, receivePerson.getDeptId()).getData();
                ReceiveOrgUnit orgUnit = new ReceiveOrgUnit();
                orgUnit.setId(receivePerson.getDeptId());
                orgUnit.setName(department != null ? department.getName() : receivePerson.getDeptName() + "(该部门已不存在)");
                listMap.add(orgUnit);
            }
        }
        return Y9Result.success(listMap);
    }
}
