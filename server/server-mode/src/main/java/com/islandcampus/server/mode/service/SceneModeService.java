package com.islandcampus.server.mode.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.islandcampus.server.mode.entity.SceneMode;

public interface SceneModeService extends IService<SceneMode> {

    void deleteMode(Long id);

    SceneMode getByCode(String code);
}
