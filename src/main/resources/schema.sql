-- 0. 기존 테이블 삭제 (외래 키 참조 역순으로 삭제)
DROP TABLE IF EXISTS missing_pet_report;
DROP TABLE IF EXISTS chat_message;
DROP TABLE IF EXISTS notification;
DROP TABLE IF EXISTS follow;
DROP TABLE IF EXISTS subscription;
DROP TABLE IF EXISTS payment;
DROP TABLE IF EXISTS post_interaction;
DROP TABLE IF EXISTS comment;
DROP TABLE IF EXISTS post_image;
DROP TABLE IF EXISTS missing_pet_post;
DROP TABLE IF EXISTS chat_room;
DROP TABLE IF EXISTS subscription_plan;
DROP TABLE IF EXISTS post_main;
DROP TABLE IF EXISTS linked_account;
DROP TABLE IF EXISTS member;

-- 1. member
CREATE TABLE IF NOT EXISTS member (
    id                      BIGINT       NOT NULL AUTO_INCREMENT,
    email                   VARCHAR(100) NULL,
    password                VARCHAR(255) NULL,
    nickname                VARCHAR(50)  NOT NULL,
    species                 VARCHAR(50)  NOT NULL,
    sex                     VARCHAR(10)  NOT NULL,
    birth_date              DATE         NOT NULL,
    intro                   VARCHAR(255) NULL,
    profile_image           VARCHAR(255) NULL,
    address                 VARCHAR(255) NULL,
    status                  VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    role                    VARCHAR(20)  NOT NULL DEFAULT 'ROLE_USER',
    created_at              DATETIME     DEFAULT CURRENT_TIMESTAMP,
    info_provide_agreement  DATETIME     NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_email (email)
);

-- 2. linked_account
CREATE TABLE IF NOT EXISTS linked_account (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    member_id        BIGINT       NOT NULL,
    provider         VARCHAR(50)  NOT NULL,
    provider_user_id VARCHAR(100) NOT NULL,
    provider_email   VARCHAR(100) NOT NULL,
    created_at       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_linked_account (member_id, provider),
    UNIQUE KEY uk_member_linked_account_info (provider, provider_user_id),
    CONSTRAINT fk_linked_account_member
        FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE
);

-- 3. post_main
CREATE TABLE IF NOT EXISTS post_main (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    member_id           BIGINT       NOT NULL,
    type                TINYINT      NOT NULL DEFAULT 1,
    content             TEXT         NOT NULL,
    bgm_url             VARCHAR(255) NULL,
    is_subscriber_only  TINYINT(1)   NOT NULL DEFAULT 0,
    hashtags            TEXT         NULL,
    created_at          DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_post_main_member
        FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE
);

-- 4. post_image
CREATE TABLE IF NOT EXISTS post_image (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    post_id    BIGINT       NOT NULL,
    image_url  VARCHAR(255) NOT NULL,
    sort_order INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_post_image_post
        FOREIGN KEY (post_id) REFERENCES post_main (id) ON DELETE CASCADE
);

-- 5. comment
CREATE TABLE IF NOT EXISTS comment (
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    post_id    BIGINT   NOT NULL,
    member_id  BIGINT   NOT NULL,
    content    TEXT     NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_comment_post
        FOREIGN KEY (post_id) REFERENCES post_main (id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_member
        FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE
);

-- 6. post_interaction
CREATE TABLE IF NOT EXISTS post_interaction (
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    member_id        BIGINT      NOT NULL,
    post_id          BIGINT      NOT NULL,
    interaction_type VARCHAR(20) NOT NULL,
    created_at       DATETIME    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_post_interaction_type (member_id, post_id, interaction_type),
    CONSTRAINT fk_post_interaction_member
        FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE,
    CONSTRAINT fk_post_interaction_post
        FOREIGN KEY (post_id) REFERENCES post_main (id) ON DELETE CASCADE
);

-- 7. payment
CREATE TABLE IF NOT EXISTS payment (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    member_id        BIGINT       NOT NULL,
    target_member_id BIGINT       NOT NULL,
    imp_uid          VARCHAR(100) NULL,
    merchant_uid     VARCHAR(100) NOT NULL,
    amount           INT          NOT NULL,
    category         VARCHAR(30)  NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    pay_type         VARCHAR(30)  NOT NULL,
    created_at       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    completed_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payment_merchant_uid (merchant_uid),
    CONSTRAINT fk_payment_member
        FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_target_member
        FOREIGN KEY (target_member_id) REFERENCES member (id) ON DELETE CASCADE
);

-- 8. subscription_plan
CREATE TABLE IF NOT EXISTS subscription_plan (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    member_id   BIGINT       NOT NULL,
    plan_name   VARCHAR(50)  NOT NULL,
    price       INT          NOT NULL,
    description TEXT         NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_plan_name (member_id, plan_name),
    CONSTRAINT fk_subscription_plan_member
        FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE
);

-- 9. subscription
CREATE TABLE IF NOT EXISTS subscription (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    member_id        BIGINT       NOT NULL,
    target_member_id BIGINT       NOT NULL,
    plan_id          BIGINT       NOT NULL,
    customer_uid     VARCHAR(100) NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    started_at       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    ended_at         DATETIME     NULL,
    next_billing_at  DATETIME     NULL,
    agreement        TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    CONSTRAINT fk_subscription_member
        FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE,
    CONSTRAINT fk_subscription_target_member
        FOREIGN KEY (target_member_id) REFERENCES member (id) ON DELETE CASCADE,
    CONSTRAINT fk_subscription_plan
        FOREIGN KEY (plan_id) REFERENCES subscription_plan (id) ON DELETE CASCADE
);

-- 10. follow
CREATE TABLE IF NOT EXISTS follow (
    id           BIGINT   NOT NULL AUTO_INCREMENT,
    follower_id  BIGINT   NOT NULL,
    following_id BIGINT   NOT NULL,
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_follower_following (follower_id, following_id),
    CONSTRAINT fk_follow_follower
        FOREIGN KEY (follower_id) REFERENCES member (id) ON DELETE CASCADE,
    CONSTRAINT fk_follow_following
        FOREIGN KEY (following_id) REFERENCES member (id) ON DELETE CASCADE
);

-- 11. notification
CREATE TABLE IF NOT EXISTS notification (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    member_id         BIGINT       NOT NULL,
    sender_id         BIGINT       NOT NULL,
    notification_type VARCHAR(30)  NOT NULL,
    content           VARCHAR(255) NOT NULL,
    is_checked        TINYINT(1)   NOT NULL DEFAULT 0,
    created_at        DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_notification_member
        FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE,
    CONSTRAINT fk_notification_sender
        FOREIGN KEY (sender_id) REFERENCES member (id) ON DELETE CASCADE
);

-- 12. chat_room
CREATE TABLE IF NOT EXISTS chat_room (
    id             BIGINT     NOT NULL AUTO_INCREMENT,
    member1_id     BIGINT     NOT NULL,
    member2_id     BIGINT     NOT NULL,
    member1_exited TINYINT(1) NOT NULL DEFAULT 0,
    member2_exited TINYINT(1) NOT NULL DEFAULT 0,
    created_at     DATETIME   DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_chat (member1_id, member2_id),
    CONSTRAINT fk_chat_room_member1
        FOREIGN KEY (member1_id) REFERENCES member (id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_room_member2
        FOREIGN KEY (member2_id) REFERENCES member (id) ON DELETE CASCADE
);

-- 13. chat_message
CREATE TABLE IF NOT EXISTS chat_message (
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    room_id    BIGINT   NOT NULL,
    sender_id  BIGINT   NOT NULL,
    message    TEXT     NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_chat_message_room
        FOREIGN KEY (room_id) REFERENCES chat_room (id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_message_sender
        FOREIGN KEY (sender_id) REFERENCES member (id)
);

-- 14. missing_pet_post
CREATE TABLE IF NOT EXISTS missing_pet_post (
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    member_id       BIGINT         NOT NULL,
    missing_date    DATE           NOT NULL,
    missing_address VARCHAR(255)   NOT NULL,
    detail          TEXT           NULL,
    image_url       VARCHAR(255)   NULL,
    status          VARCHAR(20)    NOT NULL,
    latitude        DECIMAL(10,7)  NULL,
    longitude       DECIMAL(10,7)  NULL,
    created_at      DATETIME       DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME       DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_missing_pet_post_member
        FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE
);

-- 15. missing_pet_report
CREATE TABLE IF NOT EXISTS missing_pet_report (
    id                  BIGINT         NOT NULL AUTO_INCREMENT,
    missing_pet_post_id BIGINT         NOT NULL,
    member_id           BIGINT         NOT NULL,
    address             VARCHAR(255)   NOT NULL,
    detail              TEXT           NULL,
    image_url           VARCHAR(255)   NULL,
    sight_at            DATETIME       NOT NULL,
    latitude            DECIMAL(10,7)  NULL,
    longitude           DECIMAL(10,7)  NULL,
    created_at          DATETIME       DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_missing_pet_report_post
        FOREIGN KEY (missing_pet_post_id) REFERENCES missing_pet_post (id) ON DELETE CASCADE,
    CONSTRAINT fk_missing_pet_report_member
        FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE
);
