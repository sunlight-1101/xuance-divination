package com.zhexuan.divination.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zhexuan.divination.common.BizException;
import com.zhexuan.divination.service.AlmanacService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AlmanacServiceImpl implements AlmanacService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Cache<String, Map<String, Object>> cache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(24, TimeUnit.HOURS)
            .build();

    @Value("${almanac.enabled:true}")
    private boolean enabled;

    @Value("${almanac.base-url:https://cn.apihz.cn/api/time/getzdday.php}")
    private String baseUrl;

    @Value("${almanac.id:88888888}")
    private String appId;

    @Value("${almanac.key:88888888}")
    private String appKey;

    @Override
    public Map<String, Object> getDay(String date) {
        if (!enabled) {
            throw new BizException("万年历接口暂未启用");
        }
        LocalDate day;
        try {
            day = LocalDate.parse(date);
        } catch (Exception e) {
            throw new BizException("日期格式应为 YYYY-MM-DD");
        }

        Map<String, Object> cached = cache.getIfPresent(date);
        if (cached != null) {
            return cached;
        }

        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("id", appId)
                .queryParam("key", appKey)
                .queryParam("nian", day.getYear())
                .queryParam("yue", day.getMonthValue())
                .queryParam("ri", day.getDayOfMonth())
                .toUriString();
        try {
            String body = restTemplate.getForObject(url, String.class);
            if (body == null || body.trim().isEmpty()) {
                return fallback(date, "万年历接口返回为空");
            }
            Map<String, Object> raw = objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
            Object code = raw.get("code");
            if (code != null && !"200".equals(String.valueOf(code))) {
                return fallback(date, String.valueOf(raw.getOrDefault("msg", "万年历接口调用失败")));
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("available", true);
            result.put("source", "apihz");
            result.put("date", date);
            result.put("raw", raw);
            result.put("solar", raw.get("ynian") + "-" + raw.get("yyue") + "-" + raw.get("yri"));
            result.put("week", raw.get("xingqi"));
            result.put("lunar", String.valueOf(raw.getOrDefault("nyue", "")) + String.valueOf(raw.getOrDefault("nri", "")));
            result.put("festival", raw.getOrDefault("jieri", ""));
            result.put("lunarFestival", raw.getOrDefault("YIFESTIVAL", ""));
            result.put("yearGanzhi", raw.getOrDefault("ganzhinian", raw.get("YEARGANZHI")));
            result.put("monthGanzhi", raw.getOrDefault("ganzhiyue", raw.get("MONTHINGANZHIEXACT")));
            result.put("dayGanzhi", raw.getOrDefault("ganzhiri", raw.get("DAYINGANZHIEXACT")));
            result.put("jieqi", raw.getOrDefault("jieqimsg", raw.get("JIEQICN")));
            result.put("wuhou", raw.getOrDefault("WUHOU", ""));
            result.put("yi", raw.getOrDefault("yi", ""));
            result.put("ji", raw.getOrDefault("ji", ""));
            result.put("chong", raw.getOrDefault("xiangchong", raw.get("DAYCHONGDESC")));
            result.put("pengzu", raw.getOrDefault("pengzu", ""));
            result.put("tianShen", raw.getOrDefault("DAYTIANSHEN", ""));
            result.put("tianShenType", raw.getOrDefault("DAYTIANSHENTYPE", ""));
            result.put("xiu", raw.getOrDefault("xingxiu", raw.get("XIU")));
            result.put("xiuLuck", raw.getOrDefault("XIULUCK", ""));
            result.put("xiuSong", raw.getOrDefault("XIUSONG", ""));
            result.put("xunKong", raw.getOrDefault("DAYXUNKONG", ""));
            result.put("naYin", raw.getOrDefault("DAYNAYIN", ""));
            cache.put(date, result);
            return result;
        } catch (Exception e) {
            return fallback(date, "万年历接口暂不可用：" + e.getMessage());
        }
    }

    private Map<String, Object> fallback(String date, String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("available", false);
        result.put("source", "local-fallback");
        result.put("date", date);
        result.put("message", message);
        return result;
    }
}
