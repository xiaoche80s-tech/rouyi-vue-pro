package cn.iocoder.yudao.module.opshub.controller.admin.signing;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.opshub.controller.admin.signing.vo.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.DealerInfoDO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.signing.SigningContractDO;
import cn.iocoder.yudao.module.opshub.service.dealer.DealerInfoService;
import cn.iocoder.yudao.module.opshub.service.signing.SigningContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 签约进度")
@RestController
@RequestMapping("/opshub/signing")
@Validated
public class SigningContractController {

    @Resource
    private SigningContractService signingContractService;

    @Resource
    private DealerInfoService dealerInfoService;

    @GetMapping("/page")
    @Operation(summary = "获得签约合同分页")
    @PreAuthorize("@ss.hasPermission('dealer:signing:query')")
    public CommonResult<PageResult<SigningContractRespVO>> getSigningContractPage(@Valid SigningContractPageReqVO pageReqVO) {
        PageResult<SigningContractDO> pageResult = signingContractService.getSigningContractPage(pageReqVO);
        PageResult<SigningContractRespVO> voPageResult = BeanUtils.toBean(pageResult, SigningContractRespVO.class);
        fillDealerNames(voPageResult.getList());
        return success(voPageResult);
    }

    @GetMapping("/get")
    @Operation(summary = "获得签约合同详情")
    @Parameter(name = "id", description = "合同ID", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:signing:query')")
    public CommonResult<SigningContractRespVO> getSigningContract(@RequestParam("id") Long id) {
        SigningContractDO contract = signingContractService.getSigningContract(id);
        SigningContractRespVO vo = BeanUtils.toBean(contract, SigningContractRespVO.class);
        if (vo != null && contract != null) {
            DealerInfoDO dealer = dealerInfoService.getDealer(contract.getDealerId());
            if (dealer != null) {
                vo.setDealerName(dealer.getDealerName());
            }
        }
        return success(vo);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获得签约统计数据")
    @PreAuthorize("@ss.hasPermission('dealer:signing:query')")
    public CommonResult<SigningContractStatisticsRespVO> getStatistics() {
        return success(signingContractService.getStatistics());
    }

    @GetMapping("/trend")
    @Operation(summary = "获得签约趋势数据")
    @Parameter(name = "timeDimension", description = "时间维度(month/quarter/year)")
    @PreAuthorize("@ss.hasPermission('dealer:signing:query')")
    public CommonResult<List<SigningContractTrendRespVO>> getTrend(
            @RequestParam(value = "timeDimension", defaultValue = "month") String timeDimension) {
        return success(signingContractService.getTrend(timeDimension));
    }

    @PostMapping("/create")
    @Operation(summary = "创建合同")
    @PreAuthorize("@ss.hasPermission('dealer:signing:create')")
    public CommonResult<Long> createSigningContract(@Valid @RequestBody SigningContractCreateReqVO reqVO) {
        return success(signingContractService.createSigningContract(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新合同")
    @PreAuthorize("@ss.hasPermission('dealer:signing:update')")
    public CommonResult<Boolean> updateSigningContract(@Valid @RequestBody SigningContractUpdateReqVO reqVO) {
        signingContractService.updateSigningContract(reqVO);
        return success(true);
    }

    @PostMapping("/sign")
    @Operation(summary = "经销商发起签署")
    @Parameter(name = "id", description = "合同ID", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:signing:sign')")
    public CommonResult<Boolean> signContract(@RequestParam("id") Long id) {
        signingContractService.signContract(id);
        return success(true);
    }

    @PostMapping("/upload-sign-proof")
    @Operation(summary = "执行员上传盖章文件")
    @PreAuthorize("@ss.hasPermission('dealer:signing:upload-proof')")
    public CommonResult<Boolean> uploadSignProof(@RequestParam("id") Long id,
                                                  @RequestParam("signProofUrl") String signProofUrl) {
        signingContractService.uploadSignProof(id, signProofUrl);
        return success(true);
    }

    // ========== 辅助方法 ==========

    private void fillDealerNames(List<SigningContractRespVO> list) {
        if (list == null || list.isEmpty()) return;
        Set<Long> dealerIds = list.stream()
                .map(SigningContractRespVO::getDealerId)
                .collect(Collectors.toSet());
        Map<Long, String> dealerNameMap = dealerIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> {
                            DealerInfoDO dealer = dealerInfoService.getDealer(id);
                            return dealer != null ? dealer.getDealerName() : "";
                        }));
        for (SigningContractRespVO vo : list) {
            vo.setDealerName(dealerNameMap.getOrDefault(vo.getDealerId(), ""));
        }
    }

}
