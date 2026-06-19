package cn.iocoder.yudao.module.opshub.controller.admin.signing;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.opshub.controller.admin.signing.vo.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.basedata.BasedataFileDO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.DealerInfoDO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.DealerProductLineDO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.signing.SigningContractDO;
import cn.iocoder.yudao.module.opshub.enums.BasedataFileTypeEnum;
import cn.iocoder.yudao.module.opshub.service.basedata.BasedataFileService;
import cn.iocoder.yudao.module.opshub.service.dealer.DealerInfoService;
import cn.iocoder.yudao.module.opshub.service.dealer.DealerProductLineService;
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

    @Resource
    private DealerProductLineService dealerProductLineService;

    @Resource
    private BasedataFileService basedataFileService;

    @GetMapping("/page")
    @Operation(summary = "获得签约合同分页")
    @PreAuthorize("@ss.hasPermission('dealer:signing:query')")
    public CommonResult<PageResult<SigningContractRespVO>> getSigningContractPage(@Valid SigningContractPageReqVO pageReqVO) {
        PageResult<SigningContractDO> pageResult = signingContractService.getSigningContractPage(pageReqVO);
        PageResult<SigningContractRespVO> voPageResult = BeanUtils.toBean(pageResult, SigningContractRespVO.class);
        fillDealerNames(voPageResult.getList());
        fillProductLineNames(voPageResult.getList());
        fillAttachmentCounts(voPageResult.getList());
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
            // 回填产品线名称
            if (StrUtil.isNotBlank(contract.getProductLineCode())) {
                dealerProductLineService.getSimpleList().stream()
                        .filter(pl -> pl.getProductLineCode().equals(contract.getProductLineCode()))
                        .findFirst()
                        .ifPresent(pl -> vo.setProductLineName(pl.getProductLineName()));
            }
            // 回填附件数量
            List<BasedataFileDO> attachments = basedataFileService.getContractAttachments(
                    contract.getDealerCode(), contract.getContractCode());
            vo.setAttachmentCount(attachments.size());
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

    @GetMapping("/attachments")
    @Operation(summary = "获取合同附件列表")
    @Parameter(name = "contractId", description = "合同ID", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:signing:query')")
    public CommonResult<List<ContractAttachmentRespVO>> getContractAttachments(
            @RequestParam("contractId") Long contractId) {
        SigningContractDO contract = signingContractService.getSigningContract(contractId);
        if (contract == null) {
            return success(List.of());
        }
        List<BasedataFileDO> files = basedataFileService.getContractAttachments(
                contract.getDealerCode(), contract.getContractCode());
        List<ContractAttachmentRespVO> voList = BeanUtils.toBean(files, ContractAttachmentRespVO.class);
        // 填充文件子类型名称
        for (ContractAttachmentRespVO vo : voList) {
            vo.setFileTypeName(getFileTypeName(vo.getFileType()));
        }
        return success(voList);
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

    private void fillProductLineNames(List<SigningContractRespVO> list) {
        if (list == null || list.isEmpty()) return;
        // 产品线数据量小，一次查出构建 Map
        Map<String, String> plNameMap = dealerProductLineService.getSimpleList().stream()
                .collect(Collectors.toMap(
                        DealerProductLineDO::getProductLineCode,
                        DealerProductLineDO::getProductLineName,
                        (a, b) -> a));
        for (SigningContractRespVO vo : list) {
            if (StrUtil.isNotBlank(vo.getProductLineCode())) {
                vo.setProductLineName(plNameMap.getOrDefault(vo.getProductLineCode(), ""));
            }
        }
    }

    /**
     * 批量填充附件数量（避免 N+1）
     * 收集所有 dealerCode，一次性查出 category='contract' 的文件，按 file_no 分组计数
     */
    private void fillAttachmentCounts(List<SigningContractRespVO> list) {
        if (list == null || list.isEmpty()) return;
        Set<String> dealerCodes = list.stream()
                .map(SigningContractRespVO::getDealerCode)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        if (dealerCodes.isEmpty()) return;

        List<BasedataFileDO> contractFiles = basedataFileService.getContractFilesByDealerCodes(dealerCodes);
        // 按 file_no 分组计数
        Map<String, Integer> countMap = contractFiles.stream()
                .collect(Collectors.groupingBy(
                        f -> f.getFileNo() != null ? f.getFileNo() : "",
                        Collectors.summingInt(e -> 1)));

        for (SigningContractRespVO vo : list) {
            vo.setAttachmentCount(countMap.getOrDefault(
                    vo.getContractCode() != null ? vo.getContractCode() : "", 0));
        }
    }

    private String getFileTypeName(String fileType) {
        if (fileType == null) return null;
        for (BasedataFileTypeEnum typeEnum : BasedataFileTypeEnum.values()) {
            if (typeEnum.getCode().equals(fileType)) {
                return typeEnum.getName();
            }
        }
        return fileType;
    }

}
