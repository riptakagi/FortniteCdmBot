package me.trollcoding.cdmbotsample.utils

import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.entities.Role

object DiscordUtils {

    fun hasRole(member: Member, roleName: String): Boolean {
        for (role in member.roles) {
            if (role.name.equals(roleName)) {
                return true
            }
        }
        return false
    }

    fun getRoleByName(name: String, guild: Guild) : Role?{
        for(role in guild.roles){
            if(role.name.equals(name, true)){
                return role
            }
        }
        return null
    }

    fun isExistsChannelByName(channelName: String, guild: Guild): Boolean {
        for (channel in guild.channels) {
            if (channel.name.equals(channelName, true)) {
                return true
            }
        }
        return false
    }

    fun getField(fields: MutableList<MessageEmbed.Field>, name: String) : MessageEmbed.Field? {
        for(field in fields){
            if(field.name.equals(name, true)){
                return field
            }
        }
        return null
    }
}