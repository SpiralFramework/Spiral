package org.abimon.spiral.core.data

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object InstantSerialisation {
    object SERIALISER : JsonSerializer<Instant>() {
        /**
         * Method that can be called to ask implementation to serialize
         * values of type this serializer handles.
         *
         * @param value Value to serialize; can **not** be null.
         * @param gen Generator used to output resulting Json content
         * @param serializers Provider that can be used to get serializers for
         * serializing Objects value contains, if any.
         */
        override fun serialize(value: Instant, gen: JsonGenerator, serializers: SerializerProvider?) {
            gen.writeString(value.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_ZONED_DATE_TIME))
        }

        override fun handledType(): Class<Instant> = Instant::class.java
    }

    object DESERIALISER : JsonDeserializer<Instant>() {
        /**
         * Method that can be called to ask implementation to deserialize
         * JSON content into the value type this serializer handles.
         * Returned instance is to be constructed by method itself.
         *
         *
         * Pre-condition for this method is that the parser points to the
         * first event that is part of value to deserializer (and which
         * is never JSON 'null' literal, more on this below): for simple
         * types it may be the only value; and for structured types the
         * Object start marker or a FIELD_NAME.
         *
         *
         *
         * The two possible input conditions for structured types result
         * from polymorphism via fields. In the ordinary case, Jackson
         * calls this method when it has encountered an OBJECT_START,
         * and the method implementation must advance to the next token to
         * see the first field name. If the application configures
         * polymorphism via a field, then the object looks like the following.
         * <pre>
         * {
         * "@class": "class name",
         * ...
         * }
        </pre> *
         * Jackson consumes the two tokens (the <tt>@class</tt> field name
         * and its value) in order to learn the class and select the deserializer.
         * Thus, the stream is pointing to the FIELD_NAME for the first field
         * after the @class. Thus, if you want your method to work correctly
         * both with and without polymorphism, you must begin your method with:
         * <pre>
         * if (p.getCurrentToken() == JsonToken.START_OBJECT) {
         * p.nextToken();
         * }
        </pre> *
         * This results in the stream pointing to the field name, so that
         * the two conditions align.
         *
         *
         * Post-condition is that the parser will point to the last
         * event that is part of deserialized value (or in case deserialization
         * fails, event that was not recognized or usable, which may be
         * the same event as the one it pointed to upon call).
         *
         *
         * Note that this method is never called for JSON null literal,
         * and thus deserializers need (and should) not check for it.
         *
         * @param p Parsed used for reading JSON content
         * @param ctxt Context that can be used to access information about
         * this deserialization activity.
         *
         * @return Deserialized value
         */
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): Instant = ZonedDateTime.parse(p.valueAsString, DateTimeFormatter.ISO_ZONED_DATE_TIME).toInstant()

        override fun handledType(): Class<Instant> = Instant::class.java
    }

    class MODULE : SimpleModule("Instant Serialisation", Version.unknownVersion(), mapOf(Instant::class.java to DESERIALISER), listOf(SERIALISER))
}