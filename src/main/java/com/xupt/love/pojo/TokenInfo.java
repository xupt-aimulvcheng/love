package com.xupt.love.pojo;

public class TokenInfo {
    private String accessToken;
    private int expiresIn;
    private String refreshToken;
    private String openid;
    private String scope;
    private Integer isSnapshotUser;
    private String unionid;

    // Getter和Setter方法

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Integer getIsSnapshotUser() {
        return isSnapshotUser;
    }

    public void setIsSnapshotUser(Integer isSnapshotUser) {
        this.isSnapshotUser = isSnapshotUser;
    }

    public String getUnionid() {
        return unionid;
    }

    public void setUnionid(String unionid) {
        this.unionid = unionid;
    }
}
