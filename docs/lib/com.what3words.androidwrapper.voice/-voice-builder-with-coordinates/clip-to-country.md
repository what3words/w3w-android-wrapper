//[lib](../../../index.md)/[com.what3words.androidwrapper.voice](../index.md)/[VoiceBuilderWithCoordinates](index.md)/[clipToCountry](clip-to-country.md)

# clipToCountry

[androidJvm]\
fun [clipToCountry](clip-to-country.md)(countryCodes: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)<[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)>): [VoiceBuilderWithCoordinates](index.md)

Restricts autosuggest to only return results inside the countries specified by comma-separated list of uppercase ISO 3166-1 alpha-2 country codes (for example, to restrict to Belgium and the UK, use [clipToCountry](clip-to-country.md) ("GB", "BE"). [clipToCountry](clip-to-country.md) will also accept lowercase country codes. Entries must be two a-z letters. WARNING: If the two-letter code does not correspond to a country, there is no error: API simply returns no results.

#### Return

a [VoiceBuilderWithCoordinates](index.md) instance

## Parameters

androidJvm

| | |
|---|---|
| countryCodes | countries to clip results too |
