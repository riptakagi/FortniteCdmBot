package me.trollcoding.cdmbotsample.listener

import me.trollcoding.cdmbotsample.Bot
import me.trollcoding.cdmbotsample.utils.DateUtils
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import java.awt.Color
import java.util.*

class MessageReplacerListener (val bot : Bot) : ListenerAdapter() {

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent?) {
        if (event != null) {
            if (event.author.isBot) return
        }

        val channel = event!!.channel

        event.apply {
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
            if(channel.name == "announcements"){
                event.message.delete().complete()
                channel.sendMessage(
                    EmbedBuilder().apply {
                        setColor(Color(47, 139, 251))
                        setTitle("Announcement")
                        setDescription(message.contentDisplay)
                        setFooter(
                            "$name#${user.discriminator}・${DateUtils.dateTimeFormatByDate(Date())}",
                            user.avatarUrl
                        )
                    }.build()
                ).queue()
            }
            else if(channel.name == "schedules"){
                event.message.delete().complete()
                channel.sendMessage(
                    EmbedBuilder().apply {
                        setColor(Color(47, 139, 251))
                        setTitle("Schedule")
                        setDescription(message.contentDisplay)
                        setFooter(
                            "$name#${user.discriminator}・${DateUtils.dateTimeFormatByDate(Date())}",
                            user.avatarUrl
                        )
                    }.build()
                ).queue()
            }
            else if(channel.name == "rules"){
                event.message.delete().complete()
                channel.sendMessage(
                    EmbedBuilder().apply {
                        setColor(Color(47, 139, 251))
                        setTitle("Rules")
                        setDescription(message.contentDisplay)
                    }.build()
                ).queue()
            }
            else if(channel.name == "information"){
                event.message.delete().complete()
                channel.sendMessage(
                    EmbedBuilder().apply {
                        setColor(Color(47, 139, 251))
                        setTitle("Information")
                        setDescription(message.contentDisplay)
                    }.build()
                ).queue()
            }
        }
    }
}