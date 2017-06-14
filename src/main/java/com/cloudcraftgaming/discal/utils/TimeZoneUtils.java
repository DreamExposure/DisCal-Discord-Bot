package com.cloudcraftgaming.discal.utils;

import org.joda.time.DateTimeZone;

/**
 * Created by Nova Fox on 4/7/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class TimeZoneUtils {
	private static final String BAD_TZs = "America/Adak,America/Atka,US/Aleutian,HST,America/Juneau,America/Nome,America/Yakutat,America/Dawson,PST8PDT,America/Boise,America/Cambridge_Bay,America/Chihuahua,America/Inuvik,America/Ojinaga,MST,MST7MDT,America/Bahia_Banderas,America/Indiana/Knox,America/Indiana/Tell_City,America/Matamoros,America/Menominee,America/Merida,America/Monterrey,America/North_Dakota/Center,America/North_Dakota/New_Salem,America/Rainy_River,America/Rankin_Inlet,America/Swift_Current,America/Atikokan,America/Coral_Harbour,US/Michigan,America/Indiana/Indianapolis,America/Indiana/Marengo,America/Indiana/Petersburg,America/Indiana/Vevay,America/Indiana/Vincennes,America/Indiana/Winamac,America/Kentucky/Louisville,America/Louisville,America/Kentucky/Monticello,America/Nipigon,America/Pangnirtung,America/Resolute,America/Thunder_Bay,EST,EST5EDT,America/Argentina/San_Luis,America/Eirunepe,America/Glace_Bay,America/Goose_Bay,America/Moncton,America/Argentina/Catamarca,America/Argentina/Catamarca,America/Argentina/ComodRivadavia,America/Catamarca,America/Argentina/Cordoba,America/Cordoba,America/Rosario,America/Argentina/Jujuy,America/Jujuy,America/Argentina/La_Rioja,America/Argentina/Mendoza,America/Mendoza,America/Argentina/Rio_Gallegos,America/Argentina/Salta,America/Argentina/San_Juan,America/Argentina/Tucuman,America/Argentina/Ushuaia,America/Santarem,Atlantic/Madeira,Etc/UCT,UCT,Etc/UTC,Etc/Universal,Etc/Zulu,Universal,Zulu,CET,MET,EET,Europe/Simferopol,Europe/Uzhgorod,Europe/Zaporozhye,Europe/Volgograd,Asia/Oral,Asia/Samarkand,Asia/Novokuznetsk,Asia/Pontianak,Asia/Kashgar,Asia/Kuching,Asia/Urumqi,Asia/Sakhalin,Antarctica/Macquarie,Asia/Anadyr,Pacific/Chatham";

    public static boolean isValid(String value) {
        try {
            DateTimeZone tz = DateTimeZone.forID(value);
            return tz != null && !isBadTz(value);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean isBadTz(String value) {
    	return BAD_TZs.contains(value);
	}
}