package info.spiralframework.base.util

data class SystemTarget(val os: OS, val arch: ARCH) {
    companion object {
        fun determineFromSystem(): SystemTarget = SystemTarget(OS.determineFromSystem(), ARCH.determineFromSystem())
    }

    enum class OS(val targetName: String) {
        WINDOWS("win"),
        MACOS("macos"),
        LINUX("linux"),
        OTHER("other");

        companion object {
            fun determineFromSystem(): OS {
                val osName = System.getProperty("os.name").toLowerCase()

                return when {
                    osName.startsWith("windows") -> WINDOWS
                    osName.startsWith("mac") -> MACOS
                    osName.startsWith("linux") -> LINUX
                    else -> OTHER
                }
            }
        }
    }

    enum class ARCH(val targetName: String) {
        X86("x86"),
        X86_64("x86_64"),
        ARM("arm"),
        ARM_64("arm_64"),
        MIPS("mips"),
        MIPS_64("mips_64"),
        JVM("jvm");

        companion object {
            fun determineFromSystem(): ARCH {
                val osArch = System.getProperty("os.arch").toLowerCase()

                return when {
                    osArch.startsWith("x86_64") ||
                            osArch.startsWith("amd64") ||
                            osArch.startsWith("ia64") -> X86_64
                    osArch.startsWith("arm64") -> ARM_64
                    osArch.startsWith("mips64") -> MIPS_64
                    osArch.startsWith("arm") -> ARM
                    osArch.startsWith("mips") -> MIPS
                    osArch.contains("86") -> X86
                    else -> JVM
                }
            }
        }
    }

    fun format(): String = "${os.targetName}:${arch.targetName}"
}