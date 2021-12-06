//[lib](../../../index.md)/[com.what3words.androidwrapper.voice](../index.md)/[VoiceBuilderWithCoordinates](index.md)/[clipToCircle](clip-to-circle.md)

# clipToCircle

[androidJvm]\
fun [clipToCircle](clip-to-circle.md)(centre: Coordinates?, radius: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)? = 1.0): [VoiceBuilderWithCoordinates](index.md)

Restrict autosuggest results to a circle, specified by Coordinates representing the centre of the circle, plus the [radius](clip-to-circle.md) in kilometres. For convenience, longitude is allowed to wrap around 180 degrees. For example 181 is equivalent to -179.

#### Return

a [VoiceBuilderWithCoordinates](index.md) instance

## Parameters

androidJvm

| | |
|---|---|
| centre | the centre of the circle |
| radius | the radius of the circle in kilometres |
