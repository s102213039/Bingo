package com.yanli.bingo

data class Member(
    val uid: String,
    val nickname: String?,
    val displayName: String,
    val avatarId: Int) {
    constructor() : this("", null, "", 0)
}