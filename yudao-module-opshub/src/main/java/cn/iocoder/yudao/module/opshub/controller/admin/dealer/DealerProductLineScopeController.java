package cn.iocoder.yudao.module.opshub.controller.admin.dealer;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo.ExecutorProductLineScopeAssignReqVO;
import cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo.ExecutorScopeUserRespVO;
import cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo.ProductLineItemVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.DealerProductLineDO;
import cn.iocoder.yudao.module.opshub.service.dealer.DealerProductLineService;
import cn.iocoder.yudao.module.opshub.service.dealer.ExecutorProductLineScopeService;
import cn.iocoder.yudao.module.opshub.service.dealer.UserScopeDTO;
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

import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - 执行员产品线授权管理")
@RestController
@RequestMapping("/opshub/product-line-scope")
@Validated
public class DealerProductLineScopeController {

    @Resource
    private ExecutorProductLineScopeService executorPlScopeService;

    @Resource
    private DealerProductLineService productLineService;

    @Resource
    private AdminUserApi adminUserApi;

    @GetMapping("/list")
    @Operation(summary = "查看用户授权的产品线")
    @Parameter(name = "userId", description = "用户ID", required = true)
    @PreAuthorize("@ss.hasPermission('system:user:query')")
    public CommonResult<Set<String>> getListByUserId(@RequestParam("userId") Long userId) {
        return success(executorPlScopeService.getProductLineCodesByUserId(userId));
    }

    @PostMapping("/assign")
    @Operation(summary = "分配用户产品线授权")
    @PreAuthorize("@ss.hasPermission('system:user:update')")
    public CommonResult<Boolean> assign(@Valid @RequestBody ExecutorProductLineScopeAssignReqVO reqVO) {
        executorPlScopeService.assign(reqVO.getUserId(), reqVO.getProductLineCodes());
        return success(true);
    }

    @GetMapping("/users")
    @Operation(summary = "查看产品线关联的所有执行员用户ID")
    @Parameter(name = "productLineCode", description = "产品线编码", required = true)
    @PreAuthorize("@ss.hasPermission('system:user:query')")
    public CommonResult<Set<Long>> getUserIdsByProductLineCode(@RequestParam("productLineCode") String productLineCode) {
        return success(executorPlScopeService.getUserIdsByProductLineCode(productLineCode));
    }

    @GetMapping("/scope-users")
    @Operation(summary = "获取所有有产品线授权的用户列表")
    @PreAuthorize("@ss.hasPermission('system:user:query')")
    public CommonResult<List<ExecutorScopeUserRespVO>> getScopeUsers() {
        // 1. 获取所有授权用户及产品线编码
        List<UserScopeDTO> scopeDTOs = executorPlScopeService.getAllUserProductLineScopes();
        if (scopeDTOs.isEmpty()) {
            return success(Collections.emptyList());
        }
        // 2. 批量获取用户昵称
        Set<Long> userIds = convertSet(scopeDTOs, UserScopeDTO::getUserId);
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        // 3. 获取产品线 code->name 映射
        List<DealerProductLineDO> allProductLines = productLineService.getSimpleList();
        Map<String, String> codeNameMap = allProductLines.stream()
                .collect(Collectors.toMap(DealerProductLineDO::getProductLineCode, DealerProductLineDO::getProductLineName, (a, b) -> a));
        // 4. 组装 VO
        List<ExecutorScopeUserRespVO> result = convertList(scopeDTOs, dto -> {
            ExecutorScopeUserRespVO vo = new ExecutorScopeUserRespVO();
            vo.setUserId(dto.getUserId());
            AdminUserRespDTO user = userMap.get(dto.getUserId());
            vo.setNickname(user != null ? user.getNickname() : "");
            vo.setProductLines(convertList(dto.getCodes(), code -> {
                ProductLineItemVO itemVO = new ProductLineItemVO();
                itemVO.setProductLineCode(code);
                itemVO.setProductLineName(codeNameMap.getOrDefault(code, code));
                return itemVO;
            }));
            return vo;
        });
        return success(result);
    }

}
