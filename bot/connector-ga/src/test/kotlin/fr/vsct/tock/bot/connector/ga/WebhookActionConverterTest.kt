/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.connector.ga

import com.fasterxml.jackson.module.kotlin.readValue
import com.nhaarman.mockito_kotlin.mock
import fr.vsct.tock.bot.connector.ga.model.request.GARequest
import fr.vsct.tock.bot.engine.ConnectorController
import fr.vsct.tock.bot.engine.action.SendChoice
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.resource
import org.junit.Test
import kotlin.test.assertTrue

/**
 *
 */
class WebhookActionConverterTest {

    val controller: ConnectorController = mock()
    val appId = "test"
    val optionRequest: GARequest = mapper.readValue(resource("/request_with_option.json"))
    val optionWithRawTextRequest: GARequest = mapper.readValue(resource("/request_with_option_and_raw_text.json"))

    @Test
    fun toEvent_shouldReturnsSendChoice_whenOptionArgAndSameInputText() {
        val e = WebhookActionConverter.toEvent(optionRequest, appId)
        assertTrue(e is SendChoice)
    }

    @Test
    fun toEvent_shouldReturnsSendSentence_whenOptionArgAndDifferentInputText() {
        val e = WebhookActionConverter.toEvent(optionWithRawTextRequest, appId)
        assertTrue(e is SendSentence)
    }


}