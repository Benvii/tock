/*
 * Copyright (C) 2019 VSCT
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

package fr.vsct.tock.bot.connector.twitter.model.outcoming

import fr.vsct.tock.bot.connector.twitter.model.TwitterPublicConnectorMessage
import mu.KotlinLogging
import org.apache.commons.lang3.exception.ExceptionUtils

data class Tweet(val text: String, val dmRecipientID: String? = null): TwitterPublicConnectorMessage() {
    private val logger = KotlinLogging.logger {}

    init {
        val stackTrace = ExceptionUtils.getStackTrace(Throwable())
        logger.error { "Twitt debug new Tweet" + stackTrace }
    }
}