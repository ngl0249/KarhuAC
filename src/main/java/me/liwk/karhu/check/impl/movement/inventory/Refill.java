package me.liwk.karhu.check.impl.movement.inventory;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClientStatus;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.*;
import me.liwk.karhu.handler.collision.type.MaterialChecks;
import me.liwk.karhu.manager.alert.MiscellaneousAlertPoster;
import me.liwk.karhu.util.MathUtil;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayDeque;
import java.util.Deque;

@CheckInfo(name = "Refill", category = Category.MOVEMENT, subCategory = SubCategory.INVENTORY, experimental = true)
public final class Refill extends PacketCheck {


    private int ticks, lastSlot;
    private int quickMovesToExempt, oneDiffMoves, doubleClicks, invOpenTick, hackerTick, startTime, emptyClicks;
    private Deque<Integer> clicks = new ArrayDeque<>();
    private Deque<Double> sdd = new ArrayDeque<>();

    public Refill(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(Event packet) {
        if (packet instanceof WindowEvent) {

            WindowEvent event = (WindowEvent) packet;

            WrapperPlayClientClickWindow.WindowClickType clickType = event.getClickType();

            int slot = event.getSlot();
            int windowId = event.getWindowId();

            // Check if hotbarslot is full, but all the clicks are slot AIR

            int timeBetween = data.getTotalTicks() - invOpenTick;

            if (timeBetween <= 2) {
                if ((slot < 11 || slot > 15) && (slot < 20 || slot > 24)) {
                    hackerTick = data.getTotalTicks();
                }
            }

            if (event.getItemStack().getType().getName().getKey().contains("AIR")) {
                ++emptyClicks;
            }


            if (slot == lastSlot && clickType == WrapperPlayClientClickWindow.WindowClickType.QUICK_MOVE) {
                if (ticks <= 5) {
                    if (getFilledSlotsBar() == 9) {
                        quickMovesToExempt = 18;
                    }
                }

                if (quickMovesToExempt == 0) ++doubleClicks;
            }

            boolean exempt = quickMovesToExempt != 0;

            int slotDiff = Math.abs(slot - lastSlot);

            if (slot != lastSlot && ticks <= 5
                    && windowId == 0 && !exempt
                    && slot <= 35 && clickType == WrapperPlayClientClickWindow.WindowClickType.QUICK_MOVE) {

                if (slotDiff == 1) {
                    ++oneDiffMoves;
                }

                if (clicks.isEmpty()) {
                    startTime = data.getTotalTicks();
                }

                clicks.add(ticks);
                if (clicks.size() >= 5) {
                    double std = MathUtil.getStandardDeviation(MathUtil.dequeTranslator(clicks));

                    if (std < 0.6) {
                        sdd.add(std);
                    }

                    double requiredStd = oneDiffMoves >= 4 ? 0.25
                            : (doubleClicks <= 1) ? 0.35 : 0.5;

                    if (hackerTick > data.getTotalTicks() - 25 && !data.isNewerThan8()) {
                        requiredStd = 0.8;
                    }

                    int timeSinceFirst = data.elapsed(startTime);

                    if (emptyClicks == 0 && timeSinceFirst <= 15) {
                        requiredStd += 0.15;
                    }

                    if (std < requiredStd) {
                        increase(1 + std);
                        MiscellaneousAlertPoster.postMitigation(data, violations,
                                "Refill",
                                "* Low std" +
                                        "\n §f* std §b" + String.format("%.3f", std) + " | " + requiredStd +
                                        "\n §f* odM §b" + oneDiffMoves +
                                        "\n §f* dC §b" + doubleClicks +
                                        "\n §f* eC §b" + emptyClicks +
                                        "\n §f* eC §b" + timeSinceFirst
                        );
                    } else {
                        decrease(std);
                    }

                    clicks.clear();
                    doubleClicks = 0;
                    oneDiffMoves = 0;
                    emptyClicks = 0;
                }

                if (sdd.size() >= 3) {
                    double stdFromList = MathUtil.getAverage(sdd);

                    if (stdFromList < 0.3) {
                        increase(1 + stdFromList);
                        MiscellaneousAlertPoster.postMitigation(data, violations,
                                "Refill",
                                "* Low std long term" +
                                        "\n §f* std §b" + String.format("%.2f", stdFromList) + " | 0.05" +
                                        "\n §f* odM §b" + oneDiffMoves +
                                        "\n §f* dC §b" + doubleClicks
                        );
                        data.setHitsToCancel(data.getHitsToCancel() + 2);

                    } else {
                        decrease(stdFromList);
                    }
                    sdd.clear();
                }
            }

            ticks = 0;
            lastSlot = slot;
            quickMovesToExempt = Math.max(0, quickMovesToExempt - 1);
            invOpenTick = 0;
        } else if (packet instanceof FlyingEvent) {
            if (!data.isNewerThan8()) {
                ++ticks;

                if (data.getTotalTicks() % 40 == 0 && !clicks.isEmpty()) {
                    clicks.removeFirst();
                    oneDiffMoves = Math.max(0, oneDiffMoves - 1);
                    doubleClicks = Math.max(0, doubleClicks - 1);
                }
            }
        } else if (packet instanceof TransactionEvent) {

            //Terrible support for 1.9+, refill users most likely Vape/Drip user on 1.7-1.8 tho.

            if (data.isNewerThan8() && data.getCurrentClientTransaction() % 2 == 0) {
                ++ticks;

                if (data.getTotalTicks() % 40 == 0 && !clicks.isEmpty()) {
                    clicks.removeFirst();
                    oneDiffMoves = Math.max(0, oneDiffMoves - 1);
                    doubleClicks = Math.max(0, doubleClicks - 1);
                }
            }

        } else if (packet instanceof ClientCommandEvent) {

            if (((ClientCommandEvent) packet).getClientCommand().equals(WrapperPlayClientClientStatus.Action.OPEN_INVENTORY_ACHIEVEMENT)) {
                invOpenTick = data.getTotalTicks();
            }

        }
    }

    private int getFilledSlotsBar() {
        Inventory inv = data.getBukkitPlayer().getInventory();

        // In 1.8.8, use getContents() and loop through first 9 slots
        ItemStack[] contents = inv.getContents();

        int filledSlotsBar = 0;

        for (int slotLoop = 0; slotLoop < 9; slotLoop++) {
            ItemStack item = contents[slotLoop];

            // Check if the slot is not empty
            if (item != null) {
                // Do something with the item
                // For example, you could log it, modify it, or perform an action
                if (!MaterialChecks.AIR.contains(item.getType())) {
                    ++filledSlotsBar;
                }
            }
        }
        return filledSlotsBar;
    }
}
