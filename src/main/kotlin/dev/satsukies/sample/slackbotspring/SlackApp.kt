package dev.satsukies.sample.slackbotspring

import com.slack.api.app_backend.slash_commands.response.SlashCommandResponse
import com.slack.api.bolt.App
import com.slack.api.bolt.AppConfig
import com.slack.api.bolt.response.ResponseTypes
import com.slack.api.methods.MethodsClient
import com.slack.api.model.event.AppMentionEvent
import com.slack.api.model.event.MessageEvent
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.regex.Pattern

@Configuration
class SlackApp(
  @Value("\${app.slack.bot-token}") val token: String,
  @Value("\${app.slack.signing-secret}") val secret: String
) {

  @Bean
  fun loadAppConfig(): AppConfig {
    return AppConfig.builder()
      .singleTeamBotToken(token)
      .signingSecret(secret)
      .build()
  }

  @Bean
  fun initSlackApp(config: AppConfig): App {

    val app = App(config)

    /**
     * スラッシュコマンドのハンドリング実装
     *
     * Slackの管理画面で、定義したコマンドを登録しないとハンドルできないので注意。
     * cf. https://slack.dev/java-slack-sdk/guides/ja/getting-started-with-bolt `/hello コマンドの有効化`セクション
     */
    app.command("/hello") { _, context ->
      val response = SlashCommandResponse.builder()
        .responseType(ResponseTypes.inChannel)
        .text("呼ばれたのでチャンネルの参加者全員に見えるメッセージを送っています")
        .build()

      // 非同期でメッセージを送信
      context.respond(response)

      // メッセージを処理したことを返す。
      // Bolt アプリは Slack API サーバーからのリクエストに対して
      // 3 秒以内に ack() メソッドで応答する必要があります。3 秒以内に応答しなかった場合、
      // Slack API は一定時間経過後にリトライします。
      // cf. https://slack.dev/java-slack-sdk/guides/ja/events-api
      context.ack()
    }

    /**
     * botへのメンションのハンドリング実装
     *
     * メッセージ内容に応じて処理を変えるため、payloadからメッセージを取り出して必要箇所だけsplitして処理しています。
     */
    app.event(AppMentionEvent::class.java) { payload, context ->
      val event: AppMentionEvent = payload.event
      val client = context.client()
      val channelId = context.channelId

      val responseMsg: String = when (event.text.split(" ")[1]) {
        "ping" -> "pong"
        "なるほど" -> "気づいたんや…"
        else -> "ちょっと何言ってるかわからないですね（CV.サンド富澤）"
      }

      postChatInChannel(client, channelId, responseMsg)

      context.ack()
    }

    /**
     * メッセージがpostされた時のハンドリング実装
     *
     * 特定のメッセージに反応させるために、正規表現を使っています。
     * cf. [REGEX_PATTERN_JAVA], [javaPattern]
     */
    app.message(javaPattern) { payload, context ->
      val msgEvent: MessageEvent = payload.event
      val client = context.client()

      val channelId = msgEvent.channel
      val ts = msgEvent.ts

      addReaction(client, channelId, ts, "yoshi")
      postChatInChannel(client, channelId, "いますぐダウンロー\nド")

      context.ack()
    }

    return app
  }

  /**
   * 任意のchannelにメッセージを投稿する
   *
   * @param client 処理実行に必要なクライアント
   * @param channelId 投稿先のSlackチャンネルID
   * @param message 投稿するメッセージ本文
   */
  private fun postChatInChannel(
    client: MethodsClient,
    channelId: String,
    message: String
  ) {
    val chatPostMessage = client.chatPostMessage { builder ->
      builder.channel(channelId).text(message)
    }

    if (chatPostMessage.isOk) {
      println("Post message success.")
    } else {
      System.err.println(
        "Post message failed." +
          "\n\tchannelId = $channelId" +
          "\n\tmessage = $message" +
          "\n\treason = ${chatPostMessage.error}"
      )
    }
  }

  /**
   * 任意のメッセージにリアクションを付与する
   *
   * @param client 処理実行に必要なクライアント
   * @param channelId リアクション付与するメッセージの存在するSlackチャンネルID
   * @param timestamp リアクション付与するメッセージのタイムスタンプ
   * @param reaction 付与するリアクション。前後のコロンは不要。
   */
  private fun addReaction(
    client: MethodsClient,
    channelId: String,
    timestamp: String,
    reaction: String
  ) {
    val addReaction = client.reactionsAdd { builder ->
      builder.channel(channelId)
        .timestamp(timestamp)
        .name(reaction)
    }

    if (addReaction.isOk) {
      println("Add reaction success.")
    } else {
      System.err.println(
        "Add reaction failed." +
          "\n\tchannelId = $channelId" +
          "\n\ttimestamp = $timestamp" +
          "\n\treaction = $reaction" +
          "\n\treason = ${addReaction.error}"
      )
    }
  }

  companion object {
    private const val REGEX_PATTERN_JAVA: String = "あなたとJava"

    private val javaPattern: Pattern = Pattern.compile(REGEX_PATTERN_JAVA)
  }
}