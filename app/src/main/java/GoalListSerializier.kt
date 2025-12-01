package com.depi.budgettracker

import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.serializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.serializers.LocalDateTimeIso8601Serializer

object GoalListSerializer : Serializer<List<Goal>> {
    private val customJson = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            contextual(LocalDateTimeIso8601Serializer)
        }
    }

    override val defaultValue: List<Goal>
        get() = emptyList()

    override suspend fun readFrom(input: InputStream): List<Goal> {
        return try {
            customJson.decodeFromString(serializer<List<Goal>>(), input.readBytes().decodeToString())
        } catch (e: SerializationException) {
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: List<Goal>, output: OutputStream) {
        output.write(
            customJson.encodeToString(serializer<List<Goal>>(), t).encodeToByteArray()
        )
    }
}