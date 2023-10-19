package com.quixicon.domain.entities.enums

import com.google.gson.annotations.JsonAdapter
import com.quixicon.domain.adapters.ValuedEnumAdapter

/**
 * Enum class for card types
 */
@JsonAdapter(ValuedEnumAdapter::class)
enum class LanguageCode(override val value: String) : ValuedEnum<String> {
    UNDEFINED(""),
    RU("ru"), // Russian
    EN("en"), // English
    FR("fr"), // French
    ES("es"), // Spanish
    SV("sv"), // Swedish
    HI("hi"), // Hindi
    NE("ne"), // Nepali
    ZH("zh"), // Chinese
    CS("cs"), // Czech
    AR("ar"), // Arabic
    KA("ka"), // Georgian
    DE("de"), // German
    EL("el"), // Greek
    HE("he"), // Hebrew
    ID("id"), // Indonesian
    IT("it"), // Italian
    JA("ja"), // Japanese
    PT("pt"), // Portuguese
    PL("pl"), // Polish
    TH("th"), // Thai
    TR("tr"), // Turkish
    UK("uk") // Ukrainian
}
