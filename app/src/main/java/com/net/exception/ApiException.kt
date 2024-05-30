package com.net.exception
class ApiException(val code: Int, override val message: String?) : RuntimeException(message)