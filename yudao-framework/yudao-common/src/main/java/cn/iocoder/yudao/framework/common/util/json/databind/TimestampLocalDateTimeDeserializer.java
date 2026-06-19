package cn.iocoder.yudao.framework.common.util.json.databind;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 基于时间戳的 LocalDateTime 反序列化器
 * <p>
 * 同时支持两种输入格式：
 * <ul>
 *   <li>数值型时间戳（long 毫秒）：如 {@code 1781867256022}</li>
 *   <li>字符串日期：如 {@code "2026-06-19 10:00:00"} 或 ISO 格式 {@code "2026-06-19T10:00:00"}</li>
 * </ul>
 *
 * @author 老五
 */
public class TimestampLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    public static final TimestampLocalDateTimeDeserializer INSTANCE = new TimestampLocalDateTimeDeserializer();

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        // 情况一：数值型时间戳（long 毫秒）
        if (p.currentToken() == JsonToken.VALUE_NUMBER_INT) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(p.getValueAsLong()), ZoneId.systemDefault());
        }
        // 情况二：字符串日期（前端 el-date-picker 发送 "YYYY-MM-DD HH:mm:ss"）
        if (p.currentToken() == JsonToken.VALUE_STRING) {
            String text = p.getText().trim();
            if (text.isEmpty()) {
                return null;
            }
            try {
                return LocalDateTime.parse(text, DATE_TIME_FORMATTER);
            } catch (Exception e) {
                // ISO 格式兜底（如 "2026-06-19T10:00:00"）
                return LocalDateTime.parse(text);
            }
        }
        return null;
    }

}
