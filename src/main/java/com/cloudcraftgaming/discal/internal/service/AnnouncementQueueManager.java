package com.cloudcraftgaming.discal.internal.service;

/**
 * Created by Nova Fox on 7/23/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class AnnouncementQueueManager {
	private static AnnouncementQueueManager instance;
	
	
	
	private AnnouncementQueueManager() {} //Prevent initialization
	
	public static AnnouncementQueueManager getManager() {
		if (instance == null) {
			instance = new AnnouncementQueueManager();
		}
		
		return instance;
	}
}