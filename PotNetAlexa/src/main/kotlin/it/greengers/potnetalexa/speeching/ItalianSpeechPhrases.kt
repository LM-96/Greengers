package it.greengers.potnetalexa.speeching

class ItalianSpeechPhrases() : SpeechPhrases {

    override val LANGUAGE: Language = Language.ITALIAN
    private val goodbye = mutableListOf<String>()

    init {
        goodbye.add("Ciao. Ricordati sempre di controllare lo stato della tua pianta")
        goodbye.add("Arrivederci. Mi raccomando, non trascurare la tua pianta")
    }

    override fun getGoodbyePhrase(): String {
        return goodbye.getCasual()
    }


}