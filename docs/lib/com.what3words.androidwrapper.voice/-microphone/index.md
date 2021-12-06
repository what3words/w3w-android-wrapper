//[lib](../../../index.md)/[com.what3words.androidwrapper.voice](../index.md)/[Microphone](index.md)

# Microphone

[androidJvm]\
class [Microphone](index.md)

## Constructors

| | |
|---|---|
| [Microphone](-microphone.md) | [androidJvm]<br>fun [Microphone](-microphone.md)() |
| [Microphone](-microphone.md) | [androidJvm]<br>fun [Microphone](-microphone.md)(recordingRate: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), encoding: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), channel: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), audioSource: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) |

## Types

| Name | Summary |
|---|---|
| [Companion](-companion/index.md) | [androidJvm]<br>object [Companion](-companion/index.md) |

## Functions

| Name | Summary |
|---|---|
| [onError](on-error.md) | [androidJvm]<br>fun [onError](on-error.md)(callback: [Consumer](https://developer.android.com/reference/kotlin/androidx/core/util/Consumer.html)<[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)>): [Microphone](index.md)<br>[onError](on-error.md) callback will be called if there's some issue starting the microphone, i. |
| [onListening](on-listening.md) | [androidJvm]<br>fun [onListening](on-listening.md)(callback: [Consumer](https://developer.android.com/reference/kotlin/androidx/core/util/Consumer.html)<[Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)?>): [Microphone](index.md)<br>[onListening](on-listening.md) callback will return the volume of the microphone while recording from 0.0-1.0, i.e: 0.5, 50% (0.0 min, 1. |

## Properties

| Name | Summary |
|---|---|
| [isListening](is-listening.md) | [androidJvm]<br>var [isListening](is-listening.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false |
