package org.dreamexposure.discal.server.network.discal

import com.google.common.io.CharStreams
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.`object`.network.discal.ConnectedClient
import org.dreamexposure.discal.core.logger.LogFeed
import org.dreamexposure.discal.core.logger.`object`.LogObject
import org.dreamexposure.discal.core.utils.GlobalConst
import org.dreamexposure.discal.server.DisCalServer
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import kotlin.concurrent.timerTask

object NetworkMediator {
    //TODO: Use flux interval instead of timer eventually
    private var timer: Timer? = null

    fun init() {
        timer = Timer(true)

        timer?.schedule(timerTask {
            val downShards = mutableListOf<ConnectedClient>()

            for (client in DisCalServer.networkInfo.clients) {
                if (System.currentTimeMillis() > client.lastKeepAlive + (5 * GlobalConst.oneMinuteMs))
                    downShards.add(client) //Missed last 5+ heartbeats...
            }

            //Now we issue the restarts for the shards
            Flux.fromIterable(downShards)
                    .flatMap(this@NetworkMediator::issueRestart)
                    .subscribe()

            downShards.forEach(this@NetworkMediator::issueRestart)

        }, GlobalConst.oneMinuteMs, GlobalConst.oneMinuteMs)
    }

    fun shutdown() {
        if (timer != null) timer?.cancel()
    }

    private fun issueRestart(client: ConnectedClient): Mono<Void> {
        if (BotSettings.USE_RESTART_SERVICE.get().equals("true", true)) {
            DisCalServer.networkInfo.removeClient(client.clientIndex, "Restart service not active!")
            return Mono.empty()
        }

        return Mono.fromCallable {
            val session = createSession(client.ipForRestart, client.portForRestart)
            session.connect()

            val channel = session.openChannel("exec") as ChannelExec

            //Tell network manager to remove this client until it restarts.
            DisCalServer.networkInfo
                    .removeClient(client.clientIndex, "Restart issued by mediator for missed heartbeats")

            //Do it
            try {
                channel.setCommand(BotSettings.RESTART_CMD.get().replace("%index%", client.clientIndex.toString()))
                channel.inputStream = null
                val output = channel.inputStream
                channel.connect()

                @Suppress("UnstableApiUsage")
                CharStreams.toString(InputStreamReader(output))
            } catch (e: JSchException) {
                LogFeed.log(LogObject
                        .forException("[NETWORK] Shard restart failure", "${client.clientIndex} s2", e, this.javaClass))
                closeConnection(channel, session)
            } catch (e: IOException) {
                LogFeed.log(LogObject
                        .forException("[NETWORK] Shard restart failure", "${client.clientIndex} s2", e, this.javaClass))
                closeConnection(channel, session)
            } finally {
                closeConnection(channel, session)
            }
        }.then()
    }

    @Throws(JSchException::class)
    private fun createSession(ip: String, port: Int): Session {
        //Handle config
        val config = Properties()
        config["StrictHostKeyChecking"] = "no"

        //Handle identity
        val jSch = JSch()
        jSch.addIdentity(BotSettings.RESTART_SSH_KEY.get())

        //Actual session
        val session = jSch.getSession(BotSettings.RESTART_USER.get(), ip, port)
        session.setConfig(config)

        return session
    }

    private fun closeConnection(channel: ChannelExec, session: Session) {
        try {
            channel.disconnect()
        } catch (ignored: Exception) {
        }
        session.disconnect()
    }
}
