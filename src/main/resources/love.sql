CREATE TABLE WeChatUser (
                            userId BIGINT PRIMARY KEY AUTO_INCREMENT,
                            openId VARCHAR(255) UNIQUE NOT NULL,
                            nickName VARCHAR(255),
                            sex INT CHECK (sex IN (0, 1, 2)),
                            province VARCHAR(255),
                            city VARCHAR(255),
                            country VARCHAR(255),
                            headImgUrl VARCHAR(255),
                            username VARCHAR(255) UNIQUE,
                            password VARCHAR(255)
);
