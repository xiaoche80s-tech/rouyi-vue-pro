package cn.iocoder.yudao.module.opshub.controller.admin.dealer;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo.DealerItemVO;
import cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo.DealerScopeUserRespVO;
import cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo.DealerUserScopeAssignReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.DealerInfoDO;
import cn.iocoder.yudao.module.opshub.service.dealer.DealerInfoService;
import cn.iocoder.yudao.module.opshub.service.dealer.DealerUserScopeService;
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

@Tag(name = "管理后台 - 用户经销商授权管理")
@RestController
@RequestMapping("/opshub/dealer-scope")
@Validated
public class DealerUserScopeController {

    @Resource
    private DealerUserScopeService dealerUserScopeService;

    @Resource
    private DealerInfoService dealerInfoService;

    @Resource
    private AdminUserApi adminUserApi;

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

    @GetMapping("/scope-users")
    @Operation(summary = "获取所有有经销商授权的用户列表")
    @PreAuthorize("@ss.hasPermission('system:user:query')")
    public CommonResult<List<DealerScopeUserRespVO>> getScopeUsers() {
        // 1. 获取所有授权用户及经销商编码
        List<UserScopeDTO> scopeDTOs = dealerUserScopeService.getAllUserDealerScopes();
        if (scopeDTOs.isEmpty()) {
            return success(Collections.emptyList());
        }
        // 2. 批量获取用户昵称
        Set<Long> userIds = convertSet(scopeDTOs, UserScopeDTO::getUserId);
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        // 3. 获取经销商 code->name 映射
        List<DealerInfoDO> allDealers = dealerInfoService.getSimpleList();
        Map<String, String> codeNameMap = allDealers.stream()
                .collect(Collectors.toMap(DealerInfoDO::getDealerCode, DealerInfoDO::getDealerName, (a, b) -> a));
        // 4. 组装 VO
        List<DealerScopeUserRespVO> result = convertList(scopeDTOs, dto -> {
            DealerScopeUserRespVO vo = new DealerScopeUserRespVO();
            vo.setUserId(dto.getUserId());
            AdminUserRespDTO user = userMap.get(dto.getUserId());
            vo.setNickname(user != null ? user.getNickname() : "");
            vo.setDealers(convertList(dto.getCodes(), code -> {
                DealerItemVO itemVO = new DealerItemVO();
                itemVO.setDealerCode(code);
                itemVO.setDealerName(codeNameMap.getOrDefault(code, code));
                return itemVO;
            }));
            return vo;
        });
        return success(result);
    }

}
