package com.example.data.model

import java.util.Locale
import kotlin.math.pow

// --------------------------------------------------------
// Calculadora Toxicológica de Chocolate (Teobromina)
// --------------------------------------------------------
enum class ChocolateType(val displayName: String, val theobromineMgPerGram: Double) {
    WHITE("Chocolate Blanco", 0.009), // Prácticamente inocuo
    MILK("Chocolate con Leche", 2.3),
    SEMI_SWEET("Chocolate Semiamargo (Dark 50%)", 5.5),
    DARK_HIGH("Chocolate Amargo Puro (70% - 85% Cacao)", 14.0),
    BAKING_UNSWEETENED("Chocolate de Repostería / Amargo sin azúcar", 16.0),
    COCOA_POWDER("Cacao en Polvo Puro / Cocoa", 26.0)
}

data class ChocolateToxicityResult(
    val totalTheobromineMg: Double,
    val mgPerKg: Double,
    val severityLevel: ToxicitySeverity,
    val clinicalSignsExpected: String,
    val treatmentProtocol: String
)

enum class ToxicitySeverity(val label: String) {
    BENIGN("Sin riesgo clínico significativo"),
    MILD("Leve: Signos gastrointestinales (Vómito, diarrea)"),
    MODERATE("Moderado: Cardiotóxico (Taquicardia, arritmias, hipertensión)"),
    SEVERE("SEVERO / CRÍTICO: Neurotóxico (Temblores, convulsiones, paro cardíaco)")
}

object EmergencyToxCalculator {

    fun calculateChocolateToxicity(
        dogWeightKg: Double,
        chocolateType: ChocolateType,
        gramsEaten: Double
    ): ChocolateToxicityResult {
        if (dogWeightKg <= 0 || gramsEaten <= 0) {
            return ChocolateToxicityResult(0.0, 0.0, ToxicitySeverity.BENIGN, "Sin datos", "Sin tratamiento")
        }

        val totalTheobromineMg = gramsEaten * chocolateType.theobromineMgPerGram
        val mgPerKg = totalTheobromineMg / dogWeightKg

        val (severity, signs, treatment) = when {
            mgPerKg < 20.0 -> Triple(
                ToxicitySeverity.BENIGN,
                "Dosis inferior a 20 mg/kg. Posible molestia estomacal leve por grasa o azúcar.",
                "Monitoreo en casa. Proporcionar agua abundante."
            )
            mgPerKg < 40.0 -> Triple(
                ToxicitySeverity.MILD,
                "Polidipsia, vómitos, diarrea y distensión abdominal.",
                "Inducción de vómito si ingesta ocurrió hace < 2 horas (Apomorfina o Agua Oxigenada 3% al 1-2 ml/kg). Carbón activado 1-2 g/kg VO."
            )
            mgPerKg < 60.0 -> Triple(
                ToxicitySeverity.MODERATE,
                "Taquicardia (>160-180 lpm), arritmias ventriculares, agitación motora e hipertensión.",
                "Hospitalización inmediata, fluidoterapia IV de soporte para diuresis rápida (la teobromina se reabsorbe en vejiga: colocar sonda urinaria), monitoreo ECG, beta-bloqueador (Atenolol/Metoprolol) si taquicardia severa."
            )
            else -> Triple(
                ToxicitySeverity.SEVERE,
                "¡EMERGENCIA VITAL! Hipertermia, temblores musculares, rigidez, convulsiones tónico-clónicas, arritmias ventriculares malignas y muerte.",
                "Control de convulsiones (Diazepam o Midazolam 0.5 mg/kg IV, o Levetiracetam/Propofol en infusión), soporte ventilatorio, sonda vesical a permanencia para evitar reabsorción urinaria, perfusión con Hartmann y carbón activado seriado."
            )
        }

        return ChocolateToxicityResult(
            totalTheobromineMg = totalTheobromineMg,
            mgPerKg = mgPerKg,
            severityLevel = severity,
            clinicalSignsExpected = signs,
            treatmentProtocol = treatment
        )
    }

    // --------------------------------------------------------
    // Algoritmo de RCP / Soporte Vital (Guías RECOVER)
    // --------------------------------------------------------
    data class CprDrugDose(
        val drugName: String,
        val concentrationMgMl: Double,
        val lowDoseMgKg: Double,
        val highDoseMgKg: Double,
        val route: String,
        val intervalMinutes: String,
        val clinicalPurpose: String
    ) {
        fun calculateVolumeMl(weightKg: Double, useHighDose: Boolean = false): Double {
            val dose = if (useHighDose) highDoseMgKg else lowDoseMgKg
            return (weightKg * dose) / concentrationMgMl
        }
    }

    val recoverCprDrugs = listOf(
        CprDrugDose(
            drugName = "Epinefrina / Adrenalina (1:1000)",
            concentrationMgMl = 1.0,
            lowDoseMgKg = 0.01, // Dosis baja estándar RECOVER: 0.01 mg/kg (0.01 ml/kg)
            highDoseMgKg = 0.1,  // Dosis alta tras 2 ciclos (>10 min): 0.1 mg/kg
            route = "IV / Intraósea (o 2x por tubo ET diluida)",
            intervalMinutes = "Cada 3 a 5 minutos (ciclos alternos)",
            clinicalPurpose = "Vasoconstricción periférica alfa-1 para redireccionar flujo a cerebro y corazón."
        ),
        CprDrugDose(
            drugName = "Atropina Sulfato 1 mg/ml",
            concentrationMgMl = 1.0,
            lowDoseMgKg = 0.04,
            highDoseMgKg = 0.04,
            route = "IV / IO / ET",
            intervalMinutes = "Dosis única o repetir 1 vez si asfixia/asistolia",
            clinicalPurpose = "Anticolinérgico para bloqueo vagal severo o bradiarritmias terminales."
        ),
        CprDrugDose(
            drugName = "Naloxona (Reversor de Opioides)",
            concentrationMgMl = 0.4,
            lowDoseMgKg = 0.04,
            highDoseMgKg = 0.04,
            route = "IV / IO / IM",
            intervalMinutes = "Dosis única de reversión",
            clinicalPurpose = "Revertir depresión respiratoria por fentanilo, tramadol, morfina o buprenorfina."
        ),
        CprDrugDose(
            drugName = "Atipamezol 5 mg/ml (Reversor Alfa-2)",
            concentrationMgMl = 5.0,
            lowDoseMgKg = 0.075,
            highDoseMgKg = 0.075,
            route = "IV lenta / IO en paro",
            intervalMinutes = "Dosis única de reversión",
            clinicalPurpose = "Revertir inmediatamente xilacina, dexmedetomidina o medetomidina."
        ),
        CprDrugDose(
            drugName = "Flumazenil 0.1 mg/ml (Reversor Benzodiacepinas)",
            concentrationMgMl = 0.1,
            lowDoseMgKg = 0.01,
            highDoseMgKg = 0.02,
            route = "IV / IO",
            intervalMinutes = "Dosis de reversión",
            clinicalPurpose = "Revertir depresión del SNC por midazolam o diazepam."
        )
    )

    // --------------------------------------------------------
    // Transfusión Sanguínea (Canina y Felina)
    // --------------------------------------------------------
    data class BloodTransfusionResult(
        val volumeWholeBloodMl: Double,
        val volumePackedRedCellsMl: Double,
        val maxInfusionRateFirst30MinMlHr: Double,
        val standardRateMlHr: Double,
        val instructions: String
    )

    fun calculateBloodTransfusion(
        species: AnimalSpecies,
        weightKg: Double,
        currentHematocritPercent: Double,
        targetHematocritPercent: Double = 25.0,
        donorHematocritPercent: Double = 40.0
    ): BloodTransfusionResult {
        // Factor de volemia: Perro 90 ml/kg, Gato 66 ml/kg
        val bloodVolumeFactor = if (species == AnimalSpecies.FELINE) 66.0 else 90.0
        val target = targetHematocritPercent.coerceIn(15.0, 35.0)
        val current = currentHematocritPercent.coerceIn(5.0, 30.0)
        val donor = if (donorHematocritPercent <= 0) 40.0 else donorHematocritPercent

        // Fórmula: Peso * Factor * (Ht_deseado - Ht_actual) / Ht_donante
        val wholeBloodMl = weightKg * bloodVolumeFactor * ((target - current) / donor)
        val packedRedCellsMl = wholeBloodMl * 0.60 // Aprox concentrado de eritrocitos

        // Velocidad: 0.25 - 0.5 ml/kg/h los primeros 30 min (revisar shock transfusional)
        val testRate = weightKg * 0.5
        // Luego 4-6 horas para pasar el volumen completo
        val regularRate = wholeBloodMl / 4.0

        val instructions = "Administrar a ${SafetyDoseGuard.formatSafe(testRate)} ml/h durante los primeros 30 minutos monitoreando temperatura, FC, FR y eritema/urticaria. Si es tolerado, aumentar a ${SafetyDoseGuard.formatSafe(regularRate)} ml/h para completar en 4 horas."

        return BloodTransfusionResult(
            volumeWholeBloodMl = wholeBloodMl,
            volumePackedRedCellsMl = packedRedCellsMl,
            maxInfusionRateFirst30MinMlHr = testRate,
            standardRateMlHr = regularRate,
            instructions = instructions
        )
    }

    // --------------------------------------------------------
    // Superficie Corporal (m^2) para Oncología / Quimioterapia
    // --------------------------------------------------------
    data class BodySurfaceAreaResult(
        val bsaM2: Double,
        val formulaUsed: String,
        val doxorubicinSampleDoseMg: Double,
        val vincristineSampleDoseMg: Double
    )

    fun calculateBsa(
        species: AnimalSpecies,
        weightKg: Double
    ): BodySurfaceAreaResult {
        val weightGrams = weightKg * 1000.0
        // K factor: Dogs = 10.1, Cats = 10.0
        val k = if (species == AnimalSpecies.FELINE) 10.0 else 10.1
        // BSA = (k * weightGrams^(2/3)) / 10,000
        val bsa = (k * weightGrams.pow(2.0 / 3.0)) / 10000.0

        // Doxorrubicina dosis estándar: 30 mg/m2 (en perros >10kg) o 1 mg/kg en pequeños
        val doxo = bsa * 30.0
        // Vincristina: 0.5 - 0.7 mg/m2
        val vinc = bsa * 0.7

        return BodySurfaceAreaResult(
            bsaM2 = bsa,
            formulaUsed = "BSA (m²) = ($k × Peso_gramos^(2/3)) / 10,000",
            doxorubicinSampleDoseMg = doxo,
            vincristineSampleDoseMg = vinc
        )
    }
}
