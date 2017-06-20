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

package fr.vsct.tock.bot.mongo

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import fr.vsct.tock.bot.admin.dialog.ActionReport
import fr.vsct.tock.bot.admin.dialog.DialogReport
import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.bot.definition.StoryDefinition
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.SendAttachment
import fr.vsct.tock.bot.engine.action.SendChoice
import fr.vsct.tock.bot.engine.action.SendLocation
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.dialog.ActionState
import fr.vsct.tock.bot.engine.dialog.ArchivedEntityValue
import fr.vsct.tock.bot.engine.dialog.BotMetadata
import fr.vsct.tock.bot.engine.dialog.ContextValue
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.dialog.EntityStateValue
import fr.vsct.tock.bot.engine.dialog.State
import fr.vsct.tock.bot.engine.dialog.Story
import fr.vsct.tock.bot.engine.event.EventType
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.UserLocation
import fr.vsct.tock.shared.jackson.AnyValueWrapper
import java.time.Instant
import java.time.Instant.now

/**
 *
 */
internal data class DialogCol(val playerIds: Set<PlayerId>,
                              var _id: String,
                              val state: StateMongoWrapper,
                              val stories: List<StoryMongoWrapper>,
                              val lastUpdateDate: Instant = now()) {

    companion object {
        private fun getActionWrapper(action: Action): ActionMongoWrapper {
            return when (action) {
                is SendSentence -> SendSentenceMongoWrapper(action)
                is SendChoice -> SendChoiceMongoWrapper(action)
                is SendAttachment -> SendAttachmentMongoWrapper(action)
                is SendLocation -> SendLocationMongoWrapper(action)
                else -> error("action type not supported : $action")
            }
        }
    }

    constructor(dialog: Dialog) : this(
            dialog.playerIds,
            dialog.id,
            StateMongoWrapper(dialog.state),
            dialog.stories.map { StoryMongoWrapper(it) }
    )

    fun toDialog(storyDefinitionProvider: (String) -> StoryDefinition): Dialog {
        return stories.map { it.toStory(storyDefinitionProvider) }.let {
            Dialog(
                    playerIds,
                    _id,
                    state.toState(it.flatMap { it.actions }.map { it.id to it }.toMap()),
                    it.toMutableList()
            )
        }
    }

    fun toDialogReport(): DialogReport {
        return DialogReport(
                stories.flatMap { it.actions }
                        .map { it.toAction() }
                        .mapNotNull {
                            when (it) {
                                is SendSentence ->
                                    ActionReport(
                                            it.playerId,
                                            it.date,
                                            EventType.sentence,
                                            it.text,
                                            it.messages
                                    )
                                is SendChoice ->
                                    ActionReport(
                                            it.playerId,
                                            it.date,
                                            EventType.choice,
                                            intent = it.intentName,
                                            parameters = it.parameters
                                    )
                                is SendAttachment ->
                                    ActionReport(
                                            it.playerId,
                                            it.date,
                                            EventType.attachment,
                                            url = it.url,
                                            attachmentType = it.type
                                    )
                                is SendLocation ->
                                    ActionReport(
                                            it.playerId,
                                            it.date,
                                            EventType.location,
                                            userLocation = it.location
                                    )
                                else -> null
                            }
                        }
        )
    }

    data class StateMongoWrapper(
            var currentIntent: Intent?,
            @JsonDeserialize(contentAs = EntityStateValueWrapper::class)
            val entityValues: Map<String, EntityStateValueWrapper>,
            @JsonDeserialize(contentAs = AnyValueWrapper::class)
            val context: Map<String, AnyValueWrapper>) {


        constructor(state: State) : this(
                state.currentIntent,
                state.entityValues.mapValues { EntityStateValueWrapper(it.value) },
                state.context.map { e -> e.key to AnyValueWrapper(e.value) }.toMap()
        )

        fun toState(actionsMap: Map<String, Action>): State {
            return State(
                    currentIntent,
                    entityValues.mapValues { it.value.toEntityStateValue(actionsMap) }.toMutableMap(),
                    context.mapValues { it.value.value!! }.toMutableMap())
        }

    }

    data class EntityStateValueWrapper(
            val value: ContextValue?,
            val history: List<ArchivedEntityValueWrapper>) {

        constructor(value: EntityStateValue) : this(value.value, value.history.map { ArchivedEntityValueWrapper(it) })

        fun toEntityStateValue(actionsMap: Map<String, Action>): EntityStateValue {
            return EntityStateValue(
                    value,
                    history.map { it.toArchivedEntityValue(actionsMap) }.toMutableList()
            )
        }
    }

    class ArchivedEntityValueWrapper(
            val entityValue: ContextValue?,
            val actionId: String?) {

        constructor(value: ArchivedEntityValue) : this(value.entityValue, value.action?.id)

        fun toArchivedEntityValue(actionsMap: Map<String, Action>): ArchivedEntityValue {
            return ArchivedEntityValue(
                    entityValue,
                    actionsMap.get(actionId ?: ""))
        }
    }


    class StoryMongoWrapper(val storyDefinitionId: String,
                            var currentIntent: Intent?,
                            val actions: List<ActionMongoWrapper>) {

        constructor(story: Story) : this(
                story.definition.id,
                story.currentIntent,
                story.actions.map { getActionWrapper(it) })

        fun toStory(storyDefinitionProvider: (String) -> StoryDefinition): Story {
            return Story(
                    storyDefinitionProvider.invoke(storyDefinitionId),
                    currentIntent,
                    actions.map { it.toAction() }.toMutableList()
            )
        }


    }


    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes(
            JsonSubTypes.Type(value = SendSentenceMongoWrapper::class, name = "sentence"),
            JsonSubTypes.Type(value = SendChoiceMongoWrapper::class, name = "choice"),
            JsonSubTypes.Type(value = SendAttachmentMongoWrapper::class, name = "attachment"),
            JsonSubTypes.Type(value = SendLocationMongoWrapper::class, name = "location"))
    abstract class ActionMongoWrapper() {

        lateinit var id: String
        lateinit var date: Instant
        lateinit var state: ActionState
        lateinit var botMetadata: BotMetadata
        lateinit var playerId: PlayerId
        lateinit var recipientId: PlayerId
        lateinit var applicationId: String


        fun assignFrom(action: Action) {
            id = action.id
            date = action.date
            state = action.state
            botMetadata = action.botMetadata
            playerId = action.playerId
            recipientId = action.recipientId
            applicationId = action.applicationId
        }

        abstract fun toAction(): Action
    }

    @JsonTypeName(value = "sentence")
    class SendSentenceMongoWrapper(val text: String?,
                                   val messages: List<AnyValueWrapper>)
        : ActionMongoWrapper() {

        constructor(sentence: SendSentence) : this(sentence.text, sentence.messages.map { AnyValueWrapper(it) }) {
            assignFrom(sentence)
        }

        override fun toAction(): Action {
            return SendSentence(
                    playerId,
                    applicationId,
                    recipientId,
                    text,
                    messages.map { it.value as ConnectorMessage }.toMutableList(),
                    id,
                    date,
                    state,
                    botMetadata)
        }
    }

    @JsonTypeName(value = "choice")
    class SendChoiceMongoWrapper(val intentName: String,
                                 val parameters: Map<String, String>) : ActionMongoWrapper() {

        constructor(choice: SendChoice) : this(choice.intentName, choice.parameters) {
            assignFrom(choice)
        }

        override fun toAction(): Action {
            return SendChoice(
                    playerId,
                    applicationId,
                    recipientId,
                    intentName,
                    parameters,
                    id,
                    date,
                    state,
                    botMetadata)
        }
    }

    @JsonTypeName(value = "attachment")
    class SendAttachmentMongoWrapper(val url: String,
                                     val type: SendAttachment.AttachmentType) : ActionMongoWrapper() {

        constructor(attachment: SendAttachment) : this(attachment.url, attachment.type) {
            assignFrom(attachment)
        }

        override fun toAction(): Action {
            return SendAttachment(
                    playerId,
                    applicationId,
                    recipientId,
                    url,
                    type,
                    id,
                    date,
                    state,
                    botMetadata)
        }
    }

    @JsonTypeName(value = "location")
    class SendLocationMongoWrapper(val location: UserLocation?) : ActionMongoWrapper() {

        constructor(location: SendLocation) : this(location.location) {
            assignFrom(location)
        }

        override fun toAction(): Action {
            return SendLocation(
                    playerId,
                    applicationId,
                    recipientId,
                    location,
                    id,
                    date,
                    state,
                    botMetadata)
        }
    }


}


