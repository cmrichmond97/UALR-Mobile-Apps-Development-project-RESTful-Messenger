package edu.ualr.cmrichmond.restfulmessenger

data class UserResponse(
    val key: String? = null,
    val username: Array<String>? = null,
    val password1: Array<String>? = null,
    val password: Array<String>? = null,
    val non_field_errors: Array<String>? = null
)

data class ChannelInfo(
        val pk: Int? = null,
        val name: String? = null

)

data class User(
        val pk: String? = null,
        val username: String? = null
)

data class MessageInfo(
        val channel: String? = null,
        val message: String? = null,
        val timestamp: String? = null,
        val user: User? = null
)

data class ChatResponse(
        val detail: String? = null
)