package com.example.gamegiaido.config;

import com.example.gamegiaido.model.GameMode;
import com.example.gamegiaido.model.GameRoom;
import com.example.gamegiaido.model.ObjectLockType;
import com.example.gamegiaido.model.PlayerProfile;
import com.example.gamegiaido.model.QuizQuestion;
import com.example.gamegiaido.model.QuizTopic;
import com.example.gamegiaido.model.Role;
import com.example.gamegiaido.model.RoomObject;
import com.example.gamegiaido.model.UserAccount;
import com.example.gamegiaido.repository.GameRoomRepository;
import com.example.gamegiaido.repository.PlayerProfileRepository;
import com.example.gamegiaido.repository.QuizQuestionRepository;
import com.example.gamegiaido.repository.QuizTopicRepository;
import com.example.gamegiaido.repository.RoomObjectRepository;
import com.example.gamegiaido.repository.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final QuizTopicRepository quizTopicRepository;
    private final GameRoomRepository gameRoomRepository;
    private final RoomObjectRepository roomObjectRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final UserAccountRepository userAccountRepository;
    private final PlayerProfileRepository playerProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(QuizTopicRepository quizTopicRepository,
                           GameRoomRepository gameRoomRepository,
                           RoomObjectRepository roomObjectRepository,
                           QuizQuestionRepository quizQuestionRepository,
                           UserAccountRepository userAccountRepository,
                           PlayerProfileRepository playerProfileRepository,
                           PasswordEncoder passwordEncoder) {
        this.quizTopicRepository = quizTopicRepository;
        this.gameRoomRepository = gameRoomRepository;
        this.roomObjectRepository = roomObjectRepository;
        this.quizQuestionRepository = quizQuestionRepository;
        this.userAccountRepository = userAccountRepository;
        this.playerProfileRepository = playerProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        ensureDefaultAdmin();

        if (quizTopicRepository.count() > 0) {
            return;
        }

        QuizTopic scenarioTopic = new QuizTopic();
        scenarioTopic.setName("Kịch bản");
        scenarioTopic.setDescription("Thí Nghiệm Bị Ngắt Quãng");
        quizTopicRepository.save(scenarioTopic);

        createScenarioRoom(scenarioTopic);
    }

    private void createScenarioRoom(QuizTopic topic) {
        GameRoom room = new GameRoom();
        room.setName("Thí Nghiệm Bị Ngắt Quãng");
        room.setDescription("Thu thập vật phẩm, giải mã tủ, kích hoạt máy quét và thoát bằng keycard.");
        room.setMode(GameMode.QUIZ_ESCAPE_ROOM);
        room.setRoomOrder(1);
        room.setTopic(topic);
        gameRoomRepository.save(room);

        RoomObject cabinetCode = new RoomObject();
        cabinetCode.setObjectName("Tủ gỗ khóa mã 3 số");
        cabinetCode.setHint("Dùng bút chì tô giấy trắng để lộ mã");
        cabinetCode.setRequiredStep(1);
        cabinetCode.setLockType(ObjectLockType.QUIZ);
        cabinetCode.setLocked(true);
        cabinetCode.setHotspotX(68);
        cabinetCode.setHotspotY(33);
        cabinetCode.setRoom(room);
        roomObjectRepository.save(cabinetCode);

        QuizQuestion codeQuestion = new QuizQuestion();
        codeQuestion.setRoomObject(cabinetCode);
        codeQuestion.setQuestionText("Nhập mã 3 chữ số để mở tủ gỗ");
        codeQuestion.setClueText("Mã xuất hiện khi tô bút chì lên tờ giấy trắng");
        codeQuestion.setAnswerCode("741");
        codeQuestion.setOptionA("---");
        codeQuestion.setOptionB("---");
        codeQuestion.setOptionC("---");
        codeQuestion.setOptionD("---");
        codeQuestion.setCorrectOption("A");
        quizQuestionRepository.save(codeQuestion);

        RoomObject microscope = new RoomObject();
        microscope.setObjectName("Kính hiển vi");
        microscope.setHint("Lắp ống kính và mẫu vật để soi");
        microscope.setRequiredStep(2);
        microscope.setLockType(ObjectLockType.INTERACTION);
        microscope.setLocked(true);
        microscope.setHotspotX(40);
        microscope.setHotspotY(38);
        microscope.setRoom(room);
        roomObjectRepository.save(microscope);

        RoomObject scanner = new RoomObject();
        scanner.setObjectName("Máy quét cạnh cửa");
        scanner.setHint("Đổ dung dịch tím vào phễu để kích hoạt");
        scanner.setRequiredStep(3);
        scanner.setLockType(ObjectLockType.INTERACTION);
        scanner.setLocked(true);
        scanner.setHotspotX(78);
        scanner.setHotspotY(45);
        scanner.setRoom(room);
        roomObjectRepository.save(scanner);

        RoomObject finalConsole = new RoomObject();
        finalConsole.setObjectName("Bảng nhập hình dạng vi khuẩn");
        finalConsole.setHint("Nhập hình dạng quan sát được từ kính hiển vi");
        finalConsole.setRequiredStep(4);
        finalConsole.setLockType(ObjectLockType.QUIZ);
        finalConsole.setLocked(true);
        finalConsole.setHotspotX(82);
        finalConsole.setHotspotY(28);
        finalConsole.setRoom(room);
        roomObjectRepository.save(finalConsole);

        QuizQuestion finalQuestion = new QuizQuestion();
        finalQuestion.setRoomObject(finalConsole);
        finalQuestion.setQuestionText("Hình dạng vi khuẩn bạn nhìn thấy là gì?");
        finalQuestion.setClueText("Gợi ý nằm trong kính hiển vi");
        finalQuestion.setAnswerCode(null);
        finalQuestion.setOptionA("Hình Tròn");
        finalQuestion.setOptionB("Hình Tam Giác");
        finalQuestion.setOptionC("Hình Vuông");
        finalQuestion.setOptionD("Hình Lục Giác");
        finalQuestion.setCorrectOption("B");
        quizQuestionRepository.save(finalQuestion);
    }

    private void ensureDefaultAdmin() {
        if (userAccountRepository.existsByUsername("admin@gmail.com")) {
            return;
        }

        UserAccount adminAccount = new UserAccount();
        adminAccount.setUsername("admin@gmail.com");
        adminAccount.setPassword(passwordEncoder.encode("Admin123@"));
        adminAccount.setRole(Role.ADMIN);
        adminAccount.setVerified(true);
        userAccountRepository.save(adminAccount);

        PlayerProfile adminProfile = new PlayerProfile();
        adminProfile.setDisplayName("Quản trị viên");
        adminProfile.setAccount(adminAccount);
        playerProfileRepository.save(adminProfile);
    }
}
