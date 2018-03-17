package org.abimon.spiral.util

import org.abimon.osl.LineMatcher
import org.abimon.osl.SpiralParser
import org.abimon.visi.lang.and
import org.parboiled.Parboiled
import org.parboiled.Rule
import org.parboiled.annotations.BuildParseTree

@BuildParseTree
open class OBJParser(isParboiledCreated: Boolean) : SpiralParser(isParboiledCreated) {
    companion object {
        val VERTEX_ID = "OBJ-MODEL-VERTEX"
        val UV_ID = "OBJ-MODEL-UV"
        val FACE_ID = "OBJ-MODEL-FACE"

        fun toVertex(list: List<Any?>): Vertex {
            val x = list[0].toString().toFloatOrNull() ?: 1.0f
            val y = list[1].toString().toFloatOrNull() ?: 1.0f
            val z = list[2].toString().toFloatOrNull() ?: 1.0f

            return x to y and z
        }

        fun toUV(list: List<Any?>): UV {
            val u = list[0].toString().toFloatOrNull() ?: 1.0f
            val v = list[1].toString().toFloatOrNull() ?: 1.0f

            return u to v
        }

        fun toFace(list: List<Any?>): TriFace {
            val a = list[0].toString().substringBefore('/').toIntOrNull() ?: 1
            val b = list[1].toString().substringBefore('/').toIntOrNull() ?: 2
            val c = list[2].toString().substringBefore('/').toIntOrNull() ?: 3

            return (a - 1) to (b - 1) and (c - 1)
        }

        operator fun invoke(): OBJParser = Parboiled.createParser(OBJParser::class.java, true)
    }

    open fun Lines(): Rule = Sequence(
            clearState(),
            Sequence(ZeroOrMore(Sequence(Line(), Ch('\n'))), Line())
    )

    open fun Line(): Rule = FirstOf(
            Comment(),
            Vertex(),
            UV(),
            Face(),
            ZeroOrMore(LineMatcher),
            EOI
    )


    open fun Vertex(): Rule = Sequence(
            clearTmpStack(VERTEX_ID),
            'v',
            ' ',
            pushTmpAction(VERTEX_ID, VERTEX_ID),
            Float(),
            pushTmpAction(VERTEX_ID),
            ' ',
            Float(),
            pushTmpAction(VERTEX_ID),
            ' ',
            Float(),
            pushTmpAction(VERTEX_ID),
            pushTmpStack(VERTEX_ID)
    )

    open fun UV(): Rule = Sequence(
            clearTmpStack(UV_ID),
            "vt",
            ' ',
            pushTmpAction(UV_ID, UV_ID),
            Float(),
            pushTmpAction(UV_ID),
            ' ',
            Float(),
            pushTmpAction(UV_ID),
            pushTmpStack(UV_ID)
    )

    open fun Face(): Rule = Sequence(
            clearTmpStack(FACE_ID),
            'f',
            ' ',
            pushTmpAction(FACE_ID, FACE_ID),
            FaceID(),
            pushTmpAction(FACE_ID),
            ' ',
            FaceID(),
            pushTmpAction(FACE_ID),
            ' ',
            FaceID(),
            pushTmpAction(FACE_ID),
            pushTmpStack(FACE_ID)
    )

    open fun FaceID(): Rule = FirstOf(
            Sequence(OneOrMore(Digit()), "//", OneOrMore(Digit())),
            Sequence(OneOrMore(Digit()), '/', OneOrMore(Digit()), '/', OneOrMore(Digit())),
            Sequence(OneOrMore(Digit()), '/', OneOrMore(Digit())),
            OneOrMore(Digit())
    )
    //open fun FaceID(): Rule = Sequence(OneOrMore(Digit()), "//", OneOrMore(Digit()))

    open fun Float(): Rule = Sequence(OneOrMore(Digit()), Optional(Sequence('.', OneOrMore(Digit()))))
}