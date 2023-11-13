drop table WeChatUser;
CREATE TABLE WeChatUser (
                            userId BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户数据库的ID',
                            openId VARCHAR(255) UNIQUE NOT NULL COMMENT '用户的唯一标识',
                            nickName VARCHAR(255) COMMENT '用户昵称',
                            sex INT CHECK (sex IN (0, 1, 2)) COMMENT '用户的性别，1: 男性, 2: 女性, 0: 未知',
                            province VARCHAR(255) COMMENT '用户所在省份',
                            city VARCHAR(255) COMMENT '用户所在城市',
                            country VARCHAR(255) COMMENT '用户所在国家',
                            headImgUrl VARCHAR(255) COMMENT '用户头像URL，若用户更换头像，原有头像URL将失效',
                            privilege TEXT COMMENT '用户特权信息，存储为JSON字符串或其他格式',
                            unionId VARCHAR(255) COMMENT '用户在微信开放平台的统一标识',
                            username VARCHAR(255) UNIQUE COMMENT '用户名',
                            password VARCHAR(255) COMMENT '密码',
                            appID VARCHAR(255) COMMENT '公众号',
                            deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识，0表示未删除，1表示已删除'
);
