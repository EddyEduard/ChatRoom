package com.personal.chatroommobile.data

sealed class Result<out T : Any> {

    data class Success<out T : Any>(val data: T) : Result<T>()
    data class Warning<out T : Any>(val warning: String) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Warning<*> -> "Warning[warning=$warning]"
            is Error -> "Error[exception=$exception]"
        }
    }
}