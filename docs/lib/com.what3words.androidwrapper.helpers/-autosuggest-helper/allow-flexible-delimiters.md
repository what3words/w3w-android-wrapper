//[lib](../../../index.md)/[com.what3words.androidwrapper.helpers](../index.md)/[AutosuggestHelper](index.md)/[allowFlexibleDelimiters](allow-flexible-delimiters.md)

# allowFlexibleDelimiters

[androidJvm]\
fun [allowFlexibleDelimiters](allow-flexible-delimiters.md)(boolean: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)): [AutosuggestHelper](index.md)

Flexible delimiters feature allows our regex to be less precise on delimiters, this means that "filled count soa" or "filled,count,soa" will be parsed to "filled.count.soa" and send to our autosuggest API.

#### Return

a [AutosuggestHelper](index.md) instance

## Parameters

androidJvm

| | |
|---|---|
| boolean | enables flexible delimiters feature enabled (false by default) |
