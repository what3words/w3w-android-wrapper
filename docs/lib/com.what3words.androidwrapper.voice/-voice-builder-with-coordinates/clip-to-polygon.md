//[lib](../../../index.md)/[com.what3words.androidwrapper.voice](../index.md)/[VoiceBuilderWithCoordinates](index.md)/[clipToPolygon](clip-to-polygon.md)

# clipToPolygon

[androidJvm]\
fun [clipToPolygon](clip-to-polygon.md)(polygon: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)<Coordinates>): [VoiceBuilderWithCoordinates](index.md)

Restrict autosuggest results to a polygon, specified by a collection of Coordinates. The polygon should be closed, i.e. the first element should be repeated as the last element; also the list should contain at least 4 entries. The API is currently limited to accepting up to 25 pairs.

#### Return

a [VoiceBuilderWithCoordinates](index.md) instance

## Parameters

androidJvm

| | |
|---|---|
| polygon | the list of Coordinates that form the polygon to clip results too |
