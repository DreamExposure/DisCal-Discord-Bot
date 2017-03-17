package com.cloudcraftgaming.discal.eventlisteners;

import com.cloudcraftgaming.discal.internal.network.discordpw.UpdateListData;
import com.cloudcraftgaming.discal.module.misc.TimeManager;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;

/**
 * Created by Nova Fox on 1/2/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("unused")
public class ReadyEventListener {
    @EventSubscriber
    public void onReadyEvent(ReadyEvent event) {
        TimeManager.getManager().init();
        //Once DisCal is ready, initialize the Announcer!
        UpdateListData.updateSiteBotMeta();
    }
}