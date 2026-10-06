package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class PatientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val species: String, // String representation of AnimalSpecies
    val breed: String = "",
    val ageMonths: Int = 12,
    val weightKg: Double,
    val sex: String = "Macho",
    val ownerName: String = "",
    val ownerPhone: String = "",
    val allergies: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "patient_weights")
data class PatientWeightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val weightKg: Double,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "vaccination_records")
data class VaccinationRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val patientName: String,
    val treatmentType: String, // "Vacuna Múltiple", "Rabia", "Desparasitación Interna", "Antipulgas/Garrapatas"
    val appliedDate: String,
    val nextDueDate: String,
    val batchNumber: String = "",
    val isDone: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "consultation_history")
data class ConsultationHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val reason: String,
    val templateType: String = "GENERAL", // "GENERAL", "DERMATOLOGIA", "OFTALMOLOGIA", "URGENCIA", "EXOTICOS"
    val diagnosis: String,
    val treatment: String,
    val weightAtConsultKg: Double,
    val heartRate: Int? = null,
    val respRate: Int? = null,
    val temperature: Double? = null,
    val crtSeconds: Double? = null,
    val soapSubjective: String = "",
    val soapObjective: String = "",
    val soapAssessment: String = "",
    val soapPlan: String = "",
    val soapNotes: String = "",
    val isAudioTranscribed: Boolean = false
)

@Entity(tableName = "drugs")
data class DrugEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val activeIngredient: String,
    val mexicanTradeNames: String, // Comma separated, e.g. "Meloxivet, Pet's Pain, Meloxic"
    val targetSpecies: String, // Comma separated species codes
    val minDoseMgKg: Double,
    val maxDoseMgKg: Double,
    val defaultDoseMgKg: Double,
    val concentrationMgMl: Double, // mg/ml or mg/unit
    val unit: String = "mg/ml",
    val route: String = "PO",
    val recommendedIntervalHours: Int = 24,
    val contraindications: String = "",
    val notes: String = "",
    val isCustom: Boolean = false,
    val isUserPersonal: Boolean = false, // Etiqueta "dosis personal"
    val isApprovedByAdmin: Boolean = true, // Admin approval for clinic sharing
    val createdByStaff: String = "Sistema General"
)

@Entity(tableName = "hospital_patients")
data class HospitalPatientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long = 0,
    val patientName: String,
    val species: String,
    val cageBox: String, // e.g. "Jaula 3", "Box A"
    val diagnosis: String,
    val medicationName: String,
    val doseDescription: String, // e.g. "1.5 ml (Ceftriaxona 100mg/ml)"
    val scheduledTime: String, // e.g. "14:00"
    val nextDoseTimestamp: Long,
    val isDue: Boolean = false,
    val isDone: Boolean = false,
    val status: String = "UNDER_OBSERVATION",
    val notes: String = ""
)

@Entity(tableName = "treatment_sheet_records")
data class TreatmentSheetRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hospitalPatientId: Long,
    val patientName: String,
    val cageBox: String,
    val medicationName: String,
    val doseMl: String,
    val route: String,
    val hourSlot: String, // "08:00", "12:00", "16:00", "20:00", "00:00", "04:00"
    val isDone: Boolean = false,
    val administeredBy: String = "",
    val administeredTimestamp: Long = 0L,
    val notes: String = ""
)

@Entity(tableName = "inventory_products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // "Fármaco", "Alimento", "Accesorio", "Material Quirúrgico", "Psicotrópico Controlado"
    val barcode: String = "",
    val batchNumber: String = "L-2026-A1", // Lote
    val stock: Int,
    val minStockAlert: Int = 5,
    val unitCost: Double,
    val unitPrice: Double,
    val expiryDate: String = "2027-12-31", // YYYY-MM-DD
    val daysUntilExpiry: Int = 365,
    val isControlled: Boolean = false, // COFEPRIS / SENASICA
    val controlledGroup: String = "NORMAL", // "NORMAL", "GRUPO_I_ESTUPEFACIENTE", "GRUPO_II_ANTIBIOTICO", "GRUPO_III_PSICOTROPICO"
    val supplierName: String = "",
    val supplierPhone: String = "",
    val imagePath: String = ""
)

@Entity(tableName = "controlled_substance_logs")
data class ControlledSubstanceLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val folioRecipe: String, // Folio oficial COFEPRIS/SENASICA
    val drugName: String,
    val activeIngredient: String,
    val batchNumber: String,
    val patientName: String,
    val ownerName: String,
    val doctorName: String,
    val doctorLicense: String,
    val quantityDispensed: Double, // en ml o piezas
    val balanceRemaining: Double,
    val groupType: String, // "GRUPO_II_ANTIBIOTICO", "GRUPO_III_PSICOTROPICO", "GRUPO_I_ESTUPEFACIENTE"
    val notes: String = ""
)

@Entity(tableName = "informed_consents")
data class InformedConsentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val patientName: String,
    val species: String,
    val ownerName: String,
    val ownerIdCard: String = "",
    val procedureType: String, // "CIRUGIA_ANESTESIA", "EUTANASIA_HUMANITARIA", "HOSPITALIZACION_CRITICA"
    val diagnosisDescription: String,
    val doctorName: String,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val hasDigitalSignature: Boolean = true,
    val signatureNotes: String = "Firmado conforme en pantalla táctil de VetDosis Pro"
)

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val totalAmount: Double,
    val totalCost: Double,
    val paymentMethod: String = "Efectivo", // "Efectivo", "Tarjeta", "Transferencia"
    val itemsSummary: String, // e.g. "Meloxivet 50ml x 2, Consulta general"
    val clientName: String = ""
)

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientName: String,
    val species: String,
    val ownerName: String,
    val ownerPhone: String,
    val dateString: String, // e.g. "2026-10-10"
    val timeString: String, // e.g. "10:30"
    val reason: String, // "Vacunación", "Cirugía", "Consulta", "Desparasitación"
    val status: String = "Pendiente", // "Pendiente", "Confirmada", "Atendida", "Cancelada"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "staff_emergency_profiles")
data class StaffEmergencyProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val role: String, // "Director Médico", "Médico Veterinario", "Estilista Canino", "Asistente Clínico"
    val age: Int = 30,
    val phone: String,
    val address: String,
    val bloodType: String = "O+", // O+, A+, B+, AB+, O-, A-, B-, AB-
    val allergies: String = "Ninguna conocida",
    val preexistingConditions: String = "Ninguna",
    val emergencyContactName: String,
    val emergencyContactPhone: String,
    val emergencyContactRelationship: String = "Familiar",
    val medicalInsuranceOrNotes: String = "IMSS / Seguro de Gastos Médicos. Vacuna antirrábica al corriente."
)

@Entity(tableName = "clinic_tasks")
data class ClinicTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: String = "NORMAL", // "ALTA", "MEDIA", "NORMAL"
    val assignedToStaff: String = "Todos",
    val createdByStaff: String = "Admin",
    val dueDate: String = "Hoy",
    val isCompleted: Boolean = false,
    val completedByStaff: String = "",
    val completedTimestamp: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "clinic_team_messages")
data class ClinicTeamMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderName: String,
    val senderRole: String,
    val message: String,
    val category: String = "GENERAL", // "URGENTE", "AVISO", "GENERAL", "TURNO"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "grooming_appointments")
data class GroomingAppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientName: String,
    val species: String = "Canino",
    val breed: String = "",
    val ownerName: String = "",
    val ownerPhone: String = "",
    val serviceType: String, // "Baño Básico & Uñas", "Baño Medicado", "Corte de Raza", "Deslanado"
    val assignedGroomerStaff: String,
    val dateString: String,
    val timeString: String,
    val skinObservations: String = "Sin lesiones cutáneas aparentes",
    val cost: Double = 350.0,
    val groomerCommission: Double = 140.0, // Monto para el estilista
    val status: String = "Programada", // "Programada", "En Baño / Secado", "Terminada", "Cancelada"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "clinic_services")
data class ClinicServiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // "CONSULTA", "CIRUGIA", "ESTETICA", "LABORATORIO", "PREVENTIVO", "HOSPITAL"
    val description: String = "",
    val standardPrice: Double,
    val defaultStaffCommissionType: String = "FIXED", // "FIXED", "PERCENTAGE"
    val defaultStaffCommissionValue: Double = 100.0, // Pesos o porcentaje
    val isCustom: Boolean = false
)

@Entity(tableName = "staff_performance_commissions")
data class StaffPerformanceCommissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val staffName: String,
    val serviceName: String,
    val serviceCategory: String,
    val patientName: String,
    val totalBilledAmount: Double,
    val commissionEarned: Double,
    val notes: String = "",
    val dateTimestamp: Long = System.currentTimeMillis(),
    val isPaidOut: Boolean = false
)
