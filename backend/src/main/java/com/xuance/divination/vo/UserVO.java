package com.xuance.divination.vo;

public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String role;
    private String gender;
    private String birthDate;
    private String birthTime;
    private String birthPlace;
    private String birthDayGanZhi;
    private String birthDayMaster;
    private Integer status;
    private String createTime;
    private String token;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
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
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
