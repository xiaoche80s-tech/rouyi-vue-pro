package cn.iocoder.yudao.module.opshub.controller.admin.dealer;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.DealerInfoDO;
import cn.iocoder.yudao.module.opshub.service.dealer.DealerInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 经销商管理")
@RestController
@RequestMapping("/opshub/dealer")
@Validated
public class DealerInfoController {

    @Resource
    private DealerInfoService dealerInfoService;

    @PostMapping("/create")
    @Operation(summary = "新增经销商")
    @PreAuthorize("@ss.hasPermission('dealer:mgmt:create')")
    public CommonResult<Long> createDealer(@Valid @RequestBody DealerInfoSaveReqVO createReqVO) {
        return success(dealerInfoService.createDealer(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改经销商")
    @PreAuthorize("@ss.hasPermission('dealer:mgmt:update')")
    public CommonResult<Boolean> updateDealer(@Valid @RequestBody DealerInfoSaveReqVO updateReqVO) {
        dealerInfoService.updateDealer(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除经销商")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:mgmt:delete')")
    public CommonResult<Boolean> deleteDealer(@RequestParam("id") Long id) {
        dealerInfoService.deleteDealer(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得经销商")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:mgmt:query')")
    public CommonResult<DealerInfoRespVO> getDealer(@RequestParam("id") Long id) {
        DealerInfoDO dealer = dealerInfoService.getDealer(id);
        return success(BeanUtils.toBean(dealer, DealerInfoRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得经销商分页")
    @PreAuthorize("@ss.hasPermission('dealer:mgmt:query')")
    public CommonResult<PageResult<DealerInfoRespVO>> getDealerPage(@Valid DealerInfoPageReqVO pageReqVO) {
        PageResult<DealerInfoDO> pageResult = dealerInfoService.getDealerPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, DealerInfoRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得经销商精简列表")
    @PreAuthorize("@ss.hasPermission('dealer:mgmt:query')")
    public CommonResult<List<DealerInfoSimpleRespVO>> getSimpleList() {
        List<DealerInfoDO> list = dealerInfoService.getSimpleList();
        return success(BeanUtils.toBean(list, DealerInfoSimpleRespVO.class));
    }

}
