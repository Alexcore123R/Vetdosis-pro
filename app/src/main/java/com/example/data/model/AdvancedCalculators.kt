package com.example.data.model

import java.util.Locale
import kotlin.math.pow

object SafetyDoseGuard {

    fun checkDoseSafety(
        drugName: String,
        species: AnimalSpecies,
        weightKg: Double,
        enteredDoseMgKg: Double,
        minSafeMgKg: Double,
        maxSafeMgKg: Double
    ): DoseSafetyAlert {
        // Absolute check against extreme human input errors (e.g. 10x or 100x overdose)
        if (enteredDoseMgKg > maxSafeMgKg * 3.0 && maxSafeMgKg > 0) {
            return DoseSafetyAlert(
                status = DoseSafetyStatus.DANGER_EXCESSIVE,
                title = "¡ALERTA CRÍTICA: RIESGO DE INTOXICACIÓN O MUERTE!",
                message = "La dosis ingresada (${formatSafe(enteredDoseMgKg)} mg/kg) supera por más de 300% el límite máximo publicado (${formatSafe(maxSafeMgKg)} mg/kg). Verifica si hubo un error decimal.",
                isBlocked = true
            )
        }

        if (enteredDoseMgKg > maxSafeMgKg && maxSafeMgKg > 0) {
            return DoseSafetyAlert(
                status = DoseSafetyStatus.WARNING_HIGH,
                title = "Dosis superior al rango recomendado",
                message = "La dosis de ${formatSafe(enteredDoseMgKg)} mg/kg excede el tope de ${formatSafe(maxSafeMgKg)} mg/kg para ${species.displayName}. Puede causar efectos adversos.",
                isBlocked = false
            )
        }

        if (enteredDoseMgKg < minSafeMgKg && enteredDoseMgKg > 0) {
            return DoseSafetyAlert(
                status = DoseSafetyStatus.WARNING_LOW,
                title = "Dosis sub-terapéutica",
                message = "La dosis de ${formatSafe(enteredDoseMgKg)} mg/kg está por debajo del mínimo eficaz (${formatSafe(minSafeMgKg)} mg/kg). Riesgo de fracaso terapéutico o resistencia bacteriana.",
                isBlocked = false
            )
        }

        return DoseSafetyAlert(
            status = DoseSafetyStatus.SAFE,
            title = "Dosis en rango seguro",
            message = "La dosis se encuentra dentro de los márgenes terapéuticos estándar.",
            isBlocked = false
        )
    }

    fun checkSpeciesContraindications(drugName: String, species: AnimalSpecies, breed: String = ""): String? {
        val lowerDrug = drugName.lowercase()
        val lowerBreed = breed.lowercase()

        if (species == AnimalSpecies.FELINE && (lowerDrug.contains("permetrina") || lowerDrug.contains("piretroide"))) {
            return "¡CONTRAINDICACIÓN FATAL! Las permetrinas causan neurotoxicidad severa y convulsiones mortales en felinos."
        }
        if (species == AnimalSpecies.FELINE && lowerDrug.contains("carprofeno")) {
            return "No recomendado en felinos por alto riesgo de falla renal aguda y necrosis hepática."
        }
        if (species == AnimalSpecies.RABBIT && (lowerDrug.contains("amoxicilina") || lowerDrug.contains("ampicilina") || lowerDrug.contains("penicilina") || lowerDrug.contains("clindamicina"))) {
            return "¡CONTRAINDICACIÓN FATAL! Los betalactámicos y lincosamidas orales causan enterotoxemia por disbiosis mortal en conejos."
        }
        if (species == AnimalSpecies.CANINE && lowerDrug.contains("ivermectina") && (lowerBreed.contains("collie") || lowerBreed.contains("pastor australiano") || lowerBreed.contains("sheltie"))) {
            return "¡PRECAUCIÓN SEVERA! Razas tipo Collie presentan frecuentemente mutación del gen MDR-1 (ABCB1), causando neurotoxicidad letal con ivermectina."
        }

        return null
    }

    fun formatSafe(number: Double): String {
        return String.format(Locale.US, "%.2f", number)
    }
}

enum class DoseSafetyStatus {
    SAFE,
    WARNING_LOW,
    WARNING_HIGH,
    DANGER_EXCESSIVE
}

data class DoseSafetyAlert(
    val status: DoseSafetyStatus,
    val title: String,
    val message: String,
    val isBlocked: Boolean
)

// --------------------------------------------------------
// Fluidoterapia Completa (Mantenimiento, Deshidratación, Pérdidas)
// --------------------------------------------------------
object FluidTherapyCalculator {

    data class FluidResult(
        val maintenance24hMl: Double,
        val dehydrationDeficitMl: Double,
        val ongoingLossesMl: Double,
        val totalVolume24hMl: Double,
        val rateMlPerHour: Double,
        val microDropsPerMin: Double, // 60 gtt/ml (gotas/min = ml/h)
        val normoDropsPerMin: Double  // 20 gtt/ml (gotas/min = ml/h / 3)
    )

    fun calculateFluidPlan(
        weightKg: Double,
        species: AnimalSpecies,
        dehydrationPercent: Double, // e.g. 5.0, 8.0, 10.0%
        ongoingLossesMl: Double = 0.0,
        hoursToRehydrate: Int = 24
    ): FluidResult {
        // Maintenance calculation:
        // Canine: 60 ml/kg/day or 132 * kg^0.75
        // Feline: 45 ml/kg/day or 80 * kg^0.75
        val maintenanceRate = when (species) {
            AnimalSpecies.FELINE -> 45.0
            AnimalSpecies.EQUINE -> 50.0
            AnimalSpecies.BOVINE -> 50.0
            AnimalSpecies.RABBIT -> 80.0
            else -> 60.0
        }
        val maintenance24h = weightKg * maintenanceRate

        // Deficit = Weight (kg) * % * 10
        val deficit = weightKg * (dehydrationPercent.coerceIn(0.0, 15.0)) * 10.0

        val totalVolume = maintenance24h + deficit + ongoingLossesMl
        val hours = if (hoursToRehydrate <= 0) 24 else hoursToRehydrate
        val mlPerHour = totalVolume / hours

        val microDrops = mlPerHour // 1 ml/h = 1 microgota/min (con factor 60)
        val normoDrops = mlPerHour / 3.0 // factor 20 (gotas/min = (ml/h * 20) / 60 = ml/h / 3)

        return FluidResult(
            maintenance24hMl = maintenance24h,
            dehydrationDeficitMl = deficit,
            ongoingLossesMl = ongoingLossesMl,
            totalVolume24hMl = totalVolume,
            rateMlPerHour = mlPerHour,
            microDropsPerMin = microDrops,
            normoDropsPerMin = normoDrops
        )
    }
}

// --------------------------------------------------------
// CRI (Infusión Continua) en mcg/kg/min o mg/kg/h
// --------------------------------------------------------
object CriCalculator {

    data class CriResult(
        val totalDrugMgToAdd: Double,
        val totalDrugMlToAdd: Double,
        val infusionRateMlPerHour: Double,
        val dropsPerMinMicro: Double,
        val durationHoursOfBag: Double,
        val instructions: String
    )

    fun calculateCri(
        weightKg: Double,
        doseMcgKgMin: Double, // microgramos por kg por minuto
        bagVolumeMl: Double,  // tamaño bolsa suero en ml (ej 250, 500, 1000)
        fluidRateMlHr: Double, // velocidad deseada de infusión de fluidos ml/h
        drugConcentrationMgMl: Double // concentración fármaco mg/ml
    ): CriResult {
        if (fluidRateMlHr <= 0 || drugConcentrationMgMl <= 0 || weightKg <= 0) {
            return CriResult(0.0, 0.0, 0.0, 0.0, 0.0, "Datos insuficientes")
        }

        // Dose in mg per hour: (doseMcgKgMin * weightKg * 60) / 1000
        val doseMgPerHour = (doseMcgKgMin * weightKg * 60.0) / 1000.0

        // Bag duration in hours: bagVolumeMl / fluidRateMlHr
        val bagHours = bagVolumeMl / fluidRateMlHr

        // Total mg needed for the bag: doseMgPerHour * bagHours
        val totalMgToAdd = doseMgPerHour * bagHours

        // Total ml of drug needed: totalMgToAdd / drugConcentrationMgMl
        val totalMlToAdd = totalMgToAdd / drugConcentrationMgMl

        val microDrops = fluidRateMlHr

        val instructions = "Extraer ${SafetyDoseGuard.formatSafe(totalMlToAdd)} ml de la bolsa de suero de ${bagVolumeMl.toInt()} ml y agregar exactamente ${SafetyDoseGuard.formatSafe(totalMlToAdd)} ml (${SafetyDoseGuard.formatSafe(totalMgToAdd)} mg) del fármaco concentrado. Infundir a ${SafetyDoseGuard.formatSafe(fluidRateMlHr)} ml/h (${SafetyDoseGuard.formatSafe(microDrops)} microgotas/min)."

        return CriResult(
            totalDrugMgToAdd = totalMgToAdd,
            totalDrugMlToAdd = totalMlToAdd,
            infusionRateMlPerHour = fluidRateMlHr,
            dropsPerMinMicro = microDrops,
            durationHoursOfBag = bagHours,
            instructions = instructions
        )
    }
}

// --------------------------------------------------------
// Peso Metabólico en Exóticos (Alometría W^0.75)
// --------------------------------------------------------
object ExoticMetabolicCalculator {

    data class MetabolicResult(
        val metabolicWeightKg: Double,
        val standardDoseMg: Double,
        val allometricAdjustedDoseMg: Double,
        val volumeMl: Double,
        val explanation: String
    )

    fun calculateMetabolicDose(
        speciesType: String, // "Aves", "Reptiles", "Pequeños Mamíferos / Roedores"
        patientWeightGrams: Double,
        doseMgKg: Double,
        concentrationMgMl: Double
    ): MetabolicResult {
        val weightKg = patientWeightGrams / 1000.0
        val metabolicWeight = weightKg.pow(0.75)

        // Allometric scaling factor K
        // Birds: k ~ 1.2, Reptiles: k ~ 0.5 (lower basal metabolism), Rodents: k ~ 1.0
        val kFactor = when (speciesType) {
            "Reptiles" -> 0.6
            "Aves" -> 1.25
            else -> 1.0
        }

        val standardDoseMg = weightKg * doseMgKg
        val allometricDoseMg = (metabolicWeight * doseMgKg) * kFactor
        val volumeMl = if (concentrationMgMl > 0) allometricDoseMg / concentrationMgMl else 0.0

        val explanation = "En $speciesType (peso real: ${SafetyDoseGuard.formatSafe(patientWeightGrams)} g = ${SafetyDoseGuard.formatSafe(weightKg)} kg), el peso metabólico alométrico calculado es de ${SafetyDoseGuard.formatSafe(metabolicWeight)} kg^0.75 (factor k=$kFactor). Esto compensa la alta o baja tasa metabólica para evitar subdosificaciones o toxicidades."

        return MetabolicResult(
            metabolicWeightKg = metabolicWeight,
            standardDoseMg = standardDoseMg,
            allometricAdjustedDoseMg = allometricDoseMg,
            volumeMl = volumeMl,
            explanation = explanation
        )
    }
}

// --------------------------------------------------------
// Casos de Producción y Dosis Colectivas (Bovinos, Ovinos, Cerdos)
// --------------------------------------------------------
object HerdProductionCalculator {

    data class HerdDoseResult(
        val totalHeads: Int,
        val totalHerdWeightKg: Double,
        val dosePerHeadMg: Double,
        val volumePerHeadMl: Double,
        val totalDrugVolumeForHerdLiters: Double,
        val waterMedicationPpm: Double,
        val totalDrugForTankGrams: Double,
        val explanation: String
    )

    fun calculateHerdDose(
        headCount: Int,
        avgWeightKg: Double,
        doseMgKg: Double,
        concentrationMgMl: Double, // or mg/gram
        waterTankVolumeLiters: Double = 1000.0
    ): HerdDoseResult {
        val totalWeight = headCount * avgWeightKg
        val dosePerHeadMg = avgWeightKg * doseMgKg
        val volPerHeadMl = if (concentrationMgMl > 0) dosePerHeadMg / concentrationMgMl else 0.0
        val totalVolHerdMl = volPerHeadMl * headCount
        val totalVolHerdLiters = totalVolHerdMl / 1000.0

        // Estimated daily water intake per head is ~ 8-10% of body weight
        val waterIntakePerHeadLiters = avgWeightKg * 0.10
        val totalWaterHerdLiters = waterIntakePerHeadLiters * headCount

        // Total drug needed in water: total active mg needed in daily water
        val totalActiveGramsNeeded = (headCount * dosePerHeadMg) / 1000.0

        // Concentration in water tank (ppm = mg/L)
        val tankVolume = if (waterTankVolumeLiters <= 0) 1000.0 else waterTankVolumeLiters
        val gramsForTank = (totalActiveGramsNeeded / totalWaterHerdLiters) * tankVolume
        val ppm = (gramsForTank * 1000.0) / tankVolume

        val explanation = "Para un lote de $headCount cabezas (peso promedio ${SafetyDoseGuard.formatSafe(avgWeightKg)} kg, total ${SafetyDoseGuard.formatSafe(totalWeight)} kg): Cada animal requiere ${SafetyDoseGuard.formatSafe(volPerHeadMl)} ml individuales (${SafetyDoseGuard.formatSafe(dosePerHeadMg)} mg). Si se medica en el tinaco/tanque de $tankVolume L de agua de bebida, disolver ${SafetyDoseGuard.formatSafe(gramsForTank)} gramos de principio activo (${SafetyDoseGuard.formatSafe(ppm)} ppm)."

        return HerdDoseResult(
            totalHeads = headCount,
            totalHerdWeightKg = totalWeight,
            dosePerHeadMg = dosePerHeadMg,
            volumePerHeadMl = volPerHeadMl,
            totalDrugVolumeForHerdLiters = totalVolHerdLiters,
            waterMedicationPpm = ppm,
            totalDrugForTankGrams = gramsForTank,
            explanation = explanation
        )
    }
}
