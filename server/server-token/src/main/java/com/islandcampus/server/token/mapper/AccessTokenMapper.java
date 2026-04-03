package com.islandcampus.server.token.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.islandcampus.server.token.mapper.entity.AccessToken;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccessTokenMapper extends BaseMapper<AccessToken> {
}
