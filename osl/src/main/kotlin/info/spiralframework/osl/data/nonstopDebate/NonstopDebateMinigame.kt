package info.spiralframework.osl.data.nonstopDebate

import info.spiralframework.formats.customLin
import info.spiralframework.formats.customNonstopDebate
import info.spiralframework.formats.data.NonstopDebateSection
import info.spiralframework.formats.game.hpa.DR1
import info.spiralframework.formats.game.hpa.DR2
import info.spiralframework.formats.game.hpa.HopesPeakKillingGame
import info.spiralframework.formats.scripting.CustomLin
import info.spiralframework.formats.scripting.CustomNonstopDebate
import info.spiralframework.formats.scripting.lin.*
import info.spiralframework.formats.scripting.lin.dr1.DR1LoadScriptEntry
import info.spiralframework.formats.scripting.lin.dr1.DR1RunScript
import info.spiralframework.formats.scripting.lin.dr1.DR1TrialCameraEntry
import info.spiralframework.formats.scripting.lin.dr2.DR2RunScriptEntry
import info.spiralframework.formats.utils.and
import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.firstOfInstanceOrNull
import java.util.concurrent.ThreadLocalRandom

class NonstopDebateMinigame(val game: HopesPeakKillingGame) {
    companion object {
        val MIN_LABEL = 0
        val MAX_LABEL = (256 * 256)

        val MAX_VALUE = (256 * 256)
    }

    var debateNumber: Int = 0
    var coupledScript: Triple<Int, Int, Int>? = ThreadLocalRandom.current().nextInt(255) to ThreadLocalRandom.current().nextInt(255) and ThreadLocalRandom.current().nextInt(255)

    val customNonstopDebate = customNonstopDebate { this.game = this@NonstopDebateMinigame.game }

    val preScriptEntries: MutableList<LinScript> = ArrayList()
    val preTextEntries: MutableList<LinScript> = ArrayList()
    val postTextEntries: MutableList<LinScript> = ArrayList()
    val postScriptEntries: MutableList<LinScript> = ArrayList()

    val textSections: MutableList<NonstopDebateMinigameSection> = ArrayList()
    val workingTextStack: MutableList<LinScript> = ArrayList()
    var sectionStack: NonstopDebateSection? = null
    val correctStack: MutableList<LinScript> = ArrayList()
    val incorrectStack: MutableList<LinScript> = ArrayList()
    val textStack: MutableList<LinScript> = ArrayList()

    var characterDefined: Boolean = false
    var spriteDefined: Boolean = false
    var voiceDefined: Boolean = false
    var chapterDefined: Boolean = false
    var hasWeakPointDefined: Boolean = false

    var sectionCharacter: Int?
        get() = sectionStack?.character
        set(value) {
            if (value != null) {
                characterDefined = true
                sectionStack?.character = value
            }
        }

    var sectionSprite: Int?
        get() = sectionStack?.sprite
        set(value) {
            if (value != null) {
                spriteDefined = true
                sectionStack?.sprite = value
            }
        }

    var sectionVoice: Int?
        get() = sectionStack?.voice
        set(value) {
            if (value != null) {
                voiceDefined = true
                sectionStack?.voice = value
            }
        }

    var sectionChapter: Int?
        get() = sectionStack?.chapter
        set(value) {
            if (value != null) {
                chapterDefined = true
                sectionStack?.chapter = value
            }
        }

    var sectionHasWeakPoint: Boolean?
        get() = sectionStack?.hasWeakPoint
        set(value) {
            if (value != null) {
                hasWeakPointDefined = true
                sectionStack?.hasWeakPoint = value
            }
        }

    val labels: MutableList<Int> = ArrayList()

    fun addPreScriptEntry(entry: LinScript) {
        preScriptEntries.add(entry)
    }

    fun addPreTextEntry(entry: LinScript) {
        preTextEntries.add(entry)
    }

    fun addPostTextEntry(entry: LinScript) {
        postTextEntries.add(entry)
    }

    fun addPostScriptEntry(entry: LinScript) {
        postScriptEntries.add(entry)
    }

    fun addPreScriptEntries(entries: Array<LinScript>) {
        preScriptEntries.addAll(entries)
    }

    fun addPreTextEntries(entries: Array<LinScript>) {
        preTextEntries.addAll(entries)
    }

    fun addPostTextEntries(entries: Array<LinScript>) {
        postTextEntries.addAll(entries)
    }

    fun addPostScriptEntries(entries: Array<LinScript>) {
        postScriptEntries.addAll(entries)
    }

    fun addWorkingTextEntry(entry: LinScript) {
        workingTextStack.add(entry)
    }

    fun addWorkingTextEntries(entries: Array<LinScript>) {
        workingTextStack.addAll(entries)
    }

    fun addTextEntry(entry: LinScript) {
        workingTextStack.add(entry)
        textStack.add(entry)
    }

    fun addTextEntries(entries: Array<LinScript>) {
        workingTextStack.addAll(entries)
        textStack.addAll(entries)
    }

    fun addCorrectEntry(entry: LinScript) {
        correctStack.add(entry)
    }

    fun addCorrectEntries(entries: Array<LinScript>) {
        correctStack.addAll(entries)
    }

    fun addIncorrectEntry(entry: LinScript) {
        incorrectStack.add(entry)
    }

    fun addIncorrectEntries(entries: Array<LinScript>) {
        incorrectStack.addAll(entries)
    }

    fun getLabel(): Int {
        val existingLabels: MutableList<Int> = ArrayList()

        existingLabels.addAll(preScriptEntries.mapNotNull { script -> (script as? SetLabelEntry)?.id })
        existingLabels.addAll(preTextEntries.mapNotNull { script -> (script as? SetLabelEntry)?.id })
        existingLabels.addAll(postTextEntries.mapNotNull { script -> (script as? SetLabelEntry)?.id })
        existingLabels.addAll(postScriptEntries.mapNotNull { script -> (script as? SetLabelEntry)?.id })

        for (i in MIN_LABEL until MAX_LABEL) {
            if (i !in existingLabels && i !in labels) {
                labels.add(i)
                return i
            }
        }

        return -1 //This should never happen
    }

    fun push() = addSection(null)
    fun addSection(section: NonstopDebateSection?) {
        if (sectionStack != null) {
            val oldSection = sectionStack!!
            val oldScripts = workingTextStack.toTypedArray()

            if (!characterDefined) {
                oldSection.character = workingTextStack.firstOfInstanceOrNull(SpeakerEntry::class)?.characterID
                        ?: workingTextStack.firstOfInstanceOrNull(SpriteEntry::class)?.characterID
                                ?: workingTextStack.firstOfInstanceOrNull(VoiceLineEntry::class)?.characterID
                                ?: 0
            }

            if (!spriteDefined) {
                oldSection.sprite = workingTextStack.firstOfInstanceOrNull(SpriteEntry::class)?.spriteID ?: 0
            }

            if (!voiceDefined) {
                oldSection.voice = workingTextStack.firstOfInstanceOrNull(VoiceLineEntry::class)?.voiceLineID
                        ?: MAX_VALUE
            }

            if (!chapterDefined) {
                oldSection.chapter = workingTextStack.firstOfInstanceOrNull(VoiceLineEntry::class)?.chapter ?: MAX_VALUE
            }

            if (!hasWeakPointDefined) {
                oldSection.hasWeakPoint = workingTextStack.filterIsInstance(TextEntry::class.java).any { entry ->
                    entry.text?.hasWeakPoint() ?: false
                }
            }

            val textEntries: Array<LinScript> = if (textStack.isEmpty()) {
                arrayOf(workingTextStack.firstOfInstanceOrNull(TextEntry::class), workingTextStack.firstOfInstanceOrNull(SpriteEntry::class)).filterNotNull().toTypedArray()
            } else {
                textStack.toTypedArray()
            }

            textSections.add(NonstopDebateMinigameSection(oldSection, textEntries, correctStack.toTypedArray(), incorrectStack.toTypedArray()))
        }

        sectionStack = section
        correctStack.clear()
        incorrectStack.clear()
        textStack.clear()
        workingTextStack.clear()
    }

    private fun String.hasWeakPoint(): Boolean = contains("CLT ${OpenSpiralLanguageParser.colourCodeForNameAndGame(game, "weak_point")}")

    operator fun get(index: Int): Int? = sectionStack?.get(index)
    operator fun set(index: Int, value: Int) {
        if (index < sectionStack?.data?.size ?: 0) {
            when (index) {
                0x15 -> sectionCharacter = value
                0x16 -> sectionSprite = value
                0x19 -> sectionVoice = value
                0x1B -> sectionChapter = value
                else -> sectionStack?.set(index, value)
            }
        }
    }


    //Main Script
    //Debate Script
    //Debate Data
    fun makeObjects(chapter: Int? = null, scene: Int? = null, room: Int? = null): Triple<CustomLin, CustomLin, CustomNonstopDebate> {
        push()

        val customScriptMain = customLin {
            this@NonstopDebateMinigame.preScriptEntries.forEach(this::add)
            add(UnknownEntry(0x33, intArrayOf(9, 0, 0, 0)))

            if (coupledScript != null) {
                val (a, b, c) = coupledScript!!
                when (game) {
                    DR1 -> add(DR1RunScript(chapter ?: a, scene ?: b, room ?: c))
                    DR2 -> add(DR2RunScriptEntry(chapter ?: a, scene ?: b, room ?: c))
                    else -> TODO("RunScriptEntry is undocumented for $game")
                }
            }

            this@NonstopDebateMinigame.preTextEntries.forEach(this::add)

            val endOfScript = getLabel()

            add(ChangeUIEntry(31, 1))
            add(GoToLabelEntry.forGame(game, endOfScript))

            this@NonstopDebateMinigame.textSections.forEachIndexed { index, (_, _, correct, incorrect) ->
                this.add(UnknownEntry(0x2E, intArrayOf((0 + index) shr 8, (0 + index) and 0xFF)))
                this.add(ChangeUIEntry(1, 1))
                this.addAll(incorrect)
                this.add(ChangeUIEntry(1, 0))

                this.add(UnknownEntry(0x2E, intArrayOf((10000 + index) shr 8, (10000 + index) and 0xFF)))
                if (correct.isNotEmpty()) {
                    this.addAll(correct)
                } else {
                    when (game) {
                        DR1 -> {
                            this.add(VoiceLineEntry(0, 99, 69, 100))
                            this.add(AnimationEntry(512, 0, 0, 0, 0, 0, 1))
                            this.add(DR1TrialCameraEntry(0, 143))
                            this.add(ChangeUIEntry(31, 0))
                            this.add(AnimationEntry(512, 0, 0, 0, 0, 0, 255))
                            if (chapter != null && scene != null) {
                                this.add(DR1LoadScriptEntry(chapter, scene + 1, 0))
                                this.add(StopScriptEntry())
                            }
                        }
                    }
                }
                this.add(ChangeUIEntry(31, 0))
            }

            this@NonstopDebateMinigame.postTextEntries.forEach(this::add)

            add(UnknownEntry(0x2E, intArrayOf(255, 255)))
            add(SetLabelEntry.forGame(game, endOfScript))

            this@NonstopDebateMinigame.postScriptEntries.forEach(this::add)

            add(StopScriptEntry())
        }

        val customScriptDebate = customLin {
            add(ChangeUIEntry(21, debateNumber))
            add(ChangeUIEntry(22, 1))
            add(GoToLabelEntry.forGame(game, 0))
            this@NonstopDebateMinigame.textSections.forEachIndexed { index, (_, text) ->
                add(UnknownEntry(0x2E, intArrayOf((0 + index) shr 8, (0 + index) and 0xFF)))

                text?.let(this::addAll)

                when (game) {
                    DR1 -> add(WaitFrameEntry.DR1)
                    DR2 -> add(WaitFrameEntry.DR2)
                    else -> TODO("Add wait frame support for $game")
                }
            }

            add(UnknownEntry(0x2E, intArrayOf(255, 255)))
            add(SetLabelEntry.forGame(game, 0))
            add(UnknownEntry(0x1C, intArrayOf()))
            add(StopScriptEntry())
        }

        customNonstopDebate.apply {
            sections.clear()
            this@NonstopDebateMinigame.textSections.forEachIndexed { index, (section) ->
                section.textID = index
                this.section(section)
            }
        }

        return customScriptMain to customScriptDebate and customNonstopDebate
    }
}
