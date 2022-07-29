package com.what3words.androidwrapper.helpers

import java.util.regex.Pattern

internal val splitRegex = Regex("[.｡。･・︒។։။۔።।,-_/ ]+")

@Deprecated("", ReplaceWith("com.what3words.javawrapper.What3WordsV3.isPossible3wa()"))
fun String.isPossible3wa(): Boolean {
    val regex =
        "^/*(?:(?:\\p{L}\\p{M}*)+[.｡。･・︒។։။۔።।](?:\\p{L}\\p{M}*)+[.｡。･・︒។։။۔።।](?:\\p{L}\\p{M}*)+|(?:\\p{L}\\p{M}*)+([\u0020\u00A0](?:\\p{L}\\p{M}*)+){1,3}[.｡。･・︒។։။۔።।](?:\\p{L}\\p{M}*)+([\u0020\u00A0](?:\\p{L}\\p{M}*)+){1,3}[.｡。･・︒។։။۔።।](?:\\p{L}\\p{M}*)+([\u0020\u00A0](?:\\p{L}\\p{M}*)+){1,3})$"
    Pattern.compile(regex).also {
        return it.matcher(this).find()
    }
}

@Deprecated("", ReplaceWith("com.what3words.javawrapper.What3WordsV3.didYouMean3wa()"))
fun String.didYouMean3wa(): Boolean {
    val dymRegex =
        "^/*(?:\\p{L}\\p{M}*){1,}[.｡。･・︒។։။۔።। ,\\\\^_/+'&\\:;|　-]{1,2}(?:\\p{L}\\p{M}*){1,}[.｡。･・︒។։။۔።। ,\\\\^_/+'&\\:;|　-]{1,2}(?:\\p{L}\\p{M}*){1,}$"
    Pattern.compile(dymRegex).also {
        return it.matcher(this).find()
    }
}

@Deprecated("",  ReplaceWith("com.what3words.javawrapper.What3WordsV3.searchPossible3wa()"))
fun String.searchPossible3wa(): List<String> {
    val searchRegex =
        "(?:\\p{L}\\p{M}*){1,}[.｡。･・︒។։။۔።।](?:\\p{L}\\p{M}*){1,}[.｡。･・︒។։။۔።।](?:\\p{L}\\p{M}*){1,}"
    Regex(searchRegex).also {
        return it.findAll(this).map { it.value }.toList()
    }
}

