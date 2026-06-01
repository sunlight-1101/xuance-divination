package com.zhexuan.divination.service.impl;

import com.zhexuan.divination.dto.ZiweiChartDTO;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZiweiChartServiceImplTest {

    private final ZiweiChartServiceImpl service = new ZiweiChartServiceImpl();

    @Test
    @SuppressWarnings("unchecked")
    void buildsKnownSampleChartFromExternalReference() {
        ZiweiChartDTO dto = new ZiweiChartDTO();
        dto.setGender("男");
        dto.setCalendarType("SOLAR");
        dto.setBirthDate("2004-11-01");
        dto.setBirthTime("00:30");
        dto.setBirthPlace("沧州");

        Map<String, Object> chart = service.buildChart(dto);
        List<Map<String, Object>> palaces = (List<Map<String, Object>>) chart.get("palaces");

        assertEquals("甲申", chart.get("yearGanZhi"));
        assertEquals("子", chart.get("hourBranch"));
        assertEquals("戌", chart.get("mingGong"));
        assertEquals("戌", chart.get("shenGong"));
        assertEquals("火六局", chart.get("fiveElementBureau"));
        assertEquals("辰", chart.get("douJun"));

        assertMainStars(palace(palaces, "戌"), "七杀");
        assertMainStars(palace(palaces, "酉"), "太阳", "天梁");
        assertMainStars(palace(palaces, "申"), "武曲", "天相");
        assertMainStars(palace(palaces, "未"), "天同", "巨门");
        assertMainStars(palace(palaces, "午"), "贪狼");
        assertMainStars(palace(palaces, "巳"), "太阴");
        assertMainStars(palace(palaces, "辰"), "廉贞", "天府");
        assertMainStars(palace(palaces, "寅"), "破军");
        assertMainStars(palace(palaces, "子"), "紫微");
        assertMainStars(palace(palaces, "亥"), "天机");

        Map<String, Object> ming = palace(palaces, "戌");
        Map<String, Object> luck = (Map<String, Object>) ming.get("majorLuck");
        assertEquals(6, luck.get("startAge"));
        assertEquals(15, luck.get("endAge"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void ziweiPlacementUsesBureauRuleInsteadOfLinearApproximation() {
        ZiweiChartDTO dto = new ZiweiChartDTO();
        dto.setGender("男");
        dto.setCalendarType("LUNAR");
        dto.setBirthDate("2004-09-01");
        dto.setBirthTime("00:30");

        Map<String, Object> chart = service.buildChart(dto);
        List<Map<String, Object>> palaces = (List<Map<String, Object>>) chart.get("palaces");

        assertMainStars(palace(palaces, "酉"), "紫微");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> palace(List<Map<String, Object>> palaces, String branch) {
        for (Map<String, Object> palace : palaces) {
            if (branch.equals(palace.get("branch"))) {
                return palace;
            }
        }
        throw new AssertionError("Missing palace " + branch);
    }

    @SuppressWarnings("unchecked")
    private void assertMainStars(Map<String, Object> palace, String... expectedStars) {
        List<String> stars = (List<String>) palace.get("mainStars");
        for (String expectedStar : expectedStars) {
            assertTrue(stars.contains(expectedStar), palace.get("branch") + " missing " + expectedStar + " in " + stars);
        }
    }
}
