package me.liwk.karhu.event;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClientStatus;
import lombok.Getter;

@Getter
public class ClientCommandEvent extends Event {

    private final WrapperPlayClientClientStatus.Action clientCommand;

    public ClientCommandEvent(WrapperPlayClientClientStatus.Action clientCommand) {
        this.clientCommand = clientCommand;
    }

}
