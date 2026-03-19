DELETE FROM player_room_progresses;
DELETE FROM play_histories;
DELETE FROM quiz_questions;
DELETE FROM room_key_configs;
DELETE FROM room_objects;
DELETE FROM game_rooms;
DELETE FROM quiz_topics;

INSERT INTO quiz_topics (name, description)
VALUES
    ('Kịch bản', 'Thí Nghiệm Bị Ngắt Quãng');

INSERT INTO game_rooms (name, description, room_order, mode, topic_id)
SELECT
    'Thí Nghiệm Bị Ngắt Quãng',
    'Thu thập vật phẩm, giải mã tủ, kích hoạt máy quét và thoát bằng keycard.',
    1,
    'QUIZ_ESCAPE_ROOM',
    id
FROM quiz_topics
WHERE name = 'Kịch bản'
LIMIT 1;

INSERT INTO room_objects (object_name, hint, hotspot_x, hotspot_y, locked, required_step, lock_type, room_id)
VALUES
    ('Tủ gỗ khóa mã 3 số', 'Dùng bút chì tô giấy trắng để lộ mã', 68, 33, b'1', 1, 'QUIZ', (SELECT id FROM game_rooms WHERE name = 'Thí Nghiệm Bị Ngắt Quãng' LIMIT 1)),
    ('Kính hiển vi', 'Lắp ống kính và mẫu vật để soi', 40, 38, b'1', 2, 'INTERACTION', (SELECT id FROM game_rooms WHERE name = 'Thí Nghiệm Bị Ngắt Quãng' LIMIT 1)),
    ('Máy quét cạnh cửa', 'Đổ dung dịch tím vào phễu để kích hoạt', 78, 45, b'1', 3, 'INTERACTION', (SELECT id FROM game_rooms WHERE name = 'Thí Nghiệm Bị Ngắt Quãng' LIMIT 1)),
    ('Bảng nhập hình dạng vi khuẩn', 'Nhập hình dạng quan sát được từ kính hiển vi', 82, 28, b'1', 4, 'QUIZ', (SELECT id FROM game_rooms WHERE name = 'Thí Nghiệm Bị Ngắt Quãng' LIMIT 1));

INSERT INTO quiz_questions (room_object_id, question_text, clue_text, answer_code, optiona, optionb, optionc, optiond, correct_option)
VALUES
    ((SELECT id FROM room_objects WHERE object_name = 'Tủ gỗ khóa mã 3 số' AND room_id = (SELECT id FROM game_rooms WHERE name = 'Thí Nghiệm Bị Ngắt Quãng' LIMIT 1) LIMIT 1),
     'Nhập mã 3 chữ số để mở tủ gỗ', 'Mã xuất hiện khi tô bút chì lên tờ giấy trắng', '741', '---', '---', '---', '---', 'A'),
    ((SELECT id FROM room_objects WHERE object_name = 'Bảng nhập hình dạng vi khuẩn' AND room_id = (SELECT id FROM game_rooms WHERE name = 'Thí Nghiệm Bị Ngắt Quãng' LIMIT 1) LIMIT 1),
     'Hình dạng vi khuẩn bạn nhìn thấy là gì?', 'Gợi ý nằm trong kính hiển vi', NULL, 'Hình Tròn', 'Hình Tam Giác', 'Hình Vuông', 'Hình Lục Giác', 'B');

INSERT INTO room_key_configs (room_id, key_code, key_name, image_url, spot_x, spot_y)
VALUES
    ((SELECT id FROM game_rooms WHERE name = 'Thí Nghiệm Bị Ngắt Quãng' LIMIT 1), 'lens', 'Ống kính', '/images/items/lens.svg', 22, 55),
    ((SELECT id FROM game_rooms WHERE name = 'Thí Nghiệm Bị Ngắt Quãng' LIMIT 1), 'pencil', 'Bút chì', '/images/items/pencil.svg', 78, 22),
    ((SELECT id FROM game_rooms WHERE name = 'Thí Nghiệm Bị Ngắt Quãng' LIMIT 1), 'white_paper', 'Tờ giấy trắng', '/images/items/white-paper.svg', 48, 56),
    ((SELECT id FROM game_rooms WHERE name = 'Thí Nghiệm Bị Ngắt Quãng' LIMIT 1), 'glass_cup', 'Cốc thủy tinh', '/images/items/glass-cup.svg', 18, 28),
    ((SELECT id FROM game_rooms WHERE name = 'Thí Nghiệm Bị Ngắt Quãng' LIMIT 1), 'yellow_powder', 'Lọ bột màu vàng', '/images/items/yellow-powder.svg', 12, 67);
