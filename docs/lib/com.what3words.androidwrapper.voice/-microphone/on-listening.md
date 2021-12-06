//[lib](../../../index.md)/[com.what3words.androidwrapper.voice](../index.md)/[Microphone](index.md)/[onListening](on-listening.md)

# onListening

[androidJvm]\
fun [onListening](on-listening.md)(callback: [Consumer](https://developer.android.com/reference/kotlin/androidx/core/util/Consumer.html)<[Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)?>): [Microphone](index.md)

[onListening](on-listening.md) callback will return the volume of the microphone while recording from 0.0-1.0, i.e: 0.5, 50% (0.0 min, 1.0 max volume)

#### Return

a [Microphone](index.md) instance

## Parameters

androidJvm

| | |
|---|---|
| callback | with a float 0.0-1.0 with the microphone volume, useful for animations, etc. |
