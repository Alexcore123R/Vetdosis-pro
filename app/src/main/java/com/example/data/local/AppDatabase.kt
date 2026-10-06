package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.*
import com.example.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PatientEntity::class,
        PatientWeightEntity::class,
        VaccinationRecordEntity::class,
        ConsultationHistoryEntity::class,
        DrugEntity::class,
        HospitalPatientEntity::class,
        TreatmentSheetRecordEntity::class,
        ProductEntity::class,
        ControlledSubstanceLogEntity::class,
        InformedConsentEntity::class,
        SaleEntity::class,
        AppointmentEntity::class,
        StaffEmergencyProfileEntity::class,
        ClinicTaskEntity::class,
        ClinicTeamMessageEntity::class,
        GroomingAppointmentEntity::class,
        ClinicServiceEntity::class,
        StaffPerformanceCommissionEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun drugDao(): DrugDao
    abstract fun hospitalDao(): HospitalDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun staffDao(): StaffDao
    abstract fun clinicOperationsDao(): ClinicOperationsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vet_dosis_database.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val drugDao = database.drugDao()
            val inventoryDao = database.inventoryDao()
            val patientDao = database.patientDao()
            val hospitalDao = database.hospitalDao()
            val appointmentDao = database.appointmentDao()

            if (drugDao.getDrugCount() == 0) {
                drugDao.insertAllDrugs(PrepopulatedData.defaultDrugs)
            }
            if (inventoryDao.getProductCount() == 0) {
                inventoryDao.insertAllProducts(PrepopulatedData.defaultProducts)
            }

            // Initial demo patient with weights and vaccines
            val patientId = patientDao.insertPatient(
                PatientEntity(
                    name = "Rocky",
                    species = "CANINE",
                    breed = "Golden Retriever",
                    ageMonths = 36,
                    weightKg = 28.5,
                    sex = "Macho",
                    ownerName = "Carlos Mendoza",
                    ownerPhone = "+525544332211",
                    allergies = "Ninguna conocida",
                    notes = "Paciente tranquilo, vacunación al corriente."
                )
            )

            // Weight curve history
            val now = System.currentTimeMillis()
            val dayMs = 86400000L
            patientDao.insertWeightRecord(PatientWeightEntity(patientId = patientId, weightKg = 26.2, dateTimestamp = now - 90 * dayMs, notes = "Peso previo"))
            patientDao.insertWeightRecord(PatientWeightEntity(patientId = patientId, weightKg = 27.4, dateTimestamp = now - 45 * dayMs, notes = "Revisión mensual"))
            patientDao.insertWeightRecord(PatientWeightEntity(patientId = patientId, weightKg = 28.5, dateTimestamp = now, notes = "Peso actual"))

            // Preventive medicine vaccines
            patientDao.insertVaccination(
                VaccinationRecordEntity(
                    patientId = patientId,
                    patientName = "Rocky",
                    treatmentType = "Vacuna Múltiple Canina (Séxtuple DHPPiL)",
                    appliedDate = "2026-04-15",
                    nextDueDate = "2026-10-15",
                    batchNumber = "VAC-2026-99",
                    isDone = false,
                    notes = "Refuerzo anual programado"
                )
            )
            patientDao.insertVaccination(
                VaccinationRecordEntity(
                    patientId = patientId,
                    patientName = "Rocky",
                    treatmentType = "Desparasitación Interna (Praziquantel/Pirantel)",
                    appliedDate = "2026-07-10",
                    nextDueDate = "2026-10-20",
                    batchNumber = "DES-8812",
                    isDone = false,
                    notes = "Dosis según peso (3 tabletas)"
                )
            )

            // Treatment Sheet Record (Matriz Paciente x Hora)
            hospitalDao.insertTreatmentSheetItem(
                TreatmentSheetRecordEntity(
                    hospitalPatientId = 1L,
                    patientName = "Rocky",
                    cageBox = "Jaula Caninos 02",
                    medicationName = "Meloxivet 5% (Meloxicam)",
                    doseMl = "1.14 ml",
                    route = "SC",
                    hourSlot = "14:00",
                    isDone = true,
                    administeredBy = "MVZ. Alejandro Morales",
                    administeredTimestamp = now - 3600000L,
                    notes = "Tolerado sin dolor local"
                )
            )
            hospitalDao.insertTreatmentSheetItem(
                TreatmentSheetRecordEntity(
                    hospitalPatientId = 1L,
                    patientName = "Rocky",
                    cageBox = "Jaula Caninos 02",
                    medicationName = "Ondansetrón 2mg/ml",
                    doseMl = "4.20 ml",
                    route = "IV lenta",
                    hourSlot = "18:00",
                    isDone = false,
                    administeredBy = "",
                    notes = "Revisar vía venosa permeable"
                )
            )

            // Controlled substance initial entry
            inventoryDao.insertControlledLog(
                ControlledSubstanceLogEntity(
                    folioRecipe = "SENASICA-REC-2026-0041",
                    drugName = "Anesket 100mg/ml 50ml",
                    activeIngredient = "Ketamina HCl (Psicotrópico Grupo III)",
                    batchNumber = "LOT-KET-881A",
                    patientName = "Rocky",
                    ownerName = "Carlos Mendoza",
                    doctorName = "MVZ. Alejandro Morales",
                    doctorLicense = "CED-VET-982341-MX",
                    quantityDispensed = 1.42,
                    balanceRemaining = 48.58,
                    groupType = "GRUPO_III_PSICOTROPICO",
                    notes = "Inducción anestésica para debridación menor. Receta original retenida en archivo clínico."
                )
            )

            // Staff Emergency Profiles (Perfil médico del personal en caso de accidente laboral)
            val staffDao = database.staffDao()
            if (staffDao.getStaffCount() == 0) {
                staffDao.insertAllStaff(
                    listOf(
                        StaffEmergencyProfileEntity(
                            fullName = "MVZ. Alejandro Morales",
                            role = "Director Médico / Cirujano",
                            age = 34,
                            phone = "+52 55 1234 5678",
                            address = "Col. Del Valle, Benito Juárez, CDMX",
                            bloodType = "O+",
                            allergies = "Alérgica a Cefalosporinas y Látex",
                            preexistingConditions = "Ninguna",
                            emergencyContactName = "Dra. Sofía Morales",
                            emergencyContactPhone = "+52 55 8877 6655",
                            emergencyContactRelationship = "Esposa",
                            medicalInsuranceOrNotes = "Seguro MetLife Póliza #99214. Esquema completo de profilaxis antirrábica."
                        ),
                        StaffEmergencyProfileEntity(
                            fullName = "Dra. Laura Hernández",
                            role = "Médica Veterinaria Especialista",
                            age = 29,
                            phone = "+52 55 9876 5432",
                            address = "Col. Roma Sur, Cuauhtémoc, CDMX",
                            bloodType = "A+",
                            allergies = "Sulfamidas y AINEs (Diclofenaco)",
                            preexistingConditions = "Asma alérgica moderada",
                            emergencyContactName = "Lic. Roberto Hernández",
                            emergencyContactPhone = "+52 55 4411 2233",
                            emergencyContactRelationship = "Padre",
                            medicalInsuranceOrNotes = "IMSS Clínica 28. Traer inhalador Salbutamol en caso de crisis."
                        ),
                        StaffEmergencyProfileEntity(
                            fullName = "Carlos Ramírez",
                            role = "Estilista Canino / Groomer",
                            age = 27,
                            phone = "+52 55 3344 5566",
                            address = "Col. Coyoacán Centro, CDMX",
                            bloodType = "B+",
                            allergies = "Picaduras de avispa / abeja (anafilaxia)",
                            preexistingConditions = "Ninguna",
                            emergencyContactName = "Mariana Ruiz",
                            emergencyContactPhone = "+52 55 7766 5544",
                            emergencyContactRelationship = "Hermana",
                            medicalInsuranceOrNotes = "Porta autoinyector de adrenalina en mochila de trabajo. Vacunado con Tétanos 2025."
                        ),
                        StaffEmergencyProfileEntity(
                            fullName = "Ana Sofía Vega",
                            role = "Asistente Veterinario & Quirófano",
                            age = 24,
                            phone = "+52 55 6677 8899",
                            address = "Col. Narvarte Poniente, CDMX",
                            bloodType = "O-",
                            allergies = "Ninguna conocida",
                            preexistingConditions = "Ninguna",
                            emergencyContactName = "Elena Vega",
                            emergencyContactPhone = "+52 55 1122 3344",
                            emergencyContactRelationship = "Madre",
                            medicalInsuranceOrNotes = "Donante universal (O-). Cobertura Médica Seguros Monterrey."
                        )
                    )
                )
            }

            // Clinic Operations: Tasks, Messages, Grooming, Services Catalog, Commissions
            val opsDao = database.clinicOperationsDao()
            if (opsDao.getTaskCount() == 0) {
                opsDao.insertAllTasks(
                    listOf(
                        ClinicTaskEntity(
                            title = "Revisar autoclave y esterilizar instrumental",
                            description = "Esterilizar paquetes de cirugía de tejidos blandos y traumatología para las 15:00h.",
                            priority = "ALTA",
                            assignedToStaff = "Ana Sofía Vega",
                            createdByStaff = "MVZ. Alejandro Morales",
                            dueDate = "Hoy 14:30",
                            isCompleted = false
                        ),
                        ClinicTaskEntity(
                            title = "Limpieza profunda y desinfección de jaula 3 y 4",
                            description = "Uso de amonio cuaternario al 5% tras alta médica del paciente canino.",
                            priority = "NORMAL",
                            assignedToStaff = "Carlos Ramírez",
                            createdByStaff = "Dra. Laura Hernández",
                            dueDate = "Hoy 16:00",
                            isCompleted = true,
                            completedByStaff = "Carlos Ramírez",
                            completedTimestamp = now - 1800000L
                        ),
                        ClinicTaskEntity(
                            title = "Auditoría de lote de psicotrópicos en caja fuerte",
                            description = "Contar mililitros restantes de Ketamina y Tramadol para conciliación con libro COFEPRIS.",
                            priority = "ALTA",
                            assignedToStaff = "MVZ. Alejandro Morales",
                            createdByStaff = "Director Médico",
                            dueDate = "Fin de turno",
                            isCompleted = false
                        )
                    )
                )
            }

            if (opsDao.getServicesCount() == 0) {
                opsDao.insertAllServices(
                    listOf(
                        ClinicServiceEntity(
                            name = "Consulta General Perro/Gato",
                            category = "CONSULTA",
                            description = "Examen físico integral, constantes vitales y receta médica.",
                            standardPrice = 450.0,
                            defaultStaffCommissionType = "PERCENTAGE",
                            defaultStaffCommissionValue = 35.0 // 35% al médico ($157.50)
                        ),
                        ClinicServiceEntity(
                            name = "Consulta de Especialidad / Urgencia",
                            category = "CONSULTA",
                            description = "Evaluación de trauma, tóxicos o casos referidos.",
                            standardPrice = 750.0,
                            defaultStaffCommissionType = "PERCENTAGE",
                            defaultStaffCommissionValue = 40.0 // 40% al especialista ($300.00)
                        ),
                        ClinicServiceEntity(
                            name = "Esterilización Quirúrgica Canina",
                            category = "CIRUGIA",
                            description = "Ovariohisterectomía con protocolo anestésico y material incluido.",
                            standardPrice = 1800.0,
                            defaultStaffCommissionType = "FIXED",
                            defaultStaffCommissionValue = 500.0 // $500 bono al cirujano
                        ),
                        ClinicServiceEntity(
                            name = "Profilaxis Dental con Ultrasonido",
                            category = "CIRUGIA",
                            description = "Limpieza de sarro supra y subgingival, pulido y flúor.",
                            standardPrice = 1200.0,
                            defaultStaffCommissionType = "FIXED",
                            defaultStaffCommissionValue = 350.0
                        ),
                        ClinicServiceEntity(
                            name = "Baño y Corte de Raza (Estética Canina)",
                            category = "ESTETICA",
                            description = "Baño con shampoo premium, corte de pelo estilizado, uñas y glándulas.",
                            standardPrice = 420.0,
                            defaultStaffCommissionType = "PERCENTAGE",
                            defaultStaffCommissionValue = 40.0 // 40% al estilista ($168.00)
                        ),
                        ClinicServiceEntity(
                            name = "Baño Medicado Dermatológico",
                            category = "ESTETICA",
                            description = "Baño terapéutico con Clorhexidina o Ketoconazol con 15 min de reposo.",
                            standardPrice = 520.0,
                            defaultStaffCommissionType = "PERCENTAGE",
                            defaultStaffCommissionValue = 40.0 // $208 al estilista
                        ),
                        ClinicServiceEntity(
                            name = "Desparasitación Interna Completa",
                            category = "PREVENTIVO",
                            description = "Aplicación de antiparasitario según peso del paciente.",
                            standardPrice = 280.0,
                            defaultStaffCommissionType = "FIXED",
                            defaultStaffCommissionValue = 60.0
                        ),
                        ClinicServiceEntity(
                            name = "Hospitalización Día Completo (24 horas)",
                            category = "HOSPITAL",
                            description = "Cuidado médico continuo, bomba de infusión y administración horaria.",
                            standardPrice = 650.0,
                            defaultStaffCommissionType = "FIXED",
                            defaultStaffCommissionValue = 150.0
                        )
                    )
                )
            }

            // Initial team messages
            opsDao.insertTeamMessage(
                ClinicTeamMessageEntity(
                    senderName = "MVZ. Alejandro Morales",
                    senderRole = "Director Médico",
                    message = "¡Buen día a todo el equipo! Hoy tenemos 3 cirugías programadas y 4 baños de estética. Por favor revisar el tablón de pendientes.",
                    category = "AVISO",
                    timestamp = now - 7200000L
                )
            )
            opsDao.insertTeamMessage(
                ClinicTeamMessageEntity(
                    senderName = "Carlos Ramírez",
                    senderRole = "Estilista Canino",
                    message = "Llegó el shampoo medicado de Clorhexidina que hacía falta en el área de baño.",
                    category = "GENERAL",
                    timestamp = now - 3600000L
                )
            )

            // Initial Grooming Appointment
            opsDao.insertGroomingAppointment(
                GroomingAppointmentEntity(
                    patientName = "Toby",
                    species = "Canino",
                    breed = "Schnauzer Miniatura",
                    ownerName = "María Fernanda Gómez",
                    ownerPhone = "+52 55 9988 1122",
                    serviceType = "Corte de Raza & Glándulas",
                    assignedGroomerStaff = "Carlos Ramírez",
                    dateString = "Hoy",
                    timeString = "11:30",
                    skinObservations = "Piel sana, solicitar corte típico de Schnauzer con cejas y barba.",
                    cost = 420.0,
                    groomerCommission = 168.0,
                    status = "En Baño / Secado"
                )
            )

            // Initial Commission Entries
            opsDao.insertCommission(
                StaffPerformanceCommissionEntity(
                    staffName = "MVZ. Alejandro Morales",
                    serviceName = "Consulta General Perro/Gato",
                    serviceCategory = "CONSULTA",
                    patientName = "Rocky",
                    totalBilledAmount = 450.0,
                    commissionEarned = 157.50,
                    notes = "Consulta Rocky (Golden Retriever)",
                    dateTimestamp = now - 86400000L,
                    isPaidOut = true
                )
            )
            opsDao.insertCommission(
                StaffPerformanceCommissionEntity(
                    staffName = "Carlos Ramírez",
                    serviceName = "Baño y Corte de Raza (Estética Canina)",
                    serviceCategory = "ESTETICA",
                    patientName = "Max (Poodle)",
                    totalBilledAmount = 420.0,
                    commissionEarned = 168.0,
                    notes = "Estética canina completa",
                    dateTimestamp = now - 86400000L,
                    isPaidOut = false
                )
            )
        }
    }
}
