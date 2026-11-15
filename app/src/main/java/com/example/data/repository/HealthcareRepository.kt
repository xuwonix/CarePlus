package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.AmbulanceRequest
import com.example.data.model.Appointment
import com.example.data.model.ChatMessage
import com.example.data.model.DiagnosticBooking
import com.example.data.model.DiagnosticCenter
import com.example.data.model.DiagnosticTest
import com.example.data.model.InsuranceClaim
import com.example.data.model.MedicalRecord
import com.example.data.model.ProcedureChecklistStep
import com.example.data.model.ProcedureGuide
import com.example.data.model.Specialist
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class HealthcareRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing CarePulse data.")
    }

    fun getCurrentUser() = auth.currentUser

    // -------------------------------------------------------------
    // USER PROFILE & ROLES
    // -------------------------------------------------------------
    suspend fun getOrCreateUserProfile(userRole: UserRole = UserRole.PATIENT): UserProfile {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val snapshot = docRef.get().await()
        if (snapshot.exists()) {
            return snapshot.toObject(UserProfile::class.java) ?: UserProfile(
                userId = uid,
                email = auth.currentUser?.email ?: "",
                displayName = auth.currentUser?.displayName ?: "CarePulse User",
                role = userRole.name
            )
        } else {
            val profile = UserProfile(
                userId = uid,
                email = auth.currentUser?.email ?: "",
                displayName = auth.currentUser?.displayName ?: "CarePulse User",
                role = userRole.name,
                phone = "+1 (555) 392-1084"
            )
            docRef.set(
                mapOf(
                    "userId" to profile.userId,
                    "email" to profile.email,
                    "displayName" to profile.displayName,
                    "role" to profile.role,
                    "phone" to profile.phone,
                    "organization" to profile.organization,
                    "licenseNumber" to profile.licenseNumber,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            return profile
        }
    }

    suspend fun updateUserRole(newRole: UserRole) {
        val uid = requireUserId()
        db.collection("users").document(uid).update(
            mapOf(
                "role" to newRole.name,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    fun observeUserProfile(): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        db.collection("users").document(uid)
            .snapshots()
            .map { it.toObject(UserProfile::class.java) }
            .catch { error ->
                Log.w("HealthcareRepo", "Error observing profile: ${error.message}")
                emit(null)
            }
            .collect { emit(it) }
    }

    // -------------------------------------------------------------
    // APPOINTMENTS
    // -------------------------------------------------------------
    fun observePatientAppointments(): Flow<List<Appointment>> = flow {
        val uid = requireUserId()
        db.collection("appointments")
            .whereEqualTo("patientId", uid)
            .snapshots()
            .map { it.toObjects(Appointment::class.java) }
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w("HealthcareRepo", "Error observing patient appointments: ${error.message}")
                emit(emptyList())
            }
            .collect { emit(it) }
    }

    fun observeSpecialistAppointments(): Flow<List<Appointment>> = flow {
        val uid = requireUserId()
        db.collection("appointments")
            .whereEqualTo("specialistId", uid)
            .snapshots()
            .map { it.toObjects(Appointment::class.java) }
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w("HealthcareRepo", "Error observing specialist appointments: ${error.message}")
                emit(emptyList())
            }
            .collect { emit(it) }
    }

    suspend fun scheduleAppointment(appointment: Appointment) {
        val uid = requireUserId()
        val docRef = db.collection("appointments").document()
        val payload = mapOf(
            "patientId" to uid,
            "patientName" to appointment.patientName.ifBlank { auth.currentUser?.displayName ?: "Patient" },
            "specialistId" to appointment.specialistId,
            "specialistName" to appointment.specialistName,
            "specialty" to appointment.specialty,
            "clinicHospital" to appointment.clinicHospital,
            "date" to appointment.date,
            "timeSlot" to appointment.timeSlot,
            "mode" to appointment.mode,
            "status" to "SCHEDULED",
            "notes" to appointment.notes,
            "prescription" to "",
            "consultationFee" to appointment.consultationFee,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
    }

    suspend fun updateAppointmentStatus(appointmentId: String, newStatus: String, prescription: String = "") {
        val docRef = db.collection("appointments").document(appointmentId)
        val updates = mutableMapOf<String, Any>(
            "status" to newStatus,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (prescription.isNotBlank()) {
            updates["prescription"] = prescription
        }
        docRef.update(updates).await()
    }

    // -------------------------------------------------------------
    // AMBULANCE & EMERGENCY DISPATCH
    // -------------------------------------------------------------
    fun observePatientAmbulanceRequests(): Flow<List<AmbulanceRequest>> = flow {
        val uid = requireUserId()
        db.collection("ambulance_requests")
            .whereEqualTo("patientId", uid)
            .snapshots()
            .map { it.toObjects(AmbulanceRequest::class.java) }
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w("HealthcareRepo", "Error observing ambulance requests: ${error.message}")
                emit(emptyList())
            }
            .collect { emit(it) }
    }

    fun observeAllAmbulanceRequests(): Flow<List<AmbulanceRequest>> = flow {
        db.collection("ambulance_requests")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(AmbulanceRequest::class.java) }
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w("HealthcareRepo", "Error observing all ambulance requests: ${error.message}")
                emit(emptyList())
            }
            .collect { emit(it) }
    }

    suspend fun requestAmbulance(
        pickupAddress: String,
        emergencyType: String,
        ambulanceType: String,
        patientPhone: String
    ): String {
        val uid = requireUserId()
        val docRef = db.collection("ambulance_requests").document()
        val payload = mapOf(
            "patientId" to uid,
            "patientName" to (auth.currentUser?.displayName ?: "Emergency Patient"),
            "patientPhone" to patientPhone,
            "pickupAddress" to pickupAddress,
            "emergencyType" to emergencyType,
            "ambulanceType" to ambulanceType,
            "status" to "DISPATCHED",
            "vehicleNumber" to "EMS-${(1000..9999).random()}",
            "driverName" to "Paramedic Team Alpha",
            "driverPhone" to "+1 (555) 911-0428",
            "etaMinutes" to 7L,
            "hospitalDestination" to "Metro Trauma & Cardiac Center",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
        return docRef.id
    }

    suspend fun updateAmbulanceStatus(requestId: String, status: String, etaMinutes: Long = 0L) {
        val updates = mutableMapOf<String, Any>(
            "status" to status,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (etaMinutes > 0) {
            updates["etaMinutes"] = etaMinutes
        }
        db.collection("ambulance_requests").document(requestId).update(updates).await()
    }

    // -------------------------------------------------------------
    // DIAGNOSTIC LABS
    // -------------------------------------------------------------
    fun observePatientDiagnosticBookings(): Flow<List<DiagnosticBooking>> = flow {
        val uid = requireUserId()
        db.collection("diagnostic_bookings")
            .whereEqualTo("patientId", uid)
            .snapshots()
            .map { it.toObjects(DiagnosticBooking::class.java) }
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w("HealthcareRepo", "Error observing diagnostic bookings: ${error.message}")
                emit(emptyList())
            }
            .collect { emit(it) }
    }

    fun observeAllDiagnosticBookings(): Flow<List<DiagnosticBooking>> = flow {
        db.collection("diagnostic_bookings")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(DiagnosticBooking::class.java) }
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w("HealthcareRepo", "Error observing all diagnostic bookings: ${error.message}")
                emit(emptyList())
            }
            .collect { emit(it) }
    }

    suspend fun bookDiagnosticTest(
        center: DiagnosticCenter,
        test: DiagnosticTest,
        scheduledDate: String,
        collectionType: String
    ) {
        val uid = requireUserId()
        val docRef = db.collection("diagnostic_bookings").document()
        val payload = mapOf(
            "patientId" to uid,
            "patientName" to (auth.currentUser?.displayName ?: "Patient"),
            "centerId" to center.id,
            "centerName" to center.name,
            "testId" to test.id,
            "testName" to test.name,
            "testCategory" to test.category,
            "scheduledDate" to scheduledDate,
            "scheduledTime" to "09:00 AM",
            "collectionType" to collectionType,
            "status" to "BOOKED",
            "resultSummary" to "Sample scheduled. Results will appear once analyzed.",
            "abnormalFlag" to false,
            "cost" to test.price,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
    }

    suspend fun updateDiagnosticStatus(bookingId: String, status: String, resultSummary: String, abnormal: Boolean = false) {
        db.collection("diagnostic_bookings").document(bookingId).update(
            mapOf(
                "status" to status,
                "resultSummary" to resultSummary,
                "abnormalFlag" to abnormal,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    // -------------------------------------------------------------
    // INSURANCE CLAIMS
    // -------------------------------------------------------------
    fun observePatientInsuranceClaims(): Flow<List<InsuranceClaim>> = flow {
        val uid = requireUserId()
        db.collection("insurance_claims")
            .whereEqualTo("patientId", uid)
            .snapshots()
            .map { it.toObjects(InsuranceClaim::class.java) }
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w("HealthcareRepo", "Error observing insurance claims: ${error.message}")
                emit(emptyList())
            }
            .collect { emit(it) }
    }

    fun observeAllInsuranceClaims(): Flow<List<InsuranceClaim>> = flow {
        db.collection("insurance_claims")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(InsuranceClaim::class.java) }
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w("HealthcareRepo", "Error observing all claims: ${error.message}")
                emit(emptyList())
            }
            .collect { emit(it) }
    }

    suspend fun submitInsuranceClaim(claim: InsuranceClaim) {
        val uid = requireUserId()
        val docRef = db.collection("insurance_claims").document()
        val payload = mapOf(
            "patientId" to uid,
            "patientName" to (auth.currentUser?.displayName ?: "Policy Holder"),
            "policyNumber" to claim.policyNumber,
            "insuranceProvider" to claim.insuranceProvider,
            "hospitalName" to claim.hospitalName,
            "procedureName" to claim.procedureName,
            "claimedAmount" to claim.claimedAmount,
            "approvedAmount" to 0.0,
            "status" to "SUBMITTED",
            "claimType" to claim.claimType,
            "documentReference" to claim.documentReference,
            "reviewerNotes" to "Under initial verification by claims team.",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
    }

    suspend fun reviewInsuranceClaim(claimId: String, status: String, approvedAmount: Double, notes: String) {
        db.collection("insurance_claims").document(claimId).update(
            mapOf(
                "status" to status,
                "approvedAmount" to approvedAmount,
                "reviewerNotes" to notes,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    // -------------------------------------------------------------
    // MEDICAL RECORDS (DIGITAL HEALTH LOCKER)
    // -------------------------------------------------------------
    fun observeMedicalRecords(): Flow<List<MedicalRecord>> = flow {
        val uid = requireUserId()
        db.collection("medical_records")
            .whereEqualTo("patientId", uid)
            .snapshots()
            .map { it.toObjects(MedicalRecord::class.java) }
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w("HealthcareRepo", "Error observing medical records: ${error.message}")
                emit(emptyList())
            }
            .collect { emit(it) }
    }

    suspend fun addMedicalRecord(record: MedicalRecord) {
        val uid = requireUserId()
        val docRef = db.collection("medical_records").document()
        val payload = mapOf(
            "patientId" to uid,
            "title" to record.title,
            "category" to record.category,
            "doctorOrFacility" to record.doctorOrFacility,
            "recordDate" to record.recordDate,
            "summary" to record.summary,
            "keyMetrics" to record.keyMetrics,
            "fileType" to record.fileType,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
    }

    // -------------------------------------------------------------
    // SECURE REAL-TIME MESSAGING
    // -------------------------------------------------------------
    fun observeConsultationMessages(consultationId: String): Flow<List<ChatMessage>> = flow {
        db.collection("chat_messages")
            .whereEqualTo("consultationId", consultationId)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { it.toObjects(ChatMessage::class.java) }
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w("HealthcareRepo", "Error observing chat messages: ${error.message}")
                emit(emptyList())
            }
            .collect { emit(it) }
    }

    suspend fun sendChatMessage(
        consultationId: String,
        recipientId: String,
        recipientName: String,
        message: String,
        messageType: String = "TEXT"
    ) {
        val uid = requireUserId()
        val docRef = db.collection("chat_messages").document()
        val payload = mapOf(
            "consultationId" to consultationId,
            "senderId" to uid,
            "senderName" to (auth.currentUser?.displayName ?: "User"),
            "senderRole" to "PATIENT",
            "recipientId" to recipientId,
            "recipientName" to recipientName,
            "message" to message,
            "isRead" to false,
            "messageType" to messageType,
            "createdAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
    }

    // -------------------------------------------------------------
    // CURATED DATA: SPECIALISTS, DIAGNOSTIC LABS, PROCEDURE GUIDES
    // -------------------------------------------------------------
    fun getLocalSpecialists(): List<Specialist> = listOf(
        Specialist(
            id = "doc_cardio_1",
            name = "Dr. Elena Rostova, MD",
            title = "Senior Interventional Cardiologist",
            specialty = "Cardiology",
            experienceYears = 16,
            rating = 4.9,
            reviewCount = 312,
            clinicHospital = "Heart & Vascular Institute of Metro",
            address = "742 Medical Plaza, Suite 400",
            fee = 140.0,
            nextAvailableSlot = "Tomorrow at 10:30 AM",
            bio = "Board-certified in cardiovascular disease and interventional cardiology with specialization in arrhythmia and preventive care."
        ),
        Specialist(
            id = "doc_neuro_2",
            name = "Dr. Marcus Chen, MD, PhD",
            title = "Consultant Neurologist & Spine Specialist",
            specialty = "Neurology",
            experienceYears = 14,
            rating = 4.8,
            reviewCount = 245,
            clinicHospital = "Metro Brain & Spine Neuroscience Center",
            address = "890 Innovation Parkway, Tower B",
            fee = 160.0,
            nextAvailableSlot = "Thursday at 02:00 PM",
            bio = "Expert in complex migraine disorders, neuropathy, neuro-rehab, and pre-op cranial assessments."
        ),
        Specialist(
            id = "doc_ortho_3",
            name = "Dr. Sarah Jenkins, MD",
            title = "Orthopedic Surgeon & Joint Replacement",
            specialty = "Orthopedics",
            experienceYears = 12,
            rating = 4.9,
            reviewCount = 189,
            clinicHospital = "ActiveLife Sports Medicine & Ortho Hospital",
            address = "310 Valley Boulevard",
            fee = 130.0,
            nextAvailableSlot = "Friday at 09:00 AM",
            bio = "Specializes in minimally invasive arthroscopy, sports injury rehabilitation, and knee/hip joint replacements."
        ),
        Specialist(
            id = "doc_pedia_4",
            name = "Dr. Tariq Al-Mansoor, MD",
            title = "Pediatrician & Adolescent Health",
            specialty = "Pediatrics",
            experienceYears = 10,
            rating = 4.9,
            reviewCount = 420,
            clinicHospital = "Children's Health Pavilion",
            address = "120 Park Lane, Children's Wing",
            fee = 95.0,
            nextAvailableSlot = "Today at 04:30 PM",
            bio = "Warm, attentive care specializing in pediatric developmental milestones, vaccinations, and childhood respiratory allergies."
        ),
        Specialist(
            id = "doc_gen_5",
            name = "Dr. Rachel Adams, DO",
            title = "General Medicine & Internal Health",
            specialty = "General Medicine",
            experienceYears = 8,
            rating = 4.7,
            reviewCount = 510,
            clinicHospital = "Metro Family Health Center",
            address = "55 Highland Road",
            fee = 75.0,
            nextAvailableSlot = "Today at 06:00 PM",
            bio = "Comprehensive primary healthcare, chronic diabetes and hypertension management, and annual wellness screenings."
        )
    )

    fun getLocalDiagnosticCenters(): List<DiagnosticCenter> = listOf(
        DiagnosticCenter(
            id = "center_1",
            name = "Metro Health Advanced Imaging & Diagnostics",
            address = "450 Health Sciences Drive",
            distanceKm = 1.8,
            rating = 4.9,
            providesHomeCollection = true,
            testsOffered = listOf(
                DiagnosticTest(
                    id = "test_1",
                    name = "Comprehensive Metabolic & Lipid Panel",
                    category = "BLOOD_WORK",
                    sampleType = "Fasting Venous Blood",
                    turnaroundTime = "12 Hours",
                    preparationSummary = "Fast for 10-12 hours prior. Water permitted.",
                    price = 45.0
                ),
                DiagnosticTest(
                    id = "test_2",
                    name = "High-Resolution Cardiac MRI with Contrast",
                    category = "RADIOLOGY",
                    sampleType = "3T MRI Imaging Scan",
                    turnaroundTime = "24 Hours",
                    preparationSummary = "Remove all metallic objects, jewelry, and credit cards. Fast 4 hours before.",
                    price = 320.0
                ),
                DiagnosticTest(
                    id = "test_3",
                    name = "12-Lead Resting & Stress ECG",
                    category = "CARDIOLOGY",
                    sampleType = "Electrophysiological Trace",
                    turnaroundTime = "Instant (30 Mins)",
                    preparationSummary = "Wear comfortable loose two-piece clothing.",
                    price = 60.0
                )
            )
        ),
        DiagnosticCenter(
            id = "center_2",
            name = "BioPath Premier Clinical Laboratories",
            address = "780 Commerce Court, Suite 102",
            distanceKm = 3.2,
            rating = 4.8,
            providesHomeCollection = true,
            testsOffered = listOf(
                DiagnosticTest(
                    id = "test_4",
                    name = "HbA1c & Fasting Insulin Glycemic Index",
                    category = "BLOOD_WORK",
                    sampleType = "Blood Sample",
                    turnaroundTime = "6 Hours",
                    preparationSummary = "No fasting strictly required for HbA1c; 8h fasting for insulin.",
                    price = 35.0
                ),
                DiagnosticTest(
                    id = "test_5",
                    name = "Whole Abdomen & Pelvic Ultrasound",
                    category = "RADIOLOGY",
                    sampleType = "Ultrasound Scan",
                    turnaroundTime = "Same Day",
                    preparationSummary = "Drink 1 liter of water 1 hour before and do not urinate until scan.",
                    price = 85.0
                )
            )
        )
    )

    fun getProcedureGuides(): List<ProcedureGuide> = listOf(
        ProcedureGuide(
            id = "proc_endoscopy",
            title = "Upper Gastrointestinal (GI) Endoscopy",
            category = "ENDOSCOPIC",
            summary = "A visual examination of the esophagus, stomach, and duodenum using a thin, flexible endoscope camera.",
            estimatedDuration = "20 to 30 minutes",
            anesthesiaType = "Intravenous conscious sedation (propofol / midazolam)",
            recoveryDays = 1,
            preparationChecklist = listOf(
                ProcedureChecklistStep(
                    stepId = "endo_1",
                    title = "Blood Thinner & Anticoagulant Protocol",
                    detail = "Consult prescribing doctor regarding halting aspirin, warfarin, or apixaban 5-7 days prior.",
                    timing = "7 Days Before"
                ),
                ProcedureChecklistStep(
                    stepId = "endo_2",
                    title = "Strict Fasting from Solid Food",
                    detail = "Do not eat solid foods after midnight or for at least 8 hours prior to procedure time.",
                    timing = "Midnight Before"
                ),
                ProcedureChecklistStep(
                    stepId = "endo_3",
                    title = "Clear Liquid Cut-off",
                    detail = "Stop drinking clear liquids (water, apple juice) exactly 3 hours before check-in.",
                    timing = "3 Hours Before"
                ),
                ProcedureChecklistStep(
                    stepId = "endo_4",
                    title = "Designated Responsible Adult Driver",
                    detail = "Arrange for an adult companion to drive you home; driving or signing legal documents is prohibited for 24h.",
                    timing = "Day of Procedure"
                )
            ),
            procedureOverview = "You will lie comfortably on your left side. A mouthguard protects your teeth. Anesthesia is administered intravenously, causing gentle sleep. The physician gently advances the scope, examining mucosal lining and taking micro-biopsies if needed without pain.",
            postCareInstructions = listOf(
                "Rest quietly at home for the remainder of the day.",
                "Start with gentle sips of water, then clear broths and crackers once swallowing reflexes return fully.",
                "A mild throat tickle or transient belching is normal and resolves within 24 hours.",
                "Do not drive, operate machinery, or consume alcohol for 24 hours after sedation."
            ),
            warningRedFlags = listOf(
                "Difficulty or sharp pain swallowing that worsens after 4 hours",
                "Fever greater than 101°F (38.3°C) or persistent chills",
                "Vomiting dark coffee-ground material or bright blood",
                "Severe, worsening abdominal or chest pain"
            )
        ),
        ProcedureGuide(
            id = "proc_mri_contrast",
            title = "MRI Scan with Gadolinium Contrast",
            category = "DIAGNOSTIC_IMAGING",
            summary = "Non-invasive magnetic resonance scan utilizing safe gadolinium contrast agent to highlight vascular and tissue pathology.",
            estimatedDuration = "45 to 60 minutes",
            anesthesiaType = "None (optional mild oral anxiolytic for claustrophobia)",
            recoveryDays = 0,
            preparationChecklist = listOf(
                ProcedureChecklistStep(
                    stepId = "mri_1",
                    title = "Implant & Metal Safety Screening",
                    detail = "Report any cardiac pacemakers, cochlear implants, aneurysm clips, metal dental implants, or shrapnel.",
                    timing = "At Booking"
                ),
                ProcedureChecklistStep(
                    stepId = "mri_2",
                    title = "Kidney Function Check (eGFR)",
                    detail = "Ensure blood creatinine/eGFR results from within past 30 days are uploaded to your locker.",
                    timing = "3 Days Before"
                ),
                ProcedureChecklistStep(
                    stepId = "mri_3",
                    title = "Remove All Body Jewelry & Wear Cotton",
                    detail = "Remove piercings, watches, belts, underwire bras, and hairpins. Wear plain athletic clothing without metal zippers.",
                    timing = "Morning of Scan"
                )
            ),
            procedureOverview = "You lie on a cushioned scanner table that slides into the magnetic bore. You will wear noise-cancelling headphones playing music or guidance. An IV line provides contrast during the second half of the sequence.",
            postCareInstructions = listOf(
                "Drink 2-3 extra glasses of water throughout the day to help your kidneys rapidly flush the contrast agent.",
                "Resume your normal daily diet and medications immediately.",
                "Reports will be processed and visible in your CarePulse Locker within 24 hours."
            ),
            warningRedFlags = listOf(
                "Hives, generalized itching, or skin redness",
                "Swelling around eyes, lips, or throat",
                "Sudden dizziness or shortness of breath (call Emergency EMS immediately)"
            )
        ),
        ProcedureGuide(
            id = "proc_knee_arthroscopy",
            title = "Knee Arthroscopy & Meniscal Repair",
            category = "SURGICAL",
            summary = "Minimally invasive keyhole surgical repair of torn meniscus cartilage or ligament smoothing inside the knee joint capsule.",
            estimatedDuration = "60 to 90 minutes",
            anesthesiaType = "General or Spinal Anesthesia with femoral nerve block",
            recoveryDays = 7,
            preparationChecklist = listOf(
                ProcedureChecklistStep(
                    stepId = "knee_1",
                    title = "Pre-operative Clearance & Lab Panel",
                    detail = "Complete pre-op ECG, CBC blood panel, and anesthesia review at the hospital clinic.",
                    timing = "5 Days Before"
                ),
                ProcedureChecklistStep(
                    stepId = "knee_2",
                    title = "Crutches & Ice Machine Preparation",
                    detail = "Adjust assistive crutches to standing height and fill freezer with gel packs for cold compression.",
                    timing = "2 Days Before"
                ),
                ProcedureChecklistStep(
                    stepId = "knee_3",
                    title = "Antimicrobial Hibiclens Shower",
                    detail = "Wash operative leg with chlorhexidine antiseptic soap the evening and morning before surgery.",
                    timing = "Night & Morning Before"
                )
            ),
            procedureOverview = "Small 5mm incisions allow the arthroscope and precision instruments to enter the knee joint. Sterile fluid distends the joint while high-definition cameras guide the surgeon to trim or suture torn cartilage cleanly.",
            postCareInstructions = listOf(
                "Practice R.I.C.E protocol: Rest, Ice (20 min on, 20 min off), Compression bandage, and Elevation above heart level.",
                "Keep surgical dressings completely dry for 72 hours. Use shower leg bags.",
                "Perform prescribed ankle pumps every hour while awake to promote healthy venous circulation.",
                "Take prescribed anti-inflammatory and pain medications strictly with meals."
            ),
            warningRedFlags = listOf(
                "Calf pain, significant swelling, or warmth behind the leg (sign of DVT)",
                "Fever above 101.5°F or spreading redness around incision portals",
                "Sudden chest pain or shortness of breath",
                "Uncontrolled severe pain unresponsive to prescribed medication"
            )
        ),
        ProcedureGuide(
            id = "proc_cataract",
            title = "Cataract Extraction with Intraocular Lens (IOL)",
            category = "SURGICAL",
            summary = "Gentle ultrasonic phacoemulsification removal of cloudy crystalline lens replaced with a high-definition foldable intraocular lens.",
            estimatedDuration = "15 to 20 minutes",
            anesthesiaType = "Topical numbing eye drops with light IV relaxation",
            recoveryDays = 2,
            preparationChecklist = listOf(
                ProcedureChecklistStep(
                    stepId = "cat_1",
                    title = "Antibiotic Eye Drop Regimen",
                    detail = "Begin prescribed topical antibiotic and anti-inflammatory eye drops 3 days prior as instructed.",
                    timing = "3 Days Before"
                ),
                ProcedureChecklistStep(
                    stepId = "cat_2",
                    title = "No Face Makeup or Perfumes",
                    detail = "Wash face thoroughly with gentle soap. Do not apply eye shadow, eyeliner, mascara, or face lotions.",
                    timing = "Morning of Surgery"
                )
            ),
            procedureOverview = "A tiny microscopic incision is made at the corneal edge. Ultrasound waves gently fragment the cloudy cataract lens, which is vacuumed away. A clear premium acrylic intraocular lens is then unfolded in place.",
            postCareInstructions = listOf(
                "Wear protective eye shield during sleep for the first week to prevent accidental rubbing.",
                "Avoid bending over with your head lower than your waist, heavy lifting (>10 lbs), or vigorous exercise.",
                "Do not get water or shampoo directly into the operated eye for 7 days.",
                "Instill prescribed post-op steroid and antibiotic eye drops on schedule."
            ),
            warningRedFlags = listOf(
                "Sudden sharp drop in visual clarity or dark curtain sensation",
                "Severe aching eye pain not relieved by mild analgesics",
                "Increasing discharge or severe conjunctival redness"
            )
        )
    )
}
