package com.example.data.model

import kotlin.math.ceil
import kotlin.math.pow

// --- GRANDES ESPECIES & PRODUCCIÓN ---

data class WaterMedicationResult(
    val herdHeadCount: Int,
    val averageWeightKg: Double,
    val totalDailyWaterLiters: Double,
    val targetPpm: Double,
    val productConcentrationPercent: Double,
    val tankVolumeLiters: Double,
    val productNeededForTankGramsOrMl: Double,
    val totalProductNeededPerDayGramsOrMl: Double,
    val tankDurationHours: Double,
    val instructions: String
)

data class FeedMedicationResult(
    val herdHeadCount: Int,
    val averageWeightKg: Double,
    val totalFeedDailyKg: Double,
    val gramsPerTon: Double,
    val tonsToMix: Double,
    val totalPremixToMixKg: Double,
    val dailyActiveIngConsumedPerHeadMg: Double,
    val instructions: String
)

data class HerdBathDosingResult(
    val headCount: Int,
    val averageWeightKg: Double,
    val totalBiomassKg: Double,
    val dosePerAnimalMl: Double,
    val totalVolumeNeededMl: Double,
    val bottleSizeMl: Double,
    val bottlesNeeded: Int,
    val leftoversMl: Double
)

data class SenasicaWithdrawalInfo(
    val drugName: String,
    val targetSpecies: String,
    val meatWithdrawalDays: Int,
    val milkWithdrawalHoursOrDays: String,
    val senasicaRegulations: String
)

object ProductionCalculators {

    val withdrawalPeriods = listOf(
        SenasicaWithdrawalInfo(
            drugName = "Ivermectina 1% inyectable",
            targetSpecies = "Bovinos, Ovinos, Porcinos",
            meatWithdrawalDays = 28,
            milkWithdrawalHoursOrDays = "NO usar en vacas en lactación / 28 días",
            senasicaRegulations = "NOM-064-ZOO. Alta persistencia tisular en grasa y tejido subcutáneo."
        ),
        SenasicaWithdrawalInfo(
            drugName = "Oxitetraciclina L.A. 200 mg/ml",
            targetSpecies = "Bovinos, Porcinos, Ovinos",
            meatWithdrawalDays = 21,
            milkWithdrawalHoursOrDays = "7 días (168 horas)",
            senasicaRegulations = "Control SENASICA de residuos antibióticos en canal de exportación."
        ),
        SenasicaWithdrawalInfo(
            drugName = "Penicilina G Procaínica + Dihidroestreptomicina",
            targetSpecies = "Bovinos, Equinos, Porcinos",
            meatWithdrawalDays = 14,
            milkWithdrawalHoursOrDays = "72 horas (3 días)",
            senasicaRegulations = "Retención obligatoria de leche para consumo humano e industria láctea."
        ),
        SenasicaWithdrawalInfo(
            drugName = "Flunixin Meglumine 50 mg/ml",
            targetSpecies = "Bovinos, Equinos, Porcinos",
            meatWithdrawalDays = 4,
            milkWithdrawalHoursOrDays = "36 horas (3 ordeños)",
            senasicaRegulations = "AINE de rápido aclaramiento, respetar periodo en ordeño."
        ),
        SenasicaWithdrawalInfo(
            drugName = "Tilosina Tartrato / Fosfato",
            targetSpecies = "Porcinos, Bovinos, Aves",
            meatWithdrawalDays = 5,
            milkWithdrawalHoursOrDays = "96 horas (4 días)",
            senasicaRegulations = "Control estricto de micoplasmas con registro en bitácora de granja."
        ),
        SenasicaWithdrawalInfo(
            drugName = "Ceftiofur Sódico / RTU",
            targetSpecies = "Bovinos, Porcinos",
            meatWithdrawalDays = 4,
            milkWithdrawalHoursOrDays = "0 días (cero horas de retiro en leche a dosis indicada)",
            senasicaRegulations = "Cefalosporina de 3ra generación de uso exclusivo bajo supervisión MVZ."
        ),
        SenasicaWithdrawalInfo(
            drugName = "Albendazol 10% oral",
            targetSpecies = "Bovinos, Ovinos, Caprinos",
            meatWithdrawalDays = 14,
            milkWithdrawalHoursOrDays = "3 días (72 horas)",
            senasicaRegulations = "Contraindicado en primer tercio de gestación (efecto teratogénico)."
        ),
        SenasicaWithdrawalInfo(
            drugName = "Enrofloxacina 10%",
            targetSpecies = "Bovinos, Porcinos, Aves",
            meatWithdrawalDays = 7,
            milkWithdrawalHoursOrDays = "PROHIBIDO en vacas lecheras en producción",
            senasicaRegulations = "Fluoroquinolona de reserva regulada por SAGARPA/SENASICA."
        )
    )

    fun calculateWaterMedication(
        headCount: Int,
        averageWeightKg: Double,
        dailyWaterIntakeLitersPerHead: Double,
        tankVolumeLiters: Double,
        targetPpmOrMgL: Double,
        productConcentrationPercent: Double // e.g. 10% = 100 mg/ml or 100 g/kg
    ): WaterMedicationResult {
        val totalDailyWater = headCount * dailyWaterIntakeLitersPerHead
        // targetPpm = mg of active ingredient per liter of water
        // If productConcentrationPercent = 10%, 1 liter/kg contains 100,000 mg
        val activeMgPerTank = tankVolumeLiters * targetPpmOrMgL
        val activeMgPerMlOrGram = (productConcentrationPercent / 100.0) * 1000.0 * 1000.0 / 1000.0 // mg/ml
        val productForTank = if (activeMgPerMlOrGram > 0) activeMgPerTank / activeMgPerMlOrGram else 0.0

        val totalActiveDailyMg = totalDailyWater * targetPpmOrMgL
        val totalProductDaily = if (activeMgPerMlOrGram > 0) totalActiveDailyMg / activeMgPerMlOrGram else 0.0

        val hoursDuration = if (totalDailyWater > 0) (tankVolumeLiters / totalDailyWater) * 24.0 else 24.0

        val instructions = "Agregar %.1f ml o g de producto al tinaco de %.0f L. " +
                "El consumo estimado del hato (%d cabezas) es de %.0f L/día. " +
                "El tinaco durará aproximadamente %.1f horas."

        return WaterMedicationResult(
            herdHeadCount = headCount,
            averageWeightKg = averageWeightKg,
            totalDailyWaterLiters = totalDailyWater,
            targetPpm = targetPpmOrMgL,
            productConcentrationPercent = productConcentrationPercent,
            tankVolumeLiters = tankVolumeLiters,
            productNeededForTankGramsOrMl = productForTank,
            totalProductNeededPerDayGramsOrMl = totalProductDaily,
            tankDurationHours = hoursDuration,
            instructions = String.format(instructions, productForTank, tankVolumeLiters, headCount, totalDailyWater, hoursDuration)
        )
    }

    fun calculateFeedMedication(
        headCount: Int,
        averageWeightKg: Double,
        dailyFeedPerHeadKg: Double,
        gramsPerTon: Double,
        tonsToMix: Double,
        premixConcentrationPercent: Double = 100.0
    ): FeedMedicationResult {
        val totalFeedDailyKg = headCount * dailyFeedPerHeadKg
        // grams of active needed per ton
        val totalActiveGrams = tonsToMix * gramsPerTon
        val totalPremixKg = (totalActiveGrams / (premixConcentrationPercent / 100.0)) / 1000.0

        // daily mg per head
        val dailyKgPerHead = dailyFeedPerHeadKg
        val dailyActivePerHeadG = (dailyKgPerHead / 1000.0) * gramsPerTon
        val dailyActivePerHeadMg = dailyActivePerHeadG * 1000.0

        val instructions = "Mezclar %.2f kg de premezcla comercial por cada %.1f toneladas de alimento elaborado. " +
                "Asegurar premezclado previo con 20-50 kg de salvado o maíz molido antes de ingresar a la mezcladora principal."

        return FeedMedicationResult(
            herdHeadCount = headCount,
            averageWeightKg = averageWeightKg,
            totalFeedDailyKg = totalFeedDailyKg,
            gramsPerTon = gramsPerTon,
            tonsToMix = tonsToMix,
            totalPremixToMixKg = totalPremixKg,
            dailyActiveIngConsumedPerHeadMg = dailyActivePerHeadMg,
            instructions = String.format(instructions, totalPremixKg, tonsToMix)
        )
    }

    fun calculateHerdBatchDosing(
        headCount: Int,
        averageWeightKg: Double,
        doseMgKg: Double,
        concentrationMgMl: Double,
        bottleSizeMl: Double = 250.0
    ): HerdBathDosingResult {
        val totalBiomass = headCount * averageWeightKg
        val singleDoseMg = averageWeightKg * doseMgKg
        val singleDoseMl = if (concentrationMgMl > 0) singleDoseMg / concentrationMgMl else 0.0
        val totalVolumeNeeded = singleDoseMl * headCount

        val bottlesNeeded = if (bottleSizeMl > 0) ceil(totalVolumeNeeded / bottleSizeMl).toInt() else 1
        val leftovers = (bottlesNeeded * bottleSizeMl) - totalVolumeNeeded

        return HerdBathDosingResult(
            headCount = headCount,
            averageWeightKg = averageWeightKg,
            totalBiomassKg = totalBiomass,
            dosePerAnimalMl = singleDoseMl,
            totalVolumeNeededMl = totalVolumeNeeded,
            bottleSizeMl = bottleSizeMl,
            bottlesNeeded = bottlesNeeded,
            leftoversMl = leftovers
        )
    }
}

// --- FAUNA SILVESTRE & EXÓTICOS ---

enum class ExoticTaxonGroup(val label: String, val kConstant: Double, val dailyWaterMlKg: Double) {
    PASSERINE_BIRDS("Aves Paseriformes (Canarios, Diamantes)", 129.0, 100.0),
    NON_PASSERINE_BIRDS("Aves No Paseriformes (Loros, Palomas, Rapaces)", 78.0, 70.0),
    EXOTIC_MAMMALS("Mamíferos Pequeños (Hurones, Conejos, Cobayos)", 70.0, 80.0),
    REPTILES("Reptiles (Quelonios, Saurios, Ofidios)", 10.0, 30.0)
}

data class MetabolicScalingResult(
    val weightGrams: Double,
    val weightKg: Double,
    val taxon: ExoticTaxonGroup,
    val basalMetabolicRateKcalDay: Double,
    val maintenanceEnergyKcalDay: Double,
    val dailyFluidMaintenanceMl: Double,
    val scalingDoseFactor: Double,
    val explanation: String
)

data class ExoticToxicityAlert(
    val drugName: String,
    val contraindicatedSpecies: String,
    val severityLevel: String, // "CRÍTICO - MORTAL", "ALTO RIESGO", "PRECAUCIÓN"
    val pathophysiologicalMechanism: String,
    val saferAlternative: String
)

object ExoticSafetyAndMetabolism {

    val toxicityAlerts = listOf(
        ExoticToxicityAlert(
            drugName = "Penicilinas, Amoxicilina, Ampicilina, Cefalosporinas (Vía Oral)",
            contraindicatedSpecies = "Conejos, Cobayos (Cuyes), Chinchillas, Hámsters",
            severityLevel = "CRÍTICO - MORTAL",
            pathophysiologicalMechanism = "Destruyen la microbiota cecal Gram-positiva normal, provocando sobrecrecimiento fulminante de Clostridium difficile y Clostridium spiriforme con enterotoxemia hemorrágica y muerte en 24-72h.",
            saferAlternative = "Enrofloxacina, Trimetoprim-Sulfas (TMS), Marbofloxacina, Metronidazol."
        ),
        ExoticToxicityAlert(
            drugName = "Ivermectina",
            contraindicatedSpecies = "Quelonios (Tortugas terrestres, semiacuáticas y marinas)",
            severityLevel = "CRÍTICO - MORTAL",
            pathophysiologicalMechanism = "Atraviesa la barrera hematoencefálica en tortugas por afinidad selectiva al receptor GABA, causando parálisis flácida ascendente, paro respiratorio irreversible y muerte.",
            saferAlternative = "Fenbendazol (20-50 mg/kg PO) para nemátodos; Prazicuantel para céstodos/tremátodos."
        ),
        ExoticToxicityAlert(
            drugName = "Fipronil (Pipetas / Spray)",
            contraindicatedSpecies = "Conejos",
            severityLevel = "CRÍTICO - MORTAL",
            pathophysiologicalMechanism = "Provoca neurotoxicidad severa con convulsiones epilépticas continuas, hipotermia, anorexia y alta tasa de letalidad en lagomorfos.",
            saferAlternative = "Selamectina (Revolution) 6-12 mg/kg o Imidacloprid (Advantage) tópico."
        ),
        ExoticToxicityAlert(
            drugName = "Gentamicina / Amikacina (Aminoglucósidos)",
            contraindicatedSpecies = "Reptiles deshidratados o con nefropatía",
            severityLevel = "ALTO RIESGO",
            pathophysiologicalMechanism = "Necrosis tubular aguda severa. Precipita gota articular y visceral masiva por ácido úrico en reptiles sin hiperhidratación previa.",
            saferAlternative = "Ceftazidima (20-22 mg/kg cada 72h a 25-30°C) con fluidoterapia previa obligatoria."
        ),
        ExoticToxicityAlert(
            drugName = "Prednisona / Dexametasona (Glucocorticoides)",
            contraindicatedSpecies = "Conejos y Hurones",
            severityLevel = "ALTO RIESGO",
            pathophysiologicalMechanism = "Los conejos son extremadamente corticoides-sensibles: inmunosupresión profunda inmediata, reactivación fulminante de Encephalitozoon cuniculi y Pasteurella multocida.",
            saferAlternative = "Meloxicam (0.5 a 1.0 mg/kg PO cada 12-24h en conejos)."
        )
    )

    fun calculateMetabolicScaling(
        weightGrams: Double,
        taxon: ExoticTaxonGroup
    ): MetabolicScalingResult {
        val weightKg = weightGrams / 1000.0
        // Kleiber equation: BMR = K * (WeightKg)^0.75
        val bmrKcal = taxon.kConstant * (weightKg.pow(0.75))
        // Maintenance energy = 1.5 * BMR for hospitalized/sick patient
        val merKcal = bmrKcal * 1.5

        // Daily fluid requirement (ml/day)
        val dailyFluid = weightKg * taxon.dailyWaterMlKg

        // Scaling factor relative to 1kg standard animal
        val scalingFactor = weightKg.pow(0.75) / weightKg

        val explanation = "Debido a su alta relación superficie/volumen corporal, los animales de menor tamaño metabolizan y eliminan fármacos a una tasa proporcionalmente mucho más acelerada. " +
                "El requerimiento basal calculado es de %.1f kcal/día y volumen de fluidos de %.1f ml/día."

        return MetabolicScalingResult(
            weightGrams = weightGrams,
            weightKg = weightKg,
            taxon = taxon,
            basalMetabolicRateKcalDay = bmrKcal,
            maintenanceEnergyKcalDay = merKcal,
            dailyFluidMaintenanceMl = dailyFluid,
            scalingDoseFactor = scalingFactor,
            explanation = String.format(explanation, bmrKcal, dailyFluid)
        )
    }
}
