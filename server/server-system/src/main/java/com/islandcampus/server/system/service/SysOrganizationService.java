package com.islandcampus.server.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.islandcampus.server.system.dto.OrgCreateDTO;
import com.islandcampus.server.system.dto.OrgUpdateDTO;
import com.islandcampus.server.system.entity.SysOrganization;

import java.util.List;

public interface SysOrganizationService extends IService<SysOrganization> {

    /**
     * 获取组织树
     */
    List<SysOrganization> getTree();

    /**
     * 获取子节点列表
     */
    List<SysOrganization> getChildren(Long parentId);

    /**
     * 创建组织
     */
    SysOrganization create(OrgCreateDTO dto);

    /**
     * 更新组织
     */
    void updateOrg(Long id, OrgUpdateDTO dto);
}
