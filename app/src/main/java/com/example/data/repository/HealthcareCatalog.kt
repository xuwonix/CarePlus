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
        "Downtown Metro",
        "Midtown Medical District",
        "North Hills",
        "Westside Valley",
        "Eastbrook"
    )

    fun getPopularSpecialties(): List<MedicalSpecialtyItem> = listOf(
        MedicalSpecialtyItem("spec_cardio", "Cardiology", "Heart & Vascular", 14),
        MedicalSpecialtyItem("spec_neuro", "Neurology", "Brain & Spine", 9),
        MedicalSpecialtyItem("spec_ortho", "Orthopedics", "Bone & Joint", 11),
        MedicalSpecialtyItem("spec_pedia", "Pediatrics", "Child Health", 16),
        MedicalSpecialtyItem("spec_derm", "Dermatology", "Skin & Hair", 8),
        MedicalSpecialtyItem("spec_gen", "General Medicine", "Primary Care", 22)
    )

    fun getHomeCareServices(): List<HomeCareService> = listOf(
        HomeCareService(
            id = "hc_nurse_1",
            title = "Skilled Nursing & Post-Op Dressing",
            category = "NURSING",
            duration = "60 Mins Visit",
            description = "Certified RN visit for surgical wound care, suture removal, vitals monitoring, and catheter/dressing maintenance.",
            price = 65.0,
            providerTeam = "CarePulse Home Nursing Team"
        ),
        HomeCareService(
            id = "hc_physio_2",
            title = "Home Physiotherapy & Mobility Rehab",
            category = "PHYSIOTHERAPY",
            duration = "45 Mins Session",
            description = "Licensed physiotherapist brings portable electrotherapy and guided exercises for knee, back, or stroke recovery.",
            price = 75.0,
            providerTeam = "ActiveLife Home Rehab"
        ),
        HomeCareService(
            id = "hc_elder_3",
            title = "Elderly Care & Assisted Living Shift",
            category = "ELDERLY_CARE",
            duration = "4 Hours Shift",
            description = "Trained medical attendant for senior vitals tracking, medication reminders, mobility help, and daily wellness.",
            price = 110.0,
            providerTeam = "SilverCare Attendant Network"
        ),
        HomeCareService(
            id = "hc_iv_4",
            title = "Home IV Hydration & Antibiotic Infusion",
            category = "IV_THERAPY",
            duration = "45 Mins Visit",
            description = "Doctor-prescribed IV fluid, vitamin drip, or prescribed antibiotic infusion administered safely at home by an RN.",
            price = 85.0,
            providerTeam = "CarePulse Infusion Nurses"
        ),
        HomeCareService(
            id = "hc_doc_5",
            title = "General Physician Home Visit",
            category = "DOCTOR_VISIT",
            duration = "30 Mins Visit",
            description = "On-demand physician house call for fever, respiratory symptoms, acute pain, or immobile patients.",
            price = 120.0,
            providerTeam = "Metro Mobile Doctors"
        )
    )

    fun getNearbyHospitalsAndCenters(selectedArea: String = "Downtown Metro"): List<NearbyFacility> {
        val offset = when (selectedArea) {
            "Midtown Medical District" -> 0.4
            "North Hills" -> 1.3
            "Westside Valley" -> 0.9
            "Eastbrook" -> 1.7
            else -> 0.0
        }
        return listOf(
            NearbyFacility(
                id = "hosp_metro_trauma",
                name = "Metro General Trauma & Cardiac Center",
                facilityType = "HOSPITAL",
                address = "742 Medical Plaza Drive",
                distanceKm = ((1.2 + offset) * 10).toInt() / 10.0,
                rating = 4.9,
                reviewCount = 840,
                isOpen24x7 = true,
                hasEmergencyUnit = true,
                providesHomeCollection = false,
                departments = listOf("24/7 ER & Trauma", "Cardiology", "Neurology", "ICU"),
                waitTimeMinutes = 12,
                phone = "+1 (555) 204-8800",
                accreditation = "JCI & Level-1 Trauma Center",
                acceptsCashlessInsurance = true
            ),
            NearbyFacility(
                id = "center_1",
                name = "Metro Health Advanced Imaging & Diagnostics",
                facilityType = "DIAGNOSTIC_CENTER",
                address = "450 Health Sciences Drive",
                distanceKm = ((1.8 + offset) * 10).toInt() / 10.0,
                rating = 4.9,
                reviewCount = 412,
                isOpen24x7 = false,
                hasEmergencyUnit = false,
                providesHomeCollection = true,
                departments = listOf("3T MRI", "CT Scan", "Lipid & Blood Panels", "ECG"),
                waitTimeMinutes = 10,
                phone = "+1 (555) 310-4420",
                accreditation = "CAP & NABL Accredited Lab",
                acceptsCashlessInsurance = true
            ),
            NearbyFacility(
                id = "hosp_activelife",
                name = "ActiveLife Sports & Orthopedic Hospital",
                facilityType = "HOSPITAL",
                address = "310 Valley Boulevard",
                distanceKm = ((2.6 + offset) * 10).toInt() / 10.0,
                rating = 4.8,
                reviewCount = 520,
                isOpen24x7 = true,
                hasEmergencyUnit = true,
                providesHomeCollection = false,
                departments = listOf("Orthopedics", "Physiotherapy", "Sports Surgery", "X-Ray"),
                waitTimeMinutes = 15,
                phone = "+1 (555) 419-7711",
                accreditation = "NABH Orthopedic Center of Excellence",
                acceptsCashlessInsurance = true
            ),
            NearbyFacility(
                id = "center_2",
                name = "BioPath Premier Clinical Laboratories",
                facilityType = "DIAGNOSTIC_CENTER",
                address = "780 Commerce Court, Suite 102",
                distanceKm = ((3.2 + offset) * 10).toInt() / 10.0,
                rating = 4.8,
                reviewCount = 630,
                isOpen24x7 = false,
                hasEmergencyUnit = false,
                providesHomeCollection = true,
                departments = listOf("Home Phlebotomy", "HbA1c & Diabetes", "Ultrasound", "Pathology"),
                waitTimeMinutes = 5,
                phone = "+1 (555) 602-1988",
                accreditation = "ISO 15189 Certified Diagnostics",
                acceptsCashlessInsurance = true
            )
        ).sortedBy { it.distanceKm }
    }

    fun getLocalSpecialists(): List<Specialist> = listOf(
        Specialist(
            id = "doc_cardio_1",
            name = "Dr. Elena Rostova, MD",
            title = "Senior Interventional Cardiologist",
            specialty = "Cardiology",
            experienceYears = 16,
            rating = 4.9,
            reviewCount = 312,
            clinicHospital = "Metro General Trauma & Cardiac Center",
            hospitalId = "hosp_metro_trauma",
            address = "742 Medical Plaza, Suite 400",
            fee = 140.0,
            nextAvailableSlot = "Tomorrow at 10:30 AM",
            bio = "Board-certified in cardiovascular disease and interventional cardiology with specialization in arrhythmia, hypertension, and preventive heart care.",
            qualifications = "MD (Johns Hopkins), FACC, Fellowship in Interventional Cardiology",
            languages = listOf("English", "Russian", "Spanish"),
            offersVideoConsult = true
        ),
        Specialist(
            id = "doc_neuro_2",
            name = "Dr. Marcus Chen, MD, PhD",
            title = "Consultant Neurologist & Spine Specialist",
            specialty = "Neurology",
            experienceYears = 14,
            rating = 4.8,
            reviewCount = 245,
            clinicHospital = "Metro General Trauma & Cardiac Center",
            hospitalId = "hosp_metro_trauma",
            address = "890 Innovation Parkway, Tower B",
            fee = 160.0,
            nextAvailableSlot = "Thursday at 02:00 PM",
            bio = "Expert in complex migraine disorders, neuropathy, stroke recovery, neuro-rehab, and pre-op cranial assessments.",
            qualifications = "MD, PhD (Stanford Neuroscience), Board Certified ABPN",
            languages = listOf("English", "Mandarin"),
            offersVideoConsult = true
        ),
        Specialist(
            id = "doc_ortho_3",
            name = "Dr. Sarah Jenkins, MD",
            title = "Orthopedic Surgeon & Joint Replacement",
            specialty = "Orthopedics",
            experienceYears = 12,
            rating = 4.9,
            reviewCount = 189,
            clinicHospital = "ActiveLife Sports & Orthopedic Hospital",
            hospitalId = "hosp_activelife",
            address = "310 Valley Boulevard",
            fee = 130.0,
            nextAvailableSlot = "Friday at 09:00 AM",
            bio = "Specializes in minimally invasive knee/shoulder arthroscopy, sports injury rehabilitation, and joint replacements.",
            qualifications = "MD (Harvard Medical School), FAAOS",
            languages = listOf("English", "French"),
            offersVideoConsult = false
        ),
        Specialist(
            id = "doc_pedia_4",
            name = "Dr. Tariq Al-Mansoor, MD",
            title = "Pediatrician & Adolescent Health",
            specialty = "Pediatrics",
            experienceYears = 10,
            rating = 4.9,
            reviewCount = 420,
            clinicHospital = "Metro General Trauma & Cardiac Center",
            hospitalId = "hosp_metro_trauma",
            address = "120 Park Lane, Children's Wing",
            fee = 95.0,
            nextAvailableSlot = "Today at 04:30 PM",
            bio = "Warm, attentive pediatric care specializing in developmental milestones, immunizations, and childhood respiratory allergies.",
            qualifications = "MD, FAAP (American Academy of Pediatrics)",
            languages = listOf("English", "Arabic"),
            offersVideoConsult = true
        ),
        Specialist(
            id = "doc_derm_6",
            name = "Dr. Priya Nair, MD",
            title = "Clinical & Cosmetic Dermatologist",
            specialty = "Dermatology",
            experienceYears = 11,
            rating = 4.9,
            reviewCount = 278,
            clinicHospital = "ActiveLife Sports & Orthopedic Hospital",
            hospitalId = "hosp_activelife",
            address = "215 Oakridge Avenue, Suite 2B",
            fee = 115.0,
            nextAvailableSlot = "Tomorrow at 02:00 PM",
            bio = "Focused on eczema, acne therapy, mole mapping, allergic skin conditions, and minimally invasive dermatologic procedures.",
            qualifications = "MD, FAAD (Board Certified Dermatologist)",
            languages = listOf("English", "Hindi"),
            offersVideoConsult = true
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
            hospitalId = "hosp_metro_trauma",
            address = "55 Highland Road",
            fee = 75.0,
            nextAvailableSlot = "Today at 06:00 PM",
            bio = "Comprehensive primary healthcare, chronic diabetes and hypertension management, fever consults, and annual wellness screenings.",
            qualifications = "DO, Board Certified Internal Medicine",
            languages = listOf("English", "Spanish"),
            offersVideoConsult = true
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
            accreditation = "CAP & NABL Accredited",
            operatingHours = "06:30 AM - 09:30 PM",
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
            accreditation = "ISO 15189 Certified Lab",
            operatingHours = "06:00 AM - 08:00 PM",
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
                ProcedureChecklistStep("endo_1", "Blood Thinner & Anticoagulant Protocol", "Consult prescribing doctor regarding halting aspirin, warfarin, or apixaban 5-7 days prior.", "7 Days Before"),
                ProcedureChecklistStep("endo_2", "Strict Fasting from Solid Food", "Do not eat solid foods after midnight or for at least 8 hours prior to procedure time.", "Midnight Before"),
                ProcedureChecklistStep("endo_3", "Clear Liquid Cut-off", "Stop drinking clear liquids (water, apple juice) exactly 3 hours before check-in.", "3 Hours Before"),
                ProcedureChecklistStep("endo_4", "Designated Responsible Adult Driver", "Arrange for an adult companion to drive you home; driving or signing legal documents is prohibited for 24h.", "Day of Procedure")
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
                ProcedureChecklistStep("mri_1", "Implant & Metal Safety Screening", "Report any cardiac pacemakers, cochlear implants, aneurysm clips, metal dental implants, or shrapnel.", "At Booking"),
                ProcedureChecklistStep("mri_2", "Kidney Function Check (eGFR)", "Ensure blood creatinine/eGFR results from within past 30 days are uploaded to your locker.", "3 Days Before"),
                ProcedureChecklistStep("mri_3", "Remove All Body Jewelry & Wear Cotton", "Remove piercings, watches, belts, underwire bras, and hairpins. Wear plain athletic clothing without metal zippers.", "Morning of Scan")
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
                ProcedureChecklistStep("knee_1", "Pre-operative Clearance & Lab Panel", "Complete pre-op ECG, CBC blood panel, and anesthesia review at the hospital clinic.", "5 Days Before"),
                ProcedureChecklistStep("knee_2", "Crutches & Ice Machine Preparation", "Adjust assistive crutches to standing height and fill freezer with gel packs for cold compression.", "2 Days Before"),
                ProcedureChecklistStep("knee_3", "Antimicrobial Hibiclens Shower", "Wash operative leg with chlorhexidine antiseptic soap the evening and morning before surgery.", "Night & Morning Before")
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
                ProcedureChecklistStep("cat_1", "Antibiotic Eye Drop Regimen", "Begin prescribed topical antibiotic and anti-inflammatory eye drops 3 days prior as instructed.", "3 Days Before"),
                ProcedureChecklistStep("cat_2", "No Face Makeup or Perfumes", "Wash face thoroughly with gentle soap. Do not apply eye shadow, eyeliner, mascara, or face lotions.", "Morning of Surgery")
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
