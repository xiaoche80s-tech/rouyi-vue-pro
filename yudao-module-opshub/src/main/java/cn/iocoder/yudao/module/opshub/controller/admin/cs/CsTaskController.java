package cn.iocoder.yudao.module.opshub.controller.admin.cs;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.cs.CsTaskDO;
import cn.iocoder.yudao.module.opshub.service.cs.CsTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 客服工单")
@RestController
@RequestMapping("/opshub/cs-task")
@Validated
public class CsTaskController {

    @Resource
    private CsTaskService csTaskService;

    @GetMapping("/page")
    @Operation(summary = "获得工单分页")
    @PreAuthorize("@ss.hasPermission('dealer:cs-task:query')")
    public CommonResult<PageResult<CsTaskRespVO>> getCsTaskPage(@Valid CsTaskPageReqVO pageReqVO) {
        PageResult<CsTaskDO> pageResult = csTaskService.getCsTaskPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, CsTaskRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得工单详情")
    @Parameter(name = "id", description = "工单ID", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:cs-task:query')")
    public CommonResult<CsTaskRespVO> getCsTask(@RequestParam("id") Long id) {
        CsTaskDO task = csTaskService.getCsTask(id);
        return success(BeanUtils.toBean(task, CsTaskRespVO.class));
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

    @PostMapping("/deliver")
    @Operation(summary = "执行员交付工单")
    @Parameter(name = "id", description = "工单ID", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:cs-task:deliver')")
    public CommonResult<Boolean> deliverTask(@RequestParam("id") Long id) {
        csTaskService.deliverTask(id);
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

}
