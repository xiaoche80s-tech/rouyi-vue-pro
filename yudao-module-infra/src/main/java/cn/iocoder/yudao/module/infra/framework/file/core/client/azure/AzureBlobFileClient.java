package cn.iocoder.yudao.module.infra.framework.file.core.client.azure;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.module.infra.framework.file.core.client.AbstractFileClient;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.sas.SasProtocol;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * 基于 Azure Blob Storage 的文件客户端实现
 *
 * 参见：<a href="https://learn.microsoft.com/en-us/azure/storage/blobs/storage-blob-upload-java">Upload a blob with Java</a>
 *
 * @author 芋道源码
 */
public class AzureBlobFileClient extends AbstractFileClient<AzureBlobFileClientConfig> {

    private static final Duration EXPIRATION_DEFAULT = Duration.ofHours(24);

    private BlobServiceClient blobServiceClient;
    private BlobContainerClient containerClient;
    private StorageSharedKeyCredential credential;

    public AzureBlobFileClient(Long id, AzureBlobFileClientConfig config) {
        super(id, config);
    }

    @Override
    protected void doInit() {
        // 补全 domain
        if (StrUtil.isEmpty(config.getDomain())) {
            config.setDomain(buildDomain());
        }
        // 初始化 Azure 凭证
        credential = new StorageSharedKeyCredential(config.getAccountName(), config.getAccountKey());
        // 初始化 BlobServiceClient
        String endpoint = config.getEndpoint();
        blobServiceClient = new BlobServiceClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildClient();
        // 初始化 BlobContainerClient
        containerClient = blobServiceClient.getBlobContainerClient(config.getContainer());
    }

    @Override
    public String upload(byte[] content, String path, String type) {
        BlobClient blobClient = containerClient.getBlobClient(path);
        // 上传文件（overwrite = true）
        blobClient.upload(BinaryData.fromBytes(content), true);
        // 设置 Content-Type
        blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType(type));
        // 返回访问 URL（通过 presignGetUrl 统一处理公开/私有）
        return presignGetUrl(path, null);
    }

    @Override
    public void delete(String path) {
        containerClient.getBlobClient(path).delete();
    }

    @Override
    public byte[] getContent(String path) {
        return IoUtil.readBytes(containerClient.getBlobClient(path).openInputStream());
    }

    @Override
    public String presignPutUrl(String path) {
        OffsetDateTime expiryTime = OffsetDateTime.now().plus(EXPIRATION_DEFAULT);
        BlobContainerSasPermission permission = new BlobContainerSasPermission()
                .setCreatePermission(true)
                .setWritePermission(true);
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, permission)
                .setProtocol(SasProtocol.HTTPS_ONLY);
        String sasToken = containerClient.generateSas(sasValues);
        return config.getDomain() + "/" + HttpUtils.encodeUrlPath(path) + "?" + sasToken;
    }

    @Override
    public String presignGetUrl(String url, Integer expirationSeconds) {
        // 1. 将 url 转换为 path
        boolean domainUrl = StrUtil.startWith(url, config.getDomain() + "/");
        String path = domainUrl ? StrUtil.removePrefix(url, config.getDomain() + "/") : url;
        if (domainUrl) {
            path = HttpUtils.removeUrlPathQueryAndFragment(path);
            path = HttpUtils.decodeUrlPath(path);
        }

        // 2.1 情况一：公开访问：无需签名
        if (!BooleanUtil.isFalse(config.getEnablePublicAccess())) {
            return config.getDomain() + "/" + HttpUtils.encodeUrlPath(path);
        }

        // 2.2 情况二：私有访问：生成 GET SAS 签名 URL
        Duration expiration = expirationSeconds != null
                ? Duration.ofSeconds(expirationSeconds) : EXPIRATION_DEFAULT;
        OffsetDateTime expiryTime = OffsetDateTime.now().plus(expiration);
        BlobContainerSasPermission permission = new BlobContainerSasPermission()
                .setReadPermission(true);
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, permission)
                .setProtocol(SasProtocol.HTTPS_ONLY);
        String sasToken = containerClient.generateSas(sasValues);
        return config.getDomain() + "/" + HttpUtils.encodeUrlPath(path) + "?" + sasToken;
    }

    /**
     * 基于 endpoint + container 构建默认的访问 Domain 地址
     *
     * @return Domain 地址，格式：https://<accountName>.blob.core.windows.net/<container>
     */
    private String buildDomain() {
        String endpoint = config.getEndpoint();
        // 移除末尾的斜杠
        endpoint = StrUtil.removeSuffix(endpoint, "/");
        return StrUtil.format("{}/{}", endpoint, config.getContainer());
    }

}
