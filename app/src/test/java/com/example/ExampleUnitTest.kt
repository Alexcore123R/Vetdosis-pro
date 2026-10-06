package com.example

import com.example.data.model.*
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testSpeciesBranchAssignment() {
        assertEquals(ClinicalBranch.SMALL_ANIMALS, AnimalSpecies.CANINE.branch)
        assertEquals(ClinicalBranch.SMALL_ANIMALS, AnimalSpecies.FELINE.branch)
        assertEquals(ClinicalBranch.EXOTICS, AnimalSpecies.RABBIT.branch)
        assertEquals(ClinicalBranch.EXOTICS, AnimalSpecies.BIRDS_EXOTICS.branch)
        assertEquals(ClinicalBranch.LARGE_PRODUCTION, AnimalSpecies.BOVINE.branch)
        assertEquals(ClinicalBranch.LARGE_PRODUCTION, AnimalSpecies.EQUINE.branch)
    }

    @Test
    fun testProductionHerdDosing() {
        val res = ProductionCalculators.calculateHerdBatchDosing(
            headCount = 50,
            averageWeightKg = 400.0,
            doseMgKg = 10.0,
            concentrationMgMl = 200.0,
            bottleSizeMl = 250.0
        )

        assertEquals(20000.0, res.totalBiomassKg, 0.01)
        assertEquals(20.0, res.dosePerAnimalMl, 0.01)
        assertEquals(1000.0, res.totalVolumeNeededMl, 0.01)
        assertEquals(4, res.bottlesNeeded)
    }

    @Test
    fun testExoticMetabolicScaling() {
        val res = ExoticSafetyAndMetabolism.calculateMetabolicScaling(
            weightGrams = 1000.0, // 1 kg
            taxon = ExoticTaxonGroup.EXOTIC_MAMMALS
        )

        assertEquals(1.0, res.weightKg, 0.001)
        assertEquals(70.0, res.basalMetabolicRateKcalDay, 0.01)
        assertEquals(105.0, res.maintenanceEnergyKcalDay, 0.01)
        assertEquals(80.0, res.dailyFluidMaintenanceMl, 0.01)
    }

    @Test
    fun testWaterMedicationCalculation() {
        val res = ProductionCalculators.calculateWaterMedication(
            headCount = 100,
            averageWeightKg = 50.0,
            dailyWaterIntakeLitersPerHead = 5.0,
            tankVolumeLiters = 1000.0,
            targetPpmOrMgL = 100.0,
            productConcentrationPercent = 10.0
        )

        assertEquals(500.0, res.totalDailyWaterLiters, 0.01)
        assertEquals(1000.0, res.productNeededForTankGramsOrMl, 0.01)
        assertEquals(48.0, res.tankDurationHours, 0.01)
    }
}
