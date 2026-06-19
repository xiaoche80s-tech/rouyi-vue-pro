package cn.iocoder.yudao.module.opshub.service.basedata;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.opshub.controller.admin.basedata.vo.BasedataFilePageReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.basedata.BasedataFileDO;

/**
 * 基础数据文件 Service 接口
 */
public interface BasedataFileService {

    /**
     * 获得基础数据文件分页
     */
    PageResult<BasedataFileDO> getBasedataFilePage(BasedataFilePageReqVO pageReqVO);

    /**
     * 获得基础数据文件详情
     */
    BasedataFileDO getBasedataFile(Long id);

    /**
     * 获得文件预签名下载 URL
     *
     * @param id 文件ID
     * @return 预签名下载 URL
     */
    String getDownloadUrl(Long id);

    /**
     * 获得各分类文件数量
     *
     * @return 分类 -> 数量
     */
    java.util.Map<String, Long> getCategoryCount();

    /**
     * 获取合同附件列表
     *
     * @param dealerCode   经销商编码
     * @param contractCode 合同编码
     * @return 附件文件列表
     */
    java.util.List<BasedataFileDO> getContractAttachments(String dealerCode, String contractCode);

    /**
     * 批量查询合同类附件（用于统计 attachmentCount）
     *
     * @param dealerCodes 经销商编码集合
     * @return category='contract' 的文件列表
     */
    java.util.List<BasedataFileDO> getContractFilesByDealerCodes(java.util.Collection<String> dealerCodes);

}
