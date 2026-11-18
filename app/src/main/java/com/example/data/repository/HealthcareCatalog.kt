package com.example.data.repository

import com.example.data.model.DiagnosticCenter
import com.example.data.model.DiagnosticTest
import com.example.data.model.HomeCareService
import com.example.data.model.MedicalSpecialtyItem
import com.example.data.model.NearbyFacility
import com.example.data.model.ProcedureChecklistStep
import com.example.data.model.ProcedureGuide
import com.example.data.model.Specialist

object HealthcareCatalog {

    val metroAreas: List<String> = listOf(
        "Mission Chariali, Tezpur",
        "Ketekibari, Tezpur",
        "Mahabhairab, Tezpur",
        "Bihaguri, Tezpur",
        "Dekargaon, Tezpur"
    )

    fun getPopularSpecialties(): List<MedicalSpecialtyItem> = listOf(
        MedicalSpecialtyItem("spec_cardio", "Cardiology", "Heart & Vascular", 8),
        MedicalSpecialtyItem("spec_neuro", "Neurology", "Brain & Spine", 6),
        MedicalSpecialtyItem("spec_ortho", "Orthopedics", "Bone & Joint", 9),
        MedicalSpecialtyItem("spec_pedia", "Pediatrics", "Child Health", 12),
        MedicalSpecialtyItem("spec_derm", "Dermatology", "Skin & Hair", 7),
        MedicalSpecialtyItem("spec_gen", "General Medicine", "Primary Care", 18)
    )

    fun getHomeCareServices(): List<HomeCareService> = listOf(
        HomeCareService(
            id = "hc_nurse_1",
            title = "Skilled Nursing & Post-Op Dressing",
            category = "NURSING",
            duration = "60 Mins Visit",
            description = "Trained GNM/B.Sc nurse visit for surgical wound care, suture removal, vitals monitoring, and catheter/dressing care.",
            price = 650.0,
            providerTeam = "Nidanmita Home Nursing Team"
        ),
        HomeCareService(
            id = "hc_physio_2",
            title = "Home Physiotherapy & Mobility Rehab",
            category = "PHYSIOTHERAPY",
            duration = "45 Mins Session",
            description = "Qualified physiotherapist brings portable electrotherapy and guided exercises for knee, back, or post-stroke recovery.",
            price = 700.0,
            providerTeam = "Brahmaputra Home Rehab, Tezpur"
        ),
        HomeCareService(
            id = "hc_elder_3",
            title = "Elderly Care & Attendant Shift",
            category = "ELDERLY_CARE",
            duration = "4 Hours Shift",
            description = "Trained medical attendant for senior vitals tracking, medication reminders, mobility support, and daily wellness.",
            price = 1200.0,
            providerTeam = "Assam Senior Care Network"
        ),
        HomeCareService(
            id = "hc_iv_4",
            title = "Home IV Hydration & Antibiotic Infusion",
            category = "IV_THERAPY",
            duration = "45 Mins Visit",
            description = "Doctor-prescribed IV fluid, vitamin drip, or antibiotic infusion administered safely at home by a registered nurse.",
            price = 850.0,
            providerTeam = "Nidanmita Infusion Nurses"
        ),
        HomeCareService(
            id = "hc_doc_5",
            title = "General Physician Home Visit",
            category = "DOCTOR_VISIT",
            duration = "30 Mins Visit",
            description = "On-demand MBBS/MD physician house call in Tezpur for fever, respiratory symptoms, acute pain, or bedridden patients.",
            price = 800.0,
            providerTeam = "Tezpur Mobile Physicians"
        )
    )

    fun getNearbyHospitalsAndCenters(selectedArea: String = "Mission Chariali, Tezpur"): List<NearbyFacility> {
        val offset = when (selectedArea) {
            "Ketekibari, Tezpur" -> 0.4
            "Mahabhairab, Tezpur" -> 0.8
            "Bihaguri, Tezpur" -> 1.2
            "Dekargaon, Tezpur" -> 1.5
            else -> 0.0
        }
        return listOf(
            NearbyFacility(
                id = "hosp_bch_tezpur",
                name = "Baptist Christian Hospital, Tezpur",
                facilityType = "HOSPITAL",
                address = "Mission Chariali, Tezpur, Assam 784001",
                distanceKm = ((1.1 + offset) * 10).toInt() / 10.0,
                rating = 4.8,
                reviewCount = 740,
                isOpen24x7 = true,
                hasEmergencyUnit = true,
                providesHomeCollection = false,
                departments = listOf("24/7 Emergency", "Orthopedics", "Pediatrics", "General Surgery"),
                waitTimeMinutes = 12,
                phone = "+91 3712-220144",
                accreditation = "NABH Accredited Multi-Specialty Hospital",
                acceptsCashlessInsurance = true
            ),
            NearbyFacility(
                id = "center_1",
                name = "Apollo Clinic & Diagnostics Tezpur",
                facilityType = "DIAGNOSTIC_CENTER",
                address = "Ketekibari, NH-15, Tezpur, Assam 784154",
                distanceKm = ((1.6 + offset) * 10).toInt() / 10.0,
                rating = 4.9,
                reviewCount = 412,
                isOpen24x7 = false,
                hasEmergencyUnit = false,
                providesHomeCollection = true,
                departments = listOf("MRI & CT Scan", "Lipid & Blood Panels", "ECG & Echo", "Home Pickup"),
                waitTimeMinutes = 10,
                phone = "+91 3712-255410",
                accreditation = "NABL Accredited Diagnostic Lab",
                acceptsCashlessInsurance = true
            ),
            NearbyFacility(
                id = "hosp_tmch_tezpur",
                name = "Tezpur Medical College & Hospital (TMCH)",
                facilityType = "HOSPITAL",
                address = "NH-15, Bihaguri, Tezpur, Assam 784010",
                distanceKm = ((2.8 + offset) * 10).toInt() / 10.0,
                rating = 4.7,
                reviewCount = 1120,
                isOpen24x7 = true,
                hasEmergencyUnit = true,
                providesHomeCollection = false,
                departments = listOf("24/7 Casualty & Trauma", "Cardiology", "Neurology", "ICU"),
                waitTimeMinutes = 18,
                phone = "+91 3712-241305",
                accreditation = "Govt. Tertiary Care & Trauma Center",
                acceptsCashlessInsurance = true
            ),
            NearbyFacility(
                id = "center_2",
                name = "Sun Diagnostic & Pathology Centre",
                facilityType = "DIAGNOSTIC_CENTER",
                address = "Mahabhairab, MC Road, Tezpur, Assam 784001",
                distanceKm = ((2.2 + offset) * 10).toInt() / 10.0,
                rating = 4.8,
                reviewCount = 530,
                isOpen24x7 = false,
                hasEmergencyUnit = false,
                providesHomeCollection = true,
                departments = listOf("Home Phlebotomy", "HbA1c & Thyroid", "Ultrasound (USG)", "Pathology"),
                waitTimeMinutes = 8,
                phone = "+91 3712-231890",
                accreditation = "ISO 15189 Certified Diagnostics",
                acceptsCashlessInsurance = true
            )
        ).sortedBy { it.distanceKm }
    }

    fun getLocalSpecialists(): List<Specialist> = listOf(
        Specialist(
            id = "doc_cardio_1",
            name = "Dr. Ananya Baruah, MD, DM",
            title = "Senior Interventional Cardiologist",
            specialty = "Cardiology",
            experienceYears = 16,
            rating = 4.9,
            reviewCount = 312,
            clinicHospital = "Tezpur Medical College & Hospital (TMCH)",
            hospitalId = "hosp_tmch_tezpur",
            address = "NH-15, Bihaguri, Tezpur, Assam",
            fee = 800.0,
            nextAvailableSlot = "Tomorrow at 10:30 AM",
            bio = "Senior cardiologist specializing in hypertension, arrhythmia, echocardiography, and preventive heart care across Sonitpur district.",
            qualifications = "MBBS, MD (GMCH), DM Cardiology",
            languages = listOf("Assamese", "English", "Hindi", "Bengali"),
            offersVideoConsult = true
        ),
        Specialist(
            id = "doc_neuro_2",
            name = "Dr. Dipankar Kalita, MD, DM",
            title = "Consultant Neurologist & Stroke Specialist",
            specialty = "Neurology",
            experienceYears = 14,
            rating = 4.8,
            reviewCount = 245,
            clinicHospital = "Tezpur Medical College & Hospital (TMCH)",
            hospitalId = "hosp_tmch_tezpur",
            address = "NH-15, Bihaguri, Tezpur, Assam",
            fee = 900.0,
            nextAvailableSlot = "Thursday at 02:00 PM",
            bio = "Specialist in migraine disorders, epilepsy, neuropathy, acute stroke thrombolysis, and neuro-rehabilitation.",
            qualifications = "MBBS, MD (Medicine), DM Neurology",
            languages = listOf("Assamese", "English", "Hindi"),
            offersVideoConsult = true
        ),
        Specialist(
            id = "doc_ortho_3",
            name = "Dr. Bikash Sharma, MS (Ortho)",
            title = "Orthopedic Surgeon & Joint Specialist",
            specialty = "Orthopedics",
            experienceYears = 12,
            rating = 4.9,
            reviewCount = 189,
            clinicHospital = "Baptist Christian Hospital, Tezpur",
            hospitalId = "hosp_bch_tezpur",
            address = "Mission Chariali, Tezpur, Assam",
            fee = 750.0,
            nextAvailableSlot = "Friday at 09:00 AM",
            bio = "Specializes in trauma fracture care, knee/shoulder arthroscopy, arthritis management, and joint replacements.",
            qualifications = "MBBS, MS Orthopedics (Assam Medical College)",
            languages = listOf("Assamese", "English", "Hindi"),
            offersVideoConsult = false
        ),
        Specialist(
            id = "doc_pedia_4",
            name = "Dr. Mousumi Hazarika, MD",
            title = "Pediatrician & Child Health Specialist",
            specialty = "Pediatrics",
            experienceYears = 11,
            rating = 4.9,
            reviewCount = 420,
            clinicHospital = "Baptist Christian Hospital, Tezpur",
            hospitalId = "hosp_bch_tezpur",
            address = "Mission Chariali, Tezpur, Assam",
            fee = 600.0,
            nextAvailableSlot = "Today at 04:30 PM",
            bio = "Dedicated pediatric care covering child immunizations, growth milestones, seasonal fever, and respiratory allergies.",
            qualifications = "MBBS, MD Pediatrics",
            languages = listOf("Assamese", "English", "Hindi", "Bengali"),
            offersVideoConsult = true
        ),
        Specialist(
            id = "doc_derm_6",
            name = "Dr. Pranab Saikia, MD (Derm)",
            title = "Clinical & Cosmetic Dermatologist",
            specialty = "Dermatology",
            experienceYears = 10,
            rating = 4.8,
            reviewCount = 278,
            clinicHospital = "Baptist Christian Hospital, Tezpur",
            hospitalId = "hosp_bch_tezpur",
            address = "Mission Chariali, Tezpur, Assam",
            fee = 650.0,
            nextAvailableSlot = "Tomorrow at 02:00 PM",
            bio = "Focused on eczema, fungal skin infections, acne care, allergy patch testing, and hair/scalp disorders.",
            qualifications = "MBBS, MD Dermatology, Venereology & Leprology",
            languages = listOf("Assamese", "English", "Hindi"),
            offersVideoConsult = true
        ),
        Specialist(
            id = "doc_gen_5",
            name = "Dr. Rituraj Goswami, MD",
            title = "General Medicine & Diabetes Physician",
            specialty = "General Medicine",
            experienceYears = 9,
            rating = 4.7,
            reviewCount = 510,
            clinicHospital = "Tezpur Medical College & Hospital (TMCH)",
            hospitalId = "hosp_tmch_tezpur",
            address = "Kanaklata OPD Clinic, Tezpur, Assam",
            fee = 500.0,
            nextAvailableSlot = "Today at 06:00 PM",
            bio = "Primary healthcare, diabetes & thyroid management, viral fever consults, hypertension care, and preventive health checkups.",
            qualifications = "MBBS, MD General Medicine",
            languages = listOf("Assamese", "English", "Hindi"),
            offersVideoConsult = true
        )
    )

    fun getLocalDiagnosticCenters(): List<DiagnosticCenter> = listOf(
        DiagnosticCenter(
            id = "center_1",
            name = "Apollo Clinic & Diagnostics Tezpur",
            address = "Ketekibari, NH-15, Tezpur, Assam",
            distanceKm = 1.6,
            rating = 4.9,
            providesHomeCollection = true,
            accreditation = "NABL Accredited Lab",
            operatingHours = "06:30 AM - 08:30 PM",
            testsOffered = listOf(
                DiagnosticTest(
                    id = "test_1",
                    name = "Complete Blood Count (CBC) & Lipid Profile",
                    category = "BLOOD_WORK",
                    sampleType = "Fasting Venous Blood",
                    turnaroundTime = "8 Hours",
                    preparationSummary = "Fast for 10-12 hours prior. Plain water permitted.",
                    price = 650.0
                ),
                DiagnosticTest(
                    id = "test_2",
                    name = "Cardiac / Brain MRI Scan with Contrast",
                    category = "RADIOLOGY",
                    sampleType = "1.5T / 3T MRI Scan",
                    turnaroundTime = "24 Hours",
                    preparationSummary = "Remove all metallic objects & jewelry. Fast 4 hours before.",
                    price = 5500.0
                ),
                DiagnosticTest(
                    id = "test_3",
                    name = "12-Lead Resting ECG & Echo Screening",
                    category = "CARDIOLOGY",
                    sampleType = "Electrophysiological Trace",
                    turnaroundTime = "Instant (30 Mins)",
                    preparationSummary = "Wear comfortable loose cotton clothing.",
                    price = 900.0
                )
            )
        ),
        DiagnosticCenter(
            id = "center_2",
            name = "Sun Diagnostic & Pathology Centre",
            address = "Mahabhairab, MC Road, Tezpur, Assam",
            distanceKm = 2.2,
            rating = 4.8,
            providesHomeCollection = true,
            accreditation = "ISO 15189 Certified Lab",
            operatingHours = "06:00 AM - 08:00 PM",
            testsOffered = listOf(
                DiagnosticTest(
                    id = "test_4",
                    name = "HbA1c, Thyroid (TSH) & Fasting Blood Sugar",
                    category = "BLOOD_WORK",
                    sampleType = "Blood Sample",
                    turnaroundTime = "6 Hours",
                    preparationSummary = "8-hour overnight fasting recommended for blood sugar.",
                    price = 480.0
                ),
                DiagnosticTest(
                    id = "test_5",
                    name = "Whole Abdomen & Pelvic Ultrasound (USG)",
                    category = "RADIOLOGY",
                    sampleType = "Ultrasound Scan",
                    turnaroundTime = "Same Day",
                    preparationSummary = "Drink 1 liter of water 1 hour before and keep bladder full.",
                    price = 1100.0
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
            anesthesiaType = "Throat numbing spray or light sedation",
            recoveryDays = 1,
            preparationChecklist = listOf(
                ProcedureChecklistStep("endo_1", "Blood Thinner & Anticoagulant Protocol", "Consult your doctor regarding halting aspirin, clopidogrel, or warfarin 5-7 days prior.", "7 Days Before"),
                ProcedureChecklistStep("endo_2", "Strict Fasting from Solid Food", "Do not eat solid food after midnight or for at least 8 hours prior to procedure time.", "Midnight Before"),
                ProcedureChecklistStep("endo_3", "Clear Liquid Cut-off", "Stop drinking clear water exactly 3 hours before hospital check-in.", "3 Hours Before"),
                ProcedureChecklistStep("endo_4", "Bring a Family Companion", "Arrange for an adult family member to accompany you home; avoid driving for 24 hours if sedated.", "Day of Procedure")
            ),
            procedureOverview = "You will lie comfortably on your left side. A mouthguard protects your teeth. Mild sedation or numbing spray keeps you comfortable while the doctor inspects the stomach lining and takes micro-biopsies if needed.",
            postCareInstructions = listOf(
                "Rest quietly at home for the remainder of the day.",
                "Start with gentle sips of water, then light dal/khichdi once swallowing feels normal.",
                "A mild sore throat is normal and settles within 24 hours.",
                "Do not drive or operate heavy machinery for 24 hours if sedated."
            ),
            warningRedFlags = listOf(
                "Difficulty or sharp pain swallowing that worsens after 4 hours",
                "Fever greater than 101°F (38.3°C) or persistent chills",
                "Vomiting dark coffee-ground material or bright red blood",
                "Severe, worsening abdominal or chest pain"
            )
        ),
        ProcedureGuide(
            id = "proc_mri_contrast",
            title = "MRI Scan with Gadolinium Contrast",
            category = "DIAGNOSTIC_IMAGING",
            summary = "Non-invasive magnetic resonance scan utilizing safe contrast dye to clearly image blood vessels and tissues.",
            estimatedDuration = "45 to 60 minutes",
            anesthesiaType = "None (earplugs/headphones provided)",
            recoveryDays = 0,
            preparationChecklist = listOf(
                ProcedureChecklistStep("mri_1", "Implant & Metal Safety Screening", "Inform the technician of any pacemakers, metal plates, stents, or dental implants.", "At Booking"),
                ProcedureChecklistStep("mri_2", "Kidney Function Check (Serum Creatinine)", "Keep a recent Serum Creatinine / eGFR lab report ready in your Nidanmita Records.", "3 Days Before"),
                ProcedureChecklistStep("mri_3", "Remove Jewelry & Wear Cotton", "Remove bangles, chains, watches, belt buckles, and hairpins before entering the scan room.", "Morning of Scan")
            ),
            procedureOverview = "You lie on a padded table that glides into the MRI scanner. Noise-dampening headphones help reduce scanner humming. An IV contrast injection is given partway through to highlight detail.",
            postCareInstructions = listOf(
                "Drink 2-3 extra glasses of water through the day to help your kidneys flush the contrast dye.",
                "Resume your normal daily meals and prescribed medicines right away.",
                "Digital reports are shared by the diagnostic centre and can be uploaded to your Nidanmita Locker within 24 hours."
            ),
            warningRedFlags = listOf(
                "Hives, skin itching, or sudden rash",
                "Swelling around eyes, lips, or throat",
                "Sudden dizziness or shortness of breath (call Emergency 108 / SOS immediately)"
            )
        ),
        ProcedureGuide(
            id = "proc_knee_arthroscopy",
            title = "Knee Arthroscopy & Meniscal Repair",
            category = "SURGICAL",
            summary = "Minimally invasive keyhole surgical repair of torn knee cartilage (meniscus) or ligament smoothing.",
            estimatedDuration = "60 to 90 minutes",
            anesthesiaType = "Spinal or General Anesthesia",
            recoveryDays = 7,
            preparationChecklist = listOf(
                ProcedureChecklistStep("knee_1", "Pre-operative Clearance & Lab Panel", "Complete pre-op ECG, CBC blood panel, and anesthesia checkup at the hospital OPD.", "5 Days Before"),
                ProcedureChecklistStep("knee_2", "Walker / Crutches & Cold Pack", "Arrange a walker or elbow crutches and ice packs for post-surgery swelling control.", "2 Days Before"),
                ProcedureChecklistStep("knee_3", "Antiseptic Wash", "Wash the knee and leg cleanly with antiseptic soap the evening and morning before surgery.", "Night & Morning Before")
            ),
            procedureOverview = "Tiny 5mm keyhole openings allow a miniature camera and precision instruments to enter the knee joint cleanly and repair or trim torn cartilage.",
            postCareInstructions = listOf(
                "Follow R.I.C.E: Rest, Ice pack (20 min on, 20 min off), Compression bandage, and Elevation above heart level.",
                "Keep surgical dressings completely dry for 72 hours.",
                "Do gentle ankle-pump exercises every hour while resting to keep blood flowing smoothly.",
                "Take prescribed pain and antibiotic medicines strictly after food."
            ),
            warningRedFlags = listOf(
                "Calf pain, tight swelling, or unusual warmth behind the lower leg",
                "Fever above 101°F or spreading redness around stitches",
                "Sudden chest pain or shortness of breath",
                "Severe pain not relieved by prescribed medicines"
            )
        ),
        ProcedureGuide(
            id = "proc_cataract",
            title = "Cataract Extraction with Intraocular Lens (IOL)",
            category = "SURGICAL",
            summary = "Gentle phacoemulsification removal of a cloudy natural eye lens replaced with a clear foldable intraocular lens.",
            estimatedDuration = "15 to 20 minutes",
            anesthesiaType = "Numbing eye drops (painless, no stitches)",
            recoveryDays = 2,
            preparationChecklist = listOf(
                ProcedureChecklistStep("cat_1", "Antibiotic Eye Drop Regimen", "Start prescribed antibiotic eye drops 3 days prior as advised by your eye surgeon.", "3 Days Before"),
                ProcedureChecklistStep("cat_2", "Clean Face & No Cosmetics", "Wash your face gently on surgery morning. Avoid kajal, creams, powder, or perfume.", "Morning of Surgery")
            ),
            procedureOverview = "A micro-opening is made at the edge of the cornea. Ultrasound energy gently clears the cloudy cataract and places a clear foldable lens inside the eye.",
            postCareInstructions = listOf(
                "Wear the protective dark glasses/eye shield as advised, especially while sleeping for the first week.",
                "Avoid bending below the waist, lifting heavy weights (>4 kg), or splashing water into the eye for 7 days.",
                "Do not rub your eye and instill post-op eye drops strictly on schedule."
            ),
            warningRedFlags = listOf(
                "Sudden drop in vision or dark curtain-like shadow",
                "Severe throbbing eye pain",
                "Increasing yellowish discharge or heavy redness"
            )
        )
    )
}
