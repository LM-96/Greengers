package it.greengers.potnetalexa.handlers

import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.dispatcher.request.handler.RequestHandler
import com.amazon.ask.model.Response
import java.util.*

import com.amazon.ask.request.Predicates.intentName
import it.greengers.potnetalexa.speeching.CurrentSpeechPhrases

class CancelAndStopIntentHandler : RequestHandler {

    override fun canHandle(input: HandlerInput): Boolean {
        return input.matches(
            intentName("AMAZON.StopIntent")
                .or(intentName("AMAZON.CancelIntent")
                    .or(intentName("AMAZON.NoIntent"))))
    }

    override fun handle(input: HandlerInput): Optional<Response> {
        return input.responseBuilder
            .withSpeech(CurrentSpeechPhrases.speechPhrases.getGoodbyePhrase())
            .wi
    }
}