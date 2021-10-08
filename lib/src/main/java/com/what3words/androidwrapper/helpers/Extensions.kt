package com.what3words.androidwrapper.helpers

import java.util.regex.Pattern

internal val split_regex = Regex("[.｡。･・︒។։။۔።।,-_/ ]+")

fun String.isPossible3wa(): Boolean {
    val regex =
        "^/*[^0-9`~!@#$%^&*()+\\-_=\\]\\[{\\}\\\\|'<,.>?/\";:£§º©®\\s]{1,}[.｡。･・︒។։။۔።।][^0-9`~!@#$%^&*()+\\-_=\\]\\[{\\}\\\\|'<,.>?/\";:£§º©®\\s]{1,}[.｡。･・︒។։။۔።।][^0-9`~!@#$%^&*()+\\-_=\\]\\[{\\}\\\\|'<,.>?/\";:£§º©®\\s]{1,}$"
    Pattern.compile(regex).also {
        return it.matcher(this).find()
    }
}

fun String.didYouMean3wa(): Boolean {
    val dymRegex = "^/*[^0-9`~!@#$%^&*()+\\-_=\\]\\[{\\}\\\\|'<,.>?/\";:£§º©®\\s]{1,}([.｡。･・︒។։။۔።।,-_/ ]+)[^0-9`~!@#$%^&*()+\\-_=\\]\\[{\\}\\\\|'<,.>?/\";:£§º©®\\s]{1,}([.｡。･・︒។։။۔።।,-_/ ]+)[^0-9`~!@#$%^&*()+\\-_=\\]\\[{\\}\\\\|'<,.>?/\";:£§º©®\\s]{1,}$"
    Pattern.compile(dymRegex).also {
        return it.matcher(this).find()
    }
}