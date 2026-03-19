package com.example.gamegiaido.service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Optional;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.stereotype.Service;

@Service
public class CraftingRecipeService {

    private final List<CraftingRecipe> recipes;

    public CraftingRecipeService() {
        this.recipes = loadRecipes();
    }

    public Optional<String> resolveClue(String roomName, String firstItemKey, String secondItemKey) {
        if (firstItemKey == null || secondItemKey == null) {
            return Optional.empty();
        }

        String normalizedRoom = normalize(roomName);
        List<String> pair = List.of(firstItemKey.trim().toLowerCase(Locale.ROOT), secondItemKey.trim().toLowerCase(Locale.ROOT))
                .stream()
                .sorted()
                .toList();

        return recipes.stream()
                .filter(recipe -> normalizedRoom.contains(normalize(recipe.getRoomKeyword())))
                .filter(recipe -> recipe.getInputs() != null && recipe.getInputs().size() == 2)
                .filter(recipe -> {
                    List<String> recipePair = recipe.getInputs().stream()
                            .map(value -> value.trim().toLowerCase(Locale.ROOT))
                            .sorted()
                            .toList();
                    return recipePair.equals(pair);
                })
                .map(CraftingRecipe::getMessage)
                .findFirst();
    }

    private List<CraftingRecipe> loadRecipes() {
        try {
            ClassPathResource resource = new ClassPathResource("game-data/crafting-recipes.json");
            try (InputStream inputStream = resource.getInputStream()) {
                String json = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
                JsonParser parser = JsonParserFactory.getJsonParser();
                List<Object> rawList = parser.parseList(json);

                List<CraftingRecipe> parsedRecipes = new ArrayList<>();
                for (Object raw : rawList) {
                    if (!(raw instanceof Map<?, ?> rawMap)) {
                        continue;
                    }

                    CraftingRecipe recipe = new CraftingRecipe();
                    recipe.setRoomKeyword(asString(rawMap.get("roomKeyword")));
                    recipe.setMessage(asString(rawMap.get("message")));

                    Object inputsRaw = rawMap.get("inputs");
                    List<String> inputs = new ArrayList<>();
                    if (inputsRaw instanceof List<?> inputList) {
                        for (Object inputValue : inputList) {
                            String normalized = asString(inputValue);
                            if (!normalized.isEmpty()) {
                                inputs.add(normalized);
                            }
                        }
                    }
                    recipe.setInputs(inputs);
                    parsedRecipes.add(recipe);
                }

                return parsedRecipes;
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể tải crafting recipes", ex);
        }
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public static class CraftingRecipe {

        private String roomKeyword;
        private List<String> inputs;
        private String message;

        public String getRoomKeyword() {
            return roomKeyword;
        }

        public void setRoomKeyword(String roomKeyword) {
            this.roomKeyword = roomKeyword;
        }

        public List<String> getInputs() {
            return inputs;
        }

        public void setInputs(List<String> inputs) {
            this.inputs = inputs;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
