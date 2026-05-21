package com.xuance.divination.service.impl;

import com.xuance.divination.common.BizException;
import com.xuance.divination.dto.ZiweiChartDTO;
import com.xuance.divination.service.ZiweiChartService;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ZiweiChartServiceImpl implements ZiweiChartService {
    private static final String[] STEMS = {"甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"};
    private static final String[] BRANCHES = {"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};
    private static final String[] PALACE_BRANCHES = {"寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥", "子", "丑"};
    private static final String[] PALACE_NAMES = {"命宫", "兄弟", "夫妻", "子女", "财帛", "疾厄", "迁移", "交友", "官禄", "田宅", "福德", "父母"};
    private static final String[] ZIWEI_GROUP = {"紫微", "天机", "", "太阳", "武曲", "天同", "", "", "廉贞"};
    private static final String[] TIANFU_GROUP = {"天府", "太阴", "贪狼", "巨门", "天相", "天梁", "七杀", "", "", "", "破军"};
    private static final String[] SIX_LUCKY = {"左辅", "右弼", "文昌", "文曲", "天魁", "天钺"};
    private static final String[] SIX_SHA = {"擎羊", "陀罗", "火星", "铃星", "地空", "地劫"};
    private static final int[] LUNAR_INFO = {
            0x04bd8,0x04ae0,0x0a570,0x054d5,0x0d260,0x0d950,0x16554,0x056a0,0x09ad0,0x055d2,
            0x04ae0,0x0a5b6,0x0a4d0,0x0d250,0x1d255,0x0b540,0x0d6a0,0x0ada2,0x095b0,0x14977,
            0x04970,0x0a4b0,0x0b4b5,0x06a50,0x06d40,0x1ab54,0x02b60,0x09570,0x052f2,0x04970,
            0x06566,0x0d4a0,0x0ea50,0x06e95,0x05ad0,0x02b60,0x186e3,0x092e0,0x1c8d7,0x0c950,
            0x0d4a0,0x1d8a6,0x0b550,0x056a0,0x1a5b4,0x025d0,0x092d0,0x0d2b2,0x0a950,0x0b557,
            0x06ca0,0x0b550,0x15355,0x04da0,0x0a5d0,0x14573,0x052d0,0x0a9a8,0x0e950,0x06aa0,
            0x0aea6,0x0ab50,0x04b60,0x0aae4,0x0a570,0x05260,0x0f263,0x0d950,0x05b57,0x056a0,
            0x096d0,0x04dd5,0x04ad0,0x0a4d0,0x0d4d4,0x0d250,0x0d558,0x0b540,0x0b6a0,0x195a6,
            0x095b0,0x049b0,0x0a974,0x0a4b0,0x0b27a,0x06a50,0x06d40,0x0af46,0x0ab60,0x09570,
            0x04af5,0x04970,0x064b0,0x074a3,0x0ea50,0x06b58,0x055c0,0x0ab60,0x096d5,0x092e0,
            0x0c960,0x0d954,0x0d4a0,0x0da50,0x07552,0x056a0,0x0abb7,0x025d0,0x092d0,0x0cab5,
            0x0a950,0x0b4a0,0x0baa4,0x0ad50,0x055d9,0x04ba0,0x0a5b0,0x15176,0x052b0,0x0a930,
            0x07954,0x06aa0,0x0ad50,0x05b52,0x04b60,0x0a6e6,0x0a4e0,0x0d260,0x0ea65,0x0d530,
            0x05aa0,0x076a3,0x096d0,0x04bd7,0x04ad0,0x0a4d0,0x1d0b6,0x0d250,0x0d520,0x0dd45,
            0x0b5a0,0x056d0,0x055b2,0x049b0,0x0a577,0x0a4b0,0x0aa50,0x1b255,0x06d20,0x0ada0,
            0x14b63,0x09370,0x049f8,0x04970,0x064b0,0x168a6,0x0ea50,0x06b20,0x1a6c4,0x0aae0,
            0x0a2e0,0x0d2e3,0x0c960,0x0d557,0x0d4a0,0x0da50,0x05d55,0x056a0,0x0a6d0,0x055d4,
            0x052d0,0x0a9b8,0x0a950,0x0b4a0,0x0b6a6,0x0ad50,0x055a0,0x0aba4,0x0a5b0,0x052b0,
            0x0b273,0x06930,0x07337,0x06aa0,0x0ad50,0x14b55,0x04b60,0x0a570,0x054e4,0x0d160,
            0x0e968,0x0d520,0x0daa0,0x16aa6,0x056d0,0x04ae0,0x0a9d4,0x0a2d0,0x0d150,0x0f252,
            0x0d520
    };

    @Override
    public Map<String, Object> buildChart(ZiweiChartDTO dto) {
        if (!StringUtils.hasText(dto.getBirthDate())) {
            throw new BizException("birthDate is required");
        }
        LocalDate solarDate = LocalDate.parse(dto.getBirthDate());
        LunarDate lunar = "LUNAR".equalsIgnoreCase(dto.getCalendarType())
                ? new LunarDate(solarDate.getYear(), solarDate.getMonthValue(), solarDate.getDayOfMonth(), Boolean.TRUE.equals(dto.getLeapMonth()))
                : solarToLunar(solarDate);
        int hourIndex = hourBranchIndex(dto.getBirthTime());
        String yearStem = STEMS[Math.floorMod(lunar.year - 4, 10)];
        String yearBranch = BRANCHES[Math.floorMod(lunar.year - 4, 12)];
        int mingIndex = Math.floorMod((lunar.month - 1) - hourIndex, 12);
        int shenIndex = Math.floorMod((lunar.month - 1) + hourIndex, 12);
        String[] palaceStems = palaceStems(yearStem);
        String bureau = bureauFromNaYin(palaceStems[mingIndex] + PALACE_BRANCHES[mingIndex]);
        int bureauNumber = bureauNumber(bureau);
        int ziweiIndex = ziweiIndex(lunar.day, bureauNumber);
        int tianfuIndex = Math.floorMod(12 - ziweiIndex, 12);

        List<Map<String, Object>> palaces = buildPalaces(mingIndex, shenIndex, palaceStems);
        addStars(palaces, ziweiIndex, ZIWEI_GROUP, true);
        addStars(palaces, tianfuIndex, TIANFU_GROUP, false);
        addAuxiliaryStars(palaces, lunar.month, hourIndex, yearStem, yearBranch);
        addCommonMinorStars(palaces, lunar.month, hourIndex, yearBranch);
        Map<String, String> transformations = addTransformations(palaces, yearStem);
        addLuckRanges(palaces, mingIndex, dto.getGender(), yearStem, lunar.year, bureauNumber);
        addLifeStages(palaces, bureau);
        addDoctorStars(palaces, yearStem, dto.getGender());
        addYearGods(palaces, yearBranch);
        addGeneralGods(palaces, yearBranch);
        addYearDecorativeStars(palaces, yearBranch);
        addStemDecorativeStars(palaces, yearStem, yearBranch);
        addVoidStars(palaces, yearStem, yearBranch);
        addMonthlyVirtueStars(palaces, lunar.month);
        addDayStars(palaces, lunar.day, lunar.month);
        addHourDecorativeStars(palaces, hourIndex);
        String lifeMaster = lifeMaster(PALACE_BRANCHES[mingIndex]);
        String bodyMaster = bodyMaster(yearBranch);
        String douJun = douJun(lunar.month, hourIndex);
        String sourcePalace = sourcePalace(yearStem);

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("input", mapOf(
                "gender", safe(dto.getGender()),
                "birthDate", dto.getBirthDate(),
                "birthTime", safe(dto.getBirthTime()),
                "birthProvince", safe(dto.getBirthProvince()),
                "birthPlace", safe(dto.getBirthPlace()),
                "calendarType", StringUtils.hasText(dto.getCalendarType()) ? dto.getCalendarType() : "SOLAR",
                "useTrueSolarTime", Boolean.TRUE.equals(dto.getUseTrueSolarTime())
        ));
        result.put("lunar", mapOf(
                "year", lunar.year,
                "month", lunar.month,
                "day", lunar.day,
                "leapMonth", lunar.leap,
                "display", lunar.year + "年" + (lunar.leap ? "闰" : "") + lunar.month + "月" + lunar.day + "日"
        ));
        result.put("yearGanZhi", yearStem + yearBranch);
        result.put("hourBranch", BRANCHES[hourIndex]);
        result.put("mingGong", PALACE_BRANCHES[mingIndex]);
        result.put("shenGong", PALACE_BRANCHES[shenIndex]);
        result.put("lifeMaster", lifeMaster);
        result.put("bodyMaster", bodyMaster);
        result.put("douJun", douJun);
        result.put("sourcePalace", sourcePalace);
        result.put("fiveElementBureau", bureau);
        result.put("transformations", transformations);
        result.put("palaces", palaces);
        result.put("notes", Arrays.asList(
                "第一阶段为基础排盘，重点完成命身宫、十二宫、十四主星、六吉六煞、四化与大限结构。",
                "农历转换支持 1900-2100 年；后续可继续校验闰月、起大限细节和各派小星。"
        ));
        return result;
    }

    private List<Map<String, Object>> buildPalaces(int mingIndex, int shenIndex, String[] palaceStems) {
        List<Map<String, Object>> palaces = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < 12; i++) {
            int nameIndex = Math.floorMod(mingIndex - i, 12);
            Map<String, Object> palace = new LinkedHashMap<String, Object>();
            palace.put("index", i);
            palace.put("branch", PALACE_BRANCHES[i]);
            palace.put("stem", palaceStems[i]);
            palace.put("ganZhi", palaceStems[i] + PALACE_BRANCHES[i]);
            palace.put("name", PALACE_NAMES[nameIndex]);
            palace.put("isMing", i == mingIndex);
            palace.put("isShen", i == shenIndex);
            palace.put("mainStars", new ArrayList<String>());
            palace.put("luckyStars", new ArrayList<String>());
            palace.put("shaStars", new ArrayList<String>());
            palace.put("otherStars", new ArrayList<String>());
            palace.put("transformations", new ArrayList<String>());
            palace.put("lifeStage", "");
            palace.put("doctorStar", "");
            palace.put("yearGod", "");
            palace.put("generalGod", "");
            palaces.add(palace);
        }
        return palaces;
    }

    @SuppressWarnings("unchecked")
    private void addStars(List<Map<String, Object>> palaces, int startIndex, String[] group, boolean clockwise) {
        for (int i = 0; i < group.length; i++) {
            if (!StringUtils.hasText(group[i])) {
                continue;
            }
            int index = clockwise ? Math.floorMod(startIndex - i, 12) : Math.floorMod(startIndex + i, 12);
            ((List<String>) palaces.get(index).get("mainStars")).add(group[i]);
        }
    }

    @SuppressWarnings("unchecked")
    private void addAuxiliaryStars(List<Map<String, Object>> palaces, int lunarMonth, int hourIndex, String yearStem, String yearBranch) {
        int left = Math.floorMod(lunarMonth + 2, 12);
        int right = Math.floorMod(4 - lunarMonth, 12);
        int wenChang = Math.floorMod(10 - hourIndex, 12);
        int wenQu = Math.floorMod(hourIndex + 4, 12);
        int[] kuiYue = kuiYueByStem(yearStem);
        int[] yangTuo = yangTuoByStem(yearStem);
        int fire = Math.floorMod(branchIndex(yearBranch) + hourIndex + 2, 12);
        int ling = Math.floorMod(branchIndex(yearBranch) - hourIndex + 8, 12);
        int skyEmpty = Math.floorMod(hourIndex + 10, 12);
        int earthEmpty = Math.floorMod(10 - hourIndex, 12);

        addTo(palaces, left, "luckyStars", SIX_LUCKY[0]);
        addTo(palaces, right, "luckyStars", SIX_LUCKY[1]);
        addTo(palaces, wenChang, "luckyStars", SIX_LUCKY[2]);
        addTo(palaces, wenQu, "luckyStars", SIX_LUCKY[3]);
        addTo(palaces, kuiYue[0], "luckyStars", SIX_LUCKY[4]);
        addTo(palaces, kuiYue[1], "luckyStars", SIX_LUCKY[5]);
        addTo(palaces, yangTuo[0], "shaStars", SIX_SHA[0]);
        addTo(palaces, yangTuo[1], "shaStars", SIX_SHA[1]);
        addTo(palaces, fire, "shaStars", SIX_SHA[2]);
        addTo(palaces, ling, "shaStars", SIX_SHA[3]);
        addTo(palaces, skyEmpty, "shaStars", SIX_SHA[4]);
        addTo(palaces, earthEmpty, "shaStars", SIX_SHA[5]);
    }

    private void addCommonMinorStars(List<Map<String, Object>> palaces, int lunarMonth, int hourIndex, String yearBranch) {
        int year = branchIndex(yearBranch);
        addTo(palaces, Math.floorMod(year + 4, 12), "otherStars", "天马");
        addTo(palaces, Math.floorMod(year + 8, 12), "otherStars", "华盖");
        addTo(palaces, Math.floorMod(year + 2, 12), "otherStars", "天哭");
        addTo(palaces, Math.floorMod(10 - year, 12), "otherStars", "天虚");
        addTo(palaces, Math.floorMod(lunarMonth + 1, 12), "otherStars", "天刑");
        addTo(palaces, Math.floorMod(7 - lunarMonth, 12), "otherStars", "天姚");
        addTo(palaces, Math.floorMod(lunarMonth + 3, 12), "otherStars", "解神");
        addTo(palaces, Math.floorMod(lunarMonth + 5, 12), "otherStars", "天巫");
        addTo(palaces, Math.floorMod(hourIndex + 2, 12), "otherStars", "台辅");
        addTo(palaces, Math.floorMod(8 - hourIndex, 12), "otherStars", "封诰");
        addTo(palaces, Math.floorMod(year + lunarMonth, 12), "otherStars", "天月");
        addTo(palaces, Math.floorMod(year - lunarMonth, 12), "otherStars", "阴煞");
    }

    @SuppressWarnings("unchecked")
    private void addTo(List<Map<String, Object>> palaces, int index, String key, String star) {
        ((List<String>) palaces.get(Math.floorMod(index, 12)).get(key)).add(star);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> addTransformations(List<Map<String, Object>> palaces, String yearStem) {
        Map<String, String> table = fourHua(yearStem);
        for (Map.Entry<String, String> entry : table.entrySet()) {
            for (Map<String, Object> palace : palaces) {
                List<String> stars = new ArrayList<String>();
                stars.addAll((List<String>) palace.get("mainStars"));
                stars.addAll((List<String>) palace.get("luckyStars"));
                stars.addAll((List<String>) palace.get("otherStars"));
                if (stars.contains(entry.getKey())) {
                    ((List<String>) palace.get("transformations")).add(entry.getKey() + entry.getValue());
                }
            }
        }
        return table;
    }

    private void addLuckRanges(List<Map<String, Object>> palaces, int mingIndex, String gender, String yearStem, int lunarYear, int startAgeBase) {
        boolean yangYear = Arrays.asList("甲", "丙", "戊", "庚", "壬").contains(yearStem);
        boolean male = "男".equals(gender);
        int direction = (male == yangYear) ? 1 : -1;
        for (int i = 0; i < 12; i++) {
            int palaceIndex = Math.floorMod(mingIndex + direction * i, 12);
            Map<String, Object> luck = new LinkedHashMap<String, Object>();
            int startAge = startAgeBase + i * 10;
            luck.put("startAge", startAge);
            luck.put("endAge", startAge + 9);
            luck.put("startYear", lunarYear + startAge - 1);
            luck.put("endYear", lunarYear + startAge + 8);
            palaces.get(palaceIndex).put("majorLuck", luck);
        }
    }

    private void addLifeStages(List<Map<String, Object>> palaces, String bureau) {
        String[] stages = {"长生", "沐浴", "冠带", "临官", "帝旺", "衰", "病", "死", "墓", "绝", "胎", "养"};
        int start = lifeStageStartIndex(bureau);
        for (int i = 0; i < stages.length; i++) {
            palaces.get(Math.floorMod(start + i, 12)).put("lifeStage", stages[i]);
        }
    }

    private int lifeStageStartIndex(String bureau) {
        if (bureau.startsWith("水") || bureau.startsWith("土")) return palaceBranchIndex("申");
        if (bureau.startsWith("木")) return palaceBranchIndex("亥");
        if (bureau.startsWith("金")) return palaceBranchIndex("巳");
        return palaceBranchIndex("寅");
    }

    private void addDoctorStars(List<Map<String, Object>> palaces, String yearStem, String gender) {
        String[] stars = {"博士", "力士", "青龙", "小耗", "将军", "奏书", "飞廉", "喜神", "病符", "大耗", "伏兵", "官府"};
        int start = luCunIndex(yearStem);
        boolean clockwise = isYangStem(yearStem) == isMale(gender);
        for (int i = 0; i < stars.length; i++) {
            int index = Math.floorMod(start + (clockwise ? i : -i), 12);
            palaces.get(index).put("doctorStar", stars[i]);
        }
    }

    private boolean isMale(String gender) {
        if (!StringUtils.hasText(gender)) {
            return true;
        }
        String value = gender.trim().toLowerCase();
        return value.contains("男") || value.contains("male") || value.equals("m") || value.equals("1");
    }

    private void addYearGods(List<Map<String, Object>> palaces, String yearBranch) {
        String[] stars = {
                "\u5c81\u5efa", "\u6666\u6c14", "\u4e27\u95e8", "\u8d2f\u7d22",
                "\u5b98\u7b26", "\u5c0f\u8017", "\u5927\u8017", "\u9f99\u5fb7",
                "\u767d\u864e", "\u5929\u5fb7", "\u540a\u5ba2", "\u75c5\u7b26"
        };
        int start = palaceBranchIndex(yearBranch);
        for (int i = 0; i < stars.length; i++) {
            palaces.get(Math.floorMod(start + i, 12)).put("yearGod", stars[i]);
        }
    }

    private void addGeneralGods(List<Map<String, Object>> palaces, String yearBranch) {
        String[] stars = {
                "\u5c06\u661f", "\u6500\u978d", "\u5c81\u9a7f", "\u606f\u795e",
                "\u534e\u76d6", "\u52ab\u715e", "\u707e\u715e", "\u5929\u715e",
                "\u6307\u80cc", "\u54b8\u6c60", "\u6708\u715e", "\u4ea1\u795e"
        };
        int start = generalStartIndex(yearBranch);
        for (int i = 0; i < stars.length; i++) {
            palaces.get(Math.floorMod(start + i, 12)).put("generalGod", stars[i]);
        }
    }

    private int generalStartIndex(String yearBranch) {
        int branch = branchIndex(yearBranch);
        if (branch == 0 || branch == 4 || branch == 8) {
            return palaceBranchIndex("\u5b50");
        }
        if (branch == 1 || branch == 5 || branch == 9) {
            return palaceBranchIndex("\u9149");
        }
        if (branch == 2 || branch == 6 || branch == 10) {
            return palaceBranchIndex("\u5348");
        }
        return palaceBranchIndex("\u536f");
    }

    private void addYearDecorativeStars(List<Map<String, Object>> palaces, String yearBranch) {
        int year = branchIndex(yearBranch);
        addTo(palaces, Math.floorMod(palaceBranchIndex("\u8fb0") + year, 12), "otherStars", "\u9f99\u6c60");
        addTo(palaces, Math.floorMod(palaceBranchIndex("\u620c") - year, 12), "otherStars", "\u51e4\u9601");
        addTo(palaces, Math.floorMod(palaceBranchIndex("\u536f") - year, 12), "otherStars", "\u7ea2\u9e3e");
        addTo(palaces, Math.floorMod(palaceBranchIndex("\u9149") - year, 12), "otherStars", "\u5929\u559c");
        addTo(palaces, Math.floorMod(palaceBranchIndex("\u7533") + year, 12), "otherStars", "\u5929\u624d");
        addTo(palaces, Math.floorMod(palaceBranchIndex("\u5bc5") + year, 12), "otherStars", "\u5929\u5bff");
        addLonelyStars(palaces, yearBranch);
    }

    private void addStemDecorativeStars(List<Map<String, Object>> palaces, String yearStem, String yearBranch) {
        addTo(palaces, luCunIndex(yearStem), "otherStars", "\u7984\u5b58");
        int[] guanFu = tianGuanTianFu(yearStem);
        addTo(palaces, guanFu[0], "otherStars", "\u5929\u5b98");
        addTo(palaces, guanFu[1], "otherStars", "\u5929\u798f");
        addTo(palaces, tianChuIndex(yearStem), "otherStars", "\u5929\u53a8");
        addTo(palaces, Math.floorMod(palaceBranchIndex(yearBranch) + 6, 12), "otherStars", "\u5929\u7a7a");
    }

    private int[] tianGuanTianFu(String stem) {
        if (sameStem(stem, "\u7532")) return new int[]{palaceBranchIndex("\u672a"), palaceBranchIndex("\u9149")};
        if (sameStem(stem, "\u4e59")) return new int[]{palaceBranchIndex("\u8fb0"), palaceBranchIndex("\u7533")};
        if (sameStem(stem, "\u4e19")) return new int[]{palaceBranchIndex("\u5df3"), palaceBranchIndex("\u5b50")};
        if (sameStem(stem, "\u4e01")) return new int[]{palaceBranchIndex("\u5bc5"), palaceBranchIndex("\u4ea5")};
        if (sameStem(stem, "\u620a")) return new int[]{palaceBranchIndex("\u536f"), palaceBranchIndex("\u536f")};
        if (sameStem(stem, "\u5df1")) return new int[]{palaceBranchIndex("\u9149"), palaceBranchIndex("\u5bc5")};
        if (sameStem(stem, "\u5e9a")) return new int[]{palaceBranchIndex("\u4ea5"), palaceBranchIndex("\u5348")};
        if (sameStem(stem, "\u8f9b")) return new int[]{palaceBranchIndex("\u7533"), palaceBranchIndex("\u5df3")};
        if (sameStem(stem, "\u58ec")) return new int[]{palaceBranchIndex("\u620c"), palaceBranchIndex("\u5348")};
        return new int[]{palaceBranchIndex("\u5348"), palaceBranchIndex("\u5df3")};
    }

    private int tianChuIndex(String stem) {
        if (sameStem(stem, "\u7532") || sameStem(stem, "\u4e59")) return palaceBranchIndex("\u5df3");
        if (sameStem(stem, "\u4e19")) return palaceBranchIndex("\u5b50");
        if (sameStem(stem, "\u4e01")) return palaceBranchIndex("\u5df3");
        if (sameStem(stem, "\u620a")) return palaceBranchIndex("\u5348");
        if (sameStem(stem, "\u5df1")) return palaceBranchIndex("\u7533");
        if (sameStem(stem, "\u5e9a")) return palaceBranchIndex("\u5bc5");
        if (sameStem(stem, "\u8f9b")) return palaceBranchIndex("\u5348");
        if (sameStem(stem, "\u58ec")) return palaceBranchIndex("\u9149");
        return palaceBranchIndex("\u4ea5");
    }

    private void addVoidStars(List<Map<String, Object>> palaces, String yearStem, String yearBranch) {
        int stem = stemIndex(yearStem);
        int branch = branchIndex(yearBranch);
        int xunStart = Math.floorMod(branch - stem, 12);
        int emptyOne = Math.floorMod(xunStart - 2, 12);
        int emptyTwo = Math.floorMod(xunStart - 1, 12);
        addTo(palaces, palaceIndexFromStandardBranch(emptyOne), "otherStars", "\u65ec\u7a7a");
        addTo(palaces, palaceIndexFromStandardBranch(emptyTwo), "otherStars", "\u65ec\u7a7a");
        addTo(palaces, Math.floorMod(palaceIndexFromStandardBranch(emptyOne) + 6, 12), "otherStars", "\u622a\u7a7a");
        addTo(palaces, Math.floorMod(palaceIndexFromStandardBranch(emptyTwo) + 6, 12), "otherStars", "\u622a\u7a7a");
    }

    private void addMonthlyVirtueStars(List<Map<String, Object>> palaces, int lunarMonth) {
        int month = Math.max(1, Math.min(12, lunarMonth));
        addTo(palaces, Math.floorMod(palaceBranchIndex("\u9149") + month - 1, 12), "otherStars", "\u5929\u5fb7");
        addTo(palaces, Math.floorMod(palaceBranchIndex("\u5df3") + month - 1, 12), "otherStars", "\u6708\u5fb7");
        addTo(palaces, Math.floorMod(palaceBranchIndex("\u620c") + month - 1, 12), "otherStars", "\u5929\u89e3");
        addTo(palaces, Math.floorMod(palaceBranchIndex("\u7533") - month + 1, 12), "otherStars", "\u5730\u89e3");
    }

    private void addLonelyStars(List<Map<String, Object>> palaces, String yearBranch) {
        int branch = branchIndex(yearBranch);
        if (branch == 2 || branch == 3 || branch == 4) {
            addTo(palaces, palaceBranchIndex("\u5df3"), "otherStars", "\u5b64\u8fb0");
            addTo(palaces, palaceBranchIndex("\u4e11"), "otherStars", "\u5be1\u5bbf");
        } else if (branch == 5 || branch == 6 || branch == 7) {
            addTo(palaces, palaceBranchIndex("\u7533"), "otherStars", "\u5b64\u8fb0");
            addTo(palaces, palaceBranchIndex("\u8fb0"), "otherStars", "\u5be1\u5bbf");
        } else if (branch == 8 || branch == 9 || branch == 10) {
            addTo(palaces, palaceBranchIndex("\u4ea5"), "otherStars", "\u5b64\u8fb0");
            addTo(palaces, palaceBranchIndex("\u672a"), "otherStars", "\u5be1\u5bbf");
        } else {
            addTo(palaces, palaceBranchIndex("\u5bc5"), "otherStars", "\u5b64\u8fb0");
            addTo(palaces, palaceBranchIndex("\u620c"), "otherStars", "\u5be1\u5bbf");
        }
    }

    private void addDayStars(List<Map<String, Object>> palaces, int lunarDay, int lunarMonth) {
        int dayOffset = Math.max(0, lunarDay - 1);
        int leftIndex = Math.floorMod(lunarMonth + 2, 12);
        int rightIndex = Math.floorMod(4 - lunarMonth, 12);
        int wenChangIndex = findStarIndex(palaces, "\u6587\u660c");
        int wenQuIndex = findStarIndex(palaces, "\u6587\u66f2");
        addTo(palaces, Math.floorMod(leftIndex + dayOffset, 12), "otherStars", "\u4e09\u53f0");
        addTo(palaces, Math.floorMod(rightIndex - dayOffset, 12), "otherStars", "\u516b\u5ea7");
        addTo(palaces, Math.floorMod(wenChangIndex + dayOffset - 1, 12), "otherStars", "\u6069\u5149");
        addTo(palaces, Math.floorMod(wenQuIndex + dayOffset - 1, 12), "otherStars", "\u5929\u8d35");
        addTo(palaces, Math.floorMod(palaceBranchIndex("\u4e11") + dayOffset, 12), "otherStars", "\u5929\u4f24");
        addTo(palaces, Math.floorMod(palaceBranchIndex("\u672a") + dayOffset, 12), "otherStars", "\u5929\u4f7f");
    }

    private void addHourDecorativeStars(List<Map<String, Object>> palaces, int hourIndex) {
        addTo(palaces, Math.floorMod(palaceBranchIndex("\u4ea5") + hourIndex, 12), "otherStars", "\u5730\u89e3");
        addTo(palaces, Math.floorMod(palaceBranchIndex("\u7533") - hourIndex, 12), "otherStars", "\u5b64\u865a");
        addTo(palaces, Math.floorMod(palaceBranchIndex("\u620c") + hourIndex, 12), "otherStars", "\u7834\u788e");
        addTo(palaces, Math.floorMod(palaceBranchIndex("\u4e11") - hourIndex, 12), "otherStars", "\u8709\u5ec9");
    }

    @SuppressWarnings("unchecked")
    private int findStarIndex(List<Map<String, Object>> palaces, String star) {
        for (int i = 0; i < palaces.size(); i++) {
            List<String> lucky = (List<String>) palaces.get(i).get("luckyStars");
            List<String> other = (List<String>) palaces.get(i).get("otherStars");
            if (lucky.contains(star) || other.contains(star)) {
                return i;
            }
        }
        return 0;
    }

    private int luCunIndex(String stem) {
        if ("甲".equals(stem)) return palaceBranchIndex("寅");
        if ("乙".equals(stem)) return palaceBranchIndex("卯");
        if ("丙".equals(stem) || "戊".equals(stem)) return palaceBranchIndex("巳");
        if ("丁".equals(stem) || "己".equals(stem)) return palaceBranchIndex("午");
        if ("庚".equals(stem)) return palaceBranchIndex("申");
        if ("辛".equals(stem)) return palaceBranchIndex("酉");
        if ("壬".equals(stem)) return palaceBranchIndex("亥");
        return palaceBranchIndex("子");
    }

    private boolean isYangStem(String stem) {
        return "甲".equals(stem) || "丙".equals(stem) || "戊".equals(stem) || "庚".equals(stem) || "壬".equals(stem);
    }

    private int ziweiIndex(int lunarDay, int bureauNumber) {
        int day = Math.max(1, Math.min(30, lunarDay));
        int multiple = (int) Math.ceil(day / (double) bureauNumber);
        int difference = multiple * bureauNumber - day;
        int count = (difference % 2 == 0) ? multiple + difference : multiple - difference;
        return Math.floorMod(count - 1, 12);
    }

    private int bureauNumber(String bureau) {
        if (bureau.startsWith("水")) return 2;
        if (bureau.startsWith("木")) return 3;
        if (bureau.startsWith("金")) return 4;
        if (bureau.startsWith("土")) return 5;
        return 6;
    }

    private String bureauFromNaYin(String ganZhi) {
        String element = naYinElement(ganZhi);
        if ("水".equals(element)) return "水二局";
        if ("木".equals(element)) return "木三局";
        if ("金".equals(element)) return "金四局";
        if ("土".equals(element)) return "土五局";
        return "火六局";
    }

    private String naYinElement(String ganZhi) {
        Map<String, String> map = new LinkedHashMap<String, String>();
        putNaYin(map, "甲子,乙丑,壬申,癸酉,庚辰,辛巳,甲午,乙未,壬寅,癸卯,庚戌,辛亥", "金");
        putNaYin(map, "丙寅,丁卯,甲戌,乙亥,戊子,己丑,丙申,丁酉,甲辰,乙巳,戊午,己未", "火");
        putNaYin(map, "戊辰,己巳,壬午,癸未,庚寅,辛卯,戊戌,己亥,壬子,癸丑,庚申,辛酉", "木");
        putNaYin(map, "庚午,辛未,戊寅,己卯,丙戌,丁亥,庚子,辛丑,戊申,己酉,丙辰,丁巳", "土");
        putNaYin(map, "壬戌,癸亥,丙子,丁丑,甲申,乙酉,壬辰,癸巳,丙午,丁未,甲寅,乙卯", "水");
        return map.containsKey(ganZhi) ? map.get(ganZhi) : "火";
    }

    private void putNaYin(Map<String, String> map, String values, String element) {
        for (String value : values.split(",")) {
            map.put(value, element);
        }
    }

    private String[] palaceStems(String yearStem) {
        int start;
        if ("甲".equals(yearStem) || "己".equals(yearStem)) start = 2;
        else if ("乙".equals(yearStem) || "庚".equals(yearStem)) start = 4;
        else if ("丙".equals(yearStem) || "辛".equals(yearStem)) start = 6;
        else if ("丁".equals(yearStem) || "壬".equals(yearStem)) start = 8;
        else start = 0;
        String[] result = new String[12];
        for (int i = 0; i < 12; i++) {
            result[i] = STEMS[(start + i) % 10];
        }
        return result;
    }

    private int[] kuiYueByStem(String stem) {
        if ("甲".equals(stem) || "戊".equals(stem) || "庚".equals(stem)) return new int[]{1, 7};
        if ("乙".equals(stem) || "己".equals(stem)) return new int[]{0, 8};
        if ("丙".equals(stem) || "丁".equals(stem)) return new int[]{11, 9};
        if ("壬".equals(stem) || "癸".equals(stem)) return new int[]{3, 5};
        return new int[]{4, 2};
    }

    private int[] yangTuoByStem(String stem) {
        int lu;
        if ("甲".equals(stem)) lu = 2;
        else if ("乙".equals(stem)) lu = 3;
        else if ("丙".equals(stem) || "戊".equals(stem)) lu = 5;
        else if ("丁".equals(stem) || "己".equals(stem)) lu = 6;
        else if ("庚".equals(stem)) lu = 8;
        else if ("辛".equals(stem)) lu = 9;
        else if ("壬".equals(stem)) lu = 11;
        else lu = 0;
        return new int[]{Math.floorMod(lu + 1, 12), Math.floorMod(lu - 1, 12)};
    }

    private Map<String, String> fourHua(String stem) {
        Map<String, String> map = new LinkedHashMap<String, String>();
        if ("甲".equals(stem)) { map.put("廉贞", "化禄"); map.put("破军", "化权"); map.put("武曲", "化科"); map.put("太阳", "化忌"); }
        else if ("乙".equals(stem)) { map.put("天机", "化禄"); map.put("天梁", "化权"); map.put("紫微", "化科"); map.put("太阴", "化忌"); }
        else if ("丙".equals(stem)) { map.put("天同", "化禄"); map.put("天机", "化权"); map.put("文昌", "化科"); map.put("廉贞", "化忌"); }
        else if ("丁".equals(stem)) { map.put("太阴", "化禄"); map.put("天同", "化权"); map.put("天机", "化科"); map.put("巨门", "化忌"); }
        else if ("戊".equals(stem)) { map.put("贪狼", "化禄"); map.put("太阴", "化权"); map.put("右弼", "化科"); map.put("天机", "化忌"); }
        else if ("己".equals(stem)) { map.put("武曲", "化禄"); map.put("贪狼", "化权"); map.put("天梁", "化科"); map.put("文曲", "化忌"); }
        else if ("庚".equals(stem)) { map.put("太阳", "化禄"); map.put("武曲", "化权"); map.put("太阴", "化科"); map.put("天同", "化忌"); }
        else if ("辛".equals(stem)) { map.put("巨门", "化禄"); map.put("太阳", "化权"); map.put("文曲", "化科"); map.put("文昌", "化忌"); }
        else if ("壬".equals(stem)) { map.put("天梁", "化禄"); map.put("紫微", "化权"); map.put("左辅", "化科"); map.put("武曲", "化忌"); }
        else { map.put("破军", "化禄"); map.put("巨门", "化权"); map.put("太阴", "化科"); map.put("贪狼", "化忌"); }
        return map;
    }

    private int hourBranchIndex(String birthTime) {
        if (!StringUtils.hasText(birthTime)) {
            return 0;
        }
        String value = birthTime.trim();
        for (int i = 0; i < BRANCHES.length; i++) {
            if (value.contains(BRANCHES[i])) {
                return i;
            }
        }
        int hour = 0;
        try {
            String normalized = value.replace("：", ":").replace("点", ":").replace("时", ":");
            hour = Integer.parseInt(normalized.split(":")[0]);
        } catch (Exception ignored) {
            return 0;
        }
        if (hour == 23) return 0;
        return (hour + 1) / 2;
    }

    private int branchIndex(String branch) {
        for (int i = 0; i < BRANCHES.length; i++) {
            if (BRANCHES[i].equals(branch)) return i;
        }
        return 0;
    }

    private int stemIndex(String stem) {
        for (int i = 0; i < STEMS.length; i++) {
            if (STEMS[i].equals(stem)) return i;
        }
        return 0;
    }

    private boolean sameStem(String value, String unicodeStem) {
        return unicodeStem.equals(value);
    }

    private int palaceIndexFromStandardBranch(int standardBranchIndex) {
        return palaceBranchIndex(BRANCHES[Math.floorMod(standardBranchIndex, 12)]);
    }

    private int palaceBranchIndex(String branch) {
        for (int i = 0; i < PALACE_BRANCHES.length; i++) {
            if (PALACE_BRANCHES[i].equals(branch)) return i;
        }
        return 0;
    }

    private String lifeMaster(String mingBranch) {
        if ("子".equals(mingBranch)) return "贪狼";
        if ("丑".equals(mingBranch) || "亥".equals(mingBranch)) return "巨门";
        if ("寅".equals(mingBranch) || "戌".equals(mingBranch)) return "禄存";
        if ("卯".equals(mingBranch) || "酉".equals(mingBranch)) return "文曲";
        if ("辰".equals(mingBranch) || "申".equals(mingBranch)) return "廉贞";
        if ("巳".equals(mingBranch) || "未".equals(mingBranch)) return "武曲";
        return "破军";
    }

    private String bodyMaster(String yearBranch) {
        if ("子".equals(yearBranch) || "午".equals(yearBranch)) return "火星";
        if ("丑".equals(yearBranch) || "未".equals(yearBranch)) return "天相";
        if ("寅".equals(yearBranch) || "申".equals(yearBranch)) return "天梁";
        if ("卯".equals(yearBranch) || "酉".equals(yearBranch)) return "天同";
        if ("辰".equals(yearBranch) || "戌".equals(yearBranch)) return "文昌";
        return "天机";
    }

    private String douJun(int lunarMonth, int hourIndex) {
        return BRANCHES[Math.floorMod(lunarMonth + hourIndex + 7, 12)];
    }

    private String sourcePalace(String yearStem) {
        if ("甲".equals(yearStem)) return "命宫";
        if ("乙".equals(yearStem)) return "兄弟";
        if ("丙".equals(yearStem)) return "夫妻";
        if ("丁".equals(yearStem)) return "子女";
        if ("戊".equals(yearStem)) return "财帛";
        if ("己".equals(yearStem)) return "疾厄";
        if ("庚".equals(yearStem)) return "迁移";
        if ("辛".equals(yearStem)) return "交友";
        if ("壬".equals(yearStem)) return "官禄";
        return "田宅";
    }

    private LunarDate solarToLunar(LocalDate date) {
        if (date.getYear() < 1900 || date.getYear() > 2100) {
            throw new BizException("Lunar conversion supports 1900-2100");
        }
        int offset = (int) ChronoUnit.DAYS.between(LocalDate.of(1900, 1, 31), date);
        int year;
        int daysOfYear = 0;
        for (year = 1900; year <= 2100 && offset > 0; year++) {
            daysOfYear = lunarYearDays(year);
            offset -= daysOfYear;
        }
        if (offset < 0) {
            offset += daysOfYear;
            year--;
        }
        int leapMonth = leapMonth(year);
        boolean leap = false;
        int month;
        int daysOfMonth = 0;
        for (month = 1; month <= 12 && offset > 0; month++) {
            if (leapMonth > 0 && month == leapMonth + 1 && !leap) {
                month--;
                leap = true;
                daysOfMonth = leapDays(year);
            } else {
                daysOfMonth = monthDays(year, month);
            }
            offset -= daysOfMonth;
            if (leap && month == leapMonth + 1) {
                leap = false;
            }
        }
        if (offset < 0) {
            offset += daysOfMonth;
            month--;
        }
        return new LunarDate(year, month, offset + 1, leap);
    }

    private int lunarYearDays(int year) {
        int sum = 348;
        int info = LUNAR_INFO[year - 1900];
        for (int i = 0x8000; i > 0x8; i >>= 1) {
            if ((info & i) != 0) sum++;
        }
        return sum + leapDays(year);
    }

    private int leapDays(int year) {
        if (leapMonth(year) == 0) return 0;
        return (LUNAR_INFO[year - 1900] & 0x10000) != 0 ? 30 : 29;
    }

    private int leapMonth(int year) {
        return LUNAR_INFO[year - 1900] & 0xf;
    }

    private int monthDays(int year, int month) {
        return (LUNAR_INFO[year - 1900] & (0x10000 >> month)) == 0 ? 29 : 30;
    }

    private Map<String, Object> mapOf(Object... values) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        for (int i = 0; i + 1 < values.length; i += 2) {
            map.put(String.valueOf(values[i]), values[i + 1]);
        }
        return map;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private static class LunarDate {
        private final int year;
        private final int month;
        private final int day;
        private final boolean leap;

        private LunarDate(int year, int month, int day, boolean leap) {
            this.year = year;
            this.month = month;
            this.day = day;
            this.leap = leap;
        }
    }
}
