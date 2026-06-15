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

}
