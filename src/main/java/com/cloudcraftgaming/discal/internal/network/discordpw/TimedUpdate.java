package com.cloudcraftgaming.discal.internal.network.discordpw;

import java.util.TimerTask;

/**
 * Created by Nova Fox on 3/28/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class TimedUpdate extends TimerTask {
    /**
     * The action to be performed by this timer task.
     */
    @Override
    public void run() {
        UpdateListData.updateSiteBotMeta();
    }
}
