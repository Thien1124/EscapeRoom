CREATE TABLE IF NOT EXISTS user_accounts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_accounts_username UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS player_profiles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    display_name VARCHAR(80) NOT NULL,
    total_score INT NOT NULL,
    reward_points INT NOT NULL DEFAULT 0,
    reward_wallet_initialized BIT(1) NOT NULL DEFAULT b'0',
    selected_character_icon VARCHAR(40) NOT NULL DEFAULT 'agent_default',
    owned_character_icons VARCHAR(600) NOT NULL DEFAULT 'agent_default',
    total_win INT NOT NULL,
    avatar_url VARCHAR(500) NULL,
    account_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_player_profiles_account_id UNIQUE (account_id),
    CONSTRAINT fk_player_profiles_account FOREIGN KEY (account_id) REFERENCES user_accounts(id)
);

SET @has_reward_points := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'player_profiles'
      AND COLUMN_NAME = 'reward_points'
);
SET @sql_reward_points := IF(
    @has_reward_points = 0,
    'ALTER TABLE player_profiles ADD COLUMN reward_points INT NOT NULL DEFAULT 0',
    'SELECT 1'
);
PREPARE stmt_reward_points FROM @sql_reward_points;
EXECUTE stmt_reward_points;
DEALLOCATE PREPARE stmt_reward_points;

SET @has_reward_wallet_initialized := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'player_profiles'
      AND COLUMN_NAME = 'reward_wallet_initialized'
);
SET @sql_reward_wallet_initialized := IF(
    @has_reward_wallet_initialized = 0,
    "ALTER TABLE player_profiles ADD COLUMN reward_wallet_initialized BIT(1) NOT NULL DEFAULT b'0'",
    'SELECT 1'
);
PREPARE stmt_reward_wallet_initialized FROM @sql_reward_wallet_initialized;
EXECUTE stmt_reward_wallet_initialized;
DEALLOCATE PREPARE stmt_reward_wallet_initialized;

SET @has_selected_icon := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'player_profiles'
      AND COLUMN_NAME = 'selected_character_icon'
);
SET @sql_selected_icon := IF(
    @has_selected_icon = 0,
    "ALTER TABLE player_profiles ADD COLUMN selected_character_icon VARCHAR(40) NOT NULL DEFAULT 'agent_default'",
    'SELECT 1'
);
PREPARE stmt_selected_icon FROM @sql_selected_icon;
EXECUTE stmt_selected_icon;
DEALLOCATE PREPARE stmt_selected_icon;

SET @has_owned_icons := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'player_profiles'
      AND COLUMN_NAME = 'owned_character_icons'
);
SET @sql_owned_icons := IF(
    @has_owned_icons = 0,
    "ALTER TABLE player_profiles ADD COLUMN owned_character_icons VARCHAR(600) NOT NULL DEFAULT 'agent_default'",
    'SELECT 1'
);
PREPARE stmt_owned_icons FROM @sql_owned_icons;
EXECUTE stmt_owned_icons;
DEALLOCATE PREPARE stmt_owned_icons;

CREATE TABLE IF NOT EXISTS quiz_topics (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_quiz_topics_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS game_rooms (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(255) NOT NULL,
    room_order INT NOT NULL,
    mode VARCHAR(30) NOT NULL,
    topic_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_game_rooms_topic FOREIGN KEY (topic_id) REFERENCES quiz_topics(id)
);

CREATE TABLE IF NOT EXISTS room_objects (
    id BIGINT NOT NULL AUTO_INCREMENT,
    object_name VARCHAR(120) NOT NULL,
    hint VARCHAR(255) NOT NULL,
    hotspot_x INT NULL,
    hotspot_y INT NULL,
    locked BIT(1) NOT NULL,
    required_step INT NOT NULL,
    lock_type VARCHAR(20) NOT NULL,
    room_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_room_objects_room FOREIGN KEY (room_id) REFERENCES game_rooms(id)
);

CREATE TABLE IF NOT EXISTS room_key_configs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    key_code VARCHAR(80) NOT NULL,
    key_name VARCHAR(120) NOT NULL,
    image_url VARCHAR(500) NULL,
    spot_x INT NOT NULL,
    spot_y INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_room_key_configs_room FOREIGN KEY (room_id) REFERENCES game_rooms(id)
);

CREATE TABLE IF NOT EXISTS quiz_questions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    room_object_id BIGINT NOT NULL,
    question_text VARCHAR(255) NOT NULL,
    clue_text VARCHAR(255) NULL,
    answer_code VARCHAR(40) NULL,
    optiona VARCHAR(255) NOT NULL,
    optionb VARCHAR(255) NOT NULL,
    optionc VARCHAR(255) NOT NULL,
    optiond VARCHAR(255) NOT NULL,
    correct_option VARCHAR(1) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_quiz_questions_room_object_id UNIQUE (room_object_id),
    CONSTRAINT fk_quiz_questions_room_object FOREIGN KEY (room_object_id) REFERENCES room_objects(id)
);

CREATE TABLE IF NOT EXISTS player_room_progresses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    player_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    current_step INT NOT NULL,
    completed BIT(1) NOT NULL,
    score INT NOT NULL,
    wrong_attempts INT NOT NULL,
    won BIT(1) NULL,
    result_finalized BIT(1) NOT NULL,
    collected_items VARCHAR(600) NOT NULL,
    discovered_clues VARCHAR(1200) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_player_room UNIQUE (player_id, room_id),
    CONSTRAINT fk_player_room_progresses_player FOREIGN KEY (player_id) REFERENCES player_profiles(id),
    CONSTRAINT fk_player_room_progresses_room FOREIGN KEY (room_id) REFERENCES game_rooms(id)
);

CREATE TABLE IF NOT EXISTS play_histories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    player_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    score INT NOT NULL,
    result VARCHAR(10) NOT NULL,
    action_count INT NOT NULL DEFAULT 0,
    played_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_play_histories_player FOREIGN KEY (player_id) REFERENCES player_profiles(id),
    CONSTRAINT fk_play_histories_room FOREIGN KEY (room_id) REFERENCES game_rooms(id)
);

CREATE TABLE IF NOT EXISTS reward_vouchers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(60) NOT NULL,
    name VARCHAR(140) NOT NULL,
    brand VARCHAR(80) NOT NULL,
    points_cost INT NOT NULL,
    total_stock INT NOT NULL,
    remaining_stock INT NOT NULL,
    expires_at DATE NOT NULL,
    active BIT(1) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_reward_vouchers_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS voucher_redemptions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    player_id BIGINT NOT NULL,
    voucher_id BIGINT NOT NULL,
    points_spent INT NOT NULL,
    issued_code VARCHAR(100) NOT NULL,
    redeemed_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_voucher_redemptions_issued_code UNIQUE (issued_code),
    CONSTRAINT uk_voucher_redeem_player_voucher UNIQUE (player_id, voucher_id),
    CONSTRAINT fk_voucher_redemptions_player FOREIGN KEY (player_id) REFERENCES player_profiles(id),
    CONSTRAINT fk_voucher_redemptions_voucher FOREIGN KEY (voucher_id) REFERENCES reward_vouchers(id)
);

SET @has_issued_code := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'voucher_redemptions'
      AND COLUMN_NAME = 'issued_code'
);
SET @sql_issued_code := IF(
    @has_issued_code = 0,
    "ALTER TABLE voucher_redemptions ADD COLUMN issued_code VARCHAR(100) NOT NULL DEFAULT 'PENDING-CODE'",
    'SELECT 1'
);
PREPARE stmt_issued_code FROM @sql_issued_code;
EXECUTE stmt_issued_code;
DEALLOCATE PREPARE stmt_issued_code;
