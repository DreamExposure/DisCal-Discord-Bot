!function(e){var t={};function n(i){if(t[i])return t[i].exports;var r=t[i]={i:i,l:!1,exports:{}};return e[i].call(r.exports,r,r.exports,n),r.l=!0,r.exports}n.m=e,n.c=t,n.d=function(e,t,i){n.o(e,t)||Object.defineProperty(e,t,{enumerable:!0,get:i})},n.r=function(e){"undefined"!=typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(e,Symbol.toStringTag,{value:"Module"}),Object.defineProperty(e,"__esModule",{value:!0})},n.t=function(e,t){if(1&t&&(e=n(e)),8&t)return e;if(4&t&&"object"==typeof e&&e&&e.__esModule)return e;var i=Object.create(null);if(n.r(i),Object.defineProperty(i,"default",{enumerable:!0,value:e}),2&t&&"string"!=typeof e)for(var r in e)n.d(i,r,function(t){return e[t]}.bind(null,r));return i},n.n=function(e){var t=e&&e.__esModule?function(){return e.default}:function(){return e};return n.d(t,"a",t),t},n.o=function(e,t){return Object.prototype.hasOwnProperty.call(e,t)},n.p="",n(n.s=0)}([function(e,t,n){"use strict";n.r(t);var i,r=function(){function e(e){this._id=e}return Object.defineProperty(e.prototype,"id",{get:function(){return this._id},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"hasExternalCalendar",{get:function(){return this._hasExternalCalendar},set:function(e){this._hasExternalCalendar=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"controlRole",{get:function(){return this._controlRole},set:function(e){this._controlRole=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"disCalChannel",{get:function(){return this._disCalChannel},set:function(e){this._disCalChannel=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"hasSimpleAnnouncements",{get:function(){return this._hasSimpleAnnouncements},set:function(e){this._hasSimpleAnnouncements=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"lang",{get:function(){return this._lang},set:function(e){this._lang=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"prefix",{get:function(){return this._prefix},set:function(e){this._prefix=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"isPatronGuild",{get:function(){return this._isPatronGuild},set:function(e){this._isPatronGuild=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"isDevGuild",{get:function(){return this._isDevGuild},set:function(e){this._isDevGuild=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"maxCalendars",{get:function(){return this._maxCalendars},set:function(e){this._maxCalendars=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"usingTwelveHour",{get:function(){return this._usingTwelveHour},set:function(e){this._usingTwelveHour=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"isBranded",{get:function(){return this._isBranded},set:function(e){this._isBranded=e},enumerable:!0,configurable:!0}),e.prototype.toJson=function(){return{guild_id:this.id,external_calendar:this.hasExternalCalendar,control_role:this.controlRole,discal_channel:this.disCalChannel,simple_announcement:this.hasSimpleAnnouncements,lang:this.lang,prefix:this.prefix,patron_guild:this.isPatronGuild,dev_guild:this.isDevGuild,max_calendars:this.maxCalendars,twelve_hour:this.usingTwelveHour,branded:this.isBranded}},e.prototype.fromJson=function(e){return this._id=e.guild_id,this.hasExternalCalendar=e.external_calendar,this.controlRole=e.control_role,this.disCalChannel=e.discal_channel,this.hasSimpleAnnouncements=e.simple_announcement,this.lang=e.lang,this.prefix=e.prefix,this.isPatronGuild=e.patron_guild,this.isDevGuild=e.dev_guild,this.maxCalendars=e.max_calendars,this.usingTwelveHour=e.twelve_hour,this.isBranded=e.branded,this},e}(),o=function(){function e(){}return Object.defineProperty(e.prototype,"id",{get:function(){return this._id},set:function(e){this._id=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"name",{get:function(){return this._name},set:function(e){this._name=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"isManaged",{get:function(){return this._isManaged},set:function(e){this._isManaged=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"isControlRole",{get:function(){return this._isControlRole},set:function(e){this._isControlRole=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"isEveryone",{get:function(){return this._isEveryone},set:function(e){this._isEveryone=e},enumerable:!0,configurable:!0}),e.prototype.toJson=function(){return{id:this.id,name:this.name,managed:this.isManaged,control_role:this.isControlRole,everyone:this.isEveryone}},e.prototype.fromJson=function(e){return this.id=e.id,this.name=e.name,this.isManaged=e.managed,this.isControlRole=e.control_role,this.isEveryone=e.everyone,this},e}(),a=function(){function e(){}return Object.defineProperty(e.prototype,"id",{get:function(){return this._id},set:function(e){this._id=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"name",{get:function(){return this._name},set:function(e){this._name=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"isDisCalChannel",{get:function(){return this._isDisCalChannel},set:function(e){this._isDisCalChannel=e},enumerable:!0,configurable:!0}),e.prototype.toJson=function(){return{id:this.id,name:this.name,discal_channel:this.isDisCalChannel}},e.prototype.fromJson=function(e){return this.id=e.id,this.name=e.name,this.isDisCalChannel=e.discal_channel,this},e}(),s=function(){function e(e){this._id=e,this._roles=[],this._channels=[],this._announcements=[]}return Object.defineProperty(e.prototype,"id",{get:function(){return this._id},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"name",{get:function(){return this._name},set:function(e){this._name=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"icon",{get:function(){return this._icon},set:function(e){this._icon=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"settings",{get:function(){return this._settings},set:function(e){this._settings=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"botNick",{get:function(){return this._botNick},set:function(e){this._botNick=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"canManageServer",{get:function(){return this._canManageServer},set:function(e){this._canManageServer=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"hasDisCalRole",{get:function(){return this._hasDisCalRole},set:function(e){this._hasDisCalRole=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"roles",{get:function(){return this._roles},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"channels",{get:function(){return this._channels},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"announcements",{get:function(){return this._announcements},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"calendar",{get:function(){return this._calendar},set:function(e){this._calendar=e},enumerable:!0,configurable:!0}),e.prototype.toJson=function(){var e={id:this.id,name:this.name,settings:this.settings.toJson(),manage_server:this.canManageServer,discal_role:this.hasDisCalRole,calendar:this.calendar.toJson()};null!=this.icon&&(e.icon=this.icon),null!=this.botNick&&""!==this.botNick&&(e.bot_nick=this.botNick);for(var t=[],n=0;n<this.roles.length;n++){var i=this.roles[n];t.push(i.toJson())}e.roles=t;var r=[];for(n=0;n<this.channels.length;n++){var o=this.channels[n];r.push(o.toJson())}e.channels=r;var a=[];for(n=0;n<this.announcements.length;n++){var s=this.announcements[n];a.push(s.toJson())}return e.announcements=a,e},e.prototype.fromJson=function(e){this.name=e.name,e.hasOwnProperty("icon_url")&&(this.icon=e.icon_url),this.settings=new r(this.id).fromJson(e.settings),e.hasOwnProperty("bot_nick")&&(this.botNick=e.bot_nick),this.canManageServer=e.manage_server,this.hasDisCalRole=e.discal_role;for(var t=0;t<e.roles.length;t++)this.roles.push((new o).fromJson(e.roles[t]));for(t=0;t<e.channels.length;t++)this.channels.push((new a).fromJson(e.channels[t]));for(t=0;t<e.announcements.length;t++)this.roles.push((new o).fromJson(e.roles[t]));return this},e}(),c=function(){function e(){}return e.showSnackbar=function(e){var t=document.getElementById("snackbar");null!=t&&(t.innerHTML=e,t.className="show",setTimeout((function(){t.className=t.className.replace("show","")}),3e3))},e}();!function(e){e[e.RSVP_GET=0]="RSVP_GET",e[e.RSVP_UPDATE=1]="RSVP_UPDATE",e[e.GUILD_SETTINGS_GET=2]="GUILD_SETTINGS_GET",e[e.GUILD_SETTINGS_UPDATE=3]="GUILD_SETTINGS_UPDATE",e[e.WEB_GUILD_GET=4]="WEB_GUILD_GET",e[e.WEB_GUILD_UPDATE=5]="WEB_GUILD_UPDATE",e[e.CALENDAR_GET=6]="CALENDAR_GET",e[e.CALENDAR_LIST=7]="CALENDAR_LIST",e[e.CALENDAR_UPDATE=8]="CALENDAR_UPDATE",e[e.CALENDAR_DELETE=9]="CALENDAR_DELETE",e[e.ANNOUNCEMENT_GET=10]="ANNOUNCEMENT_GET",e[e.ANNOUNCEMENT_LIST=11]="ANNOUNCEMENT_LIST",e[e.ANNOUNCEMENT_CREATE=12]="ANNOUNCEMENT_CREATE",e[e.ANNOUNCEMENT_UPDATE=13]="ANNOUNCEMENT_UPDATE",e[e.ANNOUNCEMENT_DELETE=14]="ANNOUNCEMENT_DELETE",e[e.EVENT_LIST_DATE=15]="EVENT_LIST_DATE",e[e.EVENT_LIST_MONTH=16]="EVENT_LIST_MONTH",e[e.EVENT_LIST_RANGE=17]="EVENT_LIST_RANGE",e[e.EVENT_CREATE=18]="EVENT_CREATE",e[e.EVENT_DELETE=19]="EVENT_DELETE",e[e.EVENT_GET=20]="EVENT_GET",e[e.EVENT_UPDATE=21]="EVENT_UPDATE"}(i||(i={}));var d,l,u=function(){function e(){}return e.showLoader=function(){document.getElementsByClassName("loader")[0].setAttribute("hidden","show")},e.hideLoader=function(){document.getElementsByClassName("loader")[0].setAttribute("hidden","hidden")},e.showCalendarContainer=function(){document.getElementById("calendar-container").setAttribute("hidden","show")},e.hideCalendarContainer=function(){document.getElementById("calendar-container").setAttribute("hidden","hidden")},e.showEventsContainer=function(){document.getElementById("events-container").setAttribute("hidden","show")},e.hideEventsContainer=function(){document.getElementById("events-container").setAttribute("hidden","hidden")},e}(),p=function(){function e(e,t){this._success=e,this._type=t}return Object.defineProperty(e.prototype,"isSuccess",{get:function(){return this._success},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"type",{get:function(){return this._type},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"code",{get:function(){return this._code},set:function(e){this._code=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"message",{get:function(){return this._message},set:function(e){this._message=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"body",{get:function(){return this._body},set:function(e){this._body=e},enumerable:!0,configurable:!0}),e}(),h=function(){function e(e,t,n){this.guildId=e,this.userId=t,this.callback=n}return e.prototype.provideApiDetails=function(e,t){this.apiKey=e,this.apiUrl=e},e.prototype.execute=function(){var e={guild_id:this.guildId,user_id:this.userId};$.ajax({url:this.apiUrl+"/v2/guild/get",headers:{"Content-Type":"application/json",Authorization:this.apiKey},method:"POST",dataType:"json",data:JSON.stringify(e),success:function(e){var t=new p(!0,i.WEB_GUILD_GET);t.code=200,t.body=e,t.message="Success",this.onComplete(t)}.bind(this),error:function(e){var t=new p(!1,i.WEB_GUILD_GET);t.code=e.status,t.body=e.responseJSON,t.message=e.responseJSON.message,this.onComplete(t)}.bind(this)})},e.prototype.onComplete=function(e){this.callback.onCallback(e)},e}(),m=function(){function e(e,t){this.guildId=e,this.callback=t}return e.prototype.provideApiDetails=function(e,t){this.apiKey=e,this.apiUrl=e},Object.defineProperty(e.prototype,"controlRole",{set:function(e){this._controlRole=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"discalChannel",{set:function(e){this._discalChannel=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"simpleAnnouncements",{set:function(e){this._simpleAnnouncements=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"lang",{set:function(e){this._lang=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"prefix",{set:function(e){this._prefix=e},enumerable:!0,configurable:!0}),e.prototype.execute=function(){var e={guild_id:this.guildId};this.controlRole.length>0&&(e.control_role=this.controlRole),this.discalChannel.length>0&&(e.discal_channel=this.discalChannel),this.updateSimpleAnnouncements&&(e.simple_announcements=this.simpleAnnouncements),this.lang.length>0&&(e.lang=this.lang),this.prefix.length>0&&(e.prefix=this.prefix),$.ajax({url:this.apiUrl+"/v2/guild/settings/update",headers:{"Content-Type":"application/json",Authorization:this.apiKey},method:"POST",dataType:"json",data:JSON.stringify(e),success:function(e){var t=new p(!0,i.GUILD_SETTINGS_UPDATE);t.code=200,t.body=e,t.message=e.message,this.onComplete(t)}.bind(this),error:function(e){var t=new p(!1,i.GUILD_SETTINGS_UPDATE);t.code=e.status,t.body=e.responseJSON,t.message=e.responseJSON.message,this.onComplete(t)}.bind(this)})},e.prototype.onComplete=function(e){this.callback.onCallback(e)},e}(),f=function(){function e(e,t){this.guildId=e,this.callback=t}return e.prototype.provideApiDetails=function(e,t){this.apiKey=e,this.apiUrl=e},Object.defineProperty(e.prototype,"botNick",{set:function(e){this._botNick=e},enumerable:!0,configurable:!0}),e.prototype.execute=function(){var e={guild_id:this.guildId,bot_nick:this._botNick};$.ajax({url:this.apiUrl+"/v2/guild/update",headers:{"Content-Type":"application/json",Authorization:this.apiKey},method:"POST",dataType:"json",data:JSON.stringify(e),success:function(e){var t=new p(!0,i.WEB_GUILD_UPDATE);t.code=200,t.body=e,t.message=e.message,this.onComplete(t)}.bind(this),error:function(e){var t=new p(!1,i.WEB_GUILD_UPDATE);t.code=e.status,t.body=e.responseJSON,t.message=e.responseJSON.message,this.onComplete(t)}.bind(this)})},e.prototype.onComplete=function(e){this.callback.onCallback(e)},e}(),b=function(){function e(e,t,n){this.apiKey=e,this.apiUrl=t,this.userId=n,this.guildId=parseInt(window.location.pathname.split("/")[2])}return e.prototype.startDashboardGuildPage=function(){var e=new h(this.guildId,this.userId,this);e.provideApiDetails(this.apiKey,this.apiUrl),e.execute()},e.prototype.handleWebGuildGet=function(e){this.guild=new s(this.guildId).fromJson(e.body),document.getElementById("nickname-input").value=this.guild.botNick,document.getElementById("nick-update-btn").onclick=function(){this.updateBotNick()}.bind(this),document.getElementById("prefix-input").value=this.guild.settings.prefix,document.getElementById("prefix-update-btn").onclick=function(){this.updatePrefix()}.bind(this),this.guild.settings.isPatronGuild?document.getElementById("patron-display").innerText="This guild is a patron guild":document.getElementById("patron-display").innerText="This guild is NOT a patron guild",this.guild.settings.isDevGuild?document.getElementById("dev-display").innerText="This guild is a dev guild":document.getElementById("dev-display").innerText="This guild is NOT a dev guild",u.hideLoader(),document.getElementById("guild-settings").hidden=!1},e.prototype.updateBotNick=function(){var e=new f(this.guildId,this);e.provideApiDetails(this.apiKey,this.apiUrl),e.botNick=document.getElementById("nickname-input").value,this.guild.botNick=e.botNick,e.execute()},e.prototype.updatePrefix=function(){var e=new m(this.guildId,this);e.provideApiDetails(this.apiKey,this.apiUrl),e.prefix=document.getElementById("prefix-input").value,this.guild.settings.prefix=e.prefix,e.execute()},e.prototype.onCallback=function(e){if(e.isSuccess)switch(e.type){case i.GUILD_SETTINGS_GET:this.handleWebGuildGet(e);break;case i.GUILD_SETTINGS_UPDATE:case i.WEB_GUILD_UPDATE:c.showSnackbar(e.message)}else switch(e.type){case i.WEB_GUILD_GET:404==e.code?(u.hideLoader(),document.getElementById("not-connected").hidden=!1,c.showSnackbar("Guild not Found")):c.showSnackbar("[ERROR] "+e.message);break;default:c.showSnackbar("[ERROR] "+e.message)}},e}();!function(e){e[e.MELROSE=0]="MELROSE",e[e.RIPTIDE=1]="RIPTIDE",e[e.MAUVE=2]="MAUVE",e[e.TANGERINE=3]="TANGERINE",e[e.DANDELION=4]="DANDELION",e[e.MAC_AND_CHEESE=5]="MAC_AND_CHEESE",e[e.TURQUOISE=6]="TURQUOISE",e[e.MERCURY=7]="MERCURY",e[e.BLUE=8]="BLUE",e[e.GREEN=9]="GREEN",e[e.RED=10]="RED",e[e.NONE=11]="NONE"}(d||(d={})),function(e){e[e.DAILY=0]="DAILY",e[e.WEEKLY=1]="WEEKLY",e[e.MONTHLY=2]="MONTHLY",e[e.YEARLY=3]="YEARLY"}(l||(l={}));var y=function(){function e(){}return Object.defineProperty(e.prototype,"id",{get:function(){return this._id},set:function(e){this._id=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"address",{get:function(){return this._address},set:function(e){this._address=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"link",{get:function(){return this._link},set:function(e){this._link=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"name",{get:function(){return this._name},set:function(e){this._name=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"description",{get:function(){return this._description},set:function(e){this._description=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"timezone",{get:function(){return this._timezone},set:function(e){this._timezone=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"isExternal",{get:function(){return this._isExternal},set:function(e){this._isExternal=e},enumerable:!0,configurable:!0}),e.prototype.toJson=function(){var e={id:this.id,address:this.address,link:this.link,name:this.name,timezone:this.timezone,external:this.isExternal};return null!=this.description&&(e.description=this.description),e},e.prototype.fromJson=function(e){return this.id=e.id,this.address=e.address,this.link=e.link,this.name=e.name,e.hasOwnProperty("description")&&(this.description=e.description),this.timezone=e.timezone,this.isExternal=e.external,this},e}(),g=function(){function e(e,t,n){this.guildId=e,this.calNum=t,this.callback=n}return e.prototype.provideApiDetails=function(e,t){this.apiKey=e,this.apiUrl=e},e.prototype.execute=function(){var e={guild_id:this.guildId,calendar_number:this.calNum};$.ajax({url:this.apiUrl+"/v2/calendar/get",headers:{"Content-Type":"application/json",Authorization:this.apiKey},method:"POST",dataType:"json",data:JSON.stringify(e),success:function(e){var t=new p(!0,i.CALENDAR_GET);t.code=200,t.body=e,t.message="Success",this.onComplete(t)}.bind(this),error:function(e){var t=new p(!1,i.CALENDAR_GET);t.code=e.status,t.body=e.responseJSON,t.message=e.responseJSON.message,this.onComplete(t)}.bind(this)})},e.prototype.onComplete=function(e){this.callback.onCallback(e)},e}(),E=function(){function e(e,t,n,i,r){this.guildId=e,this.calNum=t,this.days=n,this.epochStart=i,this.callback=r}return e.prototype.provideApiDetails=function(e,t){this.apiKey=e,this.apiUrl=e},e.prototype.execute=function(){var e={guild_id:this.guildId,calendar_number:this.calNum,days_in_month:this.days,epoch_start:this.epochStart};$.ajax({url:this.apiUrl+"/v2/events/list/month",headers:{"Content-Type":"application/json",Authorization:this.apiKey},method:"POST",dataType:"json",data:JSON.stringify(e),success:function(e){var t=new p(!0,i.EVENT_LIST_MONTH);t.code=200,t.body=e,t.message=e.message,this.onComplete(t)}.bind(this),error:function(e){var t=new p(!1,i.EVENT_LIST_MONTH);t.code=e.status,t.body=e.responseJSON,t.message=e.responseJSON.message,this.onComplete(t)}.bind(this)})},e.prototype.onComplete=function(e){this.callback.onCallback(e)},e}(),_=function(){function e(e,t,n,i){this.guildId=e,this.calNum=t,this.epochStart=n,this.callback=i}return e.prototype.provideApiDetails=function(e,t){this.apiKey=e,this.apiUrl=e},e.prototype.execute=function(){var e={guild_id:this.guildId,calendar_number:this.calNum,epoch_start:this.epochStart};$.ajax({url:this.apiUrl+"/v2/events/list/date",headers:{"Content-Type":"application/json",Authorization:this.apiKey},method:"POST",dataType:"json",data:JSON.stringify(e),success:function(e){var t=new p(!0,i.EVENT_LIST_DATE);t.code=200,t.body=e,t.message=e.message,this.onComplete(t)}.bind(this),error:function(e){var t=new p(!1,i.EVENT_LIST_DATE);t.code=e.status,t.body=e.responseJSON,t.message=e.responseJSON.message,this.onComplete(t)}.bind(this)})},e.prototype.onComplete=function(e){this.callback.onCallback(e)},e}(),v=function(){function e(){this.frequency=l.DAILY,this.interval=1,this.count=-1}return Object.defineProperty(e.prototype,"frequency",{get:function(){return this._frequency},set:function(e){this._frequency=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"interval",{get:function(){return this._interval},set:function(e){this._interval=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"count",{get:function(){return this._count},set:function(e){this._count=e},enumerable:!0,configurable:!0}),e.prototype.toJson=function(){return{frequency:l[this.frequency],interval:this.interval,count:this.count}},e.prototype.fromJson=function(e){return this.frequency=l[e.frequency],this.interval=e.interval,this.count=e.count,this},e}(),C=function(){function e(){this._eventId="",this._summary="",this._description="",this._location="",this._color=d.NONE,this._image=""}return Object.defineProperty(e.prototype,"eventId",{get:function(){return this._eventId},set:function(e){this._eventId=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"epochStart",{get:function(){return this._epochStart},set:function(e){this._epochStart=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"epochEnd",{get:function(){return this._epochEnd},set:function(e){this._epochEnd=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"summary",{get:function(){return this._summary},set:function(e){this._summary=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"description",{get:function(){return this._description},set:function(e){this._description=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"location",{get:function(){return this._location},set:function(e){this._location=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"isParent",{get:function(){return this._isParent},set:function(e){this._isParent=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"color",{get:function(){return this._color},set:function(e){this._color=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"doesRecur",{get:function(){return this._recur},set:function(e){this._recur=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"recurrence",{get:function(){return this._recurrence},set:function(e){this._recurrence=e},enumerable:!0,configurable:!0}),Object.defineProperty(e.prototype,"image",{get:function(){return this._image},set:function(e){this._image=e},enumerable:!0,configurable:!0}),e.prototype.toJson=function(){var e={event_id:this.eventId,epoch_start:this.epochStart,epoch_end:this.epochEnd,color:d[this.color],recur:this.doesRecur};return this.summary.length>0&&(e.summary=this.summary),this.description.length&&(e.description=this.description),this.location.length>0&&(e.location=this.location),this.doesRecur&&(e.recurrence=this.recurrence.toJson()),this.image.length>0&&(e.image=this.image),e},e.prototype.fromJson=function(e){return this.eventId=e.event_id,this.epochStart=e.epoch_start,this.epochEnd=e.epoch_end,e.hasOwnProperty("summary")&&(this.summary=e.summary),e.hasOwnProperty("description")&&(this.description=e.description),e.hasOwnProperty("location")&&(this.location=e.location),this.isParent=e.is_parent,this.color=d[e.color],this.doesRecur=e.recur,this.doesRecur&&(this.recurrence=(new v).fromJson(e.recurrence)),e.hasOwnProperty("image")&&(this.image=e.image),this},e}(),T=function(){function e(){this.guildId=parseInt(window.location.pathname.split("/")[2]),this.calNumber=parseInt(window.location.pathname.split("/")[3]),this.todaysDate=new Date,this.selectedDate=new Date,this.displays=[],this.apiKey="",this.apiUrl=""}return e.prototype.init=function(e,t){if(this.apiKey=e,this.apiUrl=t,"internal_error"===this.apiKey)u.hideLoader(),alert("Failed to get a read-only API key to display your calendar. \nIf you keep receiving this error, please contact the developers");else{var n=new g(this.guildId,this.calNumber,this);n.provideApiDetails(this.apiKey,this.apiUrl),n.execute(),this.getEventsForMonth()}return this},e.prototype.getMonthName=function(e){return["January","February","March","April","May","June","July","August","September","October","November","December"][e]},e.prototype.getDayName=function(e){return["Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"][e]},e.prototype.dateDisplays=function(){return["r1c1","r1c2","r1c3","r1c4","r1c5","r1c6","r1c7","r2c1","r2c2","r2c3","r2c4","r2c5","r2c6","r2c7","r3c1","r3c2","r3c3","r3c4","r3c5","r3c6","r3c7","r4c1","r4c2","r4c3","r4c4","r4c5","r4c6","r4c7","r5c1","r5c2","r5c3","r5c4","r5c5","r5c6","r5c7","r6c1","r6c2","r6c3","r6c4","r6c5","r6c6","r6c7"]},e.prototype.daysInMonth=function(){return new Date(this.selectedDate.getFullYear(),this.selectedDate.getMonth()+1,0).getDate()},e.prototype.dateDisplaysToChange=function(e){return this.dateDisplays().slice(this.dateDisplays().indexOf(e),this.dateDisplays().length-1)},e.prototype.findFirstDayOfMonthPosition=function(){var e=new Date(this.selectedDate.getFullYear(),this.selectedDate.getMonth(),1);switch(this.getDayName(e.getDay())){case"Sunday":return"r1c1";case"Monday":return"r1c2";case"Tuesday":return"r1c3";case"Wednesday":return"r1c4";case"Thursday":return"r1c5";case"Friday":return"r1c6";case"Saturday":return"r1c7"}return"r1c1"},e.prototype.changeRecurrenceEditDisplays=function(e){var t=e.id.split("-")[1];e.checked?(document.getElementById("editFrequency-"+t).disabled=!1,document.getElementById("editCount-"+t).disabled=!1,document.getElementById("editInterval-"+t).disabled=!1):(document.getElementById("editFrequency-"+t).disabled=!0,document.getElementById("editCount-"+t).disabled=!0,document.getElementById("editInterval-"+t).disabled=!0)},e.prototype.setMonth=function(e){document.getElementById("month-display").innerHTML=this.getMonthName(e.getMonth())+" "+e.getFullYear(),this.displays=[];for(var t=this.dateDisplays(),n=0;n<t.length;n++){var i=document.getElementById(t[n]);i.innerHTML="",i.className=""}for(var r=this.dateDisplaysToChange(this.findFirstDayOfMonthPosition()),o=this.daysInMonth(),a=0;a<r.length;a++){var s=a+1;if(s<=o){var c=document.getElementById(r[a]);c.innerHTML=s+"",this.displays[s]=r[a];var d=new Date(this.selectedDate.getFullYear(),this.selectedDate.getMonth(),s);s===this.selectedDate.getDate()&&(c.className="selected"),d.getMonth()===this.todaysDate.getMonth()&&d.getFullYear()===this.todaysDate.getFullYear()&&d.getDate()===this.todaysDate.getDate()&&(c.className="today")}}},e.prototype.getEventsForMonth=function(){var e=new Date(this.selectedDate.getFullYear(),this.selectedDate.getMonth(),1);e.setHours(0,0,0,0);var t=new E(this.guildId,this.calNumber,this.daysInMonth(),e.getTime(),this);t.provideApiDetails(this.apiKey,this.apiUrl),t.execute()},e.prototype.getEventsForSelectedDate=function(){var e=new Date(this.selectedDate.getFullYear(),this.selectedDate.getMonth(),this.selectedDate.getDate());e.setHours(0,0,0,0),u.hideEventsContainer();var t=new _(this.guildId,this.calNumber,e.getTime(),this);t.provideApiDetails(this.apiKey,this.apiUrl),u.hideEventsContainer(),t.execute()},e.prototype.onCallback=function(e){if(e.isSuccess)switch(e.type){case i.CALENDAR_GET:this.calendarData=(new y).fromJson(e.body),document.getElementById("view-on-google-button").href="https://calendar.google.com/calendar/embed?src="+this.calendarData.address;break;case i.EVENT_LIST_MONTH:for(var t=0;t<e.body.events.length;t++){var n=new Date(e.body.events[t].epoch_start),r=document.getElementById(this.displays[n.getDate()]);-1===r.innerHTML.indexOf("[")?r.innerHTML=n.getDate()+"[1]":r.innerHTML=n.getDate().toString()+"["+(parseInt(r.innerHTML.split("[")[1][0])+1).toString()+"]"}u.hideLoader(),u.showCalendarContainer();break;case i.EVENT_LIST_DATE:this.loadEventDisplay(e)}else c.showSnackbar("ERROR] "+e.message)},e.prototype.loadEventDisplay=function(e){for(var t=document.getElementById("event-container");t.firstChild;)t.removeChild(t.firstChild);for(var n=0;n<e.body.events.length;n++){var i=(new C).fromJson(e.body.events[n]),r=document.createElement("button");r.type="button",r.setAttribute("data-toggle","modal"),r.setAttribute("data-target","#modal-"+i.eventId),r.innerHTML="View Event With ID: "+i.eventId,t.appendChild(r),t.appendChild(document.createElement("br")),t.appendChild(document.createElement("br"));var o=document.createElement("div");o.className="modal fade",o.id="modal-"+i.eventId,o.role="dialog",t.appendChild(o);var a=document.createElement("div");a.className="modal-dialog",o.appendChild(a);var s=document.createElement("div");s.className="modal-content",a.appendChild(s);var c=document.createElement("div");c.className="modal-header",s.appendChild(c);var p=document.createElement("h4");p.className="modal-title",p.innerHTML="Viewing Event",c.appendChild(p);var h=document.createElement("div");h.className="modal-body",s.appendChild(h);var m=document.createElement("form");h.appendChild(m);var f=document.createElement("label");f.innerHTML="Summary",f.appendChild(document.createElement("br")),m.appendChild(f);var b=document.createElement("input");b.name="summary",b.type="text",b.value=i.summary,b.id="editSummary-"+i.eventId,f.appendChild(b),m.appendChild(document.createElement("br")),m.appendChild(document.createElement("br"));var y=document.createElement("label");y.innerHTML="Description",y.appendChild(document.createElement("br")),m.appendChild(y);var g=document.createElement("input");g.name="edit-description",g.type="text",g.value=i.description,g.id="editDescription-"+i.eventId,y.appendChild(g),m.appendChild(document.createElement("br")),m.appendChild(document.createElement("br"));var E=new Date(i.epochStart),_=document.createElement("label");_.innerHTML="Start Date and Time",_.appendChild(document.createElement("br")),m.appendChild(_);var v=document.createElement("input");v.name="start-date",v.type="date",v.valueAsDate=E,v.id="editStartDate-"+i.eventId,_.appendChild(v);var T=document.createElement("input");T.name="start-time",T.type="time",T.value=(E.getHours()<10?"0":"")+E.getHours()+":"+(E.getMinutes()<10?"0":"")+E.getMinutes(),T.id="editStartTime-"+i.eventId,_.appendChild(T),m.appendChild(document.createElement("br")),m.appendChild(document.createElement("br"));var D=new Date(i.epochEnd),N=document.createElement("label");N.innerHTML="End Date and Time",N.appendChild(document.createElement("br")),m.appendChild(N);var I=document.createElement("input");I.name="end-date",I.type="date",I.valueAsDate=D,I.id="editEndDate-"+i.eventId,N.appendChild(I);var O=document.createElement("input");O.name="end-time",O.type="time",O.value=(D.getHours()<10?"0":"")+D.getHours()+":"+(D.getMinutes()<10?"0":"")+D.getMinutes(),O.id="editEndTime-"+i.eventId,N.appendChild(O),m.appendChild(document.createElement("br")),m.appendChild(document.createElement("br"));var P=document.createElement("label");P.innerHTML="Location",P.appendChild(document.createElement("br")),m.appendChild(P);var A=document.createElement("input");A.name="location",A.type="text",A.value=i.location,A.id="editLocation-"+i.eventId,P.appendChild(A),m.appendChild(document.createElement("br")),m.appendChild(document.createElement("br"));var S=document.createElement("label");S.innerHTML="Color",S.appendChild(document.createElement("br")),m.appendChild(S);var M=document.createElement("select");for(var L in M.name="color",M.id="editColor-"+i.eventId,S.appendChild(M),d){var w=document.createElement("option");w.value=d[L],w.text=d[L],w.selected=d[i.color]===d[L],M.appendChild(w)}if(m.appendChild(document.createElement("br")),m.appendChild(document.createElement("br")),i.doesRecur){var j=document.createElement("label");if(j.innerHTML="Recurrence",j.appendChild(document.createElement("br")),m.appendChild(j),i.isParent){var x=document.createElement("input");x.name="enable-recurrence",x.type="checkbox",x.checked=!1,x.id="editEnableRecur-"+i.eventId,x.onclick=function(){this.changeRecurrenceEditDisplays(this)}.bind(this),j.appendChild(x),m.appendChild(document.createElement("br")),m.appendChild(document.createElement("br"));var R=document.createElement("label");R.innerHTML="Recurrence - Frequency",R.appendChild(document.createElement("br")),m.appendChild(R);var U=document.createElement("select");for(var k in U.name="frequency",U.id="editFrequency-"+i.eventId,R.appendChild(U),l){var G=document.createElement("option");G.value=l[k],G.text=l[k],G.selected=l[i.recurrence.frequency]===l[k],U.appendChild(G)}U.disabled=!0,R.appendChild(U),m.appendChild(document.createElement("br")),m.appendChild(document.createElement("br"));var B=document.createElement("label");B.innerHTML="Recurrence - Count",B.appendChild(document.createElement("br")),m.appendChild(B);var J=document.createElement("input");J.name="count",J.type="number",J.valueAsNumber=i.recurrence.count,J.min="-1",J.id="editCount-"+i.eventId,J.disabled=!0,B.appendChild(J),m.appendChild(document.createElement("br")),m.appendChild(document.createElement("br"));var H=document.createElement("label");H.innerHTML="Recurrence - Interval",H.appendChild(document.createElement("br")),m.appendChild(H);var V=document.createElement("input");V.name="interval",V.type="number",V.valueAsNumber=i.recurrence.interval,V.min="1",V.id="editInterval-"+i.eventId,V.disabled=!0,H.appendChild(V),m.appendChild(document.createElement("br")),m.appendChild(document.createElement("br"))}else{var F=document.createElement("input");F.name="ignore-cer",F.type="text",F.disabled=!0,F.value="Cannot edit child",j.appendChild(F)}m.appendChild(document.createElement("br")),m.appendChild(document.createElement("br"))}var K=document.createElement("label");K.innerHTML="Image",K.appendChild(document.createElement("br")),m.appendChild(K);var Y=document.createElement("input");Y.name="image",Y.type="text",Y.value=i.image,Y.id="editImage-"+i.eventId,K.appendChild(Y),m.appendChild(document.createElement("br")),m.appendChild(document.createElement("br"));var W=document.createElement("label");W.innerHTML="Event ID",W.appendChild(document.createElement("br")),m.appendChild(W);var q=document.createElement("input");q.type="text",q.name="id",q.value=i.eventId,q.id="editId-"+i.eventId,q.readOnly=!0,W.appendChild(q),m.appendChild(document.createElement("br")),m.appendChild(document.createElement("br"));var z=document.createElement("div");z.className="modal-footer",s.appendChild(z);var $=document.createElement("button");$.type="button",$.setAttribute("data-dismiss","modal"),$.innerHTML="Close",z.appendChild($)}u.showEventsContainer()},e}(),D=function(){function e(){this.embedCalendar=new T}return e.prototype.init=function(e,t){this.embedCalendar.init(e,t)},e.prototype.previousEmbedMonth=function(){this.embedCalendar.selectedDate.setMonth(this.embedCalendar.selectedDate.getMonth()-1),this.embedCalendar.selectedDate.setDate(1),this.embedCalendar.setMonth(this.embedCalendar.selectedDate),this.embedCalendar.getEventsForMonth()},e.prototype.nextEmbedMonth=function(){this.embedCalendar.selectedDate.setMonth(this.embedCalendar.selectedDate.getMonth()+1),this.embedCalendar.selectedDate.setDate(1),this.embedCalendar.setMonth(this.embedCalendar.selectedDate),this.embedCalendar.getEventsForMonth()},e.prototype.selectEmbedDate=function(e){var t=document.getElementById(e).innerHTML.split("[")[0];if(""!==t){var n=parseInt(t);this.embedCalendar.selectedDate.setDate(n),this.embedCalendar.setMonth(this.embedCalendar.selectedDate),this.embedCalendar.getEventsForMonth(),this.embedCalendar.getEventsForSelectedDate()}},e}();document.onload=function(){var e,t,n=document.getElementById("page-top");null!=n.dataset.embed_key?(e=n.dataset.embed_key,t=n.dataset.api_url,(new D).init(e,t)):null!=n.dataset.api_key&&function(e,t,n){new b(e,t,parseInt(n)).startDashboardGuildPage()}(n.dataset.api_key,n.dataset.api_url,n.dataset.user_id)},function(e){e("#sidebarToggle, #sidebarToggleTop").on("click",(function(){e("body").toggleClass("sidebar-toggled");var t=e(".sidebar");t.toggleClass("toggled"),t.hasClass("toggled")&&e(".sidebar .collapse").collapse(!0)})),e(window).resize((function(){e(window).width()<768&&e(".sidebar .collapse").collapse(!0)})),e("body.fixed-nav .sidebar").on("mousewheel DOMMouseScroll wheel",(function(t){if(e(window).width()>768){var n=t.originalEvent,i=n.wheelDelta||-n.detail;this.scrollTop+=30*(i<0?1:-1),t.preventDefault()}})),e(document).on("scroll",(function(){e(this).scrollTop()>100?e(".scroll-to-top").fadeIn():e(".scroll-to-top").fadeOut()})),e(document).on("click","a.scroll-to-top",(function(t){var n=e(this);e("html, body").stop().animate({scrollTop:e(n.attr("href")).offset().top},1e3,"easeInOutExpo"),t.preventDefault()}))}(jQuery)}]);