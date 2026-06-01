package com.zhexuan.divination.dto;

public class ZiweiChartDTO {
    private Long userId;
    private String gender;
    private String birthDate;
    private String birthTime;
    private String birthProvince;
    private String birthPlace;
    private String calendarType;
    private Boolean leapMonth;
    private Boolean useTrueSolarTime;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    public String getBirthTime() { return birthTime; }
    public void setBirthTime(String birthTime) { this.birthTime = birthTime; }
    public String getBirthProvince() { return birthProvince; }
    public void setBirthProvince(String birthProvince) { this.birthProvince = birthProvince; }
    public String getBirthPlace() { return birthPlace; }
    public void setBirthPlace(String birthPlace) { this.birthPlace = birthPlace; }
    public String getCalendarType() { return calendarType; }
    public void setCalendarType(String calendarType) { this.calendarType = calendarType; }
    public Boolean getLeapMonth() { return leapMonth; }
    public void setLeapMonth(Boolean leapMonth) { this.leapMonth = leapMonth; }
    public Boolean getUseTrueSolarTime() { return useTrueSolarTime; }
    public void setUseTrueSolarTime(Boolean useTrueSolarTime) { this.useTrueSolarTime = useTrueSolarTime; }
}
