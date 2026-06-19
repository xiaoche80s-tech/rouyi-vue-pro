package cn.iocoder.yudao.module.infra.framework.file.core.client.azure;

import cn.iocoder.yudao.module.infra.framework.file.core.client.FileClientConfig;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotNull;

/**
 * Azure Blob Storage 文件客户端的配置类
 *
 * @author 芋道源码
 */
@Data
public class AzureBlobFileClientConfig implements FileClientConfig {

    public static final String ENDPOINT_SUFFIX = "blob.core.windows.net";

    /**
     * 服务端点
     * 格式：https://<accountName>.blob.core.windows.net
     * 参见：https://learn.microsoft.com/en-us/azure/storage/blobs/storage-blob-upload-java
     */
    @NotNull(message = "endpoint 不能为空")
    private String endpoint;

    /**
     * 存储账户名
     * 参见：https://learn.microsoft.com/en-us/azure/storage/common/storage-account-overview
     */
    @NotNull(message = "accountName 不能为空")
    private String accountName;

    /**
     * 存储账户密钥
     * 参见：https://learn.microsoft.com/en-us/azure/storage/common/storage-account-keys-manage
     */
    @NotNull(message = "accountKey 不能为空")
    private String accountKey;

    /**
     * Blob 容器名称（等价于 S3 的 bucket）
     * 参见：https://learn.microsoft.com/en-us/azure/storage/blobs/storage-blobs-introduction
     */
    @NotNull(message = "container 不能为空")
    private String container;

    /**
     * 自定义域名（可选）
     * 适用于绑定 Azure CDN 或自定义域名的场景
     * 参见：https://learn.microsoft.com/en-us/azure/storage/blobs/storage-custom-domain-name
     */
    @URL(message = "domain 必须是 URL 格式")
    private String domain;

    /**
     * 是否公开访问
     *
     * true：公开访问，所有人都可以直接读取（容器公开读取级别）
     * false：私有访问，需要通过 SAS 签名才能访问
     */
    @NotNull(message = "是否公开访问不能为空")
    private Boolean enablePublicAccess;

}
