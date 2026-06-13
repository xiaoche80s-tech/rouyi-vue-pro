package cn.iocoder.yudao.module.opshub.controller.admin.dealer;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo.ExecutorProductLineScopeAssignReqVO;
import cn.iocoder.yudao.module.opshub.service.dealer.ExecutorProductLineScopeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 执行员产品线授权管理")
@RestController
@RequestMapping("/opshub/product-line-scope")
@Validated
public class DealerProductLineScopeController {

    @Resource
    private ExecutorProductLineScopeService executorPlScopeService;

    @GetMapping("/list")
    @Operation(summary = "查看用户授权的产品线")
    @Parameter(name = "userId", description = "用户ID", required = true)
    @PreAuthorize("@ss.hasPermission('system:user:query')")
    public CommonResult<Set<Long>> getListByUserId(@RequestParam("userId") Long userId) {
        return success(executorPlScopeService.getProductLineIdsByUserId(userId));
    }

    @PostMapping("/assign")
    @Operation(summary = "分配用户产品线授权")
    @PreAuthorize("@ss.hasPermission('system:user:update')")
    public CommonResult<Boolean> assign(@Valid @RequestBody ExecutorProductLineScopeAssignReqVO reqVO) {
        executorPlScopeService.assign(reqVO.getUserId(), reqVO.getProductLineIds());
        return success(true);
    }

    @GetMapping("/users")
    @Operation(summary = "查看产品线关联的所有执行员用户ID")
    @Parameter(name = "productLineId", description = "产品线ID", required = true)
    @PreAuthorize("@ss.hasPermission('system:user:query')")
    public CommonResult<Set<Long>> getUserIdsByProductLineId(@RequestParam("productLineId") Long productLineId) {
        return success(executorPlScopeService.getUserIdsByProductLineId(productLineId));
    }

}
