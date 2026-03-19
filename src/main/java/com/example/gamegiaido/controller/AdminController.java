package com.example.gamegiaido.controller;

import com.example.gamegiaido.dto.AdminQuestionForm;
import com.example.gamegiaido.dto.AdminRoomForm;
import com.example.gamegiaido.dto.AdminRoomKeyForm;
import com.example.gamegiaido.dto.AdminTopicForm;
import com.example.gamegiaido.dto.AdminObjectHotspotForm;
import com.example.gamegiaido.model.GameMode;
import com.example.gamegiaido.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        populateModel(model);
        return "admin";
    }

    @PostMapping("/admin/topics")
    public String createTopic(@Valid @ModelAttribute("adminTopicForm") AdminTopicForm form,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateModel(model);
            return "admin";
        }

        adminService.createTopic(form);
        redirectAttributes.addFlashAttribute("adminSuccess", "Đã tạo chủ đề mới.");
        return "redirect:/admin";
    }

    @PostMapping("/admin/rooms")
    public String createRoom(@Valid @ModelAttribute("adminRoomForm") AdminRoomForm form,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateModel(model);
            return "admin";
        }

        adminService.createRoom(form);
        redirectAttributes.addFlashAttribute("adminSuccess", "Đã tạo phòng mới với vật thể mặc định.");
        return "redirect:/admin";
    }

    @PostMapping("/admin/questions")
    public String upsertQuestion(@Valid @ModelAttribute("adminQuestionForm") AdminQuestionForm form,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateModel(model);
            return "admin";
        }

        try {
            adminService.upsertQuestion(form);
            redirectAttributes.addFlashAttribute("adminSuccess", "Đã lưu câu hỏi cho phòng.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("adminError", ex.getMessage());
        }

        return "redirect:/admin";
    }

    @PostMapping("/admin/hotspots")
    public String updateHotspots(@Valid @ModelAttribute("adminObjectHotspotForm") AdminObjectHotspotForm form,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateModel(model);
            return "admin";
        }

        try {
            adminService.updateObjectHotspot(form);
            redirectAttributes.addFlashAttribute("adminSuccess", "Đã cập nhật tọa độ hotspot cho vật thể.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("adminError", ex.getMessage());
        }

        return "redirect:/admin";
    }

    @PostMapping("/admin/room-keys")
    public String upsertRoomKey(@Valid @ModelAttribute("adminRoomKeyForm") AdminRoomKeyForm form,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateModel(model);
            return "admin";
        }

        try {
            adminService.upsertRoomKey(form);
            redirectAttributes.addFlashAttribute("adminSuccess", "Đã lưu chìa khóa ẩn cho phòng.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("adminError", ex.getMessage());
        }

        return "redirect:/admin";
    }

    private void populateModel(Model model) {
        if (!model.containsAttribute("adminTopicForm")) {
            model.addAttribute("adminTopicForm", new AdminTopicForm());
        }
        if (!model.containsAttribute("adminRoomForm")) {
            model.addAttribute("adminRoomForm", new AdminRoomForm());
        }
        if (!model.containsAttribute("adminQuestionForm")) {
            model.addAttribute("adminQuestionForm", new AdminQuestionForm());
        }
        if (!model.containsAttribute("adminObjectHotspotForm")) {
            model.addAttribute("adminObjectHotspotForm", new AdminObjectHotspotForm());
        }
        if (!model.containsAttribute("adminRoomKeyForm")) {
            model.addAttribute("adminRoomKeyForm", new AdminRoomKeyForm());
        }
        model.addAttribute("topics", adminService.getTopics());
        model.addAttribute("rooms", adminService.getRooms());
        model.addAttribute("roomObjects", adminService.getRoomObjectsForAdmin());
        model.addAttribute("roomKeys", adminService.getRoomKeysForAdmin());
        model.addAttribute("modes", GameMode.values());
    }
}
