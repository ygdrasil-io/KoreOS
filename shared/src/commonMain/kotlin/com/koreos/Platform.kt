package com.koreos

expect fun platformName(): String

fun hello(): String = "Hello from ${platformName()}"