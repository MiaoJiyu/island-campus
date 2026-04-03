package com.islandcampus.server.answer.controller;

import com.islandcampus.server.common.base.PageResult;
import com.islandcampus.server.common.base.R;
import com.islandcampus.server.answer.dto.AnswerDetailVO;
import com.islandcampus.server.answer.dto.AnswerPublishDecryptDTO;
import com.islandcampus.server.answer.dto.AnswerSetCreateDTO;
import com.islandcampus.server.answer.entity.AnswerLog;
import com.islandcampus.server.answer.entity.AnswerSet;
import com.islandcampus.server.answer.service.AnswerSetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "答案公布管理")
@RestController
@RequestMapping("/api/v1/answer")
@RequiredArgsConstructor
public class AnswerController {
    private final AnswerSetService answerSetService;

    @Operation(summary = "创建答案集")
    @PostMapping("/")
    public R<AnswerSet> create(@Valid @RequestBody AnswerSetCreateDTO dto) {
        return R.ok("创建成功", answerSetService.create(dto));
    }

    @Operation(summary = "修改答案集")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody AnswerSetCreateDTO dto) {
        answerSetService.update(id, dto); return R.ok(null);
    }

    @Operation(summary = "删除答案集")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) { answerSetService.delete(id); return R.ok(null); }

    @Operation(summary = "分页列表(教师管理)")
    @GetMapping("/")
    public R<PageResult<AnswerSet>> pageList(@RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Long current, @RequestParam(defaultValue = "10") Long size) {
        return R.ok(answerSetService.pageList(status, current, size));
    }

    @Operation(summary = "触发定时公布")
    @PostMapping("/{id}/publish/timer")
    public R<Void> publishByTimer(@PathVariable Long id) { answerSetService.publishByTime(id); return R.ok(null); }

    @Operation(summary = "解密公布")
    @PostMapping("/{id}/publish/decrypt")
    public R<Void> publishByDecrypt(@PathVariable Long id, @Valid @RequestBody AnswerPublishDecryptDTO dto) {
        answerSetService.publishByDecrypt(id, dto); return R.ok(null);
    }

    @Operation(summary = "已公布的答案集")
    @GetMapping("/published")
    public R<List<AnswerSet>> getPublished(@RequestParam(required = false) Long classId) {
        return R.ok(answerSetService.getPublishedAnswers(null, classId));
    }

    @Operation(summary = "答案详情")
    @GetMapping("/{id}/detail")
    public R<AnswerDetailVO> detail(@PathVariable Long id) { return R.ok(answerSetService.getAnswerDetail(id, null)); }

    @Operation(summary = "操作日志")
    @GetMapping("/{id}/logs")
    public R<List<AnswerLog>> logs(@PathVariable Long id) { return R.ok(answerSetService.getLogs(id)); }
}
