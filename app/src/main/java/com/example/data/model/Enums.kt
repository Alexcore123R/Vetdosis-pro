package com.example.data.model

enum class ClinicalBranch(
    val id: String,
    val title: String,
    val shortName: String,
    val subtitle: String,
    val description: String
) {
    SMALL_ANIMALS(
        "small_animals",
        "Pequeñas Especies",
        "Pequeñas",
        "Caninos & Felinos",
        "Dosis, Sedación, Frecuencias y Cuidados de Perros y Gatos"
    ),
    EXOTICS(
        "exotics",
        "Fauna Silvestre & Exóticos",
        "Exóticos",
        "Aves, Roedores, Conejos, Reptiles, Hurones",
        "Metabolismo K·W^0.75, Dosis Microgramos, Alertas de Toxicidad"
    ),
    LARGE_PRODUCTION(
        "large_production",
        "Grandes Especies & Producción",
        "Producción",
        "Bovinos, Equinos, Ovinos/Caprinos, Porcinos",
        "Dosis por Lote/Cabeza, Medicación en Agua y Alimento, Tiempos de Retiro"
    ),
    CLINIC_SUITE(
        "clinic_suite",
        "Gestión Integral de Clínica",
        "Gestión",
        "Hospital, Recetas, Lotes, Citas, Libro COFEPRIS",
        "Herramientas administrativas, inventario, control legal y agenda"
    )
}

enum class AnimalSpecies(
    val displayName: String,
    val latinName: String,
    val defaultWeightKg: Double,
    val branch: ClinicalBranch = ClinicalBranch.SMALL_ANIMALS
) {
    CANINE("Canino (Perro)", "Canis lupus familiaris", 15.0, ClinicalBranch.SMALL_ANIMALS),
    FELINE("Felino (Gato)", "Felis catus", 4.0, ClinicalBranch.SMALL_ANIMALS),
    EQUINE("Equino (Caballo)", "Equus caballus", 450.0, ClinicalBranch.LARGE_PRODUCTION),
    BOVINE("Bovino (Vaca/Toro)", "Bos taurus", 500.0, ClinicalBranch.LARGE_PRODUCTION),
    OVINE_CAPRINE("Ovino / Caprino (Borrego/Chivo)", "Ovis aries / Capra hircus", 45.0, ClinicalBranch.LARGE_PRODUCTION),
    PORCINE("Porcino (Cerdo)", "Sus domesticus", 80.0, ClinicalBranch.LARGE_PRODUCTION),
    RABBIT("Conejo", "Oryctolagus cuniculus", 2.5, ClinicalBranch.EXOTICS),
    FERRET("Hurón", "Mustela putorius furo", 1.2, ClinicalBranch.EXOTICS),
    RODENT("Roedores (Cobayo / Hámster / Chinchilla)", "Cavia porcellus / Rodentia", 0.8, ClinicalBranch.EXOTICS),
    REPTILE("Reptiles (Tortugas / Iguanas / Serpientes)", "Reptilia", 1.5, ClinicalBranch.EXOTICS),
    BIRDS_EXOTICS("Aves / Psitácidas / Rapaces", "Aves / Exotica", 0.35, ClinicalBranch.EXOTICS)
}

enum class AdministrationFrequency(val code: String, val label: String, val intervalHours: Int) {
    SID("SID", "Cada 24 horas (1 vez al día)", 24),
    BID("BID", "Cada 12 horas (2 veces al día)", 12),
    TID("TID", "Cada 8 horas (3 veces al día)", 8),
    QID("QID", "Cada 6 horas (4 veces al día)", 6),
    SINGLE("Dosis Única", "Aplicación única / Preoperatorio", 0),
    PRN("SOS", "Según sea necesario (PRN)", 0)
}

enum class AdministrationRoute(val label: String) {
    ORAL("Vía Oral (PO)"),
    SUBCUTANEOUS("Subcutánea (SC)"),
    INTRAMUSCULAR("Intramuscular (IM)"),
    INTRAVENOUS("Intravenosa (IV)"),
    TOPICAL("Tópica"),
    OPHTHALMIC("Oftálmica"),
    OTIC("Ótica")
}

enum class VitalStatus {
    NORMAL,
    WARNING,
    ALERT_HIGH,
    ALERT_LOW
}

enum class HospitalStatus(val label: String) {
    STABLE("Estable"),
    UNDER_OBSERVATION("En Observación"),
    CRITICAL("Crítico / Cuidados Intensivos"),
    POST_OP("Postquirúrgico"),
    DISCHARGED("Alta Médica")
}

enum class AppColorTheme(val label: String) {
    EMERALD_VET("Verde Clínico Esmeralda"),
    OCEAN_TEAL("Azul Quirúrgico Océano"),
    ROYAL_INDIGO("Índigo Veterinario"),
    SUNSET_AMBER("Ámbar Cálido"),
    SURGICAL_DARK("Obsidiana Quirúrgica")
}

data class DoseCalculationResult(
    val drugName: String,
    val activeIngredient: String,
    val species: AnimalSpecies,
    val weightKg: Double,
    val doseMgKg: Double,
    val concentrationMgMl: Double,
    val totalDoseMg: Double,
    val totalVolumeMl: Double,
    val frequency: AdministrationFrequency,
    val route: AdministrationRoute,
    val durationDays: Int,
    val instructions: String,
    val warnings: String? = null
)

data class PhysiologicalRange(
    val species: AnimalSpecies,
    val hrMin: Int,
    val hrMax: Int,
    val rrMin: Int,
    val rrMax: Int,
    val tempMin: Double,
    val tempMax: Double,
    val crtMaxSec: Double = 2.0,
    val systolicBpMin: Int = 100,
    val systolicBpMax: Int = 150
)
