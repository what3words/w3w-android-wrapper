package com.what3words.androidwrapper.common

interface Mapper<in F, out T> {
    fun mapFrom(from: F): T
}