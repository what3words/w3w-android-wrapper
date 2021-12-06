//[lib](../../../index.md)/[com.what3words.androidwrapper](../index.md)/[What3WordsV3](index.md)/[autosuggestWithCoordinates](autosuggest-with-coordinates.md)

# autosuggestWithCoordinates

[androidJvm]\
fun [autosuggestWithCoordinates](autosuggest-with-coordinates.md)(microphone: [Microphone](../../com.what3words.androidwrapper.voice/-microphone/index.md), voiceLanguage: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): [VoiceBuilderWithCoordinates](../../com.what3words.androidwrapper.voice/-voice-builder-with-coordinates/index.md)

The what3words Voice API allows a user to say three words into any application or service, with it returning a list of suggested what3words addresses with coordinates, through a single API call. Utilising WebSockets for realtime audio steaming, and powered by the Speechmatics WebSocket Speech API, the fast and simple interface provides a powerful AutoSuggest function, which can validate and autocorrect user input and limit it to certain geographic areas.

#### Return

a [VoiceBuilder](../../com.what3words.androidwrapper.voice/-voice-builder/index.md) instance, use [VoiceBuilder.startListening](../../com.what3words.androidwrapper.voice/-voice-builder/start-listening.md) to start recording and sending voice data to our API.

## Parameters

androidJvm

| | |
|---|---|
| microphone | with a [Microphone](../../com.what3words.androidwrapper.voice/-microphone/index.md) where developer can subscribe to [Microphone.onListening](../../com.what3words.androidwrapper.voice/-microphone/on-listening.md) and get microphone volume while recording, allowing custom inputs too as recording rates and encodings. |
| voiceLanguage | request parameter is mandatory, and must always be specified. The language code provided is used to configure both the Speechmatics ASR, and the what3words AutoSuggest algorithm. Please provide one of the following voice-language codes: ar, cmn, de, en, es, hi, ja, ko. |
