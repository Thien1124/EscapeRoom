package com.example.gamegiaido.service;

import com.example.gamegiaido.dto.AdminQuestionForm;
import com.example.gamegiaido.dto.AdminRoomForm;
import com.example.gamegiaido.dto.AdminRoomKeyForm;
import com.example.gamegiaido.dto.AdminTopicForm;
import com.example.gamegiaido.dto.AdminObjectHotspotForm;
import com.example.gamegiaido.model.GameRoom;
import com.example.gamegiaido.model.ObjectLockType;
import com.example.gamegiaido.model.QuizQuestion;
import com.example.gamegiaido.model.QuizTopic;
import com.example.gamegiaido.model.RoomKeyConfig;
import com.example.gamegiaido.model.RoomObject;
import com.example.gamegiaido.repository.GameRoomRepository;
import com.example.gamegiaido.repository.QuizQuestionRepository;
import com.example.gamegiaido.repository.QuizTopicRepository;
import com.example.gamegiaido.repository.RoomKeyConfigRepository;
import com.example.gamegiaido.repository.RoomObjectRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final QuizTopicRepository quizTopicRepository;
    private final GameRoomRepository gameRoomRepository;
    private final RoomObjectRepository roomObjectRepository;
    private final RoomKeyConfigRepository roomKeyConfigRepository;
    private final QuizQuestionRepository quizQuestionRepository;

    public AdminService(QuizTopicRepository quizTopicRepository,
                        GameRoomRepository gameRoomRepository,
                        RoomObjectRepository roomObjectRepository,
                        RoomKeyConfigRepository roomKeyConfigRepository,
                        QuizQuestionRepository quizQuestionRepository) {
        this.quizTopicRepository = quizTopicRepository;
        this.gameRoomRepository = gameRoomRepository;
        this.roomObjectRepository = roomObjectRepository;
        this.roomKeyConfigRepository = roomKeyConfigRepository;
        this.quizQuestionRepository = quizQuestionRepository;
    }

    @Transactional
    public void createTopic(AdminTopicForm form) {
        QuizTopic topic = new QuizTopic();
        topic.setName(form.getName().trim());
        topic.setDescription(form.getDescription().trim());
        quizTopicRepository.save(topic);
    }

    @Transactional
    public void createRoom(AdminRoomForm form) {
        QuizTopic topic = quizTopicRepository.findById(form.getTopicId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chủ đề"));

        GameRoom room = new GameRoom();
        room.setName(form.getName().trim());
        room.setDescription(form.getDescription().trim());
        room.setMode(form.getMode());
        room.setRoomOrder(form.getRoomOrder());
        room.setTopic(topic);
        gameRoomRepository.save(room);

        RoomObject interactionObject = new RoomObject();
        interactionObject.setObjectName("Cửa thoát hiểm");
        interactionObject.setHint("Cần tìm đủ chìa khóa trước khi mở");
        interactionObject.setRequiredStep(1);
        interactionObject.setLockType(ObjectLockType.INTERACTION);
        interactionObject.setLocked(true);
        interactionObject.setHotspotX(72);
        interactionObject.setHotspotY(22);
        interactionObject.setRoom(room);
        roomObjectRepository.save(interactionObject);

        RoomObject quizObject = new RoomObject();
        quizObject.setObjectName("Bảng câu hỏi thoát hiểm");
        quizObject.setHint("Trả lời đúng trắc nghiệm để thoát");
        quizObject.setRequiredStep(2);
        quizObject.setLockType(ObjectLockType.QUIZ);
        quizObject.setLocked(true);
        quizObject.setHotspotX(12);
        quizObject.setHotspotY(20);
        quizObject.setRoom(room);
        roomObjectRepository.save(quizObject);

        seedDefaultRoomKeys(room);
    }

    @Transactional
    public void upsertQuestion(AdminQuestionForm form) {
        RoomObject roomObject = roomObjectRepository
                .findFirstByRoomIdAndLockTypeOrderByRequiredStepAsc(form.getRoomId(), ObjectLockType.QUIZ)
                .orElseThrow(() -> new IllegalArgumentException("Phòng chưa có vật thể quiz"));

        QuizQuestion question = quizQuestionRepository.findByRoomObjectId(roomObject.getId())
                .orElseGet(QuizQuestion::new);
        question.setRoomObject(roomObject);
        question.setQuestionText(form.getQuestionText().trim());
        question.setClueText(null);
        question.setAnswerCode(null);
        question.setOptionA(normalizeOption(form.getOptionA()));
        question.setOptionB(normalizeOption(form.getOptionB()));
        question.setOptionC(normalizeOption(form.getOptionC()));
        question.setOptionD(normalizeOption(form.getOptionD()));
        String normalizedCorrect = form.getCorrectOption() == null ? "" : form.getCorrectOption().trim().toUpperCase();
        question.setCorrectOption(normalizedCorrect.isEmpty() ? "A" : normalizedCorrect);
        quizQuestionRepository.save(question);
    }

    @Transactional
    public void updateObjectHotspot(AdminObjectHotspotForm form) {
        RoomObject roomObject = roomObjectRepository.findById(form.getObjectId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vật thể"));

        roomObject.setHotspotX(form.getHotspotX());
        roomObject.setHotspotY(form.getHotspotY());
        roomObjectRepository.save(roomObject);
    }

    @Transactional
    public void upsertRoomKey(AdminRoomKeyForm form) {
        GameRoom room = gameRoomRepository.findById(form.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng"));

        String normalizedCode = form.getKeyCode().trim().toLowerCase(Locale.ROOT);
        if (normalizedCode.isEmpty()) {
            throw new IllegalArgumentException("Mã chìa khóa không hợp lệ.");
        }

        RoomKeyConfig config = roomKeyConfigRepository.findByRoomIdOrderByIdAsc(room.getId()).stream()
                .filter(item -> item.getKeyCode().equalsIgnoreCase(normalizedCode))
                .findFirst()
                .orElseGet(RoomKeyConfig::new);

        config.setRoom(room);
        config.setKeyCode(normalizedCode);
        config.setKeyName(form.getKeyName().trim());
        config.setImageUrl(normalizeImageUrl(form.getImageUrl()));
        config.setSpotX(form.getSpotX());
        config.setSpotY(form.getSpotY());
        roomKeyConfigRepository.save(config);
    }

    public List<QuizTopic> getTopics() {
        return quizTopicRepository.findAll();
    }

    public List<GameRoom> getRooms() {
        return gameRoomRepository.findAll();
    }

    public List<RoomObject> getRoomObjectsForAdmin() {
        return roomObjectRepository.findAllForAdminHotspot();
    }

    public List<RoomKeyConfig> getRoomKeysForAdmin() {
        return roomKeyConfigRepository.findAll();
    }

    private String normalizeOption(String optionValue) {
        if (optionValue == null || optionValue.trim().isEmpty()) {
            return "-";
        }
        return optionValue.trim();
    }

    private String normalizeImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return null;
        }
        return imageUrl.trim();
    }

    private void seedDefaultRoomKeys(GameRoom room) {
        String normalized = room.getName() == null ? "" : room.getName().toLowerCase(Locale.ROOT);
        if (normalized.contains("thí nghiệm")) {
            saveRoomKey(room, "microscope", "Kính hiển vi", 24, 34);
            saveRoomKey(room, "slide", "Lam kính", 68, 53);
            return;
        }
        if (normalized.contains("mật mã")) {
            saveRoomKey(room, "map_fragment", "Mảnh bản đồ", 31, 47);
            saveRoomKey(room, "uv_lamp", "Đèn UV", 74, 31);
            return;
        }
        if (normalized.contains("cổ vật")) {
            saveRoomKey(room, "stone_tablet", "Phiến đá cổ", 62, 42);
            saveRoomKey(room, "charcoal", "Than đồ nét", 21, 58);
            return;
        }

        saveRoomKey(room, "gear", "Bánh răng", 72, 54);
        saveRoomKey(room, "wire", "Cuộn dây", 28, 39);
    }

    private void saveRoomKey(GameRoom room, String keyCode, String keyName, int x, int y) {
        RoomKeyConfig config = new RoomKeyConfig();
        config.setRoom(room);
        config.setKeyCode(keyCode);
        config.setKeyName(keyName);
        config.setSpotX(x);
        config.setSpotY(y);
        roomKeyConfigRepository.save(config);
    }
}
