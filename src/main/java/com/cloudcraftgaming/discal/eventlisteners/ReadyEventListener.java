package com.cloudcraftgaming.discal.eventlisteners;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.network.discordpw.UpdateListData;
import com.cloudcraftgaming.discal.internal.service.TimeManager;
import com.cloudcraftgaming.discal.utils.MessageManager;
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
        UpdateListData.updateSiteBotMeta();

		MessageManager.reloadLangs();

        //Run any Db updating...
        if (Main.botSettings.shouldRunDatabaseUpdater()) {
            DatabaseManager.getManager().runDatabaseUpdateIfNeeded();
        }
    }
}