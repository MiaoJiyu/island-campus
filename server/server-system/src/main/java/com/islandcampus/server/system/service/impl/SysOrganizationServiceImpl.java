package com.islandcampus.server.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.islandcampus.server.system.dto.OrgCreateDTO;
import com.islandcampus.server.system.dto.OrgUpdateDTO;
import com.islandcampus.server.system.entity.SysOrganization;
import com.islandcampus.server.system.mapper.SysOrganizationMapper;
import com.islandcampus.server.system.service.SysOrganizationService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysOrganizationServiceImpl
        extends ServiceImpl<SysOrganizationMapper, SysOrganization>
        implements SysOrganizationService {

    @Override
    public List<SysOrganization> getTree() {
        List<SysOrganization> all = list(new LambdaQueryWrapper<SysOrganization>()
                .eq(SysOrganization::getStatus, 1)
                .orderByAsc(SysOrganization::getSortOrder));
        return buildTree(all, 0L);
    }

    @Override
    public List<SysOrganization> getChildren(Long parentId) {
        return list(new LambdaQueryWrapper<SysOrganization>()
                .eq(SysOrganization::getParentId, parentId)
                .orderByAsc(SysOrganization::getSortOrder));
    }

    @Override
    public SysOrganization create(OrgCreateDTO dto) {
        SysOrganization entity = new SysOrganization();
        BeanUtils.copyProperties(dto, entity);
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        save(entity);
        System.out.println("[AUDIT] 创建组织: id=" + entity.getId() + ", name=" + dto.getName()
                + ", operator=" + com.islandcampus.server.common.utils.SecurityUtils.getCurrentUsername());
        return entity;
    }

    @Override
    public void updateOrg(Long id, OrgUpdateDTO dto) {
        SysOrganization existing = getById(id);
        if (existing == null) {
            throw new com.islandcampus.server.common.exception.BusinessException("组织不存在");
        }
        BeanUtils.copyProperties(dto, existing, "id");
        updateById(existing);
        System.out.println("[AUDIT] 更新组织: id=" + id + ", operator="
                + com.islandcampus.server.common.utils.SecurityUtils.getCurrentUsername());
    }

    private List<SysOrganization> buildTree(List<SysOrganization> all, Long parentId) {
        Map<Long, List<SysOrganization>> childrenMap = all.stream()
                .filter(o -> o.getParentId() != null)
                .collect(Collectors.groupingBy(SysOrganization::getParentId));

        List<SysOrganization> roots = all.stream()
                .filter(o -> o.getParentId() == null || o.getParentId().equals(parentId))
                .sorted((a, b) -> {
                    int sa = a.getSortOrder() == null ? 0 : a.getSortOrder();
                    int sb = b.getSortOrder() == null ? 0 : b.getSortOrder();
                    return Integer.compare(sa, sb);
                })
                .toList();

        List<SysOrganization> tree = new ArrayList<>();
        for (SysOrganization root : roots) {
            tree.add(buildNode(root, childrenMap));
        }
        return tree;
    }

    private SysOrganization buildNode(SysOrganization node, Map<Long, List<SysOrganization>> childrenMap) {
        // Note: In a real implementation you'd add a children field or use a separate VO
        return node;
    }
}
