//[lib](../../../index.md)/[com.what3words.androidwrapper.voice](../index.md)/[VoiceBuilderWithCoordinates](index.md)/[focus](focus.md)

# focus

[androidJvm]\
fun [focus](focus.md)(coordinates: Coordinates?): [VoiceBuilderWithCoordinates](index.md)

This is a location, specified as a latitude (often where the user making the query is). If specified, the results will be weighted to give preference to those near the focus. For convenience, longitude is allowed to wrap around the 180 line, so 361 is equivalent to 1.

#### Return

a [VoiceBuilderWithCoordinates](index.md) instance

## Parameters

androidJvm

| | |
|---|---|
| coordinates | the focus to use |
