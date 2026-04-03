package com.islandcampus.server.answer.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.islandcampus.server.common.base.PageResult;
import com.islandcampus.server.common.exception.BusinessException;
import com.islandcampus.server.common.utils.SecurityUtils;
import com.islandcampus.server.answer.dto.*;
import com.islandcampus.server.answer.entity.AnswerLog;
import com.islandcampus.server.answer.entity.AnswerSet;
import com.islandcampus.server.answer.mapper.AnswerLogMapper;
import com.islandcampus.server.answer.mapper.AnswerSetMapper;
import com.islandcampus.server.answer.service.AnswerSetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerSetServiceImpl extends ServiceImpl<AnswerSetMapper, AnswerSet>
        implements AnswerSetService {
    private final AnswerLogMapper answerLogMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnswerSet create(AnswerSetCreateDTO dto) {
        Long creatorId = SecurityUtils.getCurrentUserId();
        AnswerSet as = new AnswerSet();
        as.setTitle(dto.getTitle());
        as.setCreatorId(creatorId);
        as.setTargetClassIds(JSON.toJSONString(dto.getTargetClassIds()));
        as.setQuestionsJson(dto.getQuestionsJson());
        as.setPublishType(dto.getPublishType());
        as.setStatus(0);
        if (dto.getPublishType() == 1) {
            if (dto.getPublishTime() == null) throw new BusinessException("定时发布必须指定发布时间");
            as.setPublishTime(dto.getPublishTime());
        } else if (dto.getPublishType() == 2) {
            if (!StringUtils.hasText(dto.getPassword())) throw new BusinessException("解密发布必须设置密码");
            as.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }
        this.save(as);
        recordLog(as.getId(), "create", creatorId, null, "创建答案集: " + dto.getTitle());
        return as;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, AnswerSetCreateDTO dto) {
        AnswerSet as = this.getById(id);
        if (as == null) throw new BusinessException(404, "答案集不存在");
        if (as.getStatus() != 0) throw new BusinessException("只有草稿状态的答案集可以修改");
        as.setTitle(dto.getTitle()); as.setQuestionsJson(dto.getQuestionsJson());
        as.setPublishType(dto.getPublishType()); as.setTargetClassIds(JSON.toJSONString(dto.getTargetClassIds()));
        if (dto.getPublishType() == 1) { as.setPublishTime(dto.getPublishTime()); }
        else if (dto.getPublishType() == 2 && StringUtils.hasText(dto.getPassword())) {
            as.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }
        this.updateById(as);
        recordLog(id, "update", SecurityUtils.getCurrentUserId(), null, "修改答案集");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        AnswerSet as = this.getById(id);
        if (as == null) throw new BusinessException(404, "答案集不存在");
        if (as.getStatus() != 0) throw new BusinessException("只有草稿状态的答案集可以删除");
        this.removeById(id);
        recordLog(id, "delete", SecurityUtils.getCurrentUserId(), null, "删除答案集: " + as.getTitle());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishByTime(Long id) {
        AnswerSet as = this.getById(id);
        if (as == null) throw new BusinessException(404, "答案集不存在");
        if (as.getPublishType() != 1) throw new BusinessException("该答案集不是定时发布类型");
        if (as.getStatus() == 1) return;
        if (as.getPublishTime() != null && as.getPublishTime().isAfter(LocalDateTime.now()))
            throw new BusinessException("尚未到发布时间");
        as.setStatus(1); this.updateById(as);
        recordLog(id, "publish_auto", as.getCreatorId(), null, "定时自动公布");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishByDecrypt(Long id, AnswerPublishDecryptDTO dto) {
        AnswerSet as = this.getById(id);
        if (as == null) throw new BusinessException(404, "答案集不存在");
        if (as.getPublishType() != 2) throw new BusinessException("该答案集不是解密发布类型");
        if (as.getStatus() == 1) throw new BusinessException("该答案集已经公布");
        if (!passwordEncoder.matches(dto.getPassword(), as.getPasswordHash())) {
            recordLog(id, "publish_decrypt_failed", SecurityUtils.getCurrentUserId(), null, "密码错误");
            throw new BusinessException("密码错误");
        }
        as.setStatus(1); this.updateById(as);
        recordLog(id, "publish_decrypt", SecurityUtils.getCurrentUserId(), null, "密码解密公布成功");
    }

    @Override
    public List<AnswerSet> getPublishedAnswers(Long tokenId, Long classId) {
        LambdaQueryWrapper<AnswerSet> w = new LambdaQueryWrapper<>();
        w.eq(AnswerSet::getStatus, 1);
        if (classId != null) w.like(AnswerSet::getTargetClassIds, String.valueOf(classId));
        w.orderByDesc(AnswerSet::getUpdatedAt);
        return this.list(w);
    }

    @Override
    public AnswerDetailVO getAnswerDetail(Long id, Long tokenId) {
        AnswerSet as = this.getById(id);
        if (as == null) throw new BusinessException(404, "答案集不存在");
        if (as.getStatus() != 1) throw new BusinessException("该答案集尚未公布");
        if (tokenId != null) recordLog(id, "view", null, tokenId, "查看答案详情");
        String statusName = switch (as.getStatus()) { case 0 -> "草稿"; case 1 -> "已公布"; case 2 -> "已撤回"; default -> "未知"; };
        return AnswerDetailVO.builder().id(as.getId()).title(as.getTitle()).status(as.getStatus())
                .statusName(statusName).publishedAt(as.getUpdatedAt())
                .questions(JSON.parseArray(as.getQuestionsJson())).targetClassIds(as.getTargetClassIds()).build();
    }

    @Override
    public PageResult<AnswerSet> pageList(Integer status, Long current, Long size) {
        Page<AnswerSet> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        LambdaQueryWrapper<AnswerSet> w = new LambdaQueryWrapper<>();
        if (status != null) w.eq(AnswerSet::getStatus, status);
        w.orderByDesc(AnswerSet::getCreatedAt);
        Page<AnswerSet> rp = this.page(page, w);
        return PageResult.of(rp.getRecords(), rp.getTotal(), rp.getCurrent(), rp.getSize());
    }

    @Override
    public List<AnswerLog> getLogs(Long answerSetId) {
        return answerLogMapper.selectList(new LambdaQueryWrapper<AnswerLog>()
                .eq(AnswerLog::getAnswerSetId, answerSetId).orderByDesc(AnswerLog::getCreatedAt));
    }

    private void recordLog(Long id, String action, Long opId, Long tokId, String detail) {
        AnswerLog l = new AnswerLog(); l.setAnswerSetId(id); l.setActionType(action);
        l.setOperatorId(opId); l.setTokenId(tokId); l.setDetail(detail);
        answerLogMapper.insert(l);
    }
}
