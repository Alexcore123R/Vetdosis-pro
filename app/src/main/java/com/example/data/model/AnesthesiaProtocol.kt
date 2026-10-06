package com.example.data.model

data class ProtocolDrug(
    val drugName: String,
    val doseMgKg: Double,
    val concentrationMgMl: Double,
    val route: String,
    val timing: String, // e.g., "15 min antes", "Al inicio", "Postoperatorio"
    val clinicalPurpose: String,
    val notes: String = "",
    val isReverser: Boolean = false,
    val reverserForDrug: String? = null
) {
    fun calculateVolumeMl(weightKg: Double): Double {
        if (concentrationMgMl <= 0) return 0.0
        val totalMg = weightKg * doseMgKg
        return totalMg / concentrationMgMl
    }
}

data class ProtocolStage(
    val stageName: String, // "Premedicación / Sedación", "Inducción", "Mantenimiento / Analgesia Intraoperatoria", "Recuperación / Reversión"
    val description: String,
    val drugs: List<ProtocolDrug>
)

data class AnesthesiaProtocol(
    val id: String,
    val title: String,
    val targetSpecies: AnimalSpecies,
    val indication: String,
    val riskCategory: String, // "ASA I - II", "ASA III - V", etc.
    val stages: List<ProtocolStage>,
    val generalWarnings: String
)

object PredefinedProtocols {
    val allProtocols = listOf(
        AnesthesiaProtocol(
            id = "canine_sed_ket_xil",
            title = "Sedación y Anestesia Quirúrgica Corta (Ketamina + Xilacina)",
            targetSpecies = AnimalSpecies.CANINE,
            indication = "Suturas, limpieza dental, debridación de heridas, procedimientos diagnósticos menores (30-45 min).",
            riskCategory = "ASA I - II (Pacientes sanos)",
            stages = listOf(
                ProtocolStage(
                    stageName = "Premedicación / Sedación",
                    description = "Relajación muscular y sedación basal con agonista alfa-2.",
                    drugs = listOf(
                        ProtocolDrug(
                            drugName = "Xilacina 2%",
                            doseMgKg = 1.0,
                            concentrationMgMl = 20.0,
                            route = "IM / IV lenta",
                            timing = "10-15 min antes del procedimiento",
                            clinicalPurpose = "Sedación profunda y miorrelajación",
                            notes = "Monitorear bradicardia. En IV usar 0.5 mg/kg."
                        ),
                        ProtocolDrug(
                            drugName = "Atropina Sulfato (Opcional)",
                            doseMgKg = 0.02,
                            concentrationMgMl = 1.0,
                            route = "SC / IM",
                            timing = "Previa si FC < 55 lpm",
                            clinicalPurpose = "Control de bradicardia excesiva y secreciones"
                        )
                    )
                ),
                ProtocolStage(
                    stageName = "Inducción Anestésica",
                    description = "Pérdida de reflejos protectores y analgesia disociativa.",
                    drugs = listOf(
                        ProtocolDrug(
                            drugName = "Ketamina 10%",
                            doseMgKg = 5.0,
                            concentrationMgMl = 100.0,
                            route = "IV lenta / IM",
                            timing = "Una vez sedado con Xilacina",
                            clinicalPurpose = "Inconsciencia y analgesia somática",
                            notes = "Duración anestésica 25-40 min."
                        )
                    )
                ),
                ProtocolStage(
                    stageName = "Analgesia Intraoperatoria",
                    description = "Refuerzo analgésico preventivo postquirúrgico.",
                    drugs = listOf(
                        ProtocolDrug(
                            drugName = "Meloxicam 5%",
                            doseMgKg = 0.2,
                            concentrationMgMl = 5.0,
                            route = "SC",
                            timing = "Intraoperatorio (con paciente normotenso)",
                            clinicalPurpose = "Control del dolor e inflamación postquirúrgica"
                        )
                    )
                ),
                ProtocolStage(
                    stageName = "Recuperación y Reversión",
                    description = "Retorno suave y reversión farmacológica si se requiere acelerar el despertar.",
                    drugs = listOf(
                        ProtocolDrug(
                            drugName = "Yohimbina (Reversor Xilacina)",
                            doseMgKg = 0.1,
                            concentrationMgMl = 2.0,
                            route = "IV lenta",
                            timing = "Al finalizar si se requiere despertar inmediato",
                            clinicalPurpose = "Antagonista alfa-2",
                            isReverser = true,
                            reverserForDrug = "Xilacina 2%"
                        )
                    )
                )
            ),
            generalWarnings = "Contraindicado en pacientes cardiópatas o con presión intracraneal/ocular elevada. Proporcionar oxígeno suplementario por mascarilla."
        ),

        AnesthesiaProtocol(
            id = "canine_feline_dex_ati",
            title = "Protocolo Reversible Seguro (Dexmedetomidina + Atipamezol)",
            targetSpecies = AnimalSpecies.CANINE,
            indication = "Exámenes radiológicos, ecografías, canalización, toma de biopsias superficiales.",
            riskCategory = "ASA I - II",
            stages = listOf(
                ProtocolStage(
                    stageName = "Sedación y Analgesia",
                    description = "Alfa-2 agonista de alta selectividad con mínima irritación.",
                    drugs = listOf(
                        ProtocolDrug(
                            drugName = "Dexmedetomidina (Dexdomitor)",
                            doseMgKg = 0.015, // 15 mcg/kg
                            concentrationMgMl = 0.5,
                            route = "IM / IV lenta",
                            timing = "5-10 min antes",
                            clinicalPurpose = "Sedación multimodal profunda y analgesia visceral",
                            notes = "Genera vasoconstricción periférica y bradicardia refleja fisiológica."
                        ),
                        ProtocolDrug(
                            drugName = "Butorfanol 1%",
                            doseMgKg = 0.2,
                            concentrationMgMl = 10.0,
                            route = "IM / IV",
                            timing = "Simultáneo con Dexmedetomidina",
                            clinicalPurpose = "Potenciación sedante y sinergia analgésica"
                        )
                    )
                ),
                ProtocolStage(
                    stageName = "Mantenimiento / Monitoreo",
                    description = "Vigilancia de oxigenación, temperatura y pulso.",
                    drugs = emptyList()
                ),
                ProtocolStage(
                    stageName = "Recuperación y Reversión Específica",
                    description = "Antagonización completa con Atipamezol en relación volumen 1:1.",
                    drugs = listOf(
                        ProtocolDrug(
                            drugName = "Atipamezol (Antisedan)",
                            doseMgKg = 0.075, // Proporción 5:1 mg respecto a dexmedetomidina
                            concentrationMgMl = 5.0,
                            route = "IM estricta",
                            timing = "Al concluir el procedimiento (despertar en 3-7 min)",
                            clinicalPurpose = "Reversor específico de Dexmedetomidina",
                            isReverser = true,
                            reverserForDrug = "Dexmedetomidina",
                            notes = "¡Mismo volumen en ml que la Dexmedetomidina aplicada!"
                        )
                    )
                )
            ),
            generalWarnings = "No administrar Atipamezol por vía intravenosa rápida. Proteger al paciente de corrientes de aire por riesgo de hipotermia."
        ),

        AnesthesiaProtocol(
            id = "feline_tiva_mida_prop",
            title = "Protocolo Felino TIVA Suave (Midazolam + Ketamina + Propofol)",
            targetSpecies = AnimalSpecies.FELINE,
            indication = "Esterilizaciones (OVH/Orquiectomía), laparotomías exploratorias, manejo felino de difícil temperamento.",
            riskCategory = "ASA I - III",
            stages = listOf(
                ProtocolStage(
                    stageName = "Premedicación (Kitty Magic Suave)",
                    description = "Relajación sin excitación ni vómito excesivo.",
                    drugs = listOf(
                        ProtocolDrug(
                            drugName = "Midazolam",
                            doseMgKg = 0.2,
                            concentrationMgMl = 5.0,
                            route = "IM",
                            timing = "15 min antes",
                            clinicalPurpose = "Benzodiacepina: miorrelajante y tranquilizante"
                        ),
                        ProtocolDrug(
                            drugName = "Tramadol",
                            doseMgKg = 2.0,
                            concentrationMgMl = 50.0,
                            route = "SC / IM",
                            timing = "Junto con premedicación",
                            clinicalPurpose = "Analgesia preventiva perioperatoria"
                        )
                    )
                ),
                ProtocolStage(
                    stageName = "Inducción Anestésica",
                    description = "Inducción suave para intubación endotraqueal segura.",
                    drugs = listOf(
                        ProtocolDrug(
                            drugName = "Propofol 1%",
                            doseMgKg = 4.0,
                            concentrationMgMl = 10.0,
                            route = "IV lenta (a efecto en 60 seg)",
                            timing = "Previo a intubación",
                            clinicalPurpose = "Inductor hipnótico de recuperación rápida",
                            notes = "Rociar previamente 0.1ml lidocaína en cuerdas vocales para evitar laringoespasmo."
                        )
                    )
                ),
                ProtocolStage(
                    stageName = "Analgesia y Mantenimiento",
                    description = "Mantenimiento con bolos reducidos de propofol o anestesia inhalatoria.",
                    drugs = listOf(
                        ProtocolDrug(
                            drugName = "Meloxicam Felino",
                            doseMgKg = 0.05,
                            concentrationMgMl = 5.0,
                            route = "SC",
                            timing = "Fin de cirugía (solo si normotenso e hidratado)",
                            clinicalPurpose = "AINE seguro a dosis ajustada felina"
                        )
                    )
                ),
                ProtocolStage(
                    stageName = "Recuperación",
                    description = "Ambiente tranquilo, tibio y con baja iluminación para felinos.",
                    drugs = emptyList()
                )
            ),
            generalWarnings = "Los gatos son propensos a hipotermia rápida y laringoespasmo. Mantener manta térmica y monitor de saturación SpO2."
        ),

        AnesthesiaProtocol(
            id = "equine_field_triple_drip",
            title = "Protocolo Equino a Campo (Xilacina + Diazepam/Midazolam + Ketamina)",
            targetSpecies = AnimalSpecies.EQUINE,
            indication = "Castración a campo, sutura de laceraciones cutáneas, remoción de masas.",
            riskCategory = "ASA I - II (Equinos de campo)",
            stages = listOf(
                ProtocolStage(
                    stageName = "Premedicación y Sedación",
                    description = "Sedación profunda y descenso seguro de la cabeza.",
                    drugs = listOf(
                        ProtocolDrug(
                            drugName = "Xilacina Equus 10%",
                            doseMgKg = 1.1,
                            concentrationMgMl = 100.0,
                            route = "IV lenta",
                            timing = "5 min antes de inducción",
                            clinicalPurpose = "Sedación, analgesia visceral y descenso seguro",
                            notes = "Esperar a que baje la cabeza hasta nivel de las rodillas antes de inducir."
                        )
                    )
                ),
                ProtocolStage(
                    stageName = "Inducción y Derribo",
                    description = "Derribo suave y controlado con relajante y disociativo.",
                    drugs = listOf(
                        ProtocolDrug(
                            drugName = "Diazepam o Midazolam",
                            doseMgKg = 0.05,
                            concentrationMgMl = 5.0,
                            route = "IV",
                            timing = "Inmediatamente antes de Ketamina",
                            clinicalPurpose = "Miorrelajación central para evitar caída rígida"
                        ),
                        ProtocolDrug(
                            drugName = "Ketamina 10%",
                            doseMgKg = 2.2,
                            concentrationMgMl = 100.0,
                            route = "IV bolo en 10 seg",
                            timing = "Post benzodiacepina",
                            clinicalPurpose = "Inducción quirúrgica (derribo en 60-90 seg)",
                            notes = "Brinda 15-20 minutos de tiempo quirúrgico útil."
                        )
                    )
                ),
                ProtocolStage(
                    stageName = "Analgesia y Mantenimiento",
                    description = "AINE antiinflamatorio para cólicos y dolor musculoesquelético.",
                    drugs = listOf(
                        ProtocolDrug(
                            drugName = "Flunixin Meglumine 5%",
                            doseMgKg = 1.1,
                            concentrationMgMl = 50.0,
                            route = "IV",
                            timing = "Intraoperatorio",
                            clinicalPurpose = "Control analgésico antiinflamatorio potente"
                        )
                    )
                ),
                ProtocolStage(
                    stageName = "Recuperación",
                    description = "Recuperación en box acolchonado o pasto limpio sin estímulos sonoros.",
                    drugs = emptyList()
                )
            ),
            generalWarnings = "Asegurar que el caballo esté en un terreno plano y seguro sin obstáculos. Mantener vendaje ocular para evitar fotofobia durante el despertar."
        )
    )
}
