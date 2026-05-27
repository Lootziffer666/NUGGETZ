package com.example.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatRequest(
    val model: String = "local-model",
    val messages: List<ChatMessage>,
    val temperature: Double = 0.3
)

@JsonClass(generateAdapter = true)
data class ChatMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class ChatResponse(
    val choices: List<ChatChoice>? = null
)

@JsonClass(generateAdapter = true)
data class ChatChoice(
    val message: ChatMessage? = null
)
