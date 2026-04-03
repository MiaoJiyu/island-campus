package com.islandcampus.server.mode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.islandcampus.server.common.exception.BusinessException;
import com.islandcampus.server.mode.entity.SceneMode;
import com.islandcampus.server.mode.mapper.SceneModeMapper;
import com.islandcampus.server.mode.service.SceneModeService;
import org.springframework.stereotype.Service;

@Service
public class SceneModeServiceImpl extends ServiceImpl<SceneModeMapper, SceneMode>
        implements SceneModeService {

    @Override
    public void deleteMode(Long id) {
        SceneMode mode = getById(id);
        if (mode == null) {
            throw new BusinessException("模式不存在");
        }
        if (mode.getIsSystem() != null && mode.getIsSystem() == 1) {
            throw new BusinessException("系统内置模式不可删除");
        }
        removeById(id);
    }

    @Override
    public SceneMode getByCode(String code) {
        return getOne(new LambdaQueryWrapper<SceneMode>()
                .eq(SceneMode::getCode, code)
                .last("LIMIT 1"));
    }
}
