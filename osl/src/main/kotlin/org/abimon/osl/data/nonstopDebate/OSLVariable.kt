package org.abimon.osl.data.nonstopDebate

import kotlin.reflect.KClass

data class OSLVariable<T : Any>(val key: String, val value: T, val klass: KClass<T>) {
    companion object {
        inline operator fun <reified T: Any> invoke(key: String, value: T): OSLVariable<T> = OSLVariable(key, value, T::class)

        fun NonstopTimelimit(limit: Int): OSLVariable<Int> = OSLVariable(KEYS.NONSTOP_TIMELIMIT, limit)

        fun NonstopCorrectEvidence(evidence: Int): OSLVariable<Int> = OSLVariable(KEYS.NONSTOP_CORRECT_EVIDENCE, evidence)
        fun NonstopChangeOperatingBlock(operatingBlock: Int): OSLVariable<Int> = OSLVariable(KEYS.NONSTOP_CHANGE_OPERATING_BLOCK, operatingBlock)
        fun NonstopChangeStage(stage: Int): OSLVariable<Int> = OSLVariable(KEYS.NONSTOP_CHANGE_STAGE, stage)
        fun NonstopDebateNumber(number: Int): OSLVariable<Int> = OSLVariable(KEYS.NONSTOP_DEBATE_NUMBER, number)
        fun NonstopDebateCoupledScript(script: Triple<Int, Int, Int>?): OSLVariable<Triple<Int, Int, Int>> = OSLVariable(KEYS.NONSTOP_DEBATE_COUPLED_SCRIPT, script ?: VALUES.NONSTOP_COUPLED_SCRIPT_NULL)
        //fun NonstopEndDebate(): OSLVariable<Unit> = OSLVariable(KEYS.NONSTOP_END_DEBATE, Unit)

        fun CompileAs(name: String): OSLVariable<String> = OSLVariable(KEYS.COMPILE_AS, name)
    }

    object KEYS {
        const val NONSTOP_TIMELIMIT = "Nonstop_TimeLimit"

        const val NONSTOP_CORRECT_EVIDENCE = "Nonstop_Correct_Evidence"
        const val NONSTOP_CHANGE_OPERATING_BLOCK = "Nonstop_Change_Operating_Block"
        const val NONSTOP_CHANGE_STAGE = "Nonstop_Change_Stage"
        const val NONSTOP_DEBATE_NUMBER = "Nonstop_Debate_Number"
        const val NONSTOP_DEBATE_COUPLED_SCRIPT = "Nonstop_Debate_Coupled_Script"
        //const val NONSTOP_END_DEBATE = "Nonstop_End_Debate"

        const val COMPILE_AS = "Compile-As"
    }

    object VALUES {
        const val NONSTOP_STAGE_PRE_SCRIPT = 0
        const val NONSTOP_STAGE_PRE_TEXT = 1
        const val NONSTOP_STAGE_TEXT = 2
        const val NONSTOP_STAGE_POST_TEXT = 3
        const val NONSTOP_STAGE_POST_SCRIPT = 4

        const val NONSTOP_OPERATING_BLOCK_NONE = 0
        const val NONSTOP_OPERATING_BLOCK_SUCCESS = 1
        const val NONSTOP_OPERATING_BLOCK_FAIL = 2
        const val NONSTOP_OPERATING_BLOCK_TEXT = 3

        val NONSTOP_COUPLED_SCRIPT_NULL = Triple(-1, -1, -1)
    }
}