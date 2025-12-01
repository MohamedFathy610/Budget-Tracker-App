package com.depi.budgettracker

import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.serializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.modules.contextual
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.serializers.LocalDateTimeIso8601Serializer



object TransactionListSerializer : Serializer<List<Transaction>> {
    private val customJson = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            contextual(LocalDateTimeIso8601Serializer)
        }
    }

    override val defaultValue: List<Transaction>
        get() = emptyList()

    override suspend fun readFrom(input: InputStream): List<Transaction> {
        return try {
            customJson.decodeFromString(serializer<List<Transaction>>(), input.readBytes().decodeToString())
        } catch (e: SerializationException) {
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: List<Transaction>, output: OutputStream) {
        output.write(
            customJson.encodeToString(serializer<List<Transaction>>(), t).encodeToByteArray()
        )
    }
}
