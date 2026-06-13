package cn.iocoder.yudao.module.opshub.controller.admin.dealer;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo.DealerInfoRespVO;
import cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo.DealerProductLinePageReqVO;
import cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo.DealerProductLineRespVO;
import cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo.DealerProductLineSaveReqVO;
import cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo.DealerProductLineSimpleRespVO;
import cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo.ProductLineBindDealerReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.DealerInfoDO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.DealerProductLineDO;
import cn.iocoder.yudao.module.opshub.service.dealer.DealerProductLineService;
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

@Tag(name = "管理后台 - 产品线管理")
@RestController
@RequestMapping("/opshub/product-line")
@Validated
public class DealerProductLineController {

    @Resource
    private DealerProductLineService productLineService;

    @PostMapping("/create")
    @Operation(summary = "新增产品线")
    @PreAuthorize("@ss.hasPermission('dealer:productline:create')")
    public CommonResult<Long> createProductLine(@Valid @RequestBody DealerProductLineSaveReqVO createReqVO) {
        return success(productLineService.createProductLine(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改产品线")
    @PreAuthorize("@ss.hasPermission('dealer:productline:update')")
    public CommonResult<Boolean> updateProductLine(@Valid @RequestBody DealerProductLineSaveReqVO updateReqVO) {
        productLineService.updateProductLine(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除产品线")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:productline:delete')")
    public CommonResult<Boolean> deleteProductLine(@RequestParam("id") Long id) {
        productLineService.deleteProductLine(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得产品线")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:productline:query')")
    public CommonResult<DealerProductLineRespVO> getProductLine(@RequestParam("id") Long id) {
        DealerProductLineDO productLine = productLineService.getProductLine(id);
        return success(BeanUtils.toBean(productLine, DealerProductLineRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得产品线分页")
    @PreAuthorize("@ss.hasPermission('dealer:productline:query')")
    public CommonResult<PageResult<DealerProductLineRespVO>> getProductLinePage(@Valid DealerProductLinePageReqVO pageReqVO) {
        PageResult<DealerProductLineDO> pageResult = productLineService.getProductLinePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, DealerProductLineRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得产品线精简列表")
    @PreAuthorize("@ss.hasPermission('dealer:productline:query')")
    public CommonResult<List<DealerProductLineSimpleRespVO>> getSimpleList() {
        List<DealerProductLineDO> list = productLineService.getSimpleList();
        return success(BeanUtils.toBean(list, DealerProductLineSimpleRespVO.class));
    }

    // ========== 经销商绑定 ==========

    @PostMapping("/bind-dealer")
    @Operation(summary = "绑定经销商")
    @PreAuthorize("@ss.hasPermission('dealer:productline:binddealer')")
    public CommonResult<Boolean> bindDealer(@Valid @RequestBody ProductLineBindDealerReqVO reqVO) {
        productLineService.bindDealer(reqVO.getProductLineId(), reqVO.getDealerId());
        return success(true);
    }

    @DeleteMapping("/unbind-dealer")
    @Operation(summary = "解绑经销商")
    @PreAuthorize("@ss.hasPermission('dealer:productline:binddealer')")
    public CommonResult<Boolean> unbindDealer(@Valid ProductLineBindDealerReqVO reqVO) {
        productLineService.unbindDealer(reqVO.getProductLineId(), reqVO.getDealerId());
        return success(true);
    }

    @GetMapping("/dealers")
    @Operation(summary = "获得产品线已绑定经销商")
    @Parameter(name = "productLineId", description = "产品线ID", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:productline:query')")
    public CommonResult<List<DealerInfoRespVO>> getDealers(@RequestParam("productLineId") Long productLineId) {
        List<DealerInfoDO> list = productLineService.getDealers(productLineId);
        return success(BeanUtils.toBean(list, DealerInfoRespVO.class));
    }

}
