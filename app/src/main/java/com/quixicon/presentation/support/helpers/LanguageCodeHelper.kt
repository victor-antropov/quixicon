package com.quixicon.presentation.support.helpers
import com.quixicon.R
import com.quixicon.domain.entities.enums.LanguageCode

object LanguageCodeHelper {
    fun getIcon(code: LanguageCode?): Int {
        return when (code) {
            LanguageCode.RU -> R.drawable.xflag_rus
            LanguageCode.EN -> R.drawable.xflag_eng
            LanguageCode.FR -> R.drawable.xflag_fra
            LanguageCode.ES -> R.drawable.xflag_spa
            LanguageCode.SV -> R.drawable.xflag_swe
            LanguageCode.NE -> R.drawable.xflag_npl
            LanguageCode.HI -> R.drawable.xflag_hin
            LanguageCode.ZH -> R.drawable.xflag_zho
            LanguageCode.CS -> R.drawable.xflag_ces
            LanguageCode.AR -> R.drawable.xflag_ara
            LanguageCode.KA -> R.drawable.xflag_kat
            LanguageCode.DE -> R.drawable.xflag_deu
            LanguageCode.EL -> R.drawable.xflag_eli
            LanguageCode.HE -> R.drawable.xflag_heb
            LanguageCode.ID -> R.drawable.xflag_ind
            LanguageCode.IT -> R.drawable.xflag_ita
            LanguageCode.JA -> R.drawable.xflag_jpn
            LanguageCode.PT -> R.drawable.xflag_por
            LanguageCode.PL -> R.drawable.xflag_pol
            LanguageCode.TH -> R.drawable.xflag_tha
            LanguageCode.TR -> R.drawable.xflag_tur
            LanguageCode.UK -> R.drawable.xflag_ukr
            LanguageCode.UNDEFINED -> R.drawable.xflag_other
            else -> R.drawable.xflag_all
        }
    }
}
