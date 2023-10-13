package com.xupt.love.pojo;

import lombok.Data;

import java.util.Arrays;

/**
 * UserProfile class represents a user profile with various personal details.
 */
@Data
public class WeChatUser {

    // 用户的唯一标识
    private String openId;
    // 用户昵称
    private String nickName;
    // 用户的性别，值为1时是男性，值为2时是女性，值为0时是未知
    private int sex;
    // 用户个人资料填写的省份
    private String province;
    // 普通用户个人资料填写的城市
    private String city;
    // 国家，如中国为CN
    private String country;
    // 用户头像，最后一个数值代表正方形头像大小（有0、46、64、96、132数值可选，0代表640*640正方形头像），用户没有头像时该项为空。若用户更换头像，原有头像URL将失效。
    private String headImgUrl;
    // 用户特权信息，json 数组，如微信沃卡用户为（chinaunicom）
    private String[] privilege;
    // 只有在用户将公众号绑定到微信开放平台账号后，才会出现该字段。
    private String unionId;

    @Override
    public String toString() {
        String gender;
        switch (sex) {
            case 1:
                gender = "男性";
                break;
            case 2:
                gender = "女性";
                break;
            default:
                gender = "未知";
                break;
        }

        return String.format(
                "用户信息:\n" +
                        "唯一标识: %s\n" +
                        "昵称: %s\n" +
                        "性别: %s\n" +
                        "省份: %s\n" +
                        "城市: %s\n" +
                        "国家: %s\n" +
                        "头像URL: %s\n" +
                        "特权信息: %s\n" +
                        "统一标识: %s",
                openId, nickName, gender, province, city, country, headImgUrl,
                Arrays.toString(privilege), unionId
        );
    }
}
