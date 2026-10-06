package com.example.data.model

data class PainQuestionItem(
    val title: String,
    val options: List<Pair<String, Int>> // Label to points
)

object PainScalesData {

    val felineGrimaceQuestions = listOf(
        PainQuestionItem(
            title = "1. Posición de las Orejas",
            options = listOf(
                "0 pts: Hacia el frente y erguidas (Relajado)" to 0,
                "1 pto: Ligeramente rotadas hacia los lados o hacia atrás" to 1,
                "2 pts: Aplanadas hacia atrás contra la cabeza (Dolor severo)" to 2
            )
        ),
        PainQuestionItem(
            title = "2. Tensión Ocular (Orbital Tightening)",
            options = listOf(
                "0 pts: Ojos abiertos y relajados" to 0,
                "1 pto: Ojos parcialmente cerrados (Entrecerrados)" to 1,
                "2 pts: Ojos fuertemente cerrados / Apretados (Dolor severo)" to 2
            )
        ),
        PainQuestionItem(
            title = "3. Tensión del Hocico (Muzzle Tension)",
            options = listOf(
                "0 pts: Hocico relajado, redondeado" to 0,
                "1 pto: Ligeramente tenso, elíptico" to 1,
                "2 pts: Fuertemente tenso, aplanado y alargado contra la mandíbula" to 2
            )
        ),
        PainQuestionItem(
            title = "4. Posición de los Bigotes",
            options = listOf(
                "0 pts: Bigotes sueltos, curvados naturalmente hacia abajo" to 0,
                "1 pto: Ligeramente curvados hacia adelante o rectos" to 1,
                "2 pts: Bigotes rectos, rígidamente proyectados hacia adelante" to 2
            )
        ),
        PainQuestionItem(
            title = "5. Posición de la Cabeza respecto a los Hombros",
            options = listOf(
                "0 pts: Cabeza por encima de la línea de los hombros" to 0,
                "1 pto: Cabeza alineada al mismo nivel que los hombros" to 1,
                "2 pts: Cabeza inclinada por debajo de los hombros o mentón apoyado en el pecho" to 2
            )
        )
    )

    val glasgowCanineQuestions = listOf(
        PainQuestionItem(
            title = "1. Postura y Comportamiento Espontáneo",
            options = listOf(
                "0 pts: Cómodo, postura normal, relajado" to 0,
                "1 pto: Inquieto, cambia de postura con frecuencia" to 1,
                "2 pts: Encogido, encorvado o postura de rezo" to 2,
                "3 pts: Rígido, tenso, rehúsa moverse" to 3
            )
        ),
        PainQuestionItem(
            title = "2. Vocalización",
            options = listOf(
                "0 pts: Silencioso / No vocaliza" to 0,
                "1 pto: Gimotea o suspira de forma ocasional" to 1,
                "2 pts: Llora o aúlla frecuentemente" to 2,
                "3 pts: Grita o aúlla constantemente sin cesar" to 3
            )
        ),
        PainQuestionItem(
            title = "3. Respuesta a la Palpación de la Herida o Área Dolorosa",
            options = listOf(
                "0 pts: No reacciona negativamente a la palpación suave" to 0,
                "1 pto: Se estremece o retira suavemente la extremidad/zona" to 1,
                "2 pts: Llora, gruñe o aparta con fuerza" to 2,
                "3 pts: Intenta morder o reacciona agresivamente al tacto" to 3
            )
        ),
        PainQuestionItem(
            title = "4. Estado de Ánimo y Atención",
            options = listOf(
                "0 pts: Alerta, mueve la cola o interactúa" to 0,
                "1 pto: Desinteresado, apático pero responde al llamado" to 1,
                "2 pts: Ansioso, temeroso, mira fijamente la herida" to 2,
                "3 pts: Deprimido o estuporoso, no responde a estímulos" to 3
            )
        ),
        PainQuestionItem(
            title = "5. Movilidad / Marcha",
            options = listOf(
                "0 pts: Camina con normalidad" to 0,
                "1 pto: Claudicación leve, marcha lenta o rígida" to 1,
                "2 pts: Claudicación severa, apoya apenas la extremidad" to 2,
                "3 pts: Rehúsa levantarse o incapacidad total para apoyarse" to 3
            )
        )
    )
}
