package me.trollcoding.cdmbotsample.obj

import me.trollcoding.cdmbotsample.Bot


class GameCode(val idLong: Long, var rawCode: String, bot: Bot){

    init {
        bot.codes.add(this)
    }
}