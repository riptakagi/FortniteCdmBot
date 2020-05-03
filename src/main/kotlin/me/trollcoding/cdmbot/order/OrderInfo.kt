package me.trollcoding.cdmbotsample.order

import me.trollcoding.cdmbotsample.Bot
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.entities.VoiceChannel
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.*

class OrderInfo(val orderId: Int, val orderNickname: String, val rawExpireDate: String, val mode: String, val server: String, val bot: Bot) {

    companion object {
        val color: Color = Color.YELLOW
        val title = "Sample CDM"
    }

    /* [0] Main Timer
    * [1] Notify Timer
    * [2] Disconnect Timer
    * [3] ChatLock Timer */
    val timers = arrayOf(Timer(false), Timer(false), Timer(false), Timer(false))

    var activeCountdown = false

    fun confirm(botControlCh: TextChannel, gameCodeCh: TextChannel, sender: User, guild: Guild, countdownVc: VoiceChannel): OrderInfo {
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        val task = object : TimerTask() {
            override fun run() {
                activeCountdown = true
                gameCodeCh.apply {
                    sendMessage(
                        EmbedBuilder().apply {
                            setColor(color)
                            setAuthor(
                                "$title - Match Announcement",
                                null,
                                sender.avatarUrl
                            )
                            setDescription("SNIPE MATCH IS STARTING")
                            addField("Mode:", mode, false)
                            addField("Server:", server, false)
                            addField("3 2 1 Countdown Starts at:", rawExpireDate, false)
                            addField(
                                "カウントダウンマッチ(CDM)開始",
                                "約15秒後に**[Countdown Room]**でカウントダウンボイスが流れます",
                                false
                            )
                            addField(
                                "Start of Count Down Match (CDM)",
                                "The count down will begin with Match starting in 15 second, match starting in 3, 2, 1 “GO”. On the sound of \"GO\" please click the [PLAY] button to queue into the CDM.",
                                false
                            )
                            setFooter(
                                "Hosted by $orderNickname",
                                sender.avatarUrl
                            )
                        }.build()
                    ).queue()
                    sendMessage(
                        EmbedBuilder().apply {
                            setColor(color)
                            addField(
                                "Countdown Room",
                                "参加希望プレイヤーは**[Countdown Room]**でBot自動カウントダウンボイスを待機してください！",
                                false
                            )
                            addField(
                                "Countdown Room",
                                "Players wishing to join should wait for Bot's automatic countdown voice at **[Countdown Room]**",
                                false
                            )
                        }.build()
                    ).queue()
                    sendMessage(
                        EmbedBuilder().apply {
                            setColor(color)
                            addField(
                                "ゲームコード共有の要請",
                                "全ての参加者はマッチング完了後、インゲーム画面左上のゲームコード後ろ3桁を入力。",
                                false
                            )
                            addField(
                                "Sharing Game Code",
                                "Once entering the match, please share the last 3 digits of the game code, which can be found on the top left corner of the screen.",
                                false
                            )
                            addField("3分後にチャットロックがかかります", "チャットロックがかかる前にゲームコードを共有してください！", false)
                        }.build()
                    ).queue()
                    bot.gameCodeMsgId = sendMessage(
                        EmbedBuilder().apply {
                            setColor(color)
                            setAuthor("現在の参加者 / Current Players")
                            clearFields()
                            val codes = LinkedHashSet<String>()
                            for (a in bot.codes) {
                                codes.add(a.rawCode)
                            }
                            for (c in codes) {
                                addField(
                                    "ID: " + c + " - " + bot.countByCode(c) + " players",
                                    bot.getMentionsByCode(c, guild),
                                    false
                                )
                            }
                        }.build()
                    ).complete().idLong
                }
                /* 321カウントダウンタスク起動 */
                val notifyTask = object : TimerTask() {
                    override fun run() {
                        bot.loadAndPlay(guild, countdownVc, "https://youtu.be/12T2yaxiolA")
                        /* 切断タスク起動 */
                        val disconnectTask = object : TimerTask() {
                            override fun run() {
                                guild.audioManager.closeAudioConnection()
                                /* チャットロックタスク起動 */
                                val chatlockTask = object : TimerTask() {
                                    override fun run() {
                                        gameCodeCh.apply {
                                            sendMessage(
                                                EmbedBuilder().apply {
                                                    setColor(color)
                                                    setAuthor("マッチが始まりました。Match has been started.")
                                                    setDescription("チャットがロックされました")
                                                    setFooter(
                                                        "Developed by TC#8210",
                                                        "https://pbs.twimg.com/profile_images/1114604548146905088/io6IeiGc_400x400.jpg"
                                                    )
                                                }.build()
                                            ).queue()
                                        }
                                        bot.codes.clear()
                                        bot.gameCodeMsgId = -1
                                        this.cancel()
                                        delete()
                                    }
                                }
                                timers[3].schedule(chatlockTask, 1000 * 180)
                                this.cancel()
                            }
                        }
                        timers[2].schedule(disconnectTask, 1000 * 10)
                        this.cancel()
                    }
                }
                timers[1].schedule(notifyTask, 1000 * 12)
                /* ホストタスク停止 */
                this.cancel()
            }
        }
        timers[0].schedule(task, sdf.parse(rawExpireDate))

        botControlCh.sendMessage(
            EmbedBuilder().apply
            {
                setColor(color)
                setAuthor(
                    "$title - Match Order",
                    null,
                    sender.avatarUrl
                )
                setDescription("マッチの予約が完了しました")
                addField("Order ID:", orderId.toString(), true)
                addField("開始日時 | Starting at:", rawExpireDate, false)
                addField("モード | Mode:", mode, false)
                addField("サーバー | Server:", server, false)
                setFooter(
                    "Ordered by $orderNickname",
                    sender.avatarUrl
                )
            }.build()
        ).queue()

        return this
    }

    fun cancel(botControlCh: TextChannel, sender: User, nickname: String) {
        if (activeCountdown) {
            botControlCh.sendMessage(EmbedBuilder().apply {
                setColor(color)
                setAuthor("Order ID: $orderId can't be canceled")
                setDescription(
                    "そのオーダーはカウントダウン中のためキャンセルをできません"
                )
            }.build()).queue()
        } else {
            for (timer in timers) {
                timer.cancel()
            }
            botControlCh.sendMessage(EmbedBuilder().apply {
                setColor(color)
                setAuthor(
                    "$title - Order Canceled",
                    null,
                    sender.avatarUrl
                )
                setDescription("オーダーをキャンセルしました")
                addField("Order ID:", orderId.toString(), true)
                addBlankField(true)
                addField("開始日時 | Starting at:", rawExpireDate, false)
                addField("モード | Mode:", mode, false)
                addField("サーバー | Server:", server, false)
                setFooter(
                    "Canceled by $nickname",
                    sender.avatarUrl
                )
            }.build()).queue()
        }
    }

    fun delete(){
        bot.orderInfoManager.removeOrderInfo(this)
    }
}
