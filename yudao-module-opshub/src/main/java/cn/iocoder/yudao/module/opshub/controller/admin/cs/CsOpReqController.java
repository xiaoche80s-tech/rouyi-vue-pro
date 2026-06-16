package cn.iocoder.yudao.module.opshub.controller.admin.cs;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.cs.CsOpReqDO;
import cn.iocoder.yudao.module.opshub.service.cs.CsOpReqService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 操作请求")
@RestController
@RequestMapping("/opshub/cs-opreq")
@Validated
public class CsOpReqController {

    @Resource
    private CsOpReqService csOpReqService;

    @GetMapping("/page")
    @Operation(summary = "获得操作请求分页")
    @PreAuthorize("@ss.hasPermission('dealer:cs-opreq:query')")
    public CommonResult<PageResult<CsOpReqRespVO>> getOpReqPage(@Valid CsOpReqPageReqVO pageReqVO) {
        PageResult<CsOpReqDO> pageResult = csOpReqService.getOpReqPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, CsOpReqRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得操作请求详情（含附件）")
    @Parameter(name = "id", description = "操作请求ID", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:cs-opreq:query')")
    public CommonResult<CsOpReqRespVO> getOpReq(@RequestParam("id") Long id) {
        CsOpReqDO opReq = csOpReqService.getOpReq(id);
        return success(BeanUtils.toBean(opReq, CsOpReqRespVO.class));
    }

    @PostMapping("/create")
    @Operation(summary = "创建操作请求")
    @PreAuthorize("@ss.hasPermission('dealer:cs-opreq:create')")
    public CommonResult<Long> createOpReq(@Valid @RequestBody CsOpReqCreateReqVO reqVO) {
        return success(csOpReqService.createOpReq(reqVO));
    }

    @PostMapping("/accept")
    @Operation(summary = "执行员接单")
    @Parameter(name = "id", description = "操作请求ID", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:cs-opreq:accept')")
    public CommonResult<Boolean> acceptOpReq(@RequestParam("id") Long id) {
        csOpReqService.acceptOpReq(id);
        return success(true);
    }

    @PostMapping("/submit")
    @Operation(summary = "执行员提交结果")
    @PreAuthorize("@ss.hasPermission('dealer:cs-opreq:submit')")
    public CommonResult<Boolean> submitOpReq(@Valid @RequestBody CsOpReqSubmitReqVO reqVO) {
        csOpReqService.submitOpReq(reqVO);
        return success(true);
    }

    @PostMapping("/verify")
    @Operation(summary = "经销商验收")
    @Parameter(name = "id", description = "操作请求ID", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:cs-opreq:verify')")
    public CommonResult<Boolean> verifyOpReq(@RequestParam("id") Long id) {
        csOpReqService.verifyOpReq(id);
        return success(true);
    }

}
