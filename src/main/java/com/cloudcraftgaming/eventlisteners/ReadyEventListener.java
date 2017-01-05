package com.cloudcraftgaming.eventlisteners;

import com.cloudcraftgaming.Main;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.Status;

/**
 * Created by Nova Fox on 1/2/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("unused")
public class ReadyEventListener {

    @EventSubscriber
    public void onReadyEvent(ReadyEvent event) {
        Main.client.changeStatus(Status.game("Discord Calendar"));
    }
}