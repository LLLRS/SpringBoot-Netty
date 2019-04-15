package me.netty.pojo.bo;

import java.io.Serializable;

public class UsersBO implements Serializable {
    private String userId;
    private String faceData;
    private String nickname;
    
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getFaceData() {
		return faceData;
	}
	public void setFaceData(String faceData) {
		this.faceData = faceData;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
}
