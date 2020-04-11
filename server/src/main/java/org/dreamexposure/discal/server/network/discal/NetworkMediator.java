package org.dreamexposure.discal.server.network.discal;

import com.google.common.io.CharStreams;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.network.discal.ConnectedClient;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.server.DisCalServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("UnusedReturnValue")
public class NetworkMediator {
	private static NetworkMediator instance;

	private Timer timer;

	private NetworkMediator() {
	}

	public static NetworkMediator get() {
		if (instance == null)
			instance = new NetworkMediator();

		return instance;
	}

	public void init() {
		timer = new Timer(true);

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				List<ConnectedClient> downShards = new ArrayList<>();

				for (ConnectedClient c : DisCalServer.getNetworkInfo().getClients()) {
					if (System.currentTimeMillis() > c.getLastKeepAlive() + (5 * GlobalConst.oneMinuteMs))
						downShards.add(c); //Missed last 5+ heartbeats...
				}

				//Now we issue the restarts for the shards...
				for (ConnectedClient c : downShards)
					issueRestart(c);

			}
		}, 60 * 1000, 60 * 1000);
	}

	public void shutdown() {
		if (timer != null)
			timer.cancel();
	}

	public String issueRestart(ConnectedClient c) {
		try {
			Session session = createSession(c.getIpForRestart(), c.getPortForRestart());
			session.connect();

			ChannelExec channel = (ChannelExec) session.openChannel("exec");

			try {
				channel.setCommand(BotSettings.RESTART_CMD.get().replace("%index%", c.getClientIndex() + ""));
				channel.setInputStream(null);
				InputStream output = channel.getInputStream();
				channel.connect();

				//noinspection UnstableApiUsage
				return CharStreams.toString(new InputStreamReader(output));
			} catch (JSchException | IOException e) {
				LogFeed.log(LogObject
						.forException("[NETWORK] Shard restart failure", c.getClientIndex() + " s2"
								, e, this.getClass()));
				closeConnection(channel, session);
			} finally {
				closeConnection(channel, session);
			}

			//Tell network manager to remove this client until it restarts.
			DisCalServer.getNetworkInfo().removeClient(c.getClientIndex(), "Restart issued by mediator for missed heartbeats");
		} catch (Exception e) {
			LogFeed.log(LogObject
					.forException("[NETWORK] Shard restart failure", c.getClientIndex() + " s1"
							, e, this.getClass()));
		}

		return "ERROR";
	}

	private Session createSession(String ip, int port) throws JSchException {
		//Handle config
		Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no");

		//Handle identity
		JSch jSch = new JSch();
		jSch.addIdentity(BotSettings.RESTART_SSH_KEY.get());

		//Actual session
		Session session = jSch.getSession(BotSettings.RESTART_USER.get(), ip, port);
		session.setConfig(config);

		return session;
	}

	private void closeConnection(ChannelExec channel, Session session) {
		try {
			channel.disconnect();
		} catch (Exception ignored) {
		}
		session.disconnect();
	}
}
