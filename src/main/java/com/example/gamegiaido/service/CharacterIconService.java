package com.example.gamegiaido.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CharacterIconService {

    public static final String DEFAULT_ICON_KEY = "agent_default";

    private static final Map<String, IconSpec> ICON_SPECS = createSpecs();

    public List<IconSpec> getCatalog() {
        return List.copyOf(ICON_SPECS.values());
    }

    public IconSpec getSpec(String key) {
        if (key == null || key.isBlank()) {
            return ICON_SPECS.get(DEFAULT_ICON_KEY);
        }
        return ICON_SPECS.getOrDefault(key.trim(), ICON_SPECS.get(DEFAULT_ICON_KEY));
    }

    public String normalizeIconKey(String key) {
        return getSpec(key).key();
    }

    public int difficultyLevel(String key) {
        return getSpec(key).difficultyLevel();
    }

    public String iconClass(String key) {
        return getSpec(key).iconClass();
    }

    public String iconTierClass(String key) {
        int cost = getSpec(key).cost();
        if (cost >= 700) {
            return "player-icon-legend";
        }
        if (cost >= 400) {
            return "player-icon-high";
        }
        if (cost >= 200) {
            return "player-icon-mid";
        }
        return "player-icon-base";
    }

    private static Map<String, IconSpec> createSpecs() {
        Map<String, IconSpec> specs = new LinkedHashMap<>();
        specs.put("agent_default", new IconSpec("agent_default", "Quản trị viên", "fas fa-user-secret", 0, 0));
        specs.put("ninja_scout", new IconSpec("ninja_scout", "Đặc vụ bóng đêm", "fas fa-user-ninja", 120, 1));
        specs.put("cyber_hunter", new IconSpec("cyber_hunter", "Thợ săn cyber", "fas fa-robot", 260, 2));
        specs.put("dragon_overlord", new IconSpec("dragon_overlord", "Chúa tể rồng", "fas fa-dragon", 420, 3));
        specs.put("phoenix_mage", new IconSpec("phoenix_mage", "Phượng hoàng", "fas fa-fire", 520, 3));
        specs.put("storm_titan", new IconSpec("storm_titan", "Titan bão tố", "fas fa-bolt", 640, 4));
        specs.put("celestial_guard", new IconSpec("celestial_guard", "Hộ vệ thiên hà", "fas fa-shield-halved", 780, 4));
        specs.put("void_emperor", new IconSpec("void_emperor", "Đế vương hư không", "fas fa-crown", 980, 5));
        return specs;
    }

    public record IconSpec(String key, String name, String iconClass, int cost, int difficultyLevel) {
    }
}
