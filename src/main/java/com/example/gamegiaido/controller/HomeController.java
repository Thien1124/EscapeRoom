package com.example.gamegiaido.controller;

import com.example.gamegiaido.repository.PlayHistoryRepository;
import com.example.gamegiaido.repository.PlayerProfileRepository;
import com.example.gamegiaido.service.PlayerProfileService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final PlayerProfileService playerProfileService;
    private final PlayerProfileRepository playerProfileRepository;
    private final PlayHistoryRepository playHistoryRepository;

    public HomeController(PlayerProfileService playerProfileService,
                          PlayerProfileRepository playerProfileRepository,
                          PlayHistoryRepository playHistoryRepository) {
        this.playerProfileService = playerProfileService;
        this.playerProfileRepository = playerProfileRepository;
        this.playHistoryRepository = playHistoryRepository;
    }

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
        model.addAttribute("profile", playerProfileService.getByUsername(username));
        model.addAttribute("isAdmin", isAdmin);
        return "home";
    }

    @GetMapping("/leaderboard")
    public String leaderboard(Model model) {
        model.addAttribute("players", playerProfileRepository.findTop20ByOrderByTotalScoreDescTotalWinDesc());
        return "leaderboard";
    }

    @GetMapping("/history")
    public String history(Authentication authentication, Model model) {
        var profile = playerProfileService.getByUsername(authentication.getName());
        model.addAttribute("histories", playHistoryRepository.findByPlayerIdOrderByPlayedAtDesc(profile.getId()));
        return "history";
    }
}
