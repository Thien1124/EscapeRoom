package com.example.gamegiaido.service;

import com.example.gamegiaido.dto.RoomMapItem;
import com.example.gamegiaido.dto.CollectibleItem;
import com.example.gamegiaido.dto.CollectibleSpot;
import com.example.gamegiaido.model.GameMode;
import com.example.gamegiaido.model.GameRoom;
import com.example.gamegiaido.model.ObjectLockType;
import com.example.gamegiaido.model.PlayHistory;
import com.example.gamegiaido.model.PlayResult;
import com.example.gamegiaido.model.PlayerProfile;
import com.example.gamegiaido.model.PlayerRoomProgress;
import com.example.gamegiaido.model.QuizQuestion;
import com.example.gamegiaido.model.RoomKeyConfig;
import com.example.gamegiaido.model.RoomObject;
import com.example.gamegiaido.repository.GameRoomRepository;
import com.example.gamegiaido.repository.PlayHistoryRepository;
import com.example.gamegiaido.repository.PlayerProfileRepository;
import com.example.gamegiaido.repository.PlayerRoomProgressRepository;
import com.example.gamegiaido.repository.QuizQuestionRepository;
import com.example.gamegiaido.repository.RoomKeyConfigRepository;
import com.example.gamegiaido.repository.RoomObjectRepository;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GamePlayService {

    private final PlayerProfileRepository playerProfileRepository;
    private final GameRoomRepository gameRoomRepository;
    private final RoomObjectRepository roomObjectRepository;
    private final RoomKeyConfigRepository roomKeyConfigRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final PlayerRoomProgressRepository playerRoomProgressRepository;
    private final PlayHistoryRepository playHistoryRepository;

    private static final int MAX_WRONG_ATTEMPTS = 5;
    private static final int BASE_ROOM_SCORE = 100;
    private static final int QUIZ_CORRECT_SCORE = 0;
    private static final int INTERACTION_SCORE = 0;
    private static final int QUIZ_WRONG_PENALTY = 2;
    private static final int COLLECT_ITEM_SCORE = 0;
    private static final int HINT_PENALTY = 5;

    private static final String FLAG_PAPER_REVEALED = "paper_revealed";
    private static final String FLAG_CABINET_OPEN = "cabinet_open";
    private static final String FLAG_MICROSCOPE_FIXED = "microscope_fixed";
    private static final String FLAG_SCANNER_ACTIVE = "scanner_active";

    private static final String ITEM_LENS = "lens";
    private static final String ITEM_PENCIL = "pencil";
    private static final String ITEM_WHITE_PAPER = "white_paper";
    private static final String ITEM_CODE_CLUE_NOTE = "code_clue_note";
    private static final String ITEM_GLASS_CUP = "glass_cup";
    private static final String ITEM_YELLOW_POWDER = "yellow_powder";
    private static final String ITEM_SLIDE = "slide";
    private static final String ITEM_BLUE_SOLUTION = "blue_solution";
    private static final String ITEM_BLUE_MIX = "blue_mix";
    private static final String ITEM_PURPLE_MIX = "purple_mix";
    private static final String ITEM_KEYCARD = "keycard";

    private static final String ITEM_UV_MAP = "uv_map";
    private static final String ITEM_CIPHER_PATH = "cipher_path";
    private static final String ITEM_MOON_STAR_KEY = "moon_star_key";
    private static final String ITEM_SHADOW_KEY = "shadow_key";

    private static final String ITEM_DECODED_TABLET = "decoded_tablet";
    private static final String ITEM_GUARDIAN_SEAL = "guardian_seal";
    private static final String ITEM_VAULT_KEY = "vault_key";

    private static final String ITEM_ROUGH_KEY = "rough_key";
    private static final String ITEM_POLISHED_KEY = "polished_key";
    private static final String ITEM_MASTER_KEY = "master_key";
    private static final String ITEM_SEALED_MASTER_KEY = "sealed_master_key";

    private static final String META_CLICK = "meta:clicks:";
    private static final String META_START = "meta:start:";
    private static final String META_FINISH = "meta:finish:";
    private static final String META_BAD_COMBINE = "meta:bad-combine:";
    private static final String META_DOOR_LEVEL = "meta:door-level:";
    private static final String META_DOOR_CORRECT = "meta:door-correct:";
    private static final String META_DOOR_DEAD_END = "meta:door-dead-end:";
    private static final String META_ITEM_LABEL = "meta:item-label:";
    private static final String STATE_PICKED_PREFIX = "state:picked:";
    private static final String FLAG_DOOR_MAZE_ACTIVE = "door_maze_active";
    private static final String FLAG_DOOR_MAZE_COMPLETED = "door_maze_completed";

    private static final int DOOR_MAZE_LEVELS = 3;
    private static final int DOOR_MAZE_OPTIONS = 3;

    private static final String SCENARIO_ROOM_KEYWORD = "thí nghiệm bị ngắt quãng";

    private static final Map<String, CollectibleItem> LAB_ITEM_CATALOG = createLabItemCatalog();

    public GamePlayService(PlayerProfileRepository playerProfileRepository,
                           GameRoomRepository gameRoomRepository,
                           RoomObjectRepository roomObjectRepository,
                           RoomKeyConfigRepository roomKeyConfigRepository,
                           QuizQuestionRepository quizQuestionRepository,
                           PlayerRoomProgressRepository playerRoomProgressRepository,
                           PlayHistoryRepository playHistoryRepository) {
        this.playerProfileRepository = playerProfileRepository;
        this.gameRoomRepository = gameRoomRepository;
        this.roomObjectRepository = roomObjectRepository;
        this.roomKeyConfigRepository = roomKeyConfigRepository;
        this.quizQuestionRepository = quizQuestionRepository;
        this.playerRoomProgressRepository = playerRoomProgressRepository;
        this.playHistoryRepository = playHistoryRepository;
    }

    @Transactional
    public PlayerRoomProgress getOrCreateProgress(String username, Long roomId) {
        PlayerProfile player = getPlayer(username);

        GameRoom room = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng"));

        return playerRoomProgressRepository.findByPlayerIdAndRoomId(player.getId(), room.getId())
                .orElseGet(() -> {
                    PlayerRoomProgress progress = new PlayerRoomProgress();
                    progress.setPlayer(player);
                    progress.setRoom(room);
                    progress.setCurrentStep(1);
                    progress.setScore(BASE_ROOM_SCORE);
                    progress.setCompleted(false);
                    Set<String> state = parseCsv(progress.getDiscoveredClues());
                    upsertMeta(state, META_CLICK, "0");
                    upsertMeta(state, META_START, String.valueOf(System.currentTimeMillis()));
                    progress.setDiscoveredClues(joinCsv(state));
                    return playerRoomProgressRepository.save(progress);
                });
    }

    public List<RoomMapItem> getRoomMap(String username, Long topicId, GameMode mode) {
        PlayerProfile player = getPlayer(username);
        List<GameRoom> rooms = gameRoomRepository.findByTopicIdAndModeOrderByRoomOrderAsc(topicId, mode);
        if (rooms.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy phòng phù hợp");
        }

        List<Long> roomIds = rooms.stream().map(GameRoom::getId).toList();
        Map<Long, PlayerRoomProgress> progressMap = playerRoomProgressRepository
                .findByPlayerIdAndRoomIdIn(player.getId(), roomIds)
                .stream()
                .collect(Collectors.toMap(progress -> progress.getRoom().getId(), progress -> progress));

        Set<Long> wonRoomIds = playHistoryRepository.findByPlayerIdAndRoomIdIn(player.getId(), roomIds)
            .stream()
            .filter(history -> history.getResult() == PlayResult.WIN)
            .map(history -> history.getRoom().getId())
            .collect(Collectors.toSet());

        boolean previousCompleted = true;
        List<RoomMapItem> roomMap = new java.util.ArrayList<>();
        for (GameRoom room : rooms) {
            PlayerRoomProgress progress = progressMap.get(room.getId());
            boolean completed = wonRoomIds.contains(room.getId())
                    || (progress != null && progress.getCompleted() && Boolean.TRUE.equals(progress.getWon()));
            boolean unlocked = previousCompleted;
            roomMap.add(new RoomMapItem(room, unlocked, completed));
            previousCompleted = completed;
        }

        return roomMap;
    }

    public boolean canAccessRoom(String username, GameRoom room) {
        List<RoomMapItem> roomMap = getRoomMap(username, room.getTopic().getId(), room.getMode());
        return roomMap.stream()
                .anyMatch(item -> item.getRoom().getId().equals(room.getId()) && item.isUnlocked());
    }

    public GameRoom getNextRoom(GameRoom room) {
        return gameRoomRepository.findByTopicIdAndModeOrderByRoomOrderAsc(room.getTopic().getId(), room.getMode())
                .stream()
                .filter(candidate -> candidate.getRoomOrder() > room.getRoomOrder())
                .min(Comparator.comparing(GameRoom::getRoomOrder))
                .orElse(null);
    }

    public List<RoomObject> getObjects(Long roomId) {
        return roomObjectRepository.findByRoomIdOrderByRequiredStepAsc(roomId);
    }

    public QuizQuestion getQuestion(Long roomId, Long objectId) {
        RoomObject roomObject = getObject(roomId, objectId);
        if (roomObject.getLockType() != ObjectLockType.QUIZ) {
            throw new IllegalArgumentException("Vật thể này không yêu cầu quiz");
        }
        return quizQuestionRepository.findByRoomObjectId(roomObject.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu hỏi"));
    }

    @Transactional
    public boolean submitQuizAnswer(String username,
                                    Long roomId,
                                    Long objectId,
                                    String selectedOption,
                                    String answerCode) {
        PlayerRoomProgress progress = getOrCreateProgress(username, roomId);
        RoomObject roomObject = getObject(roomId, objectId);

        validateStep(progress, roomObject);

        QuizQuestion question = quizQuestionRepository.findByRoomObjectId(roomObject.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu hỏi"));

        if (!hasText(selectedOption)) {
            if (!hasText(answerCode)) {
                throw new IllegalArgumentException("Bạn cần nhập đáp án trước khi nộp.");
            }
        }

        boolean useCodeAnswer = hasText(question.getAnswerCode());
        boolean isCorrect;
        if (useCodeAnswer) {
            if (!hasStateFlag(progress, FLAG_PAPER_REVEALED)) {
                throw new IllegalArgumentException("Bạn cần dùng bút chì để làm lộ mã trên tờ giấy trước.");
            }
            isCorrect = normalizeCode(question.getAnswerCode()).equals(normalizeCode(answerCode));
        } else {
            if (!hasText(selectedOption)) {
                throw new IllegalArgumentException("Bạn cần chọn đáp án trước khi nộp.");
            }
            if (isLabScenario(progress.getRoom()) && roomObject.getRequiredStep() == 4 && !hasStateFlag(progress, FLAG_SCANNER_ACTIVE)) {
                throw new IllegalArgumentException("Bạn cần kích hoạt máy quét bằng dung dịch tím trước.");
            }
            isCorrect = question.getCorrectOption().equalsIgnoreCase(selectedOption);
        }

        incrementClick(progress);

        if (isCorrect) {
            progress.setScore(progress.getScore() + QUIZ_CORRECT_SCORE);
            if (isLabScenario(progress.getRoom()) && roomObject.getRequiredStep() == 1) {
                unlockCabinetRewards(progress);
            }
            advanceProgress(progress, roomId);
        } else {
            progress.setWrongAttempts(progress.getWrongAttempts() + 1);
            progress.setScore(Math.max(0, progress.getScore() - QUIZ_WRONG_PENALTY));

            List<String> hints = getSearchHints(roomId);
            if (!hints.isEmpty()) {
                int hintIndex = Math.max(0, progress.getWrongAttempts() - 1) % hints.size();
                String hintEntry = "Gợi ý: " + hints.get(hintIndex);
                Set<String> discovered = parseCsv(progress.getDiscoveredClues());
                if (!discovered.contains(hintEntry)) {
                    discovered.add(hintEntry);
                    progress.setDiscoveredClues(joinCsv(discovered));
                }
            }

            if (progress.getWrongAttempts() >= MAX_WRONG_ATTEMPTS) {
                finalizeProgress(progress, false);
            }
            playerRoomProgressRepository.save(progress);
        }
        return isCorrect;
    }

    public List<CollectibleItem> getCollectibles(Long roomId) {
        GameRoom room = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng"));
        if (isLabScenario(room)) {
            return List.copyOf(LAB_ITEM_CATALOG.values());
        }
        List<RoomKeyConfig> roomKeyConfigs = roomKeyConfigRepository.findByRoomIdOrderByIdAsc(roomId);
        if (!roomKeyConfigs.isEmpty()) {
            List<CollectibleItem> baseItems = roomKeyConfigs.stream()
                    .map(config -> {
                        String icon = hasText(config.getImageUrl()) ? config.getImageUrl() : "🗝️";
                        return new CollectibleItem(config.getKeyCode(), config.getKeyName(), icon);
                    })
                    .toList();
            return mergeCollectibles(baseItems, getCraftedItemsForRoom(room));
        }
        return mergeCollectibles(getCollectiblesByRoomName(room.getName()), getCraftedItemsForRoom(room));
    }

    public List<CollectibleItem> getCollectedItems(String username, Long roomId) {
        PlayerRoomProgress progress = getOrCreateProgress(username, roomId);
        Set<String> collected = parseCsv(progress.getCollectedItems());
        Map<String, String> itemLabels = parseItemLabels(parseCsv(progress.getDiscoveredClues()));
        if (isLabScenario(progress.getRoom())) {
            return collected.stream()
                    .map(key -> {
                        CollectibleItem knownItem = LAB_ITEM_CATALOG.get(key);
                        if (knownItem != null) {
                            return knownItem;
                        }
                        return buildFallbackCollectible(key, itemLabels.get(key));
                    })
                    .toList();
        }
        Map<String, CollectibleItem> knownItems = getCollectibles(roomId).stream()
                .collect(Collectors.toMap(CollectibleItem::getKey, item -> item, (first, second) -> first, LinkedHashMap::new));
        return collected.stream()
                .map(key -> {
                    CollectibleItem knownItem = knownItems.get(key);
                    if (knownItem != null) {
                        return knownItem;
                    }
                    return buildFallbackCollectible(key, itemLabels.get(key));
                })
                .toList();
    }

    public List<String> getDiscoveredClues(String username, Long roomId) {
        PlayerRoomProgress progress = getOrCreateProgress(username, roomId);
        return parseCsv(progress.getDiscoveredClues()).stream()
                .filter(entry -> !entry.startsWith("state:"))
                .filter(entry -> !entry.startsWith("meta:"))
                .toList();
    }

    public List<String> getCollectedItemKeys(String username, Long roomId) {
        PlayerRoomProgress progress = getOrCreateProgress(username, roomId);
        return parseCsv(progress.getCollectedItems()).stream().toList();
    }

        public List<CollectibleSpot> getCollectibleSpots(String username, Long roomId) {
        PlayerRoomProgress progress = getOrCreateProgress(username, roomId);
        Set<String> collectedKeys = parseCsv(progress.getCollectedItems());
            Set<String> state = parseCsv(progress.getDiscoveredClues());

        GameRoom room = gameRoomRepository.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng"));

        if (isLabScenario(room)) {
            return getLabCollectibleSpots(progress, roomId);
        }

        List<RoomKeyConfig> roomKeyConfigs = roomKeyConfigRepository.findByRoomIdOrderByIdAsc(roomId);
        if (!roomKeyConfigs.isEmpty()) {
            return roomKeyConfigs.stream()
                .map(config -> new CollectibleSpot(
                    config.getKeyCode(),
                    config.getKeyName(),
                    config.getSpotX(),
                    config.getSpotY(),
                    config.getImageUrl(),
                    collectedKeys.contains(config.getKeyCode()) || state.contains(pickedFlag(config.getKeyCode()))
                ))
                .toList();
        }

        Map<String, int[]> spotMap = getSpotLayoutByRoomName(room.getName());
        return spotMap.entrySet().stream()
            .map(entry -> {
                String keyName = getCollectibles(roomId).stream()
                    .filter(item -> item.getKey().equals(entry.getKey()))
                    .map(CollectibleItem::getName)
                    .findFirst()
                    .orElse(entry.getKey());
                return new CollectibleSpot(
                    entry.getKey(),
                    keyName,
                    entry.getValue()[0],
                    entry.getValue()[1],
                    null,
                    collectedKeys.contains(entry.getKey()) || state.contains(pickedFlag(entry.getKey()))
                );
            })
            .toList();
        }

        public List<String> getSearchHints(Long roomId) {
        GameRoom room = gameRoomRepository.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng"));

        if (isLabScenario(room)) {
            return List.of(
                "Hãy quan sát kỹ khu vực tủ treo và bàn thí nghiệm."
            );
        }

        String normalized = room.getName() == null ? "" : room.getName().toLowerCase();
        if (normalized.contains("thí nghiệm")) {
            return List.of(
                "Có một phản quang nhỏ gần giá ống nghiệm.",
                "Một vật mỏng nằm sát mặt bàn kim loại."
            );
        }
        if (normalized.contains("mật mã")) {
            return List.of(
                "Ánh tím nhấp nháy ở góc tường phải.",
                "Mảnh giấy cũ ẩn gần hình khắc cổ."
            );
        }
        if (normalized.contains("cổ vật")) {
            return List.of(
                "Bụi than bám quanh bệ đá thấp.",
                "Có phiến khắc nứt ngay dưới tranh cổ."
            );
        }
        return List.of(
            "Một linh kiện nằm cạnh hộp điều khiển.",
            "Dây dẫn bị cuộn trong khe máy."
        );
        }

    @Transactional
    public String collectItem(String username, Long roomId, String itemKey) {
        PlayerRoomProgress progress = getOrCreateProgress(username, roomId);

        if (isLabScenario(progress.getRoom())) {
            return collectLabItem(progress, roomId, itemKey);
        }

        if (!hasText(itemKey)) {
            throw new IllegalArgumentException("Vật phẩm không hợp lệ.");
        }

        String normalizedItemKey = itemKey.trim().toLowerCase(Locale.ROOT);

        List<RoomKeyConfig> roomKeyConfigs = roomKeyConfigRepository.findByRoomIdOrderByIdAsc(roomId);
        List<CollectibleItem> sceneItems = roomKeyConfigs.isEmpty()
            ? getCollectiblesByRoomName(progress.getRoom().getName())
            : roomKeyConfigs.stream()
            .map(config -> new CollectibleItem(config.getKeyCode(), config.getKeyName(), config.getImageUrl()))
            .toList();

        java.util.Map<String, String> validKeyMap = sceneItems.stream()
                .collect(Collectors.toMap(
                        item -> item.getKey().toLowerCase(Locale.ROOT),
                        CollectibleItem::getKey,
                        (first, second) -> first,
                        java.util.LinkedHashMap::new
                ));

        String canonicalItemKey = validKeyMap.get(normalizedItemKey);
        if (canonicalItemKey == null) {
            throw new IllegalArgumentException("Vật phẩm không tồn tại trong phòng này.");
        }

        Set<String> collected = parseCsv(progress.getCollectedItems());
        boolean alreadyCollected = collected.stream()
                .anyMatch(existing -> existing.equalsIgnoreCase(canonicalItemKey));
        if (alreadyCollected) {
            return "Bạn đã nhặt vật phẩm này rồi.";
        }

        collected.add(canonicalItemKey);
        progress.setCollectedItems(joinCsv(collected));
        Set<String> state = parseCsv(progress.getDiscoveredClues());
        state.add(pickedFlag(canonicalItemKey));
        progress.setDiscoveredClues(joinCsv(state));
        progress.setScore(progress.getScore() + COLLECT_ITEM_SCORE);
        incrementClick(progress);
        playerRoomProgressRepository.save(progress);
        return "Đã thêm vật phẩm vào túi đồ.";
    }

    @Transactional
    public String combineItems(String username, Long roomId, String firstItemKey, String secondItemKey) {
        if (!hasText(firstItemKey) || !hasText(secondItemKey)) {
            throw new IllegalArgumentException("Bạn cần chọn đủ hai vật phẩm để kết hợp.");
        }
        String normalizedFirst = canonicalizeItemKey(firstItemKey);
        String normalizedSecond = canonicalizeItemKey(secondItemKey);

        if (normalizedFirst.equals(normalizedSecond)) {
            throw new IllegalArgumentException("Hai vật phẩm phải khác nhau để kết hợp.");
        }

        PlayerRoomProgress progress = getOrCreateProgress(username, roomId);
        Set<String> collected = parseCsv(progress.getCollectedItems());
        Set<String> normalizedCollected = collected.stream()
            .map(this::canonicalizeItemKey)
            .collect(Collectors.toSet());
        if (!normalizedCollected.contains(normalizedFirst) || !normalizedCollected.contains(normalizedSecond)) {
            throw new IllegalArgumentException("Bạn chưa nhặt đủ hai vật phẩm này.");
        }

        GameRoom room = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng"));

        if (isLabScenario(room)) {
            return combineItemsForLab(progress, normalizedFirst, normalizedSecond);
        }

        return combineItemsForAdvancedRooms(progress, room, normalizedFirst, normalizedSecond);
    }

    private String combineItemsForAdvancedRooms(PlayerRoomProgress progress,
                                                GameRoom room,
                                                String firstItemKey,
                                                String secondItemKey) {
        if (isNightCipherScenario(room)) {
            return combineItemsForNightCipher(progress, firstItemKey, secondItemKey);
        }
        if (isAncientVaultScenario(room)) {
            return combineItemsForAncientVault(progress, firstItemKey, secondItemKey);
        }
        if (isVictorianScenario(room)) {
            return combineItemsForVictorianStudy(progress, firstItemKey, secondItemKey);
        }
        return registerUncertainCombine(progress, firstItemKey, secondItemKey);
    }

    private String combineItemsForNightCipher(PlayerRoomProgress progress, String firstItemKey, String secondItemKey) {
        Set<String> collected = parseCsv(progress.getCollectedItems());
        Set<String> state = parseCsv(progress.getDiscoveredClues());
        incrementClick(progress);

        if (pairMatches(firstItemKey, secondItemKey, "map_fragment", "uv_lamp")) {
            consumeAndCreate(collected, "map_fragment", "uv_lamp", ITEM_UV_MAP);
            state.add("Bản đồ đã hiện ký tự ẩn dưới tia UV.");
            state.add("Mã mở Bảng mã UV: 586.");
            progress.setCollectedItems(joinCsv(collected));
            progress.setDiscoveredClues(joinCsv(state));
            playerRoomProgressRepository.save(progress);
            return "Bạn soi UV lên bản đồ và giải mã được Bản đồ phát quang. Mã mở Bảng mã UV là 586.";
        }

        if (pairMatches(firstItemKey, secondItemKey, ITEM_UV_MAP, "cipher_ring")) {
            consumeAndCreate(collected, ITEM_UV_MAP, "cipher_ring", ITEM_CIPHER_PATH);
            state.add("Các vòng ký hiệu đã khớp và chỉ ra lộ trình bóng đêm.");
            state.add("Mã mở Bảng mã UV: 586.");
            progress.setCollectedItems(joinCsv(collected));
            progress.setDiscoveredClues(joinCsv(state));
            playerRoomProgressRepository.save(progress);
            return "Bạn khớp vòng giải mã thành công và nhận được Lộ trình mã hóa. Mã mở Bảng mã UV: 586.";
        }

        if (pairMatches(firstItemKey, secondItemKey, "moon_shard", "star_compass")) {
            consumeAndCreate(collected, "moon_shard", "star_compass", ITEM_MOON_STAR_KEY);
            state.add("Mảnh trăng và la bàn sao tạo thành một khóa định hướng.");
            progress.setCollectedItems(joinCsv(collected));
            progress.setDiscoveredClues(joinCsv(state));
            playerRoomProgressRepository.save(progress);
            return "Bạn tạo ra Khóa thiên thể từ mảnh trăng và la bàn sao.";
        }

        if (pairMatches(firstItemKey, secondItemKey, ITEM_CIPHER_PATH, ITEM_MOON_STAR_KEY)) {
            consumeAndCreate(collected, ITEM_CIPHER_PATH, ITEM_MOON_STAR_KEY, ITEM_SHADOW_KEY);
            state.add("Bạn ghép đủ dữ kiện để tạo Chìa bóng đêm.");
            progress.setCollectedItems(joinCsv(collected));
            progress.setDiscoveredClues(joinCsv(state));
            playerRoomProgressRepository.save(progress);
            return "Hoàn tất! Bạn tạo được Chìa bóng đêm cho cơ chế cuối phòng.";
        }

        return registerUncertainCombine(progress, firstItemKey, secondItemKey);
    }

    private String combineItemsForAncientVault(PlayerRoomProgress progress, String firstItemKey, String secondItemKey) {
        Set<String> collected = parseCsv(progress.getCollectedItems());
        Set<String> state = parseCsv(progress.getDiscoveredClues());
        incrementClick(progress);

        if (pairMatches(firstItemKey, secondItemKey, "stone_tablet", "charcoal")) {
            consumeAndCreate(collected, "stone_tablet", "charcoal", ITEM_DECODED_TABLET);
            state.add("Phiến đá đã lộ rõ ký tự cổ sau khi đồ than.");
            progress.setCollectedItems(joinCsv(collected));
            progress.setDiscoveredClues(joinCsv(state));
            playerRoomProgressRepository.save(progress);
            return "Bạn giải mã được Phiến ký tự đã giải mã.";
        }

        if (pairMatches(firstItemKey, secondItemKey, "relic_coin", "scarab_token")) {
            consumeAndCreate(collected, "relic_coin", "scarab_token", ITEM_GUARDIAN_SEAL);
            state.add("Đồng xu cổ và bùa bọ hung ghép thành ấn canh gác.");
            progress.setCollectedItems(joinCsv(collected));
            progress.setDiscoveredClues(joinCsv(state));
            playerRoomProgressRepository.save(progress);
            return "Bạn tạo được Ấn canh gác.";
        }

        if (pairMatches(firstItemKey, secondItemKey, ITEM_GUARDIAN_SEAL, "obsidian_fragment")) {
            consumeAndCreate(collected, ITEM_GUARDIAN_SEAL, "obsidian_fragment", ITEM_VAULT_KEY);
            state.add("Năng lượng hắc thạch kích hoạt ấn và tạo thành Chìa kho cổ vật.");
            progress.setCollectedItems(joinCsv(collected));
            progress.setDiscoveredClues(joinCsv(state));
            playerRoomProgressRepository.save(progress);
            return "Bạn đã tạo thành công Chìa kho cổ vật.";
        }

        return registerUncertainCombine(progress, firstItemKey, secondItemKey);
    }

    private String combineItemsForVictorianStudy(PlayerRoomProgress progress, String firstItemKey, String secondItemKey) {
        Set<String> collected = parseCsv(progress.getCollectedItems());
        Set<String> state = parseCsv(progress.getDiscoveredClues());
        incrementClick(progress);

        if (pairMatches(firstItemKey, secondItemKey, "blueprint", "metal_rod")) {
            consumeAndCreate(collected, "blueprint", "metal_rod", ITEM_ROUGH_KEY);
            state.add("Bạn tiện được phôi chìa đầu tiên theo bản thiết kế.");
            progress.setCollectedItems(joinCsv(collected));
            progress.setDiscoveredClues(joinCsv(state));
            playerRoomProgressRepository.save(progress);
            return "Bạn tạo được Phôi chìa Victorian.";
        }

        if (pairMatches(firstItemKey, secondItemKey, ITEM_ROUGH_KEY, "polish_oil")) {
            consumeAndCreate(collected, ITEM_ROUGH_KEY, "polish_oil", ITEM_POLISHED_KEY);
            state.add("Phôi chìa đã được đánh bóng mượt.");
            progress.setCollectedItems(joinCsv(collected));
            progress.setDiscoveredClues(joinCsv(state));
            playerRoomProgressRepository.save(progress);
            return "Bạn hoàn thiện Bản chìa đánh bóng.";
        }

        if (pairMatches(firstItemKey, secondItemKey, ITEM_POLISHED_KEY, "clock_key")) {
            consumeAndCreate(collected, ITEM_POLISHED_KEY, "clock_key", ITEM_MASTER_KEY);
            state.add("Cơ cấu đồng hồ đã ăn khớp với bản chìa mới.");
            progress.setCollectedItems(joinCsv(collected));
            progress.setDiscoveredClues(joinCsv(state));
            playerRoomProgressRepository.save(progress);
            return "Bạn tạo được Chìa chính Victorian.";
        }

        if (pairMatches(firstItemKey, secondItemKey, ITEM_MASTER_KEY, "wax_seal")) {
            consumeAndCreate(collected, ITEM_MASTER_KEY, "wax_seal", ITEM_SEALED_MASTER_KEY);
            state.add("Con dấu sáp hoàn tất nghi thức mở cửa thoát hiểm.");
            progress.setCollectedItems(joinCsv(collected));
            progress.setDiscoveredClues(joinCsv(state));
            playerRoomProgressRepository.save(progress);
            return "Chìa chính đã được niêm ấn hoàn tất.";
        }

        return registerUncertainCombine(progress, firstItemKey, secondItemKey);
    }

    private String registerUncertainCombine(PlayerRoomProgress progress, String firstItemKey, String secondItemKey) {
        Set<String> collected = parseCsv(progress.getCollectedItems());
        String normalizedFirst = canonicalizeItemKey(firstItemKey);
        String normalizedSecond = canonicalizeItemKey(secondItemKey);
        if (!collected.contains(normalizedFirst) || !collected.contains(normalizedSecond)) {
            throw new IllegalArgumentException("Bạn chưa nhặt đủ hai vật phẩm này.");
        }

        Set<String> state = parseCsv(progress.getDiscoveredClues());
        long totalWrongCombine = readMetaLong(state, META_BAD_COMBINE, 0) + 1;
        upsertMeta(state, META_BAD_COMBINE, String.valueOf(totalWrongCombine));

        String errorKey = "unstable_compound_" + totalWrongCombine;
        String errorName = "Vat pham loi " + totalWrongCombine;
        collected.add(errorKey);
        upsertItemLabel(state, errorKey, errorName);
        state.add("Kết hợp lỗi tạo ra: " + errorName);

        progress.setCollectedItems(joinCsv(collected));
        progress.setDiscoveredClues(joinCsv(state));
        playerRoomProgressRepository.save(progress);
        return "Bạn tạo ra " + errorName + ".";
    }

    private String buildPhantomProductName(String firstItemKey, String secondItemKey, long index) {
        String first = toDisplayToken(firstItemKey);
        String second = toDisplayToken(secondItemKey);
        String[] suffixes = {
                "Mẫu phản ứng",
                "Hợp chất tạm",
                "Mảnh cộng hưởng",
                "Tinh thể lai"
        };
        String suffix = suffixes[(int) (Math.abs(index) % suffixes.length)];
        return first + " - " + second + " " + suffix;
    }

    private String toDisplayToken(String rawKey) {
        String canonical = canonicalizeItemKey(rawKey);
        if (!hasText(canonical)) {
            return "Vat Pham";
        }
        String[] parts = canonical.split("[_-]");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (!hasText(part)) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return builder.isEmpty() ? "Vat Pham" : builder.toString();
    }

    private void consumeAndCreate(Set<String> collected, String first, String second, String created) {
        if (!collected.contains(first) || !collected.contains(second)) {
            throw new IllegalArgumentException("Bạn chưa nhặt đủ hai vật phẩm này.");
        }
        collected.add(created);
    }

    private boolean pairMatches(String firstItemKey, String secondItemKey, String left, String right) {
        List<String> selectedPair = List.of(firstItemKey, secondItemKey).stream().sorted().toList();
        List<String> expectedPair = List.of(left, right).stream().sorted().toList();
        return selectedPair.equals(expectedPair);
    }

    private List<CollectibleItem> mergeCollectibles(List<CollectibleItem> baseItems, List<CollectibleItem> extraItems) {
        LinkedHashMap<String, CollectibleItem> merged = new LinkedHashMap<>();
        baseItems.forEach(item -> merged.put(item.getKey(), item));
        extraItems.forEach(item -> merged.putIfAbsent(item.getKey(), item));
        return List.copyOf(merged.values());
    }

    private List<CollectibleItem> getCraftedItemsForRoom(GameRoom room) {
        if (isNightCipherScenario(room)) {
            return List.of(
                    new CollectibleItem(ITEM_UV_MAP, "Bản đồ phát quang", "🗺️"),
                    new CollectibleItem(ITEM_CIPHER_PATH, "Lộ trình mã hóa", "🧭"),
                    new CollectibleItem(ITEM_MOON_STAR_KEY, "Khóa thiên thể", "🌙"),
                    new CollectibleItem(ITEM_SHADOW_KEY, "Chìa bóng đêm", "🔮")
            );
        }
        if (isAncientVaultScenario(room)) {
            return List.of(
                    new CollectibleItem(ITEM_DECODED_TABLET, "Phiến ký tự đã giải mã", "📜"),
                    new CollectibleItem(ITEM_GUARDIAN_SEAL, "Ấn canh gác", "🪙"),
                    new CollectibleItem(ITEM_VAULT_KEY, "Chìa kho cổ vật", "🗝️")
            );
        }
        if (isVictorianScenario(room)) {
            return List.of(
                    new CollectibleItem(ITEM_ROUGH_KEY, "Phôi chìa Victorian", "🧱"),
                    new CollectibleItem(ITEM_POLISHED_KEY, "Bản chìa đánh bóng", "🔧"),
                    new CollectibleItem(ITEM_MASTER_KEY, "Chìa chính Victorian", "🗝️"),
                    new CollectibleItem(ITEM_SEALED_MASTER_KEY, "Chìa niêm ấn", "🔐")
            );
        }
        return List.of();
    }

    @Transactional
    public void interactObject(String username, Long roomId, Long objectId) {
        PlayerRoomProgress progress = getOrCreateProgress(username, roomId);
        RoomObject roomObject = getObject(roomId, objectId);

        if (roomObject.getLockType() != ObjectLockType.INTERACTION) {
            throw new IllegalArgumentException("Vật thể này cần trả lời câu hỏi");
        }

        validateStep(progress, roomObject);

        incrementClick(progress);

        if (isLabScenario(progress.getRoom())) {
            handleLabInteraction(progress, roomObject);
            progress.setScore(progress.getScore() + INTERACTION_SCORE);
            advanceProgress(progress, roomId);
            return;
        }

        Set<String> collectedKeys = parseCsv(progress.getCollectedItems());
        List<RoomKeyConfig> roomKeyConfigs = roomKeyConfigRepository.findByRoomIdOrderByIdAsc(roomId);
        Set<String> requiredSceneKeys = roomKeyConfigs.isEmpty()
            ? getCollectiblesByRoomName(progress.getRoom().getName()).stream()
            .map(CollectibleItem::getKey)
            .collect(Collectors.toSet())
            : roomKeyConfigs.stream().map(RoomKeyConfig::getKeyCode).collect(Collectors.toSet());

        Set<String> state = parseCsv(progress.getDiscoveredClues());
        long discoveredSceneItems = requiredSceneKeys.stream()
            .filter(key -> collectedKeys.contains(key) || state.contains(pickedFlag(key)))
            .count();

        if (discoveredSceneItems < requiredSceneKeys.size()) {
            throw new IllegalArgumentException("Bạn cần tìm đủ đồ chìa khóa trước khi mở cửa.");
        }

        progress.setScore(progress.getScore() + INTERACTION_SCORE);
        advanceProgress(progress, roomId);
    }

    private RoomObject getObject(Long roomId, Long objectId) {
        return roomObjectRepository.findByIdAndRoomId(objectId, roomId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vật thể"));
    }

    private void validateStep(PlayerRoomProgress progress, RoomObject roomObject) {
        if (progress.getCompleted()) {
            throw new IllegalArgumentException("Ván chơi đã kết thúc");
        }
        if (roomObject.getRequiredStep() < progress.getCurrentStep()) {
            throw new IllegalArgumentException("Vật thể này đã được mở ở bước trước");
        }
        if (!roomObject.getRequiredStep().equals(progress.getCurrentStep())) {
            throw new IllegalArgumentException("Bạn chưa thể mở vật thể này, cần hoàn thành đúng thứ tự bước");
        }
    }

    private void advanceProgress(PlayerRoomProgress progress, Long roomId) {
        List<RoomObject> roomObjects = roomObjectRepository.findByRoomIdOrderByRequiredStepAsc(roomId);
        int maxStep = roomObjects.stream()
                .map(RoomObject::getRequiredStep)
                .max(Comparator.naturalOrder())
                .orElse(1);

        int nextStep = progress.getCurrentStep() + 1;
        progress.setCurrentStep(nextStep);
        if (nextStep > maxStep) {
            if (isDoorMazeScenario(progress.getRoom()) && !hasStateFlag(progress, FLAG_DOOR_MAZE_COMPLETED)) {
                activateDoorMaze(progress);
                return;
            }
            finalizeProgress(progress, !hasInvalidCombine(progress));
            return;
        }
        playerRoomProgressRepository.save(progress);
    }

    private boolean hasInvalidCombine(PlayerRoomProgress progress) {
        long badCombineCount = readMetaLong(parseCsv(progress.getDiscoveredClues()), META_BAD_COMBINE, 0);
        return badCombineCount > 0;
    }

    private void activateDoorMaze(PlayerRoomProgress progress) {
        Set<String> state = parseCsv(progress.getDiscoveredClues());
        setStateFlag(state, FLAG_DOOR_MAZE_ACTIVE);
        upsertMeta(state, META_DOOR_LEVEL, "0");
        upsertMeta(state, META_DOOR_DEAD_END, "0");
        upsertMeta(state, META_DOOR_CORRECT, String.valueOf(randomDoorIndex()));
        progress.setDiscoveredClues(joinCsv(state));
        playerRoomProgressRepository.save(progress);
    }

    public Map<String, Object> getDoorMazeState(String username, Long roomId) {
        PlayerRoomProgress progress = getOrCreateProgress(username, roomId);
        Set<String> state = parseCsv(progress.getDiscoveredClues());
        boolean active = state.contains(stateFlag(FLAG_DOOR_MAZE_ACTIVE)) && !Boolean.TRUE.equals(progress.getCompleted());
        boolean deadEnd = readMetaLong(state, META_DOOR_DEAD_END, 0) == 1;

        if (active && deadEnd) {
            // If player re-enters room while in dead-end state, start a fresh round.
            upsertMeta(state, META_DOOR_LEVEL, "0");
            upsertMeta(state, META_DOOR_DEAD_END, "0");
            upsertMeta(state, META_DOOR_CORRECT, String.valueOf(randomDoorIndex()));
            progress.setDiscoveredClues(joinCsv(state));
            playerRoomProgressRepository.save(progress);
            deadEnd = false;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("active", active);
        payload.put("currentLevel", readMetaLong(state, META_DOOR_LEVEL, 0));
        payload.put("totalLevels", DOOR_MAZE_LEVELS);
        payload.put("correctDoor", readMetaLong(state, META_DOOR_CORRECT, 1));
        payload.put("deadEnd", deadEnd);
        return payload;
    }

    @Transactional
    public Map<String, Object> chooseDoor(String username, Long roomId, Integer doorIndex) {
        if (doorIndex == null || doorIndex < 1 || doorIndex > DOOR_MAZE_OPTIONS) {
            throw new IllegalArgumentException("Cửa được chọn không hợp lệ.");
        }

        PlayerRoomProgress progress = getOrCreateProgress(username, roomId);
        if (Boolean.TRUE.equals(progress.getCompleted())) {
            throw new IllegalArgumentException("Ván chơi đã kết thúc.");
        }

        Set<String> state = parseCsv(progress.getDiscoveredClues());
        if (!state.contains(stateFlag(FLAG_DOOR_MAZE_ACTIVE))) {
            throw new IllegalArgumentException("Mê cung cửa chưa được kích hoạt.");
        }
        if (readMetaLong(state, META_DOOR_DEAD_END, 0) == 1) {
            throw new IllegalArgumentException("Bạn đang ở ngõ cụt. Hãy quay về điểm xuất phát để đi lại.");
        }

        incrementClick(progress);
        state = parseCsv(progress.getDiscoveredClues());

        long currentLevel = readMetaLong(state, META_DOOR_LEVEL, 0);
        int correctDoor = (int) readMetaLong(state, META_DOOR_CORRECT, 1);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("finished", false);

        if (doorIndex != correctDoor) {
            upsertMeta(state, META_DOOR_DEAD_END, "1");
            progress.setDiscoveredClues(joinCsv(state));
            playerRoomProgressRepository.save(progress);

            payload.put("success", true);
            payload.put("correct", false);
            payload.put("deadEnd", true);
            payload.put("currentLevel", currentLevel);
            payload.put("totalLevels", DOOR_MAZE_LEVELS);
            payload.put("message", "Bạn đi tới ngõ cụt. Quay về điểm xuất phát để thử lại đường khác.");
            return payload;
        }

        long nextLevel = currentLevel + 1;
        if (nextLevel >= DOOR_MAZE_LEVELS) {
            state.remove(stateFlag(FLAG_DOOR_MAZE_ACTIVE));
            setStateFlag(state, FLAG_DOOR_MAZE_COMPLETED);
            progress.setDiscoveredClues(joinCsv(state));
            finalizeProgress(progress, !hasInvalidCombine(progress));

            payload.put("success", true);
            payload.put("correct", true);
            payload.put("finished", true);
            payload.put("won", Boolean.TRUE.equals(progress.getWon()));
            payload.put("message", Boolean.TRUE.equals(progress.getWon())
                    ? "Bạn đã tìm đúng chuỗi cửa thoát và mở được đường ra!"
                    : "Phản ứng sai trước đó kích hoạt bẫy nổ ở cửa cuối. Bạn đã thua.");
            return payload;
        }

        upsertMeta(state, META_DOOR_LEVEL, String.valueOf(nextLevel));
        upsertMeta(state, META_DOOR_DEAD_END, "0");
        upsertMeta(state, META_DOOR_CORRECT, String.valueOf(randomDoorIndex()));
        progress.setDiscoveredClues(joinCsv(state));
        playerRoomProgressRepository.save(progress);

        payload.put("success", true);
        payload.put("correct", true);
        payload.put("deadEnd", false);
        payload.put("currentLevel", nextLevel);
        payload.put("totalLevels", DOOR_MAZE_LEVELS);
        payload.put("correctDoor", readMetaLong(state, META_DOOR_CORRECT, 1));
        payload.put("message", "Đúng cửa. Tiếp tục tìm lối thoát tiếp theo.");
        return payload;
    }

    @Transactional
    public Map<String, Object> resetDoorMazePath(String username, Long roomId) {
        PlayerRoomProgress progress = getOrCreateProgress(username, roomId);
        if (Boolean.TRUE.equals(progress.getCompleted())) {
            throw new IllegalArgumentException("Ván chơi đã kết thúc.");
        }

        Set<String> state = parseCsv(progress.getDiscoveredClues());
        if (!state.contains(stateFlag(FLAG_DOOR_MAZE_ACTIVE))) {
            throw new IllegalArgumentException("Mê cung cửa chưa được kích hoạt.");
        }
        if (readMetaLong(state, META_DOOR_DEAD_END, 0) != 1) {
            throw new IllegalArgumentException("Bạn chưa ở ngõ cụt để quay về.");
        }

        upsertMeta(state, META_DOOR_LEVEL, "0");
        upsertMeta(state, META_DOOR_DEAD_END, "0");
        upsertMeta(state, META_DOOR_CORRECT, String.valueOf(randomDoorIndex()));
        progress.setDiscoveredClues(joinCsv(state));
        playerRoomProgressRepository.save(progress);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("success", true);
        payload.put("deadEnd", false);
        payload.put("currentLevel", 0);
        payload.put("totalLevels", DOOR_MAZE_LEVELS);
        payload.put("correctDoor", readMetaLong(state, META_DOOR_CORRECT, 1));
        payload.put("message", "Bạn đã quay về điểm xuất phát. Lối đúng đã đổi vị trí.");
        return payload;
    }

    private int randomDoorIndex() {
        return ThreadLocalRandom.current().nextInt(1, DOOR_MAZE_OPTIONS + 1);
    }

    private void finalizeProgress(PlayerRoomProgress progress, boolean won) {
        if (progress.getResultFinalized()) {
            return;
        }

        Set<String> state = parseCsv(progress.getDiscoveredClues());
        upsertMeta(state, META_FINISH, String.valueOf(System.currentTimeMillis()));
        progress.setDiscoveredClues(joinCsv(state));

        progress.setCompleted(true);
        progress.setWon(won);
        progress.setResultFinalized(true);
        playerRoomProgressRepository.save(progress);

        PlayerProfile player = progress.getPlayer();

        Integer previousBestWinScore = playHistoryRepository
                .findTopByPlayerIdAndRoomIdAndResultOrderByScoreDescPlayedAtDesc(
                        player.getId(),
                        progress.getRoom().getId(),
                        PlayResult.WIN
                )
                .map(PlayHistory::getScore)
                .orElse(null);

        if (won) {
            if (previousBestWinScore == null) {
                player.setTotalScore(player.getTotalScore() + progress.getScore());
            } else if (progress.getScore() > previousBestWinScore) {
                player.setTotalScore(player.getTotalScore() + (progress.getScore() - previousBestWinScore));
            }
        }
        if (won) {
            player.setTotalWin(player.getTotalWin() + 1);
        }
        playerProfileRepository.save(player);

        PlayHistory history = new PlayHistory();
        history.setPlayer(player);
        history.setRoom(progress.getRoom());
        history.setScore(progress.getScore());
        history.setResult(won ? PlayResult.WIN : PlayResult.LOSE);
        history.setActionCount((int) readMetaLong(state, META_CLICK, 0));
        history.setPlayedAt(java.time.LocalDateTime.now());
        playHistoryRepository.save(history);
    }

    @Transactional
    public void restartRoomProgress(String username, Long roomId) {
        PlayerRoomProgress progress = getOrCreateProgress(username, roomId);
        if (!Boolean.TRUE.equals(progress.getCompleted())) {
            throw new IllegalArgumentException("Chỉ có thể chơi lại phòng đã hoàn thành.");
        }

        progress.setCurrentStep(1);
        progress.setCompleted(false);
        progress.setWon(null);
        progress.setResultFinalized(false);
        progress.setWrongAttempts(0);
        progress.setScore(BASE_ROOM_SCORE);
        progress.setCollectedItems("");

        Set<String> state = new LinkedHashSet<>();
        upsertMeta(state, META_CLICK, "0");
        upsertMeta(state, META_START, String.valueOf(System.currentTimeMillis()));
        progress.setDiscoveredClues(joinCsv(state));
        playerRoomProgressRepository.save(progress);
    }

    public int getMaxWrongAttempts() {
        return MAX_WRONG_ATTEMPTS;
    }

    @Transactional
    public Map<String, Object> useHint(String username, Long roomId) {
        PlayerRoomProgress progress = getOrCreateProgress(username, roomId);
        if (progress.getCompleted()) {
            throw new IllegalArgumentException("Ván chơi đã kết thúc, không thể dùng gợi ý nữa.");
        }

        List<String> hints = getSearchHints(roomId);
        String hintMessage = hints.isEmpty()
                ? "Hãy quan sát các hotspot đang phát sáng để tìm manh mối."
                : hints.get((int) (System.currentTimeMillis() % hints.size()));

        progress.setScore(Math.max(0, progress.getScore() - HINT_PENALTY));
        incrementClick(progress);

        Set<String> discovered = parseCsv(progress.getDiscoveredClues());
        discovered.add("Gợi ý(H): " + hintMessage);
        progress.setDiscoveredClues(joinCsv(discovered));
        playerRoomProgressRepository.save(progress);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("hint", hintMessage);
        payload.put("score", progress.getScore());
        payload.put("penalty", HINT_PENALTY);
        return payload;
    }

    public int getActionCount(String username, Long roomId) {
        PlayerRoomProgress progress = getOrCreateProgress(username, roomId);
        return (int) readMetaLong(parseCsv(progress.getDiscoveredClues()), META_CLICK, 0);
    }

    public long getElapsedSeconds(String username, Long roomId) {
        PlayerRoomProgress progress = getOrCreateProgress(username, roomId);
        Set<String> state = parseCsv(progress.getDiscoveredClues());
        long started = readMetaLong(state, META_START, System.currentTimeMillis());
        long finished = readMetaLong(state, META_FINISH, System.currentTimeMillis());
        if (finished < started) {
            finished = System.currentTimeMillis();
        }
        return Math.max(0, (finished - started) / 1000);
    }

    private PlayerProfile getPlayer(String username) {
        return playerProfileRepository.findByAccountUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hồ sơ người chơi"));
    }

    private List<CollectibleItem> getCollectiblesByRoomName(String roomName) {
        String normalized = roomName == null ? "" : roomName.toLowerCase();

        if (normalized.contains("mật mã")) {
            return List.of(
                    new CollectibleItem("map_fragment", "Mảnh bản đồ", "🗺️"),
                new CollectibleItem("uv_lamp", "Đèn UV", "💡"),
                new CollectibleItem("cipher_disk", "Đĩa mật mã", "💿"),
                new CollectibleItem("rune_note", "Giấy rune", "📜")
            );
        }

        if (normalized.contains("cổ vật")) {
            return List.of(
                    new CollectibleItem("stone_tablet", "Phiến đá cổ", "🪨"),
                new CollectibleItem("charcoal", "Than đồ nét", "🧱"),
                new CollectibleItem("bronze_key", "Chìa đồng cổ", "🗝️"),
                new CollectibleItem("ancient_ruby", "Hồng ngọc cổ", "💎")
            );
        }

        if (normalized.contains("victorian") || normalized.contains("nghiên cứu")) {
            return List.of(
                new CollectibleItem("clock_gear", "Bánh răng đồng hồ", "⚙️"),
                new CollectibleItem("old_blueprint", "Bản thiết kế cũ", "📘"),
                new CollectibleItem("brass_rod", "Thanh đồng", "🛠️")
            );
        }

        return List.of(
                new CollectibleItem("gear", "Bánh răng", "⚙️"),
            new CollectibleItem("wire", "Cuộn dây", "🧵"),
            new CollectibleItem("fuse", "Cầu chì", "🔋")
        );
    }

    private Map<String, int[]> getSpotLayoutByRoomName(String roomName) {
        String normalized = roomName == null ? "" : roomName.toLowerCase();
        Map<String, int[]> spots = new HashMap<>();

        if (normalized.contains("thí nghiệm")) {
            spots.put("microscope", new int[]{24, 34});
            spots.put("slide", new int[]{68, 53});
            return spots;
        }
        if (normalized.contains("mật mã")) {
            spots.put("map_fragment", new int[]{31, 47});
            spots.put("uv_lamp", new int[]{74, 31});
            spots.put("cipher_disk", new int[]{22, 66});
            spots.put("rune_note", new int[]{63, 58});
            return spots;
        }
        if (normalized.contains("cổ vật")) {
            spots.put("stone_tablet", new int[]{62, 42});
            spots.put("charcoal", new int[]{21, 58});
            spots.put("bronze_key", new int[]{74, 30});
            spots.put("ancient_ruby", new int[]{38, 28});
            return spots;
        }
        if (normalized.contains("victorian") || normalized.contains("nghiên cứu")) {
            spots.put("clock_gear", new int[]{24, 48});
            spots.put("old_blueprint", new int[]{56, 42});
            spots.put("brass_rod", new int[]{78, 63});
            return spots;
        }

        spots.put("gear", new int[]{72, 54});
        spots.put("wire", new int[]{28, 39});
        spots.put("fuse", new int[]{45, 65});
        return spots;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String canonicalizeItemKey(String rawKey) {
        if (!hasText(rawKey)) {
            return "";
        }
        String normalized = rawKey.trim().toLowerCase(Locale.ROOT).replace('-', '_');
        return switch (normalized) {
            case "whitepaper", "paper", "white_paper_item" -> ITEM_WHITE_PAPER;
            case "bluesolution", "blue_liquid", "blue_solution_item" -> ITEM_BLUE_SOLUTION;
            case "yellowpowder", "yellow_powder_item" -> ITEM_YELLOW_POWDER;
            case "glasscup", "cup" -> ITEM_GLASS_CUP;
            default -> normalized;
        };
    }

    private String normalizeCode(String rawCode) {
        return rawCode == null ? "" : rawCode.replaceAll("\\s+", "").toUpperCase();
    }

    private Set<String> parseCsv(String csv) {
        if (!hasText(csv)) {
            return new LinkedHashSet<>();
        }

        return java.util.Arrays.stream(csv.split("\\|"))
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String joinCsv(Set<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }

        return values.stream().collect(Collectors.joining("|"));
    }

    private String collectLabItem(PlayerRoomProgress progress, Long roomId, String itemKey) {
        if (!LAB_ITEM_CATALOG.containsKey(itemKey)) {
            throw new IllegalArgumentException("Vật phẩm không tồn tại trong phòng này.");
        }

        if (!isLabSceneItem(itemKey)) {
            throw new IllegalArgumentException("Vật phẩm này không thể nhặt trực tiếp từ khung cảnh.");
        }

        if (!isLabItemVisible(progress, itemKey)) {
            if (ITEM_YELLOW_POWDER.equals(itemKey)) {
                throw new IllegalArgumentException("Lọ bột vàng đang giấu trong tủ lạnh, hãy mở đúng ngăn để tìm.");
            }
            throw new IllegalArgumentException("Vật phẩm này chưa thể lấy ở thời điểm hiện tại.");
        }

        Set<String> collected = parseCsv(progress.getCollectedItems());
        Set<String> state = parseCsv(progress.getDiscoveredClues());
        if (state.contains(pickedFlag(itemKey))) {
            return "Bạn đã nhặt vật phẩm này rồi.";
        }
        if (collected.contains(itemKey)) {
            return "Bạn đã nhặt vật phẩm này rồi.";
        }

        collected.add(itemKey);
        state.add(pickedFlag(itemKey));
        progress.setCollectedItems(joinCsv(collected));
        progress.setDiscoveredClues(joinCsv(state));
        progress.setScore(progress.getScore() + COLLECT_ITEM_SCORE);
        incrementClick(progress);
        playerRoomProgressRepository.save(progress);
        return "Đã thêm vật phẩm vào túi đồ.";
    }

    private String combineItemsForLab(PlayerRoomProgress progress, String firstItemKey, String secondItemKey) {
        List<String> pair = List.of(firstItemKey.trim().toLowerCase(), secondItemKey.trim().toLowerCase()).stream().sorted().toList();
        incrementClick(progress);

        Set<String> collected = parseCsv(progress.getCollectedItems());
        Set<String> state = parseCsv(progress.getDiscoveredClues());

        if (pair.equals(List.of(ITEM_PENCIL, ITEM_WHITE_PAPER).stream().sorted().toList())) {
            if (!hasStateFlag(progress, FLAG_PAPER_REVEALED)) {
                if (!collected.contains(ITEM_PENCIL) || !collected.contains(ITEM_WHITE_PAPER)) {
                    throw new IllegalArgumentException("Bạn cần bút chì và tờ giấy trắng để làm lộ mã.");
                }

                // Keep original items and also grant the clue-note item.
                collected.add(ITEM_CODE_CLUE_NOTE);

                setStateFlag(state, FLAG_PAPER_REVEALED);
                state.add("Các con số ẩn đã hiện lên trên tờ giấy.");
                state.add("Bạn nhận được Mảnh ghi chú mã số.");
                progress.setCollectedItems(joinCsv(collected));
                progress.setDiscoveredClues(joinCsv(state));
                playerRoomProgressRepository.save(progress);
                return "Những nét chì làm hiện ra các con số ẩn! Bút chì và giấy đã đổi thành Mảnh ghi chú mã số.";
            }
            return "Các con số ẩn đã hiện rõ trên tờ giấy rồi.";
        }

        if (pair.equals(List.of(ITEM_BLUE_SOLUTION, ITEM_GLASS_CUP).stream().sorted().toList())) {
            if (!collected.contains(ITEM_BLUE_SOLUTION) || !collected.contains(ITEM_GLASS_CUP)) {
                throw new IllegalArgumentException("Bạn chưa có đủ Cốc thủy tinh và dung dịch xanh.");
            }
            collected.add(ITEM_BLUE_MIX);
            progress.setCollectedItems(joinCsv(collected));
            playerRoomProgressRepository.save(progress);
            return "Bạn pha được cốc dung dịch xanh, cần thêm thành phần nữa để kích hoạt máy quét.";
        }

        if (pair.equals(List.of(ITEM_BLUE_MIX, ITEM_YELLOW_POWDER).stream().sorted().toList())) {
            if (!collected.contains(ITEM_BLUE_MIX) || !collected.contains(ITEM_YELLOW_POWDER)) {
                throw new IllegalArgumentException("Bạn cần cốc dung dịch xanh và lọ bột vàng.");
            }
            collected.add(ITEM_PURPLE_MIX);
            collected.add(ITEM_KEYCARD);
            state.add("Dung dịch đã chuyển sang màu tím.");
            state.add("Bạn phát hiện một thẻ từ EXIT nằm ngay sau hũ bột vàng.");
            progress.setCollectedItems(joinCsv(collected));
            progress.setDiscoveredClues(joinCsv(state));
            playerRoomProgressRepository.save(progress);
            return "Dung dịch đổi sang màu tím! Bây giờ có thể đổ vào phễu máy quét.";
        }

        return registerUncertainCombine(progress, firstItemKey, secondItemKey);
    }

    private void handleLabInteraction(PlayerRoomProgress progress, RoomObject roomObject) {
        Set<String> collected = parseCsv(progress.getCollectedItems());
        Set<String> state = parseCsv(progress.getDiscoveredClues());

        if (roomObject.getRequiredStep() == 2) {
            if (!collected.contains(ITEM_LENS) || !collected.contains(ITEM_SLIDE)) {
                throw new IllegalArgumentException("Bạn cần lắp Ống kính và đặt Mẫu vật trước khi soi kính hiển vi.");
            }
            setStateFlag(state, FLAG_MICROSCOPE_FIXED);
            state.add("Vi khuẩn xếp thành hình Tam Giác.");
            progress.setDiscoveredClues(joinCsv(state));
            return;
        }

        if (roomObject.getRequiredStep() == 3) {
            if (!hasStateFlag(progress, FLAG_MICROSCOPE_FIXED)) {
                throw new IllegalArgumentException("Bạn cần soi kính hiển vi để lấy gợi ý hình dạng trước.");
            }
            if (!collected.contains(ITEM_PURPLE_MIX)) {
                throw new IllegalArgumentException("Bạn cần dung dịch màu tím để kích hoạt máy quét.");
            }
            collected.remove(ITEM_PURPLE_MIX);
            progress.setCollectedItems(joinCsv(collected));
            setStateFlag(state, FLAG_SCANNER_ACTIVE);
            progress.setDiscoveredClues(joinCsv(state));
            return;
        }

        throw new IllegalArgumentException("Vật thể này chưa thể tương tác trong kịch bản phòng thí nghiệm.");
    }

    private List<CollectibleSpot> getLabCollectibleSpots(PlayerRoomProgress progress, Long roomId) {
        Set<String> collected = parseCsv(progress.getCollectedItems());
        List<RoomKeyConfig> roomKeyConfigs = roomKeyConfigRepository.findByRoomIdOrderByIdAsc(roomId);
        Set<String> state = parseCsv(progress.getDiscoveredClues());
        return roomKeyConfigs.stream()
                .filter(config -> isLabSceneItem(config.getKeyCode()))
                .filter(config -> isLabItemVisible(progress, config.getKeyCode()))
                .map(config -> new CollectibleSpot(
                        config.getKeyCode(),
                        config.getKeyName(),
                        config.getSpotX(),
                        config.getSpotY(),
                    hasText(config.getImageUrl()) ? config.getImageUrl() : getLabDefaultImage(config.getKeyCode()),
                collected.contains(config.getKeyCode()) || state.contains(pickedFlag(config.getKeyCode()))
                ))
                .toList();
    }

    private boolean isLabSceneItem(String key) {
        return ITEM_LENS.equals(key)
                || ITEM_PENCIL.equals(key)
                || ITEM_WHITE_PAPER.equals(key)
                || ITEM_GLASS_CUP.equals(key)
                || ITEM_YELLOW_POWDER.equals(key);
    }

    private boolean isLabItemVisible(PlayerRoomProgress progress, String key) {
        if (ITEM_YELLOW_POWDER.equals(key)) {
            return progress.getCurrentStep() >= 3;
        }
        return true;
    }

    private void unlockCabinetRewards(PlayerRoomProgress progress) {
        Set<String> collected = parseCsv(progress.getCollectedItems());
        Set<String> state = parseCsv(progress.getDiscoveredClues());

        setStateFlag(state, FLAG_CABINET_OPEN);
        collected.add(ITEM_SLIDE);
        collected.add(ITEM_BLUE_SOLUTION);
        state.add("Tủ mở! Bạn nhận được Mẫu vật và chai dung dịch xanh.");

        progress.setCollectedItems(joinCsv(collected));
        progress.setDiscoveredClues(joinCsv(state));
        progress.setScore(progress.getScore() + COLLECT_ITEM_SCORE * 2);
    }

    private void incrementClick(PlayerRoomProgress progress) {
        Set<String> state = parseCsv(progress.getDiscoveredClues());
        long current = readMetaLong(state, META_CLICK, 0);
        upsertMeta(state, META_CLICK, String.valueOf(current + 1));
        if (readMetaLong(state, META_START, 0) == 0) {
            upsertMeta(state, META_START, String.valueOf(System.currentTimeMillis()));
        }
        progress.setDiscoveredClues(joinCsv(state));
    }

    private long readMetaLong(Set<String> state, String keyPrefix, long defaultValue) {
        return state.stream()
                .filter(token -> token.startsWith(keyPrefix))
                .findFirst()
                .map(token -> token.substring(keyPrefix.length()))
                .map(value -> {
                    try {
                        return Long.parseLong(value);
                    } catch (NumberFormatException ex) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }

    private void upsertMeta(Set<String> state, String keyPrefix, String value) {
        state.removeIf(token -> token.startsWith(keyPrefix));
        state.add(keyPrefix + value);
    }

    private boolean hasStateFlag(PlayerRoomProgress progress, String flag) {
        Set<String> state = parseCsv(progress.getDiscoveredClues());
        return state.contains(stateFlag(flag));
    }

    private void setStateFlag(Set<String> state, String flag) {
        state.add(stateFlag(flag));
    }

    private String stateFlag(String flag) {
        return "state:" + flag;
    }

    private String pickedFlag(String itemKey) {
        return STATE_PICKED_PREFIX + itemKey;
    }

    private void upsertItemLabel(Set<String> state, String itemKey, String itemLabel) {
        state.removeIf(token -> token.startsWith(META_ITEM_LABEL + itemKey + "="));
        state.add(META_ITEM_LABEL + itemKey + "=" + itemLabel);
    }

    private Map<String, String> parseItemLabels(Set<String> state) {
        Map<String, String> labels = new HashMap<>();
        state.stream()
                .filter(token -> token.startsWith(META_ITEM_LABEL))
                .forEach(token -> {
                    String content = token.substring(META_ITEM_LABEL.length());
                    int separator = content.indexOf('=');
                    if (separator <= 0 || separator >= content.length() - 1) {
                        return;
                    }
                    labels.put(content.substring(0, separator), content.substring(separator + 1));
                });
        return labels;
    }

    private CollectibleItem buildFallbackCollectible(String key, String label) {
        String displayName = hasText(label) ? label : toDisplayToken(key);
        return new CollectibleItem(key, displayName, "🧪");
    }

    private boolean isLabScenario(GameRoom room) {
        String roomName = room.getName() == null ? "" : room.getName().toLowerCase();
        return roomName.contains(SCENARIO_ROOM_KEYWORD) || roomName.contains("thí nghiệm bị ngắt quãng");
    }

    private boolean isDoorMazeScenario(GameRoom room) {
        return isLabScenario(room);
    }

    private boolean isNightCipherScenario(GameRoom room) {
        String roomName = room.getName() == null ? "" : room.getName().toLowerCase();
        return roomName.contains("mật mã bóng đêm");
    }

    private boolean isAncientVaultScenario(GameRoom room) {
        String roomName = room.getName() == null ? "" : room.getName().toLowerCase();
        return roomName.contains("kho cổ vật");
    }

    private boolean isVictorianScenario(GameRoom room) {
        String roomName = room.getName() == null ? "" : room.getName().toLowerCase();
        return roomName.contains("victorian") || roomName.contains("nghiên cứu");
    }

    private static Map<String, CollectibleItem> createLabItemCatalog() {
        Map<String, CollectibleItem> catalog = new LinkedHashMap<>();
        catalog.put(ITEM_LENS, new CollectibleItem(ITEM_LENS, "Ống kính", "/images/items/lens.svg"));
        catalog.put(ITEM_PENCIL, new CollectibleItem(ITEM_PENCIL, "Bút chì", "/images/items/pencil.svg"));
        catalog.put(ITEM_WHITE_PAPER, new CollectibleItem(ITEM_WHITE_PAPER, "Tờ giấy trắng", "/images/items/white-paper.svg"));
        catalog.put(ITEM_CODE_CLUE_NOTE, new CollectibleItem(ITEM_CODE_CLUE_NOTE, "Mảnh ghi chú mã số", "/images/items/lab-scene/code-clue-note.svg"));
        catalog.put(ITEM_GLASS_CUP, new CollectibleItem(ITEM_GLASS_CUP, "Cốc thủy tinh", "/images/items/glass-cup.svg"));
        catalog.put(ITEM_YELLOW_POWDER, new CollectibleItem(ITEM_YELLOW_POWDER, "Lọ bột màu vàng", "/images/items/yellow-powder.svg"));
        catalog.put(ITEM_SLIDE, new CollectibleItem(ITEM_SLIDE, "Mẫu vật (Slide)", "/images/items/slide.svg"));
        catalog.put(ITEM_BLUE_SOLUTION, new CollectibleItem(ITEM_BLUE_SOLUTION, "Dung dịch màu xanh", "/images/items/blue-solution.svg"));
        catalog.put(ITEM_BLUE_MIX, new CollectibleItem(ITEM_BLUE_MIX, "Cốc dung dịch xanh", "/images/items/blue-mix.svg"));
        catalog.put(ITEM_PURPLE_MIX, new CollectibleItem(ITEM_PURPLE_MIX, "Cốc dung dịch tím", "/images/items/purple-mix.svg"));
        catalog.put(ITEM_KEYCARD, new CollectibleItem(ITEM_KEYCARD, "Thẻ từ EXIT", "/images/items/keycard.svg"));
        return catalog;
    }

    private String getLabDefaultImage(String keyCode) {
        CollectibleItem item = LAB_ITEM_CATALOG.get(keyCode);
        if (item == null) {
            return null;
        }
        String icon = item.getIcon();
        return hasText(icon) && icon.startsWith("/") ? icon : null;
    }
}
