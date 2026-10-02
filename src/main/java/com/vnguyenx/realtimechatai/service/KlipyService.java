package com.vnguyenx.realtimechatai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class KlipyService {

    @Value("${klipy.api.key}")
    private String apiKey;

    private final RestClient restClient = RestClient.create("https://api.klipy.com");

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> searchStickers(String query, int page, String customerId) {
        Map<?, ?> response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/{apiKey}/stickers/search")
                        .queryParam("q", query)
                        .queryParam("page", page)
                        .queryParam("customer_id", customerId)
                        .build(apiKey))
                .retrieve()
                .body(Map.class);

        try {
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("data");

            return items.stream().map(item -> {
                Map<String, Object> file = (Map<String, Object>) item.get("file");
                Map<String, Object> hd = (Map<String, Object>) file.get("hd");
                Map<String, Object> webp = (Map<String, Object>) hd.get("webp");

                return Map.<String, Object>of(
                        "id", item.get("id"),
                        "title", item.get("title"),
                        "url", webp.get("url")
                );
            }).toList();

        } catch (ClassCastException | NullPointerException e) {
            throw new IllegalStateException("Không đọc được dữ liệu sticker từ Klipy");
        }
    }
}