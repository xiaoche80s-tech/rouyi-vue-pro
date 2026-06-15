package cn.iocoder.yudao.module.opshub.controller.admin.basedata;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.opshub.controller.admin.basedata.vo.BasedataFilePageReqVO;
import cn.iocoder.yudao.module.opshub.controller.admin.basedata.vo.BasedataFileRespVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.basedata.BasedataFileDO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.DealerInfoDO;
import cn.iocoder.yudao.module.opshub.enums.BasedataFileTypeEnum;
import cn.iocoder.yudao.module.opshub.service.basedata.BasedataFileService;
import cn.iocoder.yudao.module.opshub.service.basedata.impl.BasedataFileServiceImpl;
import cn.iocoder.yudao.module.opshub.service.dealer.DealerInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 基础数据")
@RestController
@RequestMapping("/opshub/basedata")
@Validated
public class BasedataFileController {

    @Resource
    private BasedataFileService basedataFileService;

    @Resource
    private DealerInfoService dealerInfoService;

    @GetMapping("/page")
    @Operation(summary = "获得基础数据文件分页")
    @PreAuthorize("@ss.hasPermission('dealer:basedata:query')")
    public CommonResult<PageResult<BasedataFileRespVO>> getBasedataFilePage(@Valid BasedataFilePageReqVO pageReqVO) {
        PageResult<BasedataFileDO> pageResult = basedataFileService.getBasedataFilePage(pageReqVO);
        // 转换 VO
        PageResult<BasedataFileRespVO> voPageResult = BeanUtils.toBean(pageResult, BasedataFileRespVO.class);
        // 填充额外字段
        fillRespVOList(voPageResult.getList());
        return success(voPageResult);
    }

    @GetMapping("/get")
    @Operation(summary = "获得基础数据文件详情")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:basedata:query')")
    public CommonResult<BasedataFileRespVO> getBasedataFile(@RequestParam("id") Long id) {
        BasedataFileDO file = basedataFileService.getBasedataFile(id);
        BasedataFileRespVO vo = BeanUtils.toBean(file, BasedataFileRespVO.class);
        if (vo != null) {
            fillRespVO(vo, file);
        }
        return success(vo);
    }

    @GetMapping("/download-url")
    @Operation(summary = "获得文件预签名下载 URL")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:basedata:download')")
    public CommonResult<String> getDownloadUrl(@RequestParam("id") Long id) {
        return success(basedataFileService.getDownloadUrl(id));
    }

    @GetMapping("/category-count")
    @Operation(summary = "获得各分类文件数量")
    @PreAuthorize("@ss.hasPermission('dealer:basedata:query')")
    public CommonResult<Map<String, Long>> getCategoryCount() {
        return success(basedataFileService.getCategoryCount());
    }

    // ========== 辅助方法 ==========

    /**
     * 填充响应 VO 列表的额外字段
     */
    private void fillRespVOList(List<BasedataFileRespVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        // 批量获取经销商名称
        Set<Long> dealerIds = list.stream()
                .map(BasedataFileRespVO::getDealerId)
                .collect(Collectors.toSet());
        Map<Long, String> dealerNameMap = dealerIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> {
                            DealerInfoDO dealer = dealerInfoService.getDealer(id);
                            return dealer != null ? dealer.getDealerName() : "";
                        }
                ));

        for (BasedataFileRespVO vo : list) {
            // 填充经销商名称
            vo.setDealerName(dealerNameMap.getOrDefault(vo.getDealerId(), ""));
            // 填充文件子类型名称
            vo.setFileTypeName(getFileTypeName(vo.getFileType()));
            // 计算有效期状态
            vo.setExpireStatus(BasedataFileServiceImpl.calculateExpireStatus(vo.getExpireDate()));
        }
    }

    /**
     * 填充单个响应 VO 的额外字段
     */
    private void fillRespVO(BasedataFileRespVO vo, BasedataFileDO file) {
        // 填充经销商名称
        DealerInfoDO dealer = dealerInfoService.getDealer(file.getDealerId());
        if (dealer != null) {
            vo.setDealerName(dealer.getDealerName());
        }
        // 填充文件子类型名称
        vo.setFileTypeName(getFileTypeName(file.getFileType()));
        // 计算有效期状态
        vo.setExpireStatus(BasedataFileServiceImpl.calculateExpireStatus(file.getExpireDate()));
    }

    /**
     * 获取文件子类型名称
     */
    private String getFileTypeName(String fileType) {
        if (fileType == null) {
            return null;
        }
        for (BasedataFileTypeEnum typeEnum : BasedataFileTypeEnum.values()) {
            if (typeEnum.getCode().equals(fileType)) {
                return typeEnum.getName();
            }
        }
        return fileType;
    }

}
