# Victorian Study Room - Escape Room Game

## Những thay đổi chính

### 1. ✅ Đã bỏ Minimap 2D
- Ẩn hoàn toàn minimap để tập trung vào 3D view
- Code minimap đã được comment out (có thể restore nếu cần)

### 2. ✅ Phòng mới: Victorian Study Room
**Tên:** Phòng Nghiên Cứu Victorian
**Thứ tự:** Room 4
**Câu chuyện:** Một học giả Victorian biến mất bí ẩn. Phòng làm việc của ông chứa nhiều manh mối.

### 3. ✅ Puzzle Logic Mới

#### **Puzzle Flow:**
1. **Step 1** - Thu thập 3 manh mối:
   - 🪟 **Cửa sổ Victoria**: Ánh sáng qua cửa sổ tạo shadow pattern → Số đầu tiên (3)
   - 📖 **Bàn làm việc**: Nhật ký cũ với cipher code → Số thứ hai (9)
   - 🧶 **Thảm Ba Tư**: Nhấc thảm lên, dưới có khắc số → Số thứ ba (7)

2. **Step 2** - Mở tủ sách:
   - 📚 **Tủ sách khóa mã**: Nhập mã **397** để mở
   - Nhận được: Blueprint máy tiện

3. **Step 3** - Sử dụng máy tiện:
   - ⚙️ **Máy tiện cổ**: Kết hợp Blueprint + Thanh kim loại + Dầu đánh bóng
   - Tạo ra: Chìa khóa thoát hiểm

4. **Step 4** - Thoát phòng:
   - 🚪 **Cửa thoát hiểm**: Dùng chìa khóa vừa tạo

### 4. ✅ 3D Models trong phòng

Room layout được thiết kế như căn phòng thật với 5 models:

1. **Persian Carpet** (`persian_malayer_runner_carpet.glb`)
   - Vị trí: Sàn nhà, giữa phòng
   - Có thể tương tác để tìm manh mối

2. **Table & Chairs** (`table_and_chairs.glb`)
   - Vị trí: Trung tâm phòng
   - Trên bàn có nhật ký với cipher

3. **Victorian Window** (`victorian_window.glb`)
   - Vị trí: Tường phía sau
   - Ánh sáng chiếu qua tạo pattern

4. **Bookshelf/Cabinet** (`basic_kitchen_cabinets_and_counter.glb`)
   - Vị trí: Tường trái
   - Khóa mã 3 chữ số

5. **Lathe Machine** (`lathe-machine/source/Lathe Machine.glb`)
   - Vị trí: Góc phải phòng
   - Dùng để kết hợp vật phẩm

### 5. ✅ Items có thể nhặt

- 🪟 Manh mối cửa sổ (số 3)
- 📖 Manh mối nhật ký (số 9)
- 🧶 Manh mối thảm (số 7)
- 📋 Blueprint máy tiện
- 🔩 Thanh kim loại
- 🧴 Dầu đánh bóng

## Cách Test Game

### 1. Khởi động server
```bash
# Nếu dùng Maven
./mvnw spring-boot:run

# Hoặc nếu PostgreSQL
# Đảm bảo PostgreSQL đang chạy và config đúng
```

### 2. Truy cập game
1. Mở browser: `http://localhost:8080`
2. Login/Register
3. Chọn **Start Game** → Chọn Topic "Kịch bản"
4. Chọn **Room 4: Phòng Nghiên Cứu Victorian**

### 3. Gameplay Test
- ✅ Di chuyển WASD + chuột để nhìn quanh
- ✅ Nhìn thấy 5 models 3D trong phòng
- ✅ Nhấn F để tương tác với objects
- ✅ Nhấn E để mở túi đồ
- ✅ Minimap đã biến mất (chỉ có 3D view)

### 4. Giải Puzzle
1. Tìm 3 manh mối từ: Cửa sổ, Bàn làm việc, Thảm
2. Mở tủ sách với mã **397**
3. Lấy Blueprint từ tủ
4. Tìm Thanh kim loại và Dầu đánh bóng
5. Tương tác với Máy tiện → Kết hợp 3 items
6. Dùng chìa khóa mở cửa thoát

## Technical Details

### Database Changes
- File: `RoomObjectSchemaUpdater.java`
- Thêm method: `seedVictorianStudyRoom()`
- Tự động seed khi khởi động app

### 3D Scene Changes
- File: `room-view.html`
- Function: `addDecorSet()` - Thêm conditional loading cho Victorian Room
- Models chỉ load khi room name contains "victorian" hoặc "nghiên cứu"

### Item Icons
- Location: `/static/images/items/`
- Format: SVG placeholders
- Các icons: window-clue, diary-clue, carpet-clue, blueprint, metal-rod, polish-oil

## Notes
- Minimap code được comment để có thể restore sau này
- 3D models tự động scale và center
- Collision detection đã được setup cho các furniture
- Room order = 4 (sau 3 rooms cũ)

## Future Improvements
- Thêm sound effects cho Victorian ambiance
- Thêm particle effects cho ánh sáng cửa sổ
- Tạo animation cho việc mở tủ/nhấc thảm
- Thêm lore/story elements qua text/audio

---
**Created:** 2026-03-24
**Version:** 1.0
**Room Order:** 4
