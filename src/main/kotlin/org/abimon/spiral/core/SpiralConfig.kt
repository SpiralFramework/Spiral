package org.abimon.spiral.core

data class SpiralConfig(var modName: String, var characters: Map<Int, String>) {
    companion object {
        val DR1 = SpiralConfig("DR1", mapOf(0 to "Makoto Naegi",
                1 to "Kiyotaka Ishimaru",
                2 to "Byakuya Togami",
                3 to "Mondo Owada",
                4 to "Leon Kuwata",
                5 to "Hifumi Yamada",
                6 to "Yasuhiro Hagakure",
                7 to "Sayaka Maizono",
                8 to "Kyoko Kirigi",
                9 to "Aoi Asahina",
                10 to "Toko Fukawa",
                11 to "Sakura Ogami",
                12 to "Celeste",
                13 to "Junko Enoshima",
                14 to "Chihiro Fujisaki",
                15 to "Monokuma",
                16 to "Real Junko Enoshima",
                17 to "Alter Ego",
                18 to "Genocider Syo",
                19 to "Jin Kirigiri",
                20 to "Makoto's Mum",
                21 to "Makoto's Dad",
                22 to "Komaru Naegi",
                23 to "Kiyondo Ishida",
                24 to "Daiya Owada",
                30 to "???"))
    }
}