package me.trollcoding.cdmbotsample

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.trollcoding.cdmbotsample.audio.GuildMusicManager
import me.trollcoding.cdmbotsample.listener.CommandListener
import me.trollcoding.cdmbotsample.listener.GameCodeListener
import me.trollcoding.cdmbotsample.listener.MessageReplacerListener
import me.trollcoding.cdmbotsample.obj.GameCode
import me.trollcoding.cdmbotsample.order.OrderInfoManager
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.VoiceChannel
import net.dv8tion.jda.core.managers.AudioManager
import java.util.*
import kotlin.collections.HashMap

class Bot (private val token: String) {

    companion object {
        @JvmStatic
        lateinit var instance: Bot
    }

    lateinit var jda: JDA
    var gameCodeMsgId: Long = -1
    val codes = LinkedList<GameCode>()
    val playerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    val musicManagers: HashMap<Long, GuildMusicManager> = HashMap()
    val orderInfoManager: OrderInfoManager = OrderInfoManager()

    fun start() {
        instance = this
        jda = JDABuilder(AccountType.BOT).setToken(token).setStatus(OnlineStatus.ONLINE).build().apply{
                addEventListener(GameCodeListener(instance))
                addEventListener(CommandListener(instance))
            addEventListener(MessageReplacerListener(instance))
            }
        AudioSourceManagers.registerRemoteSources(playerManager)
        AudioSourceManagers.registerLocalSource(playerManager)
    }

    fun getGameCodeByDiscordId(idLong: Long): GameCode? {
        for (code in codes) {
            if (code.idLong == idLong) {
                return code
            }
        }
        return null
    }

    fun countByCode(target: String): Int {
        var count = 0
        for (code in codes) {
            if (code.rawCode.equals(target, true)) {
                count++
            }
        }
        return count
    }

    fun getMentionsByCode(target: String, guild: Guild): String {
        val mentions = LinkedList<String>()
        for (code in codes) {
            if (code.rawCode == target) {
                mentions.add(guild.getMemberById(code.idLong).asMention)
            }
        }
        val sb = StringBuilder()
        val it = mentions.iterator()
        while (it.hasNext()) {
            sb.append(it.next())
            if (it.hasNext()) {
                sb.append("\n")
            }
        }
        return sb.toString()
    }

    fun putGameCode(idLong: Long, code: String) {
        val gameCode = getGameCodeByDiscordId(idLong)
        if (gameCode == null) {
            GameCode(idLong, code, this)
        } else {
            gameCode.rawCode = code
        }
    }

    @Synchronized
    fun getGuildAudioPlayer(guild: Guild): GuildMusicManager {
        val guildId = java.lang.Long.parseLong(guild.id)
        var musicManager: GuildMusicManager? = musicManagers[guildId]

        if (musicManager == null) {
            musicManager = GuildMusicManager(playerManager)
            musicManagers[guildId] = musicManager
        }

        guild.audioManager.sendingHandler = musicManager.sendHandler

        return musicManager
    }

    fun loadAndPlay(guild: Guild, voiceCh: VoiceChannel, trackUrl: String) {
        val musicManager = getGuildAudioPlayer(guild)

        playerManager.loadItemOrdered(musicManager, trackUrl, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                play(guild, voiceCh, musicManager, track)
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                var firstTrack: AudioTrack? = playlist.selectedTrack

                if (firstTrack == null) {
                    firstTrack = playlist.tracks[0]
                }

                play(guild, voiceCh, musicManager, firstTrack)
            }

            override fun noMatches() {

            }

            override fun loadFailed(exception: FriendlyException) {

            }
        })
    }

    fun play(guild: Guild, voiceCh: VoiceChannel, musicManager: GuildMusicManager, track: AudioTrack?) {
        connectToFirstVoiceChannel(guild.audioManager, voiceCh)

        musicManager.scheduler.queue(track)
    }

    private fun skipTrack(channel: TextChannel) {
        val musicManager = getGuildAudioPlayer(channel.guild)
        musicManager.scheduler.nextTrack()

        channel.sendMessage("Skipped to next track.").queue()
    }

    fun connectToFirstVoiceChannel(audioManager: AudioManager, voiceCh: VoiceChannel) {
        if (!audioManager.isConnected && !audioManager.isAttemptingToConnect) {
            audioManager.openAudioConnection(voiceCh)
        }
    }

}
