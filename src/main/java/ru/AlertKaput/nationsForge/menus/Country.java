package ru.AlertKaput.nationsForge.menus;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.AlertKaput.nationsForge.NationsForge;
import ru.AlertKaput.nationsForge.utils.ColorUtils;
import ru.AlertKaput.nationsForge.utils.DataUtils;
import ru.AlertKaput.nationsForge.utils.MenuBuilder;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Country {

    @SuppressWarnings("deprecation")
    public void openMenu(Player player) {
        // Чтение данных из database.json
        File databaseFile = NationsForge.getDatabaseFile();
        JsonObject database = DataUtils.loadDatabase(databaseFile);

        // Чтение данных из persons.json
        File personsFile = new File(NationsForge.getInstance().getDataFolder(), "persons.json");
        JsonObject personsData = DataUtils.loadDatabase(personsFile);

        // Получаем имя игрока
        String playerName = player.getName();

        String beforeColor = ColorUtils.hexToRgb("7e7e7e");
        String afterColor = ColorUtils.hexToRgb("1e93d6");
        String whiteColor = ColorUtils.hexToRgb("ffffff");

        // Создаём меню с названием "Персональная информация" и 4 строками (4*9 слотов)
        MenuBuilder menu = new MenuBuilder("1", "lorem ipsum", 4, NationsForge.getInstance());

        // Серые стеклянные панели
        int[] grayPaneSlots = {0, 1, 7, 8, 18, 26, 27, 28, 34, 35};
        for (int slot : grayPaneSlots) {
            menu.addItem(slot, Material.GRAY_STAINED_GLASS_PANE, " "); // Панели без названия
        }

        // Лианы с названием "Лианы"
        menu.addItem(9, Material.VINE, whiteColor + "Лианы");
        menu.addItem(17, Material.VINE, whiteColor + "Лианы");

        // Пустая карта с названием "Список государств" и описанием
        menu.addItem(12, Material.LEGACY_EMPTY_MAP, whiteColor + "Список государств",
                Arrays.asList(beforeColor + "| Там можно выбрать страну для игры"));

        // Информация о персонаже из persons.json
        String gender = "Не указано";
        String nationality = "Не указано";
        String age = "Не указано";

        if (personsData.has(playerName)) {
            JsonObject personInfo = personsData.getAsJsonObject(playerName);

            // Определяем пол игрока и переводим его
            if (personInfo.has("gender")) {
                String genderRaw = personInfo.get("gender").getAsString();
                if (genderRaw.equalsIgnoreCase("male")) {
                    gender = "Мужской";
                } else if (genderRaw.equalsIgnoreCase("female")) {
                    gender = "Женский";
                }
            }

            // Определяем возраст игрока и переводим его с учётом пола
            if (personInfo.has("ageGroup")) {
                String ageRaw = personInfo.get("ageGroup").getAsString();
                if (ageRaw.equalsIgnoreCase("young")) {
                    age = gender.equals("Мужской") ? "Юноша" : "Девушка";
                } else if (ageRaw.equalsIgnoreCase("adult")) {
                    age = gender.equals("Мужской") ? "Мужчина" : "Женщина";
                } else if (ageRaw.equalsIgnoreCase("old")) {
                    age = gender.equals("Мужской") ? "Пожилой" : "Пожилая";
                }
            }

            // Определяем национальность игрока
            if (personInfo.has("nationality")) {
                nationality = personInfo.get("nationality").getAsString();
            }
        }

        // Роль в государстве из database.json
        boolean hasState = false;
        String roleInState = "Безработный";
        String stateName = null;

        for (String key : database.keySet()) {
            JsonObject stateData = database.getAsJsonObject(key);

            // Проверка, если игрок — лидер
            if (stateData.has("ruler") && stateData.get("ruler").getAsString().equals(playerName)) {
                roleInState = stateData.has("leaderTitle") ? stateData.get("leaderTitle").getAsString() : "Правитель";
                hasState = true;
                stateName = stateData.get("name").getAsString();
                break;
            }
            // Проверка, если игрок — член государства
            else if (stateData.has("members") && stateData.get("members").isJsonArray()) {
                JsonArray membersArray = stateData.getAsJsonArray("members");
                for (JsonElement member : membersArray) {
                    if (member.getAsString().equals(playerName)) {
                        roleInState = "Член";
                        hasState = true;
                        stateName = stateData.get("name").getAsString();
                        break;
                    }
                }
            }
        }


        // Добавляем информацию о персонаже
        menu.addItem(14, Material.NAME_TAG, whiteColor + "Информация о вас",
                Arrays.asList(
                        beforeColor + "Пол: " + afterColor + gender,
                        beforeColor + "Национальность: " + afterColor + nationality,
                        beforeColor + "Возраст: " + afterColor + age,
                        beforeColor + "Статус в государстве: " + afterColor + roleInState
                )
        );

        // Добавляем информацию о государстве
        if (hasState) {
            menu.addItem(13, Material.TOTEM_OF_UNDYING, whiteColor + stateName);
        } else {
            menu.addItem(13, Material.TOTEM_OF_UNDYING, whiteColor + "Ваше государство",
                    Arrays.asList(
                            beforeColor + "| Нужно создать или вступить в страну"
                    )
            );
        }

        // Проверяем, является ли игрок членом государства
        if (hasState) {
            menu.addItem(22, Material.NETHER_STAR, whiteColor + "Основать свою страну",
                    List.of(
                            ChatColor.RED + "Вы не можете создать страну"
                    )
            );
        } else {
            menu.addItem(22, Material.NETHER_STAR, whiteColor + "Основать свою страну",
                    Arrays.asList(
                            beforeColor + "| Создайте новое государство под вашим",
                            beforeColor + "предводительством, и постройте своё величие"
                    )
            );
        }
        // Открываем меню для игрока
            menu.addItem(29, Material.GREEN_STAINED_GLASS_PANE, whiteColor + "Домой",
                    Arrays.asList(
                            beforeColor + "| Переместиться в главное меню сервера"
                    )
            );
        menu.open(player);
    }
}