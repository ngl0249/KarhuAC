package me.liwk.karhu.event;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import lombok.Getter;

@Getter
public class WindowEvent extends Event {

    private final long timeStamp;
    private final WrapperPlayClientClickWindow.WindowClickType clickType;
    private final int slot;
    private final int windowId;
    private final int button;
    private final ItemStack itemStack;

    public WindowEvent(long nano, WrapperPlayClientClickWindow.WindowClickType clickType, int slot, int windowId, int button, ItemStack itemStack) {
        this.timeStamp = nano;
        this.clickType = clickType;
        this.slot = slot;
        this.windowId = windowId;
        this.button = button;
        this.itemStack = itemStack;
    }

}
