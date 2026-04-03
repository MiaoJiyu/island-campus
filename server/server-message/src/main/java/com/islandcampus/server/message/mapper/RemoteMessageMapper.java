package com.islandcampus.server.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.islandcampus.server.message.entity.RemoteMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RemoteMessageMapper extends BaseMapper<RemoteMessage> {
}
