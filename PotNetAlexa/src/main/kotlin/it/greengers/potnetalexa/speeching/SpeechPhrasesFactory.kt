package it.greengers.potnetalexa.speeching

import org.apache.commons.codec.language.bm.Lang

object SpeechPhrasesFactory {

    private val SPEECH_PHRASES : Map<Language, SpeechPhrases>

    init {
        val map = mutableMapOf<Language, SpeechPhrases>()
        map[Language.ITALIAN] = ItalianSpeechPhrases()

        SPEECH_PHRASES = map.toMap()
    }


    fun getSpeechPhrases(language: Language) : SpeechPhrases? {
        return SPEECH_PHRASES[language]
    }

    fun getDefaultSpeechPhrases() : SpeechPhrases {
        return SPEECH_PHRASES[Language.ITALIAN]!!
    }

}