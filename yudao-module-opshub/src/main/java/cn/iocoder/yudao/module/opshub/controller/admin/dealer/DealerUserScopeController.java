package cn.iocoder.yudao.module.opshub.controller.admin.dealer;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo.DealerUserScopeAssignReqVO;
import cn.iocoder.yudao.module.opshub.service.dealer.DealerUserScopeService;
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

@Tag(name = "管理后台 - 用户经销商授权管理")
@RestController
@RequestMapping("/opshub/dealer-scope")
@Validated
public class DealerUserScopeController {

    @Resource
    private DealerUserScopeService dealerUserScopeService;

    @GetMapping("/list")
    @Operation(summary = "查看用户授权的经销商")
    @Parameter(name = "userId", description = "用户ID", required = true)
    @PreAuthorize("@ss.hasPermission('system:user:query')")
    public CommonResult<Set<String>> getListByUserId(@RequestParam("userId") Long userId) {
        return success(dealerUserScopeService.getDealerCodesByUserId(userId));
    }

    @PostMapping("/assign")
    @Operation(summary = "分配用户经销商授权")
    @PreAuthorize("@ss.hasPermission('system:user:update')")
    public CommonResult<Boolean> assign(@Valid @RequestBody DealerUserScopeAssignReqVO reqVO) {
        dealerUserScopeService.assign(reqVO.getUserId(), reqVO.getDealerCodes());
        return success(true);
    }

    @GetMapping("/users")
    @Operation(summary = "查看经销商关联的所有代理人用户ID")
    @Parameter(name = "dealerCode", description = "经销商编码", required = true)
    @PreAuthorize("@ss.hasPermission('system:user:query')")
    public CommonResult<Set<Long>> getUserIdsByDealerCode(@RequestParam("dealerCode") String dealerCode) {
        return success(dealerUserScopeService.getUserIdsByDealerCode(dealerCode));
    }

}
