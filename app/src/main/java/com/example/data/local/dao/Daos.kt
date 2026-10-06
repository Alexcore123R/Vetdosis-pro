package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
    @Query("SELECT * FROM patients ORDER BY name ASC")
    fun getAllPatients(): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE id = :id LIMIT 1")
    suspend fun getPatientById(id: Long): PatientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: PatientEntity): Long

    @Update
    suspend fun updatePatient(patient: PatientEntity)

    @Delete
    suspend fun deletePatient(patient: PatientEntity)

    // Consultations & Templates
    @Query("SELECT * FROM consultation_history WHERE patientId = :patientId ORDER BY dateTimestamp DESC")
    fun getConsultationHistory(patientId: Long): Flow<List<ConsultationHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsultation(history: ConsultationHistoryEntity): Long

    // Weight Curve
    @Query("SELECT * FROM patient_weights WHERE patientId = :patientId ORDER BY dateTimestamp ASC")
    fun getWeightHistory(patientId: Long): Flow<List<PatientWeightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightRecord(weight: PatientWeightEntity): Long

    // Preventive Medicine & Vaccines
    @Query("SELECT * FROM vaccination_records WHERE patientId = :patientId ORDER BY isDone ASC, nextDueDate ASC")
    fun getVaccinationRecords(patientId: Long): Flow<List<VaccinationRecordEntity>>

    @Query("SELECT * FROM vaccination_records ORDER BY isDone ASC, nextDueDate ASC")
    fun getAllVaccinationRecords(): Flow<List<VaccinationRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccination(record: VaccinationRecordEntity): Long

    @Update
    suspend fun updateVaccination(record: VaccinationRecordEntity)

    // Informed Consents
    @Query("SELECT * FROM informed_consents WHERE patientId = :patientId ORDER BY dateTimestamp DESC")
    fun getConsentsForPatient(patientId: Long): Flow<List<InformedConsentEntity>>

    @Query("SELECT * FROM informed_consents ORDER BY dateTimestamp DESC")
    fun getAllConsents(): Flow<List<InformedConsentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsent(consent: InformedConsentEntity): Long
}

@Dao
interface DrugDao {
    @Query("SELECT * FROM drugs ORDER BY name ASC")
    fun getAllDrugs(): Flow<List<DrugEntity>>

    @Query("SELECT * FROM drugs WHERE name LIKE '%' || :query || '%' OR activeIngredient LIKE '%' || :query || '%' OR mexicanTradeNames LIKE '%' || :query || '%'")
    suspend fun searchDrugs(query: String): List<DrugEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrug(drug: DrugEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDrugs(drugs: List<DrugEntity>)

    @Query("SELECT COUNT(*) FROM drugs")
    suspend fun getDrugCount(): Int

    @Update
    suspend fun updateDrug(drug: DrugEntity)

    @Delete
    suspend fun deleteDrug(drug: DrugEntity)
}

@Dao
interface HospitalDao {
    @Query("SELECT * FROM hospital_patients ORDER BY isDone ASC, nextDoseTimestamp ASC")
    fun getAllHospitalized(): Flow<List<HospitalPatientEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHospitalPatient(patient: HospitalPatientEntity): Long

    @Update
    suspend fun updateHospitalPatient(patient: HospitalPatientEntity)

    @Delete
    suspend fun deleteHospitalPatient(patient: HospitalPatientEntity)

    // Treatment Sheet Matrix (Paciente x Hora)
    @Query("SELECT * FROM treatment_sheet_records ORDER BY hourSlot ASC, isDone ASC")
    fun getAllTreatmentSheetRecords(): Flow<List<TreatmentSheetRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTreatmentSheetItem(record: TreatmentSheetRecordEntity): Long

    @Update
    suspend fun updateTreatmentSheetItem(record: TreatmentSheetRecordEntity)
}

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM inventory_products WHERE stock <= minStockAlert")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProducts(products: List<ProductEntity>)

    @Query("SELECT COUNT(*) FROM inventory_products")
    suspend fun getProductCount(): Int

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    // Controlled Substances Book (COFEPRIS / SENASICA)
    @Query("SELECT * FROM controlled_substance_logs ORDER BY timestamp DESC")
    fun getAllControlledLogs(): Flow<List<ControlledSubstanceLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertControlledLog(log: ControlledSubstanceLogEntity): Long
}

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments ORDER BY dateString ASC, timeString ASC")
    fun getAllAppointments(): Flow<List<AppointmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity): Long

    @Update
    suspend fun updateAppointment(appointment: AppointmentEntity)

    @Delete
    suspend fun deleteAppointment(appointment: AppointmentEntity)
}

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff_emergency_profiles ORDER BY fullName ASC")
    fun getAllStaffProfiles(): Flow<List<StaffEmergencyProfileEntity>>

    @Query("SELECT * FROM staff_emergency_profiles WHERE id = :id LIMIT 1")
    suspend fun getStaffProfileById(id: Long): StaffEmergencyProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaffProfile(profile: StaffEmergencyProfileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStaff(profiles: List<StaffEmergencyProfileEntity>)

    @Query("SELECT COUNT(*) FROM staff_emergency_profiles")
    suspend fun getStaffCount(): Int

    @Update
    suspend fun updateStaffProfile(profile: StaffEmergencyProfileEntity)

    @Delete
    suspend fun deleteStaffProfile(profile: StaffEmergencyProfileEntity)
}

@Dao
interface ClinicOperationsDao {
    // Tasks & Notes Board
    @Query("SELECT * FROM clinic_tasks ORDER BY isCompleted ASC, priority ASC, timestamp DESC")
    fun getAllTasks(): Flow<List<ClinicTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: ClinicTaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTasks(tasks: List<ClinicTaskEntity>)

    @Query("SELECT COUNT(*) FROM clinic_tasks")
    suspend fun getTaskCount(): Int

    @Update
    suspend fun updateTask(task: ClinicTaskEntity)

    @Delete
    suspend fun deleteTask(task: ClinicTaskEntity)

    // Team Messages
    @Query("SELECT * FROM clinic_team_messages ORDER BY timestamp ASC")
    fun getAllTeamMessages(): Flow<List<ClinicTeamMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeamMessage(message: ClinicTeamMessageEntity): Long

    // Grooming Appointments
    @Query("SELECT * FROM grooming_appointments ORDER BY dateString ASC, timeString ASC")
    fun getAllGroomingAppointments(): Flow<List<GroomingAppointmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroomingAppointment(appt: GroomingAppointmentEntity): Long

    @Update
    suspend fun updateGroomingAppointment(appt: GroomingAppointmentEntity)

    @Delete
    suspend fun deleteGroomingAppointment(appt: GroomingAppointmentEntity)

    // Clinic Services Catalog
    @Query("SELECT * FROM clinic_services ORDER BY category ASC, name ASC")
    fun getAllServices(): Flow<List<ClinicServiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ClinicServiceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllServices(services: List<ClinicServiceEntity>)

    @Query("SELECT COUNT(*) FROM clinic_services")
    suspend fun getServicesCount(): Int

    @Update
    suspend fun updateService(service: ClinicServiceEntity)

    @Delete
    suspend fun deleteService(service: ClinicServiceEntity)

    // Staff Performance & Commissions
    @Query("SELECT * FROM staff_performance_commissions ORDER BY dateTimestamp DESC")
    fun getAllCommissions(): Flow<List<StaffPerformanceCommissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommission(comm: StaffPerformanceCommissionEntity): Long

    @Update
    suspend fun updateCommission(comm: StaffPerformanceCommissionEntity)

    @Delete
    suspend fun deleteCommission(comm: StaffPerformanceCommissionEntity)
}
