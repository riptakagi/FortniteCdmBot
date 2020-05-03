package me.trollcoding.cdmbotsample.listener

import me.trollcoding.cdmbotsample.Bot
import me.trollcoding.cdmbotsample.order.OrderInfo
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import java.awt.Color
import java.lang.Exception
import java.text.SimpleDateFormat


class CommandListener (val bot : Bot) : ListenerAdapter() {

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent?) {
        if (event != null) {
            if (event.author.isBot) return
        }

        val channel = event!!.channel

        if (channel.name != "bot-control") return

        event.apply {
            val m = bot.orderInfoManager
            val jda = event.jda
            val message = event.message
            val author = message.author
            val member = event.member
            val user = member.user
            val con = event.guild.controller
            val id = author.idLong
            val args = message.contentRaw.split(" ")
            var name = author.name
            if (member.nickname != null) {
                name = member.nickname
            }
            if (args[0].equals("!host", true)) {
                if (args.size == 5) {
                    val v = guild.getVoiceChannelsByName("Countdown Room", true).stream().findFirst()
                    val mode = args[1]
                    val server = args[2]
                    val day = args[3]
                    val time = args[4]
                    val date = "$day $time"
                    try {
                        SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(date)
                    }catch (e: Exception){
                        channel.sendMessage(EmbedBuilder().apply {
                            setColor(Color.RED)
                            setAuthor(
                                "Date Format is invalid",
                                null,
                                author.avatarUrl
                            )
                            setDescription(":x: 日付は次のようなフォーマットで入力する必要があります　例「2019/01/01 12:00:00」")
                        }.build()).queue()
                        return
                    }
                    if (v.isPresent) {
                        val countdownVc = v.get()
                        bot.codes.clear()
                        val raw = guild.getTextChannelsByName("game-codes", true).stream().findFirst()
                        if (raw.isPresent) {
                            val gamecodeCh = raw.get()
                            m.putOrderInfo(OrderInfo(m.createId(), name, date, mode, server, bot).confirm(channel, gamecodeCh, user, guild, countdownVc))
                        }else{
                            channel.sendMessage(EmbedBuilder().apply {
                                setColor(Color.RED)
                                setAuthor(
                                    "GameCode Channel can't be found",
                                    null,
                                    author.avatarUrl
                                )
                                setDescription(":x: ゲームコードチャンネル(TextChannel)を作成してください「game-codes」")
                            }.build()).queue()
                        }
                    }else{
                        channel.sendMessage(EmbedBuilder().apply {
                            setColor(Color.RED)
                            setAuthor(
                                "Countdown Room can't be found",
                                null,
                                author.avatarUrl
                            )
                            setDescription(":x: カウントダウンルーム(VoiceChannel)を作成してください「Countdown Room」")
                        }.build()).queue()
                    }
                } else {
                    channel.sendMessage(EmbedBuilder().apply {
                        setColor(Color.RED)
                        setAuthor(
                            "Command Usage",
                            null,
                            author.avatarUrl
                        )
                        setDescription(":x: !host <mode> <server> <date>\nDate Example: 2019/01/01 12:00:00")
                    }.build()).queue()
                }
            }
            else if (args[0].equals("!cancel", true)) {
                if (args.size == 2) {
                    val idStr = args[1]
                    val idInt: Int
                    try {
                        idInt = idStr.toInt()
                    }catch (e: NumberFormatException){
                        channel.sendMessage(EmbedBuilder().apply {
                            setColor(Color.RED)
                            setAuthor(
                                "Command Usage",
                                null,
                                author.avatarUrl
                            )
                            setDescription(":x: !cancel <Order ID>")
                        }.build()).queue()
                        return
                    }
                    val orderInfo = bot.orderInfoManager.getOrderInfoById(idInt)
                    if(orderInfo == null){
                        channel.sendMessage(EmbedBuilder().apply {
                            setColor(Color.RED)
                            setAuthor(
                                "Order Not Found",
                                null,
                                author.avatarUrl
                            )
                            setDescription("指定されたIDのオーダーが見つかりません")
                        }.build()).queue()
                    }else{
                        orderInfo.cancel(channel, user, name)
                        m.removeOrderInfo(orderInfo)
                    }
                }else{
                    channel.sendMessage(EmbedBuilder().apply {
                        setColor(Color.RED)
                        setAuthor(
                            "Command Usage",
                            null,
                            author.avatarUrl
                        )
                        setDescription(":x: !cancel <Order ID>")
                    }.build()).queue()
                }
            }
        }
    }
}
