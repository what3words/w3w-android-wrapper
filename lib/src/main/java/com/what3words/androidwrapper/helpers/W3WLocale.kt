package com.what3words.core.domain.language

/**
 *   RFC5646 What3Words Language Definition.
 *   All the languages supported by What3Words across all our domain, from lat/lng conversion to text, voice, image search.
 */
enum class W3WLanguage(val code: String) {
    AF(code = "af"),
    AM(code = "am"),
    AR(code = "ar"),
    BG(code = "bg"),
    BN(code = "bn"),
    BS_LATN(code = "bs-Latn"),
    BS_CYRL(code = "bs-Cyrl"),
    CA(code = "ca"),
    CS(code = "cs"),
    CY(code = "cy"),
    DA(code = "da"),
    DE(code = "de"),
    EL(code = "el"),
    EN_AU(code = "en-AU"),
    EN_CA(code = "en-CA"),
    EN_GB(code = "en-GB"),
    EN_IN(code = "en-IN"),
    EN_US(code = "en-US"),
    ES_ES(code = "es-ES"),
    ES_MX(code = "es-MX"),
    ET(code = "et"),
    FA(code = "fa"),
    FI(code = "fi"),
    FR_CA(code = "fr-CA"),
    FR_FR(code = "fr-FR"),
    GU(code = "gu"),
    HE(code = "he"),
    HI(code = "hi"),
    HR(code = "hr"),
    HU(code = "hu"),
    ID(code = "id"),
    IT(code = "it"),
    JA(code = "ja"),
    KK_CYRL(code = "kk-Cyrl"),
    KK_LATN(code = "kk-Latn"),
    KM(code = "km"),
    KN(code = "kn"),
    KO(code = "ko"),
    LO(code = "lo"),
    ML(code = "ml"),
    MN_CYRL(code = "mn-Cyrl"),
    MN_LATN(code = "mn-Latn"),
    MR(code = "mr"),
    MS(code = "ms"),
    NE(code = "ne"),
    NL(code = "nl"),
    NO(code = "no"),
    OR(code = "or"),
    PA(code = "pa"),
    PL(code = "pl"),
    PT_BR(code = "pt-BR"),
    PT_PT(code = "pt-PT"),
    RO(code = "ro"),
    RU(code = "ru"),
    SI(code = "si"),
    SK(code = "sk"),
    SR_LATN_RS(code = "sr-Latn-RS"),
    SR_CYRL_RS(code = "sr-Cyrl-RS"),
    SR_LATN_ME(code = "sr-Latn-ME"),
    SR_CYRL_ME(code = "sr-Cyrl-ME"),
    SV(code = "sv"),
    SW(code = "sw"),
    TA(code = "ta"),
    TE(code = "te"),
    TR(code = "tr"),
    UK(code = "uk"),
    UR(code = "ur"),
    VI(code = "vi"),
    XH(code = "xh"),
    ZH_HANS(code = "zh-Hans"),
    ZH_HANT_HK(code = "zh-Hant-HK"),
    ZH_HANT_TW(code = "zh-Hant-TW"),
    ZU(code = "zu");

    /**
     * Get RFC5646 language ISO 639-1 code. Example: [W3WLanguage.ZH_HANT_TW.getRegionCode] returns zh.
     *
     * @return language ISO 639-1 two letter code.
     */
    fun getLanguageCode(): String {
        return code.split("-")[0]
    }


    /**
     * Get RFC5646 script ISO 15924 code. Example: [W3WLanguage.ZH_HANT_TW.getRegionCode] returns Hant.
     *
     * @return script ISO 15924 four letter code.
     */
    fun getScriptCode(): String? {
        val split = code.split("-")
        return when {
            split.size == 2 && split[1].count() == 4 -> split[1]
            split.size == 3 && split[1].count() == 4 -> split[1]
            else -> null
        }
    }

    /**
     * Get RFC5646 region ISO 3166-1 alpha-2 code. Example: [W3WLanguage.ZH_HANT_TW.getRegionCode] returns TW.
     *
     * @return region ISO 3166-1 alpha-2 two letter code.
     */
    fun getRegionCode(): String? {
        val split = code.split("-")
        return when {
            split.size == 2 && split[1].count() == 2 -> split[1]
            split.size == 3 && split[2].count() == 2 -> split[2]
            else -> null
        }
    }
}

data class W3WLanguageInfo(
    val locale: W3WLanguage,
    val w3w: W3WDataSourceLanguage,
    val name: String,
    val nativeName: String
)

data class W3WDataSourceLanguage(
    val language: String
)
