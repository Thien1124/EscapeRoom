package com.example.gamegiaido.controller;

import com.example.gamegiaido.dto.StartGameForm;
import com.example.gamegiaido.dto.AnswerForm;
import com.example.gamegiaido.dto.RoomMapItem;
import com.example.gamegiaido.model.GameMode;
import com.example.gamegiaido.model.GameRoom;
import com.example.gamegiaido.model.PlayerRoomProgress;
import com.example.gamegiaido.model.QuizQuestion;
import com.example.gamegiaido.repository.GameRoomRepository;
import com.example.gamegiaido.repository.QuizTopicRepository;
import com.example.gamegiaido.service.GamePlayService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class GameController {

    private final QuizTopicRepository quizTopicRepository;
    private final GameRoomRepository gameRoomRepository;
    private final GamePlayService gamePlayService;

    public GameController(QuizTopicRepository quizTopicRepository,
                          GameRoomRepository gameRoomRepository,
                          GamePlayService gamePlayService) {
        this.quizTopicRepository = quizTopicRepository;
        this.gameRoomRepository = gameRoomRepository;
        this.gamePlayService = gamePlayService;
    }

    @GetMapping("/game/start")
    public String showStartGame(Model model) {
        model.addAttribute("startGameForm", new StartGameForm());
        model.addAttribute("topics", quizTopicRepository.findAll());
        model.addAttribute("modes", GameMode.values());
        return "start-game";
    }

    @PostMapping("/game/start")
    public String startGame(@Valid @ModelAttribute("startGameForm") StartGameForm form,
                            BindingResult bindingResult,
                            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("topics", quizTopicRepository.findAll());
            model.addAttribute("modes", GameMode.values());
            return "start-game";
        }

        return "redirect:/game/map?topicId=" + form.getTopicId() + "&mode=" + form.getMode().name();
    }

        @GetMapping("/game/map")
        public String showGameMap(@RequestParam Long topicId,
                      @RequestParam GameMode mode,
                      Authentication authentication,
                      Model model) {
        model.addAttribute("topic", quizTopicRepository.findById(topicId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chủ đề")));
        model.addAttribute("mode", mode);
        model.addAttribute("roomMap", gamePlayService.getRoomMap(authentication.getName(), topicId, mode));
        return "game-map";
        }

    @GetMapping("/game/rooms/{roomId}")
    public String showRoom(@PathVariable Long roomId,
                           @RequestParam(required = false) Long quizObjectId,
                           @RequestParam(required = false, defaultValue = "false") boolean replay,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        GameRoom room = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng"));

        if (!gamePlayService.canAccessRoom(authentication.getName(), room)) {
            redirectAttributes.addFlashAttribute("roomError", "Bạn cần hoàn thành phòng trước để mở phòng này.");
            return "redirect:/game/map?topicId=" + room.getTopic().getId() + "&mode=" + room.getMode().name();
        }

        if (replay) {
            try {
                gamePlayService.restartRoomProgress(authentication.getName(), roomId);
                redirectAttributes.addFlashAttribute("roomSuccess", "Đã bắt đầu chơi lại phòng để cải thiện điểm.");
                return "redirect:/game/rooms/" + roomId;
            } catch (IllegalArgumentException ex) {
                redirectAttributes.addFlashAttribute("roomError", ex.getMessage());
                return "redirect:/game/rooms/" + roomId;
            }
        }

        PlayerRoomProgress progress = gamePlayService.getOrCreateProgress(authentication.getName(), roomId);
        GameRoom nextRoom = gamePlayService.getNextRoom(room);
        List<RoomMapItem> roomMap = gamePlayService.getRoomMap(authentication.getName(), room.getTopic().getId(), room.getMode());
        GameRoom previousRoom = null;
        GameRoom nextNavigableRoom = null;
        for (int i = 0; i < roomMap.size(); i++) {
            RoomMapItem item = roomMap.get(i);
            if (!item.getRoom().getId().equals(roomId)) {
                continue;
            }
            if (i > 0 && roomMap.get(i - 1).isUnlocked()) {
                previousRoom = roomMap.get(i - 1).getRoom();
            }
            if (i < roomMap.size() - 1 && roomMap.get(i + 1).isUnlocked()) {
                nextNavigableRoom = roomMap.get(i + 1).getRoom();
            }
            break;
        }
        var objects = gamePlayService.getObjects(roomId);

        QuizQuestion inlineQuestion = null;
        Long inlineQuestionObjectId = null;
        if (quizObjectId != null && !progress.getCompleted()) {
            boolean canOpenInlineQuiz = objects.stream()
                    .anyMatch(obj -> obj.getId().equals(quizObjectId)
                            && obj.getRequiredStep().equals(progress.getCurrentStep())
                            && obj.getLockType().name().equals("QUIZ"));

            if (canOpenInlineQuiz) {
                try {
                    inlineQuestion = gamePlayService.getQuestion(roomId, quizObjectId);
                    inlineQuestionObjectId = quizObjectId;
                } catch (IllegalArgumentException ex) {
                    model.addAttribute("roomError", ex.getMessage());
                }
            } else {
                model.addAttribute("roomError", "Bạn chưa thể mở câu hỏi này ở bước hiện tại.");
            }
        }

        model.addAttribute("room", room);
        model.addAttribute("objects", objects);
        model.addAttribute("progress", progress);
        model.addAttribute("nextRoom", nextRoom);
        model.addAttribute("previousRoom", previousRoom);
        model.addAttribute("nextNavigableRoom", nextNavigableRoom);
        model.addAttribute("maxWrongAttempts", gamePlayService.getMaxWrongAttempts());
        model.addAttribute("playerName", progress.getPlayer().getDisplayName());
        model.addAttribute("inlineQuestion", inlineQuestion);
        model.addAttribute("inlineQuestionObjectId", inlineQuestionObjectId);
        model.addAttribute("answerForm", new AnswerForm());
        model.addAttribute("collectibles", gamePlayService.getCollectibles(roomId));
        model.addAttribute("inventoryItems", gamePlayService.getCollectedItems(authentication.getName(), roomId));
        model.addAttribute("inventoryItemKeys", gamePlayService.getCollectedItemKeys(authentication.getName(), roomId));
        model.addAttribute("collectibleSpots", gamePlayService.getCollectibleSpots(authentication.getName(), roomId));
        model.addAttribute("searchHints", gamePlayService.getSearchHints(roomId));
        model.addAttribute("discoveredClues", gamePlayService.getDiscoveredClues(authentication.getName(), roomId));
        model.addAttribute("actionCount", gamePlayService.getActionCount(authentication.getName(), roomId));
        model.addAttribute("elapsedSeconds", gamePlayService.getElapsedSeconds(authentication.getName(), roomId));
        model.addAttribute("doorMazeState", gamePlayService.getDoorMazeState(authentication.getName(), roomId));
        return "room-view";
    }

    @GetMapping("/game/rooms/{roomId}/objects/{objectId}/quiz")
    public String showQuizQuestion(@PathVariable Long roomId,
                                   @PathVariable Long objectId,
                                   Authentication authentication,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        return "redirect:/game/rooms/" + roomId + "?quizObjectId=" + objectId;
    }

    @PostMapping("/game/rooms/{roomId}/objects/{objectId}/quiz")
    public String submitQuiz(@PathVariable Long roomId,
                             @PathVariable Long objectId,
                             @ModelAttribute("answerForm") AnswerForm answerForm,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            boolean isCorrect = gamePlayService.submitQuizAnswer(
                    authentication.getName(),
                    roomId,
                    objectId,
                    answerForm.getSelectedOption(),
                    answerForm.getAnswerCode());

            if (isCorrect) {
                PlayerRoomProgress progress = gamePlayService.getOrCreateProgress(authentication.getName(), roomId);
                if (Boolean.TRUE.equals(progress.getCompleted()) && !Boolean.TRUE.equals(progress.getWon())) {
                    redirectAttributes.addFlashAttribute("roomError", "Phản ứng kết hợp sai đã kích hoạt nổ ở giai đoạn cuối. Bạn đã thua màn này.");
                } else {
                    redirectAttributes.addFlashAttribute("roomSuccess", "Trả lời đúng! Vật thể đã được mở khóa.");
                }
            } else {
                PlayerRoomProgress progress = gamePlayService.getOrCreateProgress(authentication.getName(), roomId);
                int remaining = Math.max(0, gamePlayService.getMaxWrongAttempts() - progress.getWrongAttempts());
                redirectAttributes.addFlashAttribute("roomError", "Sai đáp án, bạn còn " + remaining + " lượt. Nhấn H trong game để xem gợi ý.");
            }
            return "redirect:/game/rooms/" + roomId;
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("roomError", ex.getMessage());
            return "redirect:/game/rooms/" + roomId;
        }
    }

    @PostMapping("/game/rooms/{roomId}/collect")
    public String collectItem(@PathVariable Long roomId,
                              @RequestParam String itemKey,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            String message = gamePlayService.collectItem(authentication.getName(), roomId, itemKey);
            redirectAttributes.addFlashAttribute("roomSuccess", message);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("roomError", ex.getMessage());
        }
        return "redirect:/game/rooms/" + roomId;
    }

    @PostMapping("/game/rooms/{roomId}/collect-ajax")
    @ResponseBody
    public java.util.Map<String, Object> collectItemAjax(@PathVariable Long roomId,
                                                          @RequestParam String itemKey,
                                                          Authentication authentication) {
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        try {
            String message = gamePlayService.collectItem(authentication.getName(), roomId, itemKey);
            var inventoryItems = gamePlayService.getCollectedItems(authentication.getName(), roomId);
            int inventoryCount = inventoryItems.size();
            var collectedItem = inventoryItems.stream()
                    .filter(item -> item.getKey().equalsIgnoreCase(itemKey))
                    .findFirst()
                    .orElse(null);
            payload.put("success", true);
            payload.put("message", message);
            payload.put("inventoryCount", inventoryCount);
                payload.put("itemKey", collectedItem != null ? collectedItem.getKey() : itemKey);
            if (collectedItem != null) {
                payload.put("itemName", collectedItem.getName());
                payload.put("itemIcon", collectedItem.getIcon());
            }
        } catch (IllegalArgumentException ex) {
            payload.put("success", false);
            payload.put("message", ex.getMessage());
        }
        return payload;
    }

    @PostMapping("/game/rooms/{roomId}/combine-ajax")
    @ResponseBody
    public java.util.Map<String, Object> combineItemsAjax(@PathVariable Long roomId,
                                                           @RequestParam String firstItem,
                                                           @RequestParam String secondItem,
                                                           Authentication authentication) {
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        try {
            String clue = gamePlayService.combineItems(authentication.getName(), roomId, firstItem, secondItem);
            var inventoryItems = gamePlayService.getCollectedItems(authentication.getName(), roomId);

            payload.put("success", true);
            payload.put("message", "Kết hợp thành công! " + clue);
            payload.put("inventoryCount", inventoryItems.size());
            payload.put("inventoryItems", inventoryItems.stream().map(item -> {
                java.util.Map<String, Object> itemMap = new java.util.HashMap<>();
                itemMap.put("key", item.getKey());
                itemMap.put("name", item.getName());
                itemMap.put("icon", item.getIcon());
                return itemMap;
            }).toList());
        } catch (IllegalArgumentException ex) {
            payload.put("success", false);
            payload.put("message", ex.getMessage());
        } catch (Exception ex) {
            payload.put("success", false);
            payload.put("message", "Không thể kết hợp vật phẩm lúc này, hãy thử lại.");
        }
        return payload;
    }

    @PostMapping("/game/rooms/{roomId}/maze-door-ajax")
    @ResponseBody
    public java.util.Map<String, Object> chooseMazeDoor(@PathVariable Long roomId,
                                                         @RequestParam Integer doorIndex,
                                                         Authentication authentication) {
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        try {
            payload.putAll(gamePlayService.chooseDoor(authentication.getName(), roomId, doorIndex));
        } catch (IllegalArgumentException ex) {
            payload.put("success", false);
            payload.put("message", ex.getMessage());
        } catch (Exception ex) {
            payload.put("success", false);
            payload.put("message", "Không thể xử lý lựa chọn cửa lúc này.");
        }
        return payload;
    }

    @PostMapping("/game/rooms/{roomId}/maze-door-reset-ajax")
    @ResponseBody
    public java.util.Map<String, Object> resetMazeDoorPath(@PathVariable Long roomId,
                                                            Authentication authentication) {
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        try {
            payload.putAll(gamePlayService.resetDoorMazePath(authentication.getName(), roomId));
        } catch (IllegalArgumentException ex) {
            payload.put("success", false);
            payload.put("message", ex.getMessage());
        } catch (Exception ex) {
            payload.put("success", false);
            payload.put("message", "Không thể quay về điểm xuất phát lúc này.");
        }
        return payload;
    }

    @PostMapping("/game/rooms/{roomId}/hint-ajax")
    @ResponseBody
    public java.util.Map<String, Object> useHintAjax(@PathVariable Long roomId,
                                                      Authentication authentication) {
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        try {
            java.util.Map<String, Object> hintResult = gamePlayService.useHint(authentication.getName(), roomId);
            payload.put("success", true);
            payload.put("hint", hintResult.get("hint"));
            payload.put("score", hintResult.get("score"));
            payload.put("penalty", hintResult.get("penalty"));
            payload.put("message", "Đã dùng gợi ý (-" + hintResult.get("penalty") + " điểm).");
        } catch (IllegalArgumentException ex) {
            payload.put("success", false);
            payload.put("message", ex.getMessage());
        } catch (Exception ex) {
            payload.put("success", false);
            payload.put("message", "Không thể lấy gợi ý lúc này, vui lòng thử lại.");
        }
        return payload;
    }

    @PostMapping("/game/rooms/{roomId}/combine")
    public String combineItems(@PathVariable Long roomId,
                               @RequestParam String firstItem,
                               @RequestParam String secondItem,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            String clue = gamePlayService.combineItems(authentication.getName(), roomId, firstItem, secondItem);
            redirectAttributes.addFlashAttribute("roomSuccess", "Kết hợp thành công! " + clue);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("roomError", ex.getMessage());
        }
        return "redirect:/game/rooms/" + roomId;
    }

    @PostMapping("/game/rooms/{roomId}/objects/{objectId}/interact")
    public String interactObject(@PathVariable Long roomId,
                                 @PathVariable Long objectId,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            gamePlayService.interactObject(authentication.getName(), roomId, objectId);
            PlayerRoomProgress progress = gamePlayService.getOrCreateProgress(authentication.getName(), roomId);
            if (Boolean.TRUE.equals(progress.getCompleted()) && !Boolean.TRUE.equals(progress.getWon())) {
                redirectAttributes.addFlashAttribute("roomError", "Phản ứng kết hợp sai đã kích hoạt nổ ở giai đoạn cuối. Bạn đã thua màn này.");
            } else {
                redirectAttributes.addFlashAttribute("roomSuccess", "Bạn đã tương tác thành công và mở khóa vật thể.");
            }
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("roomError", ex.getMessage());
        }
        return "redirect:/game/rooms/" + roomId;
    }
}
