package me.trollcoding.cdmbotsample.listener

import me.trollcoding.cdmbotsample.Bot
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.impl.UserImpl
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import java.awt.Color
import java.util.regex.Pattern

class GameCodeListener(val bot : Bot) : ListenerAdapter() {

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent?) {
        if (event != null) {
            if (event.author.isBot) return
        }

        val channel = event!!.channel

        if (channel.name != "game-codes") return

        event.apply {
            val author = message.author
            val user = member.user
            val id = author.idLong
            message.delete().queue()

            if(bot.gameCodeMsgId <= -1){
                if (!user.hasPrivateChannel()) user.openPrivateChannel().complete()
                (user as UserImpl).privateChannel.sendMessage(
                    EmbedBuilder().apply {
                        setColor(Color.RED)
                        setAuthor("You can't be sent code.")
                        setDescription(
                            "現在チャットロック中です \n" +
                                    "The chat locked."
                        )
                    }.build()
                ).queue()
                return
            }

            val raw = message.contentRaw
            val pattern = Pattern.compile("[a-zA-Z0-9]*")
            val matcher = pattern.matcher(raw)
            if (!matcher.matches() || raw.length != 3) {
                if (!user.hasPrivateChannel()) user.openPrivateChannel().complete()
                (user as UserImpl).privateChannel.sendMessage(
                    EmbedBuilder().apply {
                        setColor(Color.RED)
                        setAuthor("Error: Game Code is invalid.")
                        setDescription(
                            "3桁の半角英数字で入力してください。 \n" +
                                    "Please enter with 3 alphanumeric characters."
                        )
                    }.build()
                ).queue()
            } else {
                val code = raw.toLowerCase()
                bot.putGameCode(author.idLong, code)
                if (!user.hasPrivateChannel()) user.openPrivateChannel().complete()
                (user as UserImpl).privateChannel.sendMessage(
                    EmbedBuilder().apply {
                        setColor(Color.GREEN)
                        setAuthor("Successfully")
                        setDescription(
                            "あなたのゲームコードは共有されました。 \n" +
                                    "Your game code sharing has been completed."
                        )
                    }.build()
                ).queue()
                val raw = guild.getTextChannelsByName("game-codes", true).stream().findFirst()
                if (raw.isPresent) {
                    val ch = raw.get()
                    ch.getMessageById(bot.gameCodeMsgId).submit().get().editMessage(EmbedBuilder().apply {
                        setColor(Color.YELLOW)
                        setAuthor("現在の参加者 / Current Players")
                        clearFields()
                        val codes = LinkedHashSet<String>()
                        for (a in bot.codes){
                            codes.add(a.rawCode)
                        }
                        for (c in codes) {
                            addField("ID: " + c + " - " + bot.countByCode(c) + " players", bot.getMentionsByCode(c, guild), false)
                        }
                    }.build()).queue()
                }
            }
        }
    }
}

