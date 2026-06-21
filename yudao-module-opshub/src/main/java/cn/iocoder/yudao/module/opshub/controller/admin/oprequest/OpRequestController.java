package cn.iocoder.yudao.module.opshub.controller.admin.oprequest;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.opshub.controller.admin.oprequest.vo.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.oprequest.OpRequestDO;
import cn.iocoder.yudao.module.opshub.service.oprequest.OpRequestService;
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
@RequestMapping("/opshub/op-request")
@Validated
public class OpRequestController {

    @Resource
    private OpRequestService opRequestService;

    @PostMapping("/create")
    @Operation(summary = "创建操作请求")
    @PreAuthorize("@ss.hasPermission('dealer:op-request:create')")
    public CommonResult<Long> createOpRequest(@Valid @RequestBody OpRequestCreateReqVO vo) {
        return success(opRequestService.createOpRequest(vo));
    }

    @GetMapping("/page")
    @Operation(summary = "获得操作请求分页")
    @PreAuthorize("@ss.hasPermission('dealer:op-request:query')")
    public CommonResult<PageResult<OpRequestRespVO>> getOpRequestPage(@Valid OpRequestPageReqVO reqVO) {
        PageResult<OpRequestDO> pageResult = opRequestService.getOpRequestPage(reqVO);
        return success(BeanUtils.toBean(pageResult, OpRequestRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得操作请求详情")
    @Parameter(name = "id", description = "操作请求 ID", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:op-request:query')")
    public CommonResult<OpRequestRespVO> getOpRequest(@RequestParam("id") Long id) {
        return success(opRequestService.getOpRequest(id));
    }

    @PostMapping("/submit-result")
    @Operation(summary = "提交处理结果")
    @PreAuthorize("@ss.hasPermission('dealer:op-request:process')")
    public CommonResult<Boolean> submitResult(@Valid @RequestBody OpRequestSubmitResultReqVO vo) {
        opRequestService.submitResult(vo);
        return success(true);
    }

    @PostMapping("/verify")
    @Operation(summary = "经销商验收")
    @PreAuthorize("@ss.hasPermission('dealer:op-request:verify')")
    public CommonResult<Boolean> verifyRequest(@Valid @RequestBody OpRequestVerifyReqVO vo) {
        opRequestService.verifyRequest(vo);
        return success(true);
    }

    @GetMapping("/get-active-by-contract")
    @Operation(summary = "根据合同ID查找进行中的操作请求")
    @Parameter(name = "contractId", description = "合同 ID", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:op-request:query')")
    public CommonResult<OpRequestRespVO> getActiveByContractId(@RequestParam("contractId") Long contractId) {
        OpRequestDO request = opRequestService.findActiveByContractId(contractId);
        return success(request != null ? BeanUtils.toBean(request, OpRequestRespVO.class) : null);
    }

}
