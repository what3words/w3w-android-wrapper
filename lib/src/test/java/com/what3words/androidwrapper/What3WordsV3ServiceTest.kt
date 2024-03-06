package com.what3words.androidwrapper

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.what3words.androidwrapper.datasource.text.api.What3WordsV3Service
import com.what3words.androidwrapper.datasource.text.api.di.MappersFactory
import com.what3words.androidwrapper.datasource.text.api.error.BadBoundingBoxError
import com.what3words.androidwrapper.datasource.text.api.error.BadBoundingBoxTooBigError
import com.what3words.androidwrapper.datasource.text.api.error.BadClipToCircleError
import com.what3words.androidwrapper.datasource.text.api.error.BadCoordinatesError
import com.what3words.androidwrapper.datasource.text.api.error.BadFocusError
import com.what3words.androidwrapper.datasource.text.api.error.NetworkError
import com.what3words.androidwrapper.datasource.text.api.error.UnknownError
import com.what3words.androidwrapper.datasource.text.api.retrofit.W3WV3RetrofitApiClient.executeApiRequestAndHandleResponse
import com.what3words.core.types.common.W3WResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@OptIn(ExperimentalCoroutinesApi::class)
class What3WordsV3ServiceTest {

    private var mockWebServer = MockWebServer()
    private lateinit var what3WordsV3Service: What3WordsV3Service

    @Before
    fun setup() {
        mockWebServer.start()
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        what3WordsV3Service = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(What3WordsV3Service::class.java)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `convert valid coordinates to 3wa should return 3wa`() = runTest {
        // Arrange
        val coordinates = "51.521251, 0.203586"
        val expectedResponseData = """
            {
                "country": "GB",
                "square": {
                    "southwest": {
                        "lng": -0.203607,
                        "lat": 51.521238
                    },
                    "northeast": {
                        "lng": -0.203564,
                        "lat": 51.521265
                    }
                },
                "nearestPlace": "Bayswater, London",
                "coordinates": {
                    "lng": -0.203586,
                    "lat": 51.521251
                },
                "words": "index.home.raft",
                "language": "en",
                "map": "https://w3w.co/index.home.raft"
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(expectedResponseData)
        )

        // Act
        val response = what3WordsV3Service.convertTo3wa(coordinates, null, null)

        // Assert
        assert(response.isSuccessful)
        assert(response.body()?.words == "index.home.raft")
    }

    @Test
    fun `convert invalid coordinates should return error`() = runTest {
        // Arrange
        val invalidCoordinates = "191.521251, 0.203586"
        val expectedResponseData = """
            {
                "error": {
                    "code": "BadCoordinates",
                    "message": "latitude must be >=-90 and <= 90"
                }
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody(expectedResponseData)
        )

        // Act
        val response =
            executeApiRequestAndHandleResponse(resultMapper = MappersFactory.providesConvertTo3waDtoToDomainMapper()) {
                what3WordsV3Service.convertTo3wa(invalidCoordinates, null, null)
            }

        // Assert
        assert(response is W3WResult.Failure)
        response as W3WResult.Failure
        assert(response.error is BadCoordinatesError)
    }

    @Test
    fun `convert valid coordinates with Vietnamese language should return Vietnamese 3wa`() =
        runTest {
            // Arrange
            val coordinates = "51.521251, 0.203586"
            val language = "vi"
            val expectedResponseData = """
        {
            "country": "GB",
            "square": {
                "southwest": {
                    "lng": -0.203607,
                    "lat": 51.521238
                },
                "northeast": {
                    "lng": -0.203564,
                    "lat": 51.521265
                }
            },
            "nearestPlace": "Luân Đôn",
            "coordinates": {
                "lng": -0.203586,
                "lat": 51.521251
            },
            "words": "nước yến.nét vẽ.đồ gốm",
            "language": "vi",
            "map": "https://w3w.co/n%C6%B0%E1%BB%9Bc+y%E1%BA%BFn.n%C3%A9t+v%E1%BA%BD.%C4%91%E1%BB%93+g%E1%BB%91m"
        }
        """.trimIndent()

            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(expectedResponseData)
            )

            // Act
            val response = what3WordsV3Service.convertTo3wa(coordinates, language, null)

            // Assert
            assert(response.isSuccessful)
            assert(response.body()?.words == "nước yến.nét vẽ.đồ gốm")
            assert(response.body()?.locale == null)
        }

    @Test
    fun `convert valid coordinates with Kazakh Latin locale should return kk_la 3wa`() = runTest {
        // Arrange
        val coordinates = "51.521251, 0.203586"
        val locale = "kk_la"
        val expectedResponseData = """
        {
            "country": "GB",
            "square": {
                "southwest": {
                    "lng": -0.203607,
                    "lat": 51.521238
                },
                "northeast": {
                    "lng": -0.203564,
                    "lat": 51.521265
                }
            },
            "nearestPlace": "Bayswater, London",
            "coordinates": {
                "lng": -0.203586,
                "lat": 51.521251
            },
            "words": "otyr.tıredık.säulettı",
            "language": "kk",
            "locale": "kk_la",
            "map": "https://w3w.co/otyr.t%C4%B1red%C4%B1k.s%C3%A4ulett%C4%B1"
        }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(expectedResponseData)
        )

        // Act
        val response = what3WordsV3Service.convertTo3wa(coordinates, null, locale)

        // Assert
        assert(response.isSuccessful)
        assert(response.body()?.words == "otyr.tıredık.säulettı")
        assert(response.body()?.locale == "kk_la")
    }

    @Test
    fun `convert valid w3a to coordinates should return coordinates`() = runTest {
        // Arrange
        val w3a = "index.home.raft"
        val expectedResponseData = """
            {
                "country": "GB",
                "square": {
                    "southwest": {
                        "lng": -0.203607,
                        "lat": 51.521238
                    },
                    "northeast": {
                        "lng": -0.203564,
                        "lat": 51.521265
                    }
                },
                "nearestPlace": "Bayswater, London",
                "coordinates": {
                    "lng": -0.203586,
                    "lat": 51.521251
                },
                "words": "index.home.raft",
                "language": "en",
                "map": "https://w3w.co/index.home.raft"
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(expectedResponseData)
        )

        // Act
        val response = what3WordsV3Service.convertToCoordinates(w3a)

        // Assert
        assert(response.isSuccessful)
        assert(response.body()?.coordinates?.lat == 51.521251)
        assert(response.body()?.coordinates?.lng == -0.203586)
    }

    @Test
    fun `convert null to coordinates should return error`() = runBlocking {
        // Arrange
        val w3a = null
        val expectedResponseData = """
            {
                "error": {
                    "code": "MissingWords",
                    "message": "words must be specified"
                }
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody(expectedResponseData)
        )

        // Act
        val response =
            executeApiRequestAndHandleResponse(resultMapper = MappersFactory.providesConvertToCoordinatesResponseMapper()) {
                what3WordsV3Service.convertToCoordinates(w3a)
            }

        // Assert
        assert(response is W3WResult.Failure)
        response as W3WResult.Failure
        assert(response.error is UnknownError)
    }

    @Test
    fun `available language should return list of supported language`() = runTest {
        // Arrange
        val expectedResponseData = """
            {
                "languages": [
                    {
                        "nativeName": "Deutsch",
                        "code": "de",
                        "name": "German"
                    },
                    {
                        "nativeName": "हिन्दी",
                        "code": "hi",
                        "name": "Hindi"
                    },
                    {
                        "nativeName": "ລາວ",
                        "code": "lo",
                        "name": "Lao"
                    },
                    {
                        "nativeName": "Português",
                        "code": "pt",
                        "name": "Portuguese"
                    },
                    {
                        "nativeName": "Magyar",
                        "code": "hu",
                        "name": "Hungarian"
                    },
                    {
                        "nativeName": "Українська",
                        "code": "uk",
                        "name": "Ukrainian"
                    },
                    {
                        "nativeName": "Bahasa Indonesia",
                        "code": "id",
                        "name": "Bahasa Indonesia"
                    },
                    {
                        "nativeName": "اردو",
                        "code": "ur",
                        "name": "Urdu"
                    },
                    {
                        "nativeName": "മലയാളം",
                        "code": "ml",
                        "name": "Malayalam"
                    },
                    {
                        "nativeName": "Монгол хэл",
                        "code": "mn",
                        "name": "Mongolian",
                        "locales": [
                            {
                                "nativeName": "Монгол хэл (Латинаар)",
                                "name": "Mongolian (Latin)",
                                "code": "mn_la"
                            },
                            {
                                "nativeName": "Монгол хэл (Криллээр)",
                                "name": "Mongolian (Cyrillic)",
                                "code": "mn_cy"
                            }
                        ]
                    },
                    {
                        "nativeName": "Afrikaans",
                        "code": "af",
                        "name": "Afrikaans"
                    },
                    {
                        "nativeName": "मराठी",
                        "code": "mr",
                        "name": "Marathi"
                    },
                    {
                        "nativeName": "Bahasa Malaysia",
                        "code": "ms",
                        "name": "Bahasa Malaysia"
                    },
                    {
                        "nativeName": "Ελληνικά",
                        "code": "el",
                        "name": "Greek"
                    },
                    {
                        "nativeName": "English",
                        "code": "en",
                        "name": "English"
                    },
                    {
                        "nativeName": "Italiano",
                        "code": "it",
                        "name": "Italian"
                    },
                    {
                        "nativeName": "አማርኛ",
                        "code": "am",
                        "name": "Amharic"
                    },
                    {
                        "nativeName": "Español",
                        "code": "es",
                        "name": "Spanish"
                    },
                    {
                        "nativeName": "中文",
                        "code": "zh",
                        "name": "Chinese",
                        "locales": [
                            {
                                "nativeName": "中文（繁體）",
                                "name": "Chinese (Traditional)",
                                "code": "zh_tr"
                            },
                            {
                                "nativeName": "中文（简体）",
                                "name": "Chinese (Simplified)",
                                "code": "zh_si"
                            }
                        ]
                    },
                    {
                        "nativeName": "Eesti",
                        "code": "et",
                        "name": "Estonian"
                    },
                    {
                        "nativeName": "العربية",
                        "code": "ar",
                        "name": "Arabic"
                    },
                    {
                        "nativeName": "Tiếng Việt",
                        "code": "vi",
                        "name": "Vietnamese"
                    },
                    {
                        "nativeName": "日本語",
                        "code": "ja",
                        "name": "Japanese"
                    },
                    {
                        "nativeName": "नेपाली",
                        "code": "ne",
                        "name": "Nepali"
                    },
                    {
                        "nativeName": "فارسی",
                        "code": "fa",
                        "name": "Persian"
                    },
                    {
                        "nativeName": "isiZulu",
                        "code": "zu",
                        "name": "isiZulu"
                    },
                    {
                        "nativeName": "Română",
                        "code": "ro",
                        "name": "Romanian"
                    },
                    {
                        "nativeName": "Nederlands",
                        "code": "nl",
                        "name": "Dutch"
                    },
                    {
                        "nativeName": "Norsk",
                        "code": "no",
                        "name": "Norwegian"
                    },
                    {
                        "nativeName": "Suomi",
                        "code": "fi",
                        "name": "Finnish"
                    },
                    {
                        "nativeName": "Русский",
                        "code": "ru",
                        "name": "Russian"
                    },
                    {
                        "nativeName": "български",
                        "code": "bg",
                        "name": "Bulgarian"
                    },
                    {
                        "nativeName": "বাংলা",
                        "code": "bn",
                        "name": "Bengali"
                    },
                    {
                        "nativeName": "Français",
                        "code": "fr",
                        "name": "French"
                    },
                    {
                        "nativeName": "සිංහල",
                        "code": "si",
                        "name": "Sinhala"
                    },
                    {
                        "nativeName": "Slovenčina",
                        "code": "sk",
                        "name": "Slovak"
                    },
                    {
                        "nativeName": "Català",
                        "code": "ca",
                        "name": "Catalan"
                    },
                    {
                        "nativeName": "Қазақ тілі",
                        "code": "kk",
                        "name": "Kazakh",
                        "locales": [
                            {
                                "nativeName": "Қазақ тілі (кирилл)",
                                "name": "Kazakh (Cyrillic)",
                                "code": "kk_cy"
                            },
                            {
                                "nativeName": "Qazaq tılı (Latyn)",
                                "name": "Kazakh (Latin)",
                                "code": "kk_la"
                            }
                        ]
                    },
                    {
                        "nativeName": "Bosanski-Crnogorski-Hrvatski-Srpski",
                        "code": "oo",
                        "name": "Bosnian-Croatian-Montenegrin-Serbian",
                        "locales": [
                            {
                                "nativeName": "Bosanski-Crnogorski-Hrvatski-Srpski (latinica)",
                                "name": "Bosnian-Croatian-Montenegrin-Serbian (Latin)",
                                "code": "oo_la"
                            },
                            {
                                "nativeName": "Босански-Српски-Хрватски-Црногорски (ћирилица)",
                                "name": "Bosnian-Croatian-Montenegrin-Serbian (Cyrillic)",
                                "code": "oo_cy"
                            }
                        ]
                    },
                    {
                        "nativeName": "ភាសាខ្មែរ",
                        "code": "km",
                        "name": "Khmer"
                    },
                    {
                        "nativeName": "ಕನ್ನಡ",
                        "code": "kn",
                        "name": "Kannada"
                    },
                    {
                        "nativeName": "ଓଡ଼ିଆ",
                        "code": "or",
                        "name": "Odia"
                    },
                    {
                        "nativeName": "Svenska",
                        "code": "sv",
                        "name": "Swedish"
                    },
                    {
                        "nativeName": "한국어",
                        "code": "ko",
                        "name": "Korean"
                    },
                    {
                        "nativeName": "Kiswahili",
                        "code": "sw",
                        "name": "Swahili"
                    },
                    {
                        "nativeName": "தமிழ்",
                        "code": "ta",
                        "name": "Tamil"
                    },
                    {
                        "nativeName": "ગુજરાતી",
                        "code": "gu",
                        "name": "Gujarati"
                    },
                    {
                        "nativeName": "Čeština",
                        "code": "cs",
                        "name": "Czech"
                    },
                    {
                        "nativeName": "isiXhosa",
                        "code": "xh",
                        "name": "isiXhosa"
                    },
                    {
                        "nativeName": "ਪੰਜਾਬੀ",
                        "code": "pa",
                        "name": "Punjabi"
                    },
                    {
                        "nativeName": "తెలుగు",
                        "code": "te",
                        "name": "Telugu"
                    },
                    {
                        "nativeName": "ไทย",
                        "code": "th",
                        "name": "Thai"
                    },
                    {
                        "nativeName": "Cymraeg",
                        "code": "cy",
                        "name": "Welsh"
                    },
                    {
                        "nativeName": "Polski",
                        "code": "pl",
                        "name": "Polish"
                    },
                    {
                        "nativeName": "Dansk",
                        "code": "da",
                        "name": "Danish"
                    },
                    {
                        "nativeName": "עברית",
                        "code": "he",
                        "name": "Hebrew"
                    },
                    {
                        "nativeName": "Türkçe",
                        "code": "tr",
                        "name": "Turkish"
                    }
                ]
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(expectedResponseData)
        )

        // Act
        val response = what3WordsV3Service.availableLanguages()

        // Assert
        assert(response.isSuccessful)
        assert(response.body()?.languages?.size == 57)
    }

    @Test
    fun `Grid section with valid bounding box and format JSON should return what3word grid in JSON format`() =
        runTest {
            // Arrange
            val boundingBox = "52.207988,0.116126,52.208867,0.117540"
            val format = "json"
            val expectedResponseData = """
            {
              "lines": [
                  {
                      "start": {
                          "lng": 0.116126,
                          "lat": 52.208009918068136
                      },
                      "end": {
                          "lng": 0.11754,
                          "lat": 52.208009918068136
                      }
                  },
                  {
                      "start": {
                          "lng": 0.116126,
                          "lat": 52.20803686934023
                      },
                      "end": {
                          "lng": 0.11754,
                          "lat": 52.20803686934023
                      }
                  }
              ]
            }
        """.trimIndent()

            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(expectedResponseData)
            )

            // Act
            val response = what3WordsV3Service.gridSection(boundingBox)

            // Assert
            assert(response.isSuccessful)
            assert(response.body()?.lines?.size == 2)
            assert(response.body()?.lines?.get(0)?.start?.lat == 52.208009918068136)
            assert(response.body()?.lines?.get(0)?.start?.lng == 0.116126)
        }

    @Test
    fun `Grid section with invalid bounding box should return BadBoundingBoxError`() = runTest {
        val boundingBox = "172.207988,0.116126,52.208867,0.117540"
        val expectedResponseData = """
            {
                "error": {
                    "code": "BadBoundingBox",
                    "message": "Invalid bounding-box. Must be lat,lng,lat,lng with -90 <= lat <= 90."
                }
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody(expectedResponseData)
        )

        // Act
        val response =
            executeApiRequestAndHandleResponse(resultMapper = MappersFactory.providesGridSectionResponseMapper()) {
                what3WordsV3Service.gridSection(boundingBox)
            }

        // Assert
        assert(response is W3WResult.Failure)
        response as W3WResult.Failure
        assert(response.error is BadBoundingBoxError)
    }

    @Test
    fun `Grid section with too big bounding box should return BadBoundingBoxTooBigError`() {
        val boundingBox = "0.207988,0.116126,52.208867,0.117540"
        val expectedResponseData = """
            {
                "error": {
                    "code": "BadBoundingBoxTooBig",
                    "message": "The diagonal of bounding-box may not be > 4km"
                }
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody(expectedResponseData)
        )

        // Act
        val response =
            executeApiRequestAndHandleResponse(resultMapper = MappersFactory.providesGridSectionResponseMapper()) {
                what3WordsV3Service.gridSection(boundingBox)
            }

        // Assert
        assert(response is W3WResult.Failure)
        response as W3WResult.Failure
        assert(response.error is BadBoundingBoxTooBigError)
    }

    @Test
    fun `autosuggest with valid input should return list of suggestions`() = runTest {
        // Arrange
        val input = "index.home.raf"
        val expectedResponseData = """
            {
                "suggestions": [
                    {
                        "country": "GB",
                        "nearestPlace": "Bayswater, London",
                        "words": "index.home.raft",
                        "rank": 1,
                        "language": "en"
                    },
                    {
                        "country": "US",
                        "nearestPlace": "Prosperity, West Virginia",
                        "words": "indexes.home.raft",
                        "rank": 2,
                        "language": "en"
                    },
                    {
                        "country": "US",
                        "nearestPlace": "Greensboro, North Carolina",
                        "words": "index.homes.raft",
                        "rank": 3,
                        "language": "en"
                    }
                ]
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(expectedResponseData)
        )

        // Act
        val response = what3WordsV3Service.autosuggest(input, null, null, null, null, null, null, null, null, null, null, null)

        // Assert
        assert(response.isSuccessful)
        assert(response.body()?.suggestions?.size == 3)
        assert(response.body()?.suggestions?.get(0)?.words  == "index.home.raft")
    }

    @Test
    fun `autosuggest with invalid input should return empty suggestions`() = runTest {
        // Arrange
        val input = "index.hom"
        val expectedResponseData = """
            {
                "suggestions": []
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(expectedResponseData)
        )

        // Act
        val response = what3WordsV3Service.autosuggest(input, null, null, null, null, null, null, null, null, null, null, null)

        // Assert
        assert(response.isSuccessful)
        assert(response.body()?.suggestions?.size == 0)
    }

    @Test
    fun `autosuggest with option of 4 n-results should return 4 suggestions`() = runTest {
        // Arrange
        val input = "index.home.raf"
        val nResults = "4"
        val expectedResponseData = """
            {
                "suggestions": [
                    {
                        "country": "GB",
                        "nearestPlace": "Bayswater, London",
                        "words": "index.home.raft",
                        "rank": 1,
                        "language": "en"
                    },
                    {
                        "country": "US",
                        "nearestPlace": "Prosperity, West Virginia",
                        "words": "indexes.home.raft",
                        "rank": 2,
                        "language": "en"
                    },
                    {
                        "country": "US",
                        "nearestPlace": "Greensboro, North Carolina",
                        "words": "index.homes.raft",
                        "rank": 3,
                        "language": "en"
                    },
                    {
                        "country": "AU",
                        "nearestPlace": "Melville, Western Australia",
                        "words": "index.home.rafts",
                        "rank": 4,
                        "language": "en"
                    }
                ]
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(expectedResponseData)
        )

        // Act
        val response = what3WordsV3Service.autosuggest(input, nResults, null, null, null, null, null, null, null, null, null, null)

        // Assert
        assert(response.isSuccessful)
        assert(response.body()?.suggestions?.size == 4)
    }

    @Test
    fun `autosuggest with invalid focus option should return BadFocusError`() = runTest {
        // Arrange
        val input = "index.home.raf"
        val focus = "250.842404,4.361177"

        val expectedResponseData = """
            {
                "error": {
                    "code": "BadFocus",
                    "message": "lat must be >= -90 and <=90"
                }
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody(expectedResponseData)
        )

        // Act
        val response =
            executeApiRequestAndHandleResponse(resultMapper = MappersFactory.providesAutosuggestResponseMapper()) {
                what3WordsV3Service.autosuggest(input, null, focus, null, null, null, null, null, null, null, null, null)
            }

        // Assert
        assert(response is W3WResult.Failure)
        response as W3WResult.Failure
        assert(response.error is BadFocusError)
    }

    @Test
    fun `autosuggest with invalid clip to circle option should return BadClipToCircle`() = runTest {
        // Arrange
        val input = "index.home.raf"
        val clipToCircle = "250.842404,4.361177"

        val expectedResponseData = """
            {
                "error": {
                    "code": "BadClipToCircle",
                    "message": "latitudes must be >=-90 and <=90"
                }
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody(expectedResponseData)
        )

        // Act
        val response =
            executeApiRequestAndHandleResponse(resultMapper = MappersFactory.providesAutosuggestResponseMapper()) {
                what3WordsV3Service.autosuggest(input, null, null, null, null, null, clipToCircle, null, null, null, null, null)
            }

        // Assert
        assert(response is W3WResult.Failure)
        response as W3WResult.Failure
        assert(response.error is BadClipToCircleError)
    }

    @Test
    fun `test request timeout is handled gracefully`() = runTest {
        val input = "index.home.raf"

        mockWebServer.enqueue(
            MockResponse()
                .setSocketPolicy(SocketPolicy.NO_RESPONSE)
        )

        // Act
        val response =
            executeApiRequestAndHandleResponse(resultMapper = MappersFactory.providesAutosuggestResponseMapper()) {
                what3WordsV3Service.autosuggest(input, null, null, null, null, null, null, null, null, null, null, null)
            }

        assert(response is W3WResult.Failure)
        response as W3WResult.Failure
        assert(response.error is NetworkError)
    }
}