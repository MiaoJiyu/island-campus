package com.islandcampus.server.answer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.islandcampus.server.common.base.PageResult;
import com.islandcampus.server.answer.dto.AnswerDetailVO;
import com.islandcampus.server.answer.dto.AnswerPublishDecryptDTO;
import com.islandcampus.server.answer.dto.AnswerSetCreateDTO;
import com.islandcampus.server.answer.entity.AnswerLog;
import com.islandcampus.server.answer.entity.AnswerSet;

import java.util.List;

public interface AnswerSetService extends IService<AnswerSet> {
    AnswerSet create(AnswerSetCreateDTO dto);
    void update(Long id, AnswerSetCreateDTO dto);
    void delete(Long id);
    void publishByTime(Long id);
    void publishByDecrypt(Long id, AnswerPublishDecryptDTO dto);
    List<AnswerSet> getPublishedAnswers(Long tokenId, Long classId);
    AnswerDetailVO getAnswerDetail(Long id, Long tokenId);
    PageResult<AnswerSet> pageList(Integer status, Long current, Long size);
    List<AnswerLog> getLogs(Long answerSetId);
}
