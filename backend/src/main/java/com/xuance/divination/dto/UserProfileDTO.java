package com.xuance.divination.dto;

public class UserProfileDTO {
    private Long userId;
    private String gender;
    private String birthDate;
    private String birthTime;
    private String birthPlace;
    private String birthDayGanZhi;
    private String birthDayMaster;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    public String getBirthTime() { return birthTime; }
    public void setBirthTime(String birthTime) { this.birthTime = birthTime; }
    public String getBirthPlace() { return birthPlace; }
    public void setBirthPlace(String birthPlace) { this.birthPlace = birthPlace; }
    public String getBirthDayGanZhi() { return birthDayGanZhi; }
    public void setBirthDayGanZhi(String birthDayGanZhi) { this.birthDayGanZhi = birthDayGanZhi; }
    public String getBirthDayMaster() { return birthDayMaster; }
    public void setBirthDayMaster(String birthDayMaster) { this.birthDayMaster = birthDayMaster; }
}
