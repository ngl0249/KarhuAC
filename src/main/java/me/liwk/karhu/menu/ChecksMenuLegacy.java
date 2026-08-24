package me.liwk.karhu.menu;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.api.Check;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.manager.ConfigManager;
import me.liwk.karhu.util.gui.Button;
import me.liwk.karhu.util.gui.Gui;
import me.liwk.karhu.util.gui.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ChecksMenuLegacy {

    private static ConfigManager cfg = Karhu.getInstance().getConfigManager();

    /*public static void openMainMenu(Player opener) {

        int[] blueGlass = new int [] {
                0,2,4,6,8,7,8,
                18,20,22,24
        };

        int[] whiteGlass = new int[] {
                1,3,5,7,
                9,11,13,15,17,
                19,21,23,25
        };

        Gui gui = new Gui(ChatColor.translateAlternateColorCodes('&', Karhu.getInstance().getConfigManager().getAlertHoverMessageHighlight() + "&l" + Karhu.getInstance().getConfigManager().getName() + "&7 - Checks"), 27);

        if(Karhu.SERVER_VERSION.isNewerThan(ServerVersion.V_1_12_2)) {
            for (int pos : blueGlass) {
                gui.addItem(1, new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE), pos);
            }

            for (int pos : whiteGlass) {
                gui.addItem(1, new ItemStack(Material.WHITE_STAINED_GLASS_PANE), pos);
            }
        } else {
            for (int pos : blueGlass) {
                gui.addItem(1, new ItemStack(Material.getMaterial("STAINED_GLASS_PANE"), 1, (short) 3), pos);
            }

            for (int pos : whiteGlass) {
                gui.addItem(1, new ItemStack(Material.getMaterial("STAINED_GLASS_PANE"), 1, (short) 0), pos);
            }
        }

        Material type;
        String name;

        int size = 10;

        for(Category ctg : Category.values()) {
            switch (ctg.name()) {
                case "COMBAT": {
                    type = Material.DIAMOND_SWORD;
                    name = ctg.name();
                    gui.addButton(new Button(1, size, ItemUtil.makeItem(type, 1, cfg.getGuiHighlightColor() + name, Arrays.asList(
                            "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                            "§7Manage checks",
                            "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                    ))) {
                        @Override
                        public void onClick(Player clicker, ClickType clickType) {
                            gui.close(clicker);
                            openTypeGUI(clicker, SubCategory.REACH);
                        }
                    });
                    break;
                }
                case "MOVEMENT": {
                    type = Material.DIAMOND_BOOTS;
                    name = ctg.name();
                    gui.addButton(new Button(1, size, ItemUtil.makeItem(type, 1, cfg.getGuiHighlightColor() + name, Arrays.asList(
                            "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                            "§7Manage checks",
                            "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                    ))) {
                        @Override
                        public void onClick(Player clicker, ClickType clickType) {
                            gui.close(clicker);
                            openTypeGUI(clicker, SubCategory.SPEED);
                        }
                    });
                    break;
                }
                case "WORLD": {
                    type = Material.CACTUS;
                    name = ctg.name();
                    gui.addButton(new Button(1, size, ItemUtil.makeItem(type, 1, cfg.getGuiHighlightColor() + name, Arrays.asList(
                            "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                            "§7Manage checks",
                            "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                    ))) {
                        @Override
                        public void onClick(Player clicker, ClickType clickType) {
                            gui.close(clicker);
                            openTypeGUI(clicker, SubCategory.SCAFFOLD);
                        }
                    });
                    break;
                }
                case "PACKET": {
                    type = Material.TRIPWIRE_HOOK;
                    name = ctg.name();
                    gui.addButton(new Button(1, size, ItemUtil.makeItem(type, 1, cfg.getGuiHighlightColor() + name, Arrays.asList(
                            "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                            "§7Manage checks",
                            "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                    ))) {
                        @Override
                        public void onClick(Player clicker, ClickType clickType) {
                            gui.close(clicker);
                            openTypeGUI(clicker, SubCategory.TIMER);
                        }
                    });
                    break;
                }
            }
            size += 2;
        }

        gui.addButton(new Button(1, 26, ItemUtil.makeItem(Material.EMERALD, 1, cfg.getGuiHighlightColor() + "Back", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                "§7Go back to the last menu",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                KarhuMenu.openMenu(clicker);
            }
        });

        gui.open(opener);
        opener.updateInventory();
    }

    /*public static void openTypeGUI(Player opener, SubCategory subCategory) {

        int[] blueGlass = new int [] {
                0,2,4,6,8,7,8,
                18,20,22,24
        };

        int[] whiteGlass = new int[] {
                1,3,5,7,
                9,11,13,15,17,
                19,21,23,25
        };

        Gui gui = new Gui(
                ChatColor.translateAlternateColorCodes('&',
                        Karhu.getInstance().getConfigManager().getAlertHoverMessageHighlight()
                                + "&l" + Karhu.getInstance().getConfigManager().getName()
                                + "&7 - " + subCategory.getCategory()),
                27);

        if(Karhu.SERVER_VERSION.isNewerThan(ServerVersion.V_1_12_2)) {
            for (int pos : blueGlass) {
                gui.addItem(1, new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE), pos);
            }

            for (int pos : whiteGlass) {
                gui.addItem(1, new ItemStack(Material.WHITE_STAINED_GLASS_PANE), pos);
            }
        } else {
            for (int pos : blueGlass) {
                gui.addItem(1, new ItemStack(Material.getMaterial("STAINED_GLASS_PANE"), 1, (short) 3), pos);
            }

            for (int pos : whiteGlass) {
                gui.addItem(1, new ItemStack(Material.getMaterial("STAINED_GLASS_PANE"), 1, (short) 0), pos);
            }
        }

        Material type;
        String name;

        gui.addButton(new Button(1, 26, ItemUtil.makeItem(Material.EMERALD, 1, cfg.getGuiHighlightColor() + "Back", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                "§7Go back to the last menu",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                openMainMenu(clicker);
            }
        });


        switch (subCategory.getCategory()) {
            case COMBAT: {
                for (SubCategory categoryShit : SubCategory.values()) {
                    switch (categoryShit.name()) {
                        case "REACH": {
                            type = Material.DIAMOND_SWORD;
                            name = categoryShit.name();
                            gui.addButton(new Button(1, categoryShit.getSlot(), ItemUtil.makeItem(type, 1, cfg.getGuiHighlightColor() + name, Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    "§7Manage checks",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            ))) {
                                @Override
                                public void onClick(Player clicker, ClickType clickType) {
                                    gui.close(clicker);
                                    openCheckSettingGUI(clicker, SubCategory.REACH);
                                }
                            });
                            break;
                        }
                        case "KILLAURA": {
                            type = Material.BLAZE_ROD;
                            name = categoryShit.name();
                            gui.addButton(new Button(1, categoryShit.getSlot(), ItemUtil.makeItem(type, 1, cfg.getGuiHighlightColor() + name, Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    "§7Manage checks",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            ))) {
                                @Override
                                public void onClick(Player clicker, ClickType clickType) {
                                    gui.close(clicker);
                                    openCheckSettingGUI(clicker, SubCategory.KILLAURA);
                                }
                            });
                            break;
                        }
                        case "AIM": {
                            type = Material.NETHER_STAR;
                            name = categoryShit.name();
                            gui.addButton(new Button(1, categoryShit.getSlot(), ItemUtil.makeItem(type, 1, cfg.getGuiHighlightColor() + name, Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    "§7Manage checks",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            ))) {
                                @Override
                                public void onClick(Player clicker, ClickType clickType) {
                                    gui.close(clicker);
                                    openCheckSettingGUI(clicker, SubCategory.AIM);
                                }
                            });
                            break;
                        }
                        case "AUTOCLICKER": {
                            type = Material.STONE_BUTTON;
                            name = categoryShit.name();
                            gui.addButton(new Button(1, categoryShit.getSlot(), ItemUtil.makeItem(type, 1, cfg.getGuiHighlightColor() + name, Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    "§7Manage checks",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            ))) {
                                @Override
                                public void onClick(Player clicker, ClickType clickType) {
                                    gui.close(clicker);
                                    openCheckSettingGUI(clicker, SubCategory.AUTOCLICKER);
                                }
                            });
                            break;
                        }
                        case "VELOCITY": {
                            type = Material.BLAZE_POWDER;
                            name = categoryShit.name();
                            gui.addButton(new Button(1, categoryShit.getSlot(), ItemUtil.makeItem(type, 1, cfg.getGuiHighlightColor() + name, Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    "§7Manage checks",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            ))) {
                                @Override
                                public void onClick(Player clicker, ClickType clickType) {
                                    gui.close(clicker);
                                    openCheckSettingGUI(clicker, SubCategory.VELOCITY);
                                }
                            });
                            break;
                        }
                    }
                }
            }
            break;
            case MOVEMENT: {
                for (SubCategory categoryShit : SubCategory.values()) {
                    switch (categoryShit.name()) {
                        case "FLY": {
                            type = Material.FEATHER;
                            name = categoryShit.name();
                            gui.addButton(new Button(1, categoryShit.getSlot(), ItemUtil.makeItem(type, 1, cfg.getGuiHighlightColor() + name, Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    "§7Manage checks",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            ))) {
                                @Override
                                public void onClick(Player clicker, ClickType clickType) {
                                    gui.close(clicker);
                                    openCheckSettingGUI(clicker, SubCategory.FLY);
                                }
                            });
                            break;
                        }
                        case "SPEED": {
                            type = Material.GLASS_BOTTLE;
                            name = categoryShit.name();
                            gui.addButton(new Button(1, categoryShit.getSlot(), ItemUtil.makeItem(type, 1, cfg.getGuiHighlightColor() + name, Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    "§7Manage checks",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            ))) {
                                @Override
                                public void onClick(Player clicker, ClickType clickType) {
                                    gui.close(clicker);
                                    openCheckSettingGUI(clicker, SubCategory.SPEED);
                                }
                            });
                            break;
                        }
                        case "STEP": {
                            type = Karhu.SERVER_VERSION.isNewerThan(ServerVersion.V_1_12_2) ? Material.BIRCH_STAIRS : Material.getMaterial("BIRCH_WOOD_STAIRS");
                            name = categoryShit.name();
                            gui.addButton(new Button(1, categoryShit.getSlot(), ItemUtil.makeItem(type, 1, cfg.getGuiHighlightColor() + name, Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    "§7Manage checks",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            ))) {
                                @Override
                                public void onClick(Player clicker, ClickType clickType) {
                                    gui.close(clicker);
                                    openCheckSettingGUI(clicker, SubCategory.STEP);
                                }
                            });
                            break;
                        }
                        case "MOTION": {
                            type = Karhu.SERVER_VERSION.isNewerThan(ServerVersion.V_1_12_2) ? Material.COBWEB : Material.getMaterial("WEB");
                            name = categoryShit.name();
                            gui.addButton(new Button(1, categoryShit.getSlot(), ItemUtil.makeItem(type, 1, cfg.getGuiHighlightColor() + name, Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    "§7Manage checks",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            ))) {
                                @Override
                                public void onClick(Player clicker, ClickType clickType) {
                                    gui.close(clicker);
                                    openCheckSettingGUI(clicker, SubCategory.MOTION);
                                }
                            });
                            break;
                        }
                        case "NOSLOW": {
                            type = Material.IRON_SWORD;
                            name = categoryShit.name();
                            gui.addButton(new Button(1, categoryShit.getSlot(), ItemUtil.makeItem(type, 1, cfg.getGuiHighlightColor() + name, Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    "§7Manage checks",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            ))) {
                                @Override
                                public void onClick(Player clicker, ClickType clickType) {
                                    gui.close(clicker);
                                    openCheckSettingGUI(clicker, SubCategory.NOSLOW);
                                }
                            });
                            break;
                        }
                        case "INVENTORY": {
                            type = Karhu.SERVER_VERSION.isNewerThan(ServerVersion.V_1_12_2) ? Material.CHEST_MINECART : Material.getMaterial("STORAGE_MINECART");
                            name = categoryShit.name();
                            gui.addButton(new Button(1, categoryShit.getSlot(), ItemUtil.makeItem(type, 1, cfg.getGuiHighlightColor() + name, Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    "§7Manage checks",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            ))) {
                                @Override
                                public void onClick(Player clicker, ClickType clickType) {
                                    gui.close(clicker);
                                    openCheckSettingGUI(clicker, SubCategory.INVENTORY);
                                }
                            });
                            break;
                        }
                    }
                }
            }
            break;
            case WORLD: {
                for (SubCategory categoryShit : SubCategory.values()) {
                    switch (categoryShit.name()) {
                        case "SCAFFOLD": {
                            type = Material.COBBLESTONE;
                            name = categoryShit.name();
                            gui.addButton(new Button(1, categoryShit.getSlot(), ItemUtil.makeItem(type, 1, cfg.getGuiHighlightColor() + name, Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    "§7Manage checks",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            ))) {
                                @Override
                                public void onClick(Player clicker, ClickType clickType) {
                                    gui.close(clicker);
                                    openCheckSettingGUI(clicker, SubCategory.SCAFFOLD);
                                }
                            });
                            break;
                        }
                        case "PHASE": {
                            type = Material.SADDLE;
                            name = categoryShit.name();
                            gui.addButton(new Button(1, categoryShit.getSlot(), ItemUtil.makeItem(type, 1, cfg.getGuiHighlightColor() + name, Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    "§7Manage checks",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            ))) {
                                @Override
                                public void onClick(Player clicker, ClickType clickType) {
                                    gui.close(clicker);
                                    openCheckSettingGUI(clicker, SubCategory.PHASE);
                                }
                            });
                            break;
                        }
                        case "NOFALL": {
                            type = Material.ANVIL;
                            name = categoryShit.name();
                            gui.addButton(new Button(1, categoryShit.getSlot(), ItemUtil.makeItem(type, 1, cfg.getGuiHighlightColor() + name, Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    "§7Manage checks",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            ))) {
                                @Override
                                public void onClick(Player clicker, ClickType clickType) {
                                    gui.close(clicker);
                                    openCheckSettingGUI(clicker, SubCategory.NOFALL);
                                }
                            });
                            break;
                        }
                        case "BLOCK": {
                            type = Material.BRICK;
                            name = categoryShit.name();
                            gui.addButton(new Button(1, categoryShit.getSlot(), ItemUtil.makeItem(type, 1, cfg.getGuiHighlightColor() + name, Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    "§7Manage checks",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            ))) {
                                @Override
                                public void onClick(Player clicker, ClickType clickType) {
                                    gui.close(clicker);
                                    openCheckSettingGUI(clicker, SubCategory.BLOCK);
                                }
                            });
                            break;
                        }
                    }
                }
            }
            break;
            case PACKET: {
                for (SubCategory categoryShit : SubCategory.values()) {
                    switch (categoryShit.name()) {
                        case "BADPACKETS": {
                            type = Material.TNT;
                            name = categoryShit.name();
                            gui.addButton(new Button(1, categoryShit.getSlot(), ItemUtil.makeItem(type, 1, cfg.getGuiHighlightColor() + name, Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    "§7Manage checks",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            ))) {
                                @Override
                                public void onClick(Player clicker, ClickType clickType) {
                                    gui.close(clicker);
                                    openCheckSettingGUI(clicker, SubCategory.BADPACKETS);
                                }
                            });
                            break;
                        }

                        case "TIMER": {
                            type = Material.GOLDEN_CARROT;
                            name = categoryShit.name();
                            gui.addButton(new Button(1, categoryShit.getSlot(), ItemUtil.makeItem(type, 1, cfg.getGuiHighlightColor() + name, Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    "§7Manage checks",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            ))) {
                                @Override
                                public void onClick(Player clicker, ClickType clickType) {
                                    gui.close(clicker);
                                    openCheckSettingGUI(clicker, SubCategory.TIMER);
                                }
                            });
                            break;
                        }

                        case "CLIENT": {
                            type = Material.BOOK;
                            name = categoryShit.name();
                            gui.addButton(new Button(1, categoryShit.getSlot(), ItemUtil.makeItem(type, 1, cfg.getGuiHighlightColor() + name, Arrays.asList(
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                    "§7Manage checks",
                                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                            ))) {
                                @Override
                                public void onClick(Player clicker, ClickType clickType) {
                                    gui.close(clicker);
                                    openCheckSettingGUI(clicker, SubCategory.CLIENT);
                                }
                            });
                            break;
                        }
                    }
                }
            }
            break;
        }



        gui.open(opener);
        opener.updateInventory();
    }*/

    public static void openCheckSettingGUI(Player opener, SubCategory subCategory) {

        Gui gui = new Gui(ChatColor.translateAlternateColorCodes('&', Karhu.getInstance().getConfigManager().getAlertHoverMessageHighlight() + "&l" + Karhu.getInstance().getConfigManager().getName() + "&7 - " + subCategory.name()), subCategory.name().equals("AUTOCLICKER") ? 9 * 5 : 27);

        KarhuPlayer data = Karhu.getInstance().getDataManager().getPlayerData(opener);

        List<Check> guis = Arrays.stream(data.getCheckManager().getChecks()).filter(check -> check.getSubCategory() == subCategory).collect(Collectors.toList());

        int currentSlot = 0;

        gui.addButton(new Button(1,  subCategory.name().equals("AUTOCLICKER") ? (9 * 5) - 1 : 26, ItemUtil.makeItem(Material.EMERALD, 1, cfg.getGuiHighlightColor() + "Back", Arrays.asList(
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                "§7Go back to the last menu",
                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
        ))) {
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                gui.close(clicker);
                MChecksMenu.openMainMenu(clicker);
            }
        });

        for(Check checkClass : guis) {

            if(checkClass.isSilent()) continue;

            gui.addButton(new Button(1, currentSlot, ItemUtil.makeItem(Karhu.getInstance().getCheckState().isEnabled(checkClass.getName()) ? Material.ENCHANTED_BOOK : Material.BOOK, 1, cfg.getGuiHighlightColor() + (checkClass.isExperimental() ? checkClass.getName() + "§aΔ" : checkClass.getName()), checkClass.getCredits().equals("") ? Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    "§7Enabled: " + getCheckMark(checkClass, false),
                    "§7Punishable: " + getCheckMark(checkClass, true),
                    "",
                    "§7Punish-VL: " + cfg.getGuiHighlightColor() + checkClass.getBanVL(),
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
            ) : Arrays.asList(
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                    "§7Enabled: " + getCheckMark(checkClass, false),
                    "§7Punishable: " + getCheckMark(checkClass, true),
                    "",
                    "§7Punish-VL: " + cfg.getGuiHighlightColor() + checkClass.getBanVL(),
                    "",
                    "" + checkClass.getCredits(),
                    "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"))) {
                @Override
                public void onClick(Player clicker, ClickType clickType) {
                    //gui.close(clicker);
                    boolean openAgain = true;
                    if (clickType == ClickType.LEFT) {
                        if(Karhu.getInstance().getCheckState().isEnabled(checkClass.getName())) {
                            updateCheckStatus(checkClass, false, false);
                            Karhu.getInstance().getCheckState().setEnabled(checkClass.getName(), false);
                        } else {
                            updateCheckStatus(checkClass, false, true);
                            Karhu.getInstance().getCheckState().setEnabled(checkClass.getName(), true);
                        }

                    } else if (clickType == ClickType.RIGHT) {
                        if(Karhu.getInstance().getCheckState().isAutoban(checkClass.getName())) {
                            updateCheckStatus(checkClass, true, false);
                            Karhu.getInstance().getCheckState().setAutoban(checkClass.getName(), false);
                        } else {
                            updateCheckStatus(checkClass, true, true);
                            Karhu.getInstance().getCheckState().setAutoban(checkClass.getName(), true);
                        }
                    } else if (clickType == ClickType.MIDDLE) {
                        gui.close(clicker);
                        ViolationMenu.openViolationGui(clicker, checkClass, checkClass.getCheckInfo(), subCategory);
                        openAgain = false;
                    }

                    if(openAgain) {
                        Karhu.getInstance().getConfigManager().saveChecks();
                        Karhu.getInstance().getConfigManager().loadChecks(Karhu.getInstance(), true);
                        //openCheckSettingGUI(clicker, subCategory);

                        ItemStack stack = this.item;
                        ItemMeta meta = stack.getItemMeta();
                        meta.setLore(checkClass.getCredits().equals("") ? Arrays.asList(
                                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                "§7Enabled: " + getCheckMark(checkClass, false),
                                "§7Punishable: " + getCheckMark(checkClass, true),
                                "",
                                "§7Punish-VL: " + cfg.getGuiHighlightColor() + checkClass.getBanVL(),
                                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"
                        ) : Arrays.asList(
                                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤",
                                "§7Enabled: " + getCheckMark(checkClass, false),
                                "§7Punishable: " + getCheckMark(checkClass, true),
                                "",
                                "§7Punish-VL: " + cfg.getGuiHighlightColor() + checkClass.getBanVL(),
                                "",
                                "" + checkClass.getCredits(),
                                "§7§m⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤"));
                        stack.setItemMeta(meta);
                        stack.setType(Karhu.getInstance().getCheckState().isEnabled(checkClass.getName())
                                ? Material.ENCHANTED_BOOK
                                : Material.BOOK);
                        this.inv.setItem(this.pos, stack);
                    }
                }
            });

            ++currentSlot;
        }

        gui.open(opener);
        opener.updateInventory();
    }

    public static String getCheckMark(Check cs, boolean ab) {
        if(ab) {
            if(Karhu.getInstance().getCheckState().isAutoban(cs.getName())) {
                return "§a✔";
            } else {
                return "§c✗";
            }
        } else {
            if(Karhu.getInstance().getCheckState().isEnabled(cs.getName())) {
                return "§a✔";
            } else {
                return "§c✗";
            }
        }

    }

    public static void updateCheckStatus(Check check, boolean autoban, boolean status) {
        final ConfigManager checkConfig = Karhu.getInstance().getConfigManager();
        final FileConfiguration checkConfiguration = checkConfig.getChecks();

        String name = check.getName();
        String category = check.getCategory().name();

        String[] idk;

        if (name.contains(" ")) {
            idk = name.split(" ");
        } else {
            idk = new String[]{ name, "(A)" };
        }

        final String realTypeName = idk[0];
        final String typeChars = idk[1].replaceAll("[^a-zA-Z0-9]", "");

        if(autoban) {
            checkConfiguration.set(category + "." + realTypeName + "." + typeChars + ".autoban", status);
        } else {
            checkConfiguration.set(category + "." + realTypeName + "." + typeChars + ".enabled", status);
        }
    }

}
