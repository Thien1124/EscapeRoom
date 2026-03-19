package com.example.gamegiaido.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.sql.DataSource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class RoomObjectSchemaUpdater implements CommandLineRunner {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public RoomObjectSchemaUpdater(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        jdbcTemplate.execute("""
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
                )
                """);

        Set<String> columns = getRoomObjectColumns();

        if (!columns.contains("hotspot_x")) {
            jdbcTemplate.execute("ALTER TABLE room_objects ADD COLUMN hotspot_x INT NULL");
        }

        if (!columns.contains("hotspot_y")) {
            jdbcTemplate.execute("ALTER TABLE room_objects ADD COLUMN hotspot_y INT NULL");
        }

        Set<String> progressColumns = getColumns("player_room_progresses");
        if (!progressColumns.contains("collected_items")) {
            jdbcTemplate.execute("ALTER TABLE player_room_progresses ADD COLUMN collected_items VARCHAR(600) NOT NULL DEFAULT ''");
        }
        if (!progressColumns.contains("discovered_clues")) {
            jdbcTemplate.execute("ALTER TABLE player_room_progresses ADD COLUMN discovered_clues VARCHAR(1200) NOT NULL DEFAULT ''");
        }

        Set<String> questionColumns = getColumns("quiz_questions");
        if (!questionColumns.contains("clue_text")) {
            jdbcTemplate.execute("ALTER TABLE quiz_questions ADD COLUMN clue_text VARCHAR(255) NULL");
        }
        if (!questionColumns.contains("answer_code")) {
            jdbcTemplate.execute("ALTER TABLE quiz_questions ADD COLUMN answer_code VARCHAR(40) NULL");
        }

        resetToInterruptedExperimentScenario();
    }

    private Set<String> getRoomObjectColumns() throws SQLException {
        return getColumns("room_objects");
    }

    private Set<String> getColumns(String tableName) throws SQLException {
        Set<String> columns = new HashSet<>();
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet rs = metaData.getColumns(connection.getCatalog(), null, tableName, null)) {
                while (rs.next()) {
                    columns.add(rs.getString("COLUMN_NAME").toLowerCase(Locale.ROOT));
                }
            }
            if (!columns.isEmpty()) {
                return columns;
            }
            try (ResultSet rs = metaData.getColumns(connection.getCatalog(), null, tableName.toUpperCase(Locale.ROOT), null)) {
                while (rs.next()) {
                    columns.add(rs.getString("COLUMN_NAME").toLowerCase(Locale.ROOT));
                }
            }
        }
        return columns;
    }

    private void resetToInterruptedExperimentScenario() {
        jdbcTemplate.update("DELETE FROM player_room_progresses");
        jdbcTemplate.update("DELETE FROM play_histories");
        jdbcTemplate.update("DELETE FROM quiz_questions");
        jdbcTemplate.update("DELETE FROM room_key_configs");
        jdbcTemplate.update("DELETE FROM room_objects");
        jdbcTemplate.update("DELETE FROM game_rooms");
        jdbcTemplate.update("DELETE FROM quiz_topics");

        jdbcTemplate.update("INSERT INTO quiz_topics (name, description) VALUES ('Kịch bản', 'Thí Nghiệm Bị Ngắt Quãng')");

        Long topicId = jdbcTemplate.queryForObject(
                "SELECT id FROM quiz_topics WHERE name = 'Kịch bản' LIMIT 1",
                Long.class
        );

        if (topicId == null) {
            throw new IllegalStateException("Không thể tạo chủ đề kịch bản.");
        }

        jdbcTemplate.update(
                "INSERT INTO game_rooms (name, description, room_order, mode, topic_id) VALUES (?, ?, ?, ?, ?)",
                "Thí Nghiệm Bị Ngắt Quãng",
                "Thu thập vật phẩm, giải mã tủ, kích hoạt máy quét và thoát bằng keycard.",
                1,
                "QUIZ_ESCAPE_ROOM",
                topicId
        );

        Long roomId = jdbcTemplate.queryForObject(
                "SELECT id FROM game_rooms WHERE name = 'Thí Nghiệm Bị Ngắt Quãng' LIMIT 1",
                Long.class
        );

        if (roomId == null) {
            throw new IllegalStateException("Không thể tạo phòng kịch bản.");
        }

        jdbcTemplate.update(
                "INSERT INTO room_objects (object_name, hint, hotspot_x, hotspot_y, locked, required_step, lock_type, room_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                "Tủ gỗ khóa mã 3 số", "Dùng bút chì tô giấy trắng để lộ mã", 68, 33, true, 1, "QUIZ", roomId
        );
        jdbcTemplate.update(
                "INSERT INTO room_objects (object_name, hint, hotspot_x, hotspot_y, locked, required_step, lock_type, room_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                "Kính hiển vi", "Lắp ống kính và mẫu vật để soi", 40, 38, true, 2, "INTERACTION", roomId
        );
        jdbcTemplate.update(
                "INSERT INTO room_objects (object_name, hint, hotspot_x, hotspot_y, locked, required_step, lock_type, room_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                "Máy quét cạnh cửa", "Đổ dung dịch tím vào phễu để kích hoạt", 78, 45, true, 3, "INTERACTION", roomId
        );
        jdbcTemplate.update(
                "INSERT INTO room_objects (object_name, hint, hotspot_x, hotspot_y, locked, required_step, lock_type, room_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                "Bảng nhập hình dạng vi khuẩn", "Nhập hình dạng quan sát được từ kính hiển vi", 82, 28, true, 4, "QUIZ", roomId
        );

        List<Long> objectIds = jdbcTemplate.query(
                "SELECT id FROM room_objects WHERE room_id = ? ORDER BY required_step ASC",
                (rs, rowNum) -> rs.getLong("id"),
                roomId
        );

        if (objectIds.size() != 4) {
            throw new IllegalStateException("Không thể tạo đủ vật thể theo kịch bản.");
        }

        jdbcTemplate.update(
                "INSERT INTO quiz_questions (room_object_id, question_text, clue_text, answer_code, optiona, optionb, optionc, optiond, correct_option) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                objectIds.get(0),
                "Nhập mã 3 chữ số để mở tủ gỗ",
                "Mã xuất hiện khi tô bút chì lên tờ giấy trắng",
                "741",
                "---",
                "---",
                "---",
                "---",
                "A"
        );

        jdbcTemplate.update(
                "INSERT INTO quiz_questions (room_object_id, question_text, clue_text, answer_code, optiona, optionb, optionc, optiond, correct_option) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                objectIds.get(3),
                "Hình dạng vi khuẩn bạn nhìn thấy là gì?",
                "Gợi ý nằm trong kính hiển vi",
                null,
                "Hình Tròn",
                "Hình Tam Giác",
                "Hình Vuông",
                "Hình Lục Giác",
                "B"
        );

        insertScenarioSpot(roomId, "lens", "Ống kính", 22, 55);
        insertScenarioSpot(roomId, "pencil", "Bút chì", 78, 22);
        insertScenarioSpot(roomId, "white_paper", "Tờ giấy trắng", 48, 56);
        insertScenarioSpot(roomId, "glass_cup", "Cốc thủy tinh", 18, 28);
        insertScenarioSpot(roomId, "yellow_powder", "Lọ bột màu vàng", 12, 67);
    }

    private void insertScenarioSpot(Long roomId, String keyCode, String keyName, int x, int y) {
        String imageUrl = switch (keyCode) {
            case "lens" -> "/images/items/lens.svg";
            case "pencil" -> "/images/items/pencil.svg";
            case "white_paper" -> "/images/items/white-paper.svg";
            case "glass_cup" -> "/images/items/glass-cup.svg";
            case "yellow_powder" -> "/images/items/yellow-powder.svg";
            default -> null;
        };
        jdbcTemplate.update(
                "INSERT INTO room_key_configs (room_id, key_code, key_name, image_url, spot_x, spot_y) VALUES (?, ?, ?, ?, ?, ?)",
                roomId,
                keyCode,
                keyName,
                imageUrl,
                x,
                y
        );
    }
}
