package org.abimon.spiral.util

import org.abimon.visi.lang.and
import org.parboiled.BaseParser
import org.parboiled.Parboiled
import org.parboiled.Rule
import org.parboiled.annotations.BuildParseTree
import org.parboiled.parserunners.ReportingParseRunner

@BuildParseTree
open class OBJParser : BaseParser<Any>() {
    companion object {
        val parser: OBJParser = Parboiled.createParser(OBJParser::class.java)
        val runner: ReportingParseRunner<Any> = ReportingParseRunner(parser.Lines())

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
    }

    open fun Lines(): Rule = Sequence(
            clearState(),
            ParamList("OBJ", Line()),
            operateOnTmpStack(this, "OBJ") { push(it) }
    )

    open fun Line(): Rule = FirstOf(
            Comment(),
            Vertex(),
            UV(),
            Face(),
            ZeroOrMore(LineMatcher),
            EOI
    )

    open fun Comment(): Rule = Sequence("#", ZeroOrMore(LineMatcher))
    open fun Vertex(): Rule = Sequence(
            clearTmpStack(VERTEX_ID),
            'v',
            ' ',
            pushTmpAction(this, VERTEX_ID, VERTEX_ID),
            Float(),
            pushTmpAction(this, VERTEX_ID),
            ' ',
            Float(),
            pushTmpAction(this, VERTEX_ID),
            ' ',
            Float(),
            pushTmpAction(this, VERTEX_ID),
            pushTmpStack(this, VERTEX_ID),
            ZeroOrMore(LineMatcher)
    )

    open fun UV(): Rule = Sequence(
            clearTmpStack(UV_ID),
            "vt",
            ' ',
            pushTmpAction(this, UV_ID, UV_ID),
            Float(),
            pushTmpAction(this, UV_ID),
            ' ',
            Float(),
            pushTmpAction(this, UV_ID),
            pushTmpStack(this, UV_ID),
            ZeroOrMore(LineMatcher)
    )

    open fun Face(): Rule = Sequence(
            clearTmpStack(FACE_ID),
            'f',
            ' ',
            pushTmpAction(this, FACE_ID, FACE_ID),
            FaceID(),
            pushTmpAction(this, FACE_ID),
            ' ',
            FaceID(),
            pushTmpAction(this, FACE_ID),
            ' ',
            FaceID(),
            pushTmpAction(this, FACE_ID),
            pushTmpStack(this, FACE_ID),
            ZeroOrMore(LineMatcher)
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