package com.example.order.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 企业微信机器人告警服务。
 *
 * 原理：企业微信群机器人本质上就是一个 Webhook URL，
 * POST 一段 JSON 过去，机器人就会在群里发消息。
 *
 * 不需要引入企业微信 SDK，直接 HTTP 调用即可。
 */
@Service
public class WeComAlertService {

    private static final Logger log = LoggerFactory.getLogger(WeComAlertService.class);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Value("${wecom.webhook.url:}")
    private String webhookUrl;

    /**
     * 发送 Markdown 格式的告警消息到企业微信群。
     *
     * @param title   告警标题
     * @param content 告警详情（支持 Markdown）
     */
    public void sendAlert(String title, String content) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("企业微信 Webhook URL 未配置，跳过告警发送。title={}", title);
            return;
        }

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String body = String.format("""
                {
                    "msgtype": "markdown",
                    "markdown": {
                        "content": "## %s\\n> 时间：%s\\n> 详情：%s"
                    }
                }
                """, title, timestamp, content);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            log.info("企业微信告警发送完成。status={}, body={}", response.statusCode(), response.body());
        } catch (Exception e) {
            log.error("企业微信告警发送失败: {}", e.getMessage(), e);
        }
    }
}
