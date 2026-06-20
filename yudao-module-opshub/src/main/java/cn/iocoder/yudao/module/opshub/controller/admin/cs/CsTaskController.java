package cn.iocoder.yudao.module.opshub.controller.admin.cs;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.cs.CsTaskDO;
import cn.iocoder.yudao.module.opshub.service.cs.CsTaskService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 客服工单")
@RestController
@RequestMapping("/opshub/cs-task")
@Validated
public class CsTaskController {

    @Resource
    private CsTaskService csTaskService;
    @Resource
    private AdminUserApi adminUserApi;

    @GetMapping("/tab-counts")
    @Operation(summary = "获取各子标签工单数量")
    @PreAuthorize("@ss.hasPermission('dealer:cs-task:query')")
    public CommonResult<java.util.Map<String, Long>> getTabCounts() {
        return success(csTaskService.getTabCounts());
    }

    @GetMapping("/page")
    @Operation(summary = "获得工单分页")
    @PreAuthorize("@ss.hasPermission('dealer:cs-task:query')")
    public CommonResult<PageResult<CsTaskRespVO>> getCsTaskPage(@Valid CsTaskPageReqVO pageReqVO) {
        PageResult<CsTaskDO> pageResult = csTaskService.getCsTaskPage(pageReqVO);
        PageResult<CsTaskRespVO> voResult = BeanUtils.toBean(pageResult, CsTaskRespVO.class);
        // 补充处理人/提单人姓名
        fillUserNames(voResult.getList());
        return success(voResult);
    }

    @GetMapping("/get")
    @Operation(summary = "获得工单详情")
    @Parameter(name = "id", description = "工单ID", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:cs-task:query')")
    public CommonResult<CsTaskRespVO> getCsTask(@RequestParam("id") Long id) {
        CsTaskDO task = csTaskService.getCsTask(id);
        CsTaskRespVO vo = BeanUtils.toBean(task, CsTaskRespVO.class);
        if (vo != null) {
            fillUserNames(java.util.Collections.singletonList(vo));
        }
        return success(vo);
    }

    @PostMapping("/create")
    @Operation(summary = "创建工单（经销商提单）")
    @PreAuthorize("@ss.hasPermission('dealer:cs-task:create')")
    public CommonResult<Long> createCsTask(@Valid @RequestBody CsTaskCreateReqVO reqVO) {
        return success(csTaskService.createCsTask(reqVO));
    }

    @PostMapping("/accept")
    @Operation(summary = "执行员接单")
    @Parameter(name = "id", description = "工单ID", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:cs-task:accept')")
    public CommonResult<Boolean> acceptTask(@RequestParam("id") Long id) {
        csTaskService.acceptTask(id);
        return success(true);
    }

    @PostMapping("/transfer")
    @Operation(summary = "执行员转单")
    @PreAuthorize("@ss.hasPermission('dealer:cs-task:transfer')")
    public CommonResult<Boolean> transferTask(@Valid @RequestBody CsTaskTransferReqVO reqVO) {
        csTaskService.transferTask(reqVO);
        return success(true);
    }

    @PostMapping("/submit-for-approval")
    @Operation(summary = "执行员提交审批")
    @PreAuthorize("@ss.hasPermission('dealer:cs-task:deliver')")
    public CommonResult<Boolean> submitForApproval(
            @RequestParam("id") Long id,
            @RequestParam(value = "reason", required = false) String reason) {
        csTaskService.submitForApproval(id, reason);
        return success(true);
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消/关闭工单")
    @PreAuthorize("@ss.hasPermission('dealer:cs-task:cancel')")
    public CommonResult<Boolean> cancelTask(@RequestParam("id") Long id,
                                            @RequestParam(value = "reason", required = false) String reason) {
        csTaskService.cancelTask(id, reason);
        return success(true);
    }

    @PostMapping("/verify")
    @Operation(summary = "经销商验收工单")
    @PreAuthorize("@ss.hasPermission('dealer:cs-task:verify')")
    public CommonResult<Boolean> verifyTask(@Valid @RequestBody CsTaskVerifyReqVO reqVO) {
        csTaskService.verifyTask(reqVO);
        return success(true);
    }

    @PostMapping("/reprocess")
    @Operation(summary = "退回后重新处理")
    @Parameter(name = "id", description = "工单ID", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:cs-task:reprocess')")
    public CommonResult<Boolean> reprocessTask(@RequestParam("id") Long id) {
        csTaskService.reprocessTask(id);
        return success(true);
    }

    @PostMapping("/urge")
    @Operation(summary = "催办工单")
    @Parameter(name = "id", description = "工单ID", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:cs-task:urge')")
    public CommonResult<Boolean> urgeTask(@RequestParam("id") Long id) {
        csTaskService.urgeTask(id);
        return success(true);
    }

    @GetMapping("/candidate-users")
    @Operation(summary = "获取工单 BPM 候选人列表")
    @Parameter(name = "id", description = "工单ID", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:cs-task:transfer')")
    public CommonResult<Set<Long>> getTaskCandidateUserIds(@RequestParam("id") Long id) {
        return success(csTaskService.getTaskCandidateUserIds(id));
    }

    @GetMapping("/bpm-task-id")
    @Operation(summary = "获取工单当前运行的 BPM 任务 ID")
    @Parameter(name = "id", description = "工单ID", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:cs-task:transfer')")
    public CommonResult<String> getBpmTaskId(@RequestParam("id") Long id) {
        return success(csTaskService.getBpmTaskId(id));
    }

    // ========== 私有方法 ==========

    /**
     * 批量补充处理人/提单人姓名
     */
    private void fillUserNames(java.util.List<CsTaskRespVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Set<Long> userIds = list.stream()
                .flatMap(vo -> java.util.stream.Stream.of(vo.getAssigneeId(), vo.getCreatorUserId()))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return;
        }
        java.util.Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        for (CsTaskRespVO vo : list) {
            if (vo.getAssigneeId() != null) {
                AdminUserRespDTO user = userMap.get(vo.getAssigneeId());
                if (user != null) {
                    vo.setAssigneeName(user.getNickname());
                }
            }
            if (vo.getCreatorUserId() != null) {
                AdminUserRespDTO user = userMap.get(vo.getCreatorUserId());
                if (user != null) {
                    vo.setCreatorUserName(user.getNickname());
                }
            }
        }
    }

}
