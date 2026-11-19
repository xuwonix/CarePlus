package com.example.ui.i18n

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.HealthServiceCategory

enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val shortBadge: String
) {
    ENGLISH("en", "English", "English", "EN"),
    HINDI("hi", "हिन्दी", "Hindi", "हि"),
    ASSAMESE("as", "অসমীয়া", "Assamese", "অ");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: ENGLISH
    }
}

object LanguagePreferences {
    private const val PREFS_NAME = "nidanmitra_language_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    fun load(context: Context): AppLanguage {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return AppLanguage.fromCode(prefs.getString(KEY_LANGUAGE, AppLanguage.ENGLISH.code))
    }

    fun save(context: Context, language: AppLanguage) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
    }
}

data class AppStrings(
    // App & Auth
    val appName: String,
    val authSubtitle: String,
    val authDescription: String,
    val signInWithGoogle: String,
    val authenticating: String,
    val selectLanguage: String,
    val languageChangedToast: (AppLanguage) -> String,

    // Bottom Navigation & Top Bar
    val navHome: String,
    val navBooking: String,
    val navEmergency: String,
    val navRecords: String,
    val navProfile: String,
    val notificationsLabel: String,
    val languageLabel: String,
    val darkModeLabel: String,
    val darkModeSubtitle: String,
    val switchToDarkMode: String,
    val switchToLightMode: String,
    val themeChangedToast: (Boolean) -> String,

    // Home Tab
    val helloPrefix: String,
    val homeSubtitle: String,
    val searchPlaceholder: String,
    val upcomingAppointments: String,
    val viewAll: String,
    val bookNow: String,
    val noUpcomingVisits: String,
    val noUpcomingVisitsSubtitle: String,
    val healthServiceCategories: String,
    val popularSpecialties: String,
    val seeAll: String,
    val nearbyHospitalsTitle: String,
    val bookTestAction: String,
    val showingFacilitiesNear: String,
    val viewHospitalDoctors: String,
    val viewLabTests: String,
    val doctorsCountSuffix: String,

    // Service Categories
    val catDoctorsTitle: String,
    val catDoctorsSub: String,
    val catLabsTitle: String,
    val catLabsSub: String,
    val catHomeCareTitle: String,
    val catHomeCareSub: String,
    val catAmbulanceTitle: String,
    val catAmbulanceSub: String,
    val catInsuranceTitle: String,
    val catInsuranceSub: String,
    val catGuidesTitle: String,
    val catGuidesSub: String,

    // Booking Tab
    val bookingTabDoctors: String,
    val bookingTabLabs: String,
    val bookingTabHomeCare: String,
    val bookingTabMyBookings: String,
    val filterDoctorsPlaceholder: String,
    val filterLabsPlaceholder: String,
    val videoConsult: String,
    val feeLowToHigh: String,
    val bookSlot: String,
    val chatAction: String,
    val bookedOnly: String,

    // Emergency Tab
    val emergencySosTitle: String,
    val emergencySosDesc: String,
    val dispatchAmbulanceCta: String,
    val whichAmbulanceTitle: String,
    val nearbyEmergencyHospitals: String,
    val nearbyEmergencySubtitle: String,

    // Records Tab
    val healthLockerTab: String,
    val procedureGuidesTab: String,
    val scanStoreTitle: String,
    val scanStoreSubtitle: String,
    val scanUploadButton: String,

    // Profile & Insurance
    val familyProfilesTitle: String,
    val addMember: String,
    val paymentsReceiptsTitle: String,
    val myHealthInsurance: String,
    val addPolicy: String,
    val buyInsurers: String,
    val assistance: String,
    val fileClaim: String,
    val openHub: String,
    val helpAndSupport: String,
    val messageCareCoordinator: String,
    val signOut: String,
    val insuranceHubTitle: String,
    val insuranceTabPolicies: String,
    val insuranceTabPartners: String,
    val insuranceTabAssistance: String,
    val addMyPurchasedPolicy: String,
    val exploreBuyInsurance: String,
    val buyGetQuote: String,
    val checkEligibility: String,
    val askAdvisor: String,
    val chatWithAdvisor: String,

    // Directions & AI Booking Assistant
    val directionsButton: String,
    val getDirectionsMaps: String,
    val aiAssistantTitle: String,
    val aiAssistantSubtitle: String,
    val aiAssistantFabLabel: String,
    val aiAssistantBannerTitle: String,
    val aiAssistantBannerDesc: String,
    val aiAssistantBannerCta: String,
    val aiAssistantInputPlaceholder: String,
    val aiAssistantBookAction: String,
    val aiAssistantNewChat: String,
    val voiceTapToSpeak: String,
    val voiceListeningHint: String,
    val voiceStopAndSend: String,
    val voiceCancel: String,
    val voiceAutoSpeakOn: String,
    val voiceAutoSpeakOff: String,
    val voiceListenLabel: String,
    val voiceStopSpeakingLabel: String,
    val voiceMessageLabel: String,
    val voicePermissionDenied: String
) {
    fun categoryTitle(category: HealthServiceCategory): String = when (category) {
        HealthServiceCategory.SPECIALIST_CONSULT -> catDoctorsTitle
        HealthServiceCategory.LAB_DIAGNOSTICS -> catLabsTitle
        HealthServiceCategory.HOME_CARE -> catHomeCareTitle
        HealthServiceCategory.EMERGENCY_EMS -> catAmbulanceTitle
        HealthServiceCategory.HEALTH_INSURANCE -> catInsuranceTitle
        HealthServiceCategory.PROCEDURE_GUIDES -> catGuidesTitle
    }

    fun categorySubtitle(category: HealthServiceCategory): String = when (category) {
        HealthServiceCategory.SPECIALIST_CONSULT -> catDoctorsSub
        HealthServiceCategory.LAB_DIAGNOSTICS -> catLabsSub
        HealthServiceCategory.HOME_CARE -> catHomeCareSub
        HealthServiceCategory.EMERGENCY_EMS -> catAmbulanceSub
        HealthServiceCategory.HEALTH_INSURANCE -> catInsuranceSub
        HealthServiceCategory.PROCEDURE_GUIDES -> catGuidesSub
    }
}

val EnglishAppStrings = AppStrings(
    appName = "Nidanmitra",
    authSubtitle = "Tezpur & Biswanath Chariali Healthcare",
    authDescription = "Connecting patients across Tezpur & Biswanath Chariali with local specialists, rapid ambulance dispatch, diagnostic labs, health insurance partners, and clinical guidance.",
    signInWithGoogle = "Sign in with Google",
    authenticating = "Authenticating...",
    selectLanguage = "Select Language",
    languageChangedToast = { lang -> "Language set to ${lang.nativeName}" },

    navHome = "Home",
    navBooking = "Booking",
    navEmergency = "Emergency Alert",
    navRecords = "Records",
    navProfile = "Profile",
    notificationsLabel = "Notifications",
    languageLabel = "Change Language",
    darkModeLabel = "Dark Mode",
    darkModeSubtitle = "Comfortable low-light view for night hours",
    switchToDarkMode = "Switch to Dark Mode",
    switchToLightMode = "Switch to Light Mode",
    themeChangedToast = { isDark -> if (isDark) "Dark Mode enabled" else "Light Mode enabled" },

    helloPrefix = "Hello",
    homeSubtitle = "Doctors, labs, home care & hospitals",
    searchPlaceholder = "Search doctors, tests, home care, hospitals...",
    upcomingAppointments = "Upcoming Appointments",
    viewAll = "View All",
    bookNow = "Book Now",
    noUpcomingVisits = "No upcoming visits scheduled",
    noUpcomingVisitsSubtitle = "Book a specialist consultation or diagnostic lab test in Tezpur & Biswanath",
    healthServiceCategories = "Health Service Categories",
    popularSpecialties = "Popular Medical Specialties",
    seeAll = "See All",
    nearbyHospitalsTitle = "Nearby Hospitals & Diagnostic Centres",
    bookTestAction = "Book Test",
    showingFacilitiesNear = "Showing care facilities near",
    viewHospitalDoctors = "View Hospital & Doctors",
    viewLabTests = "View Lab & Tests",
    doctorsCountSuffix = "Doctors",

    catDoctorsTitle = "Doctors",
    catDoctorsSub = "OPD & Video",
    catLabsTitle = "Lab Tests",
    catLabsSub = "Home Pickup",
    catHomeCareTitle = "Home Care",
    catHomeCareSub = "Nurse & Physio",
    catAmbulanceTitle = "Ambulance",
    catAmbulanceSub = "108 Rapid SOS",
    catInsuranceTitle = "Insurance",
    catInsuranceSub = "Cashless & Buy",
    catGuidesTitle = "Care Guides",
    catGuidesSub = "Pre/Post-Op",

    bookingTabDoctors = "Doctors",
    bookingTabLabs = "Lab Tests",
    bookingTabHomeCare = "Home Care",
    bookingTabMyBookings = "My Bookings",
    filterDoctorsPlaceholder = "Filter doctors, specialties, hospitals...",
    filterLabsPlaceholder = "Search blood panels, MRI, ECG, ultrasound...",
    videoConsult = "Video Consult",
    feeLowToHigh = "Fee: Low to High",
    bookSlot = "Book Slot",
    chatAction = "Chat",
    bookedOnly = "Booked Only",

    emergencySosTitle = "Emergency Ambulance SOS",
    emergencySosDesc = "Request a Tezpur / Biswanath 108 or hospital ambulance right to your location with direct casualty pre-alert.",
    dispatchAmbulanceCta = "DISPATCH AMBULANCE SOS",
    whichAmbulanceTitle = "Which Ambulance Should I Choose?",
    nearbyEmergencyHospitals = "24/7 Emergency Hospitals Nearby",
    nearbyEmergencySubtitle = "Sorted by distance from your selected Tezpur / Biswanath area",

    healthLockerTab = "Health Locker",
    procedureGuidesTab = "Procedure Guides",
    scanStoreTitle = "Scan & Store Health Records",
    scanStoreSubtitle = "Snap a photo or upload PDFs of prescriptions, lab reports, and scans—no manual typing required.",
    scanUploadButton = "Scan or Upload Prescription / Report",

    familyProfilesTitle = "Family Profiles",
    addMember = "Add Member",
    paymentsReceiptsTitle = "Payments & Receipts",
    myHealthInsurance = "My Health Insurance",
    addPolicy = "Add Policy",
    buyInsurers = "Buy / Insurers",
    assistance = "Assistance",
    fileClaim = "File Claim",
    openHub = "Open Hub",
    helpAndSupport = "Help & Support (FAQs & Tickets)",
    messageCareCoordinator = "Message Nidanmitra Care Coordinator",
    signOut = "Sign Out",
    insuranceHubTitle = "Health Insurance Hub",
    insuranceTabPolicies = "My Policies",
    insuranceTabPartners = "Tied-Up Insurers",
    insuranceTabAssistance = "Assistance",
    addMyPurchasedPolicy = "Add My Purchased Policy",
    exploreBuyInsurance = "Explore & Buy Tied-Up Insurance",
    buyGetQuote = "Buy / Get Quote",
    checkEligibility = "Check Eligibility",
    askAdvisor = "Ask Advisor",
    chatWithAdvisor = "Chat with Insurance Advisor",

    directionsButton = "Directions",
    getDirectionsMaps = "Get Directions on Google Maps",
    aiAssistantTitle = "Nidanmitra AI Helper",
    aiAssistantSubtitle = "Guided booking in English, हिन्दी & অসমীয়া",
    aiAssistantFabLabel = "AI Helper",
    aiAssistantBannerTitle = "Need Help Booking?",
    aiAssistantBannerDesc = "Speak or type with Nidanmitra AI in English, Hindi, or Assamese (অসমীয়া) to easily find & book doctors, lab tests, or home care.",
    aiAssistantBannerCta = "Ask AI",
    aiAssistantInputPlaceholder = "Type or tap mic to speak...",
    aiAssistantBookAction = "Book Now",
    aiAssistantNewChat = "New Chat",
    voiceTapToSpeak = "Tap mic to speak",
    voiceListeningHint = "Listening... Speak in Assamese, Hindi or English",
    voiceStopAndSend = "Send Voice",
    voiceCancel = "Cancel",
    voiceAutoSpeakOn = "Voice replies ON",
    voiceAutoSpeakOff = "Voice replies OFF",
    voiceListenLabel = "Listen",
    voiceStopSpeakingLabel = "Stop",
    voiceMessageLabel = "Voice message",
    voicePermissionDenied = "Microphone permission is needed to use Voice Assistance."
)

val HindiAppStrings = AppStrings(
    appName = "निदानमित्र",
    authSubtitle = "तेजपुर और विश्वनाथ चारियाली स्वास्थ्य सेवा",
    authDescription = "तेजपुर और विश्वनाथ चारियाली के मरीजों को स्थानीय विशेषज्ञ डॉक्टरों, एम्बुलेंस, डायग्नोस्टिक लैब, स्वास्थ्य बीमा और चिकित्सा मार्गदर्शन से जोड़ना।",
    signInWithGoogle = "Google से साइन इन करें",
    authenticating = "प्रमाणीकरण हो रहा है...",
    selectLanguage = "भाषा चुनें",
    languageChangedToast = { lang -> "भाषा ${lang.nativeName} में बदल गई है" },

    navHome = "होम",
    navBooking = "बुकिंग",
    navEmergency = "आपातकाल",
    navRecords = "रिकॉर्ड्स",
    navProfile = "प्रोफ़ाइल",
    notificationsLabel = "सूचनाएँ",
    languageLabel = "भाषा बदलें",
    darkModeLabel = "डार्क मोड",
    darkModeSubtitle = "रात में आँखों के आराम के लिए डार्क थीम",
    switchToDarkMode = "डार्क मोड चालू करें",
    switchToLightMode = "लाइट मोड चालू करें",
    themeChangedToast = { isDark -> if (isDark) "डार्क मोड चालू हो गया" else "लाइट मोड चालू हो गया" },

    helloPrefix = "नमस्ते",
    homeSubtitle = "डॉक्टर, लैब टेस्ट, होम केयर और अस्पताल",
    searchPlaceholder = "डॉक्टर, टेस्ट, होम केयर, अस्पताल खोजें...",
    upcomingAppointments = "आगामी अपॉइंटमेंट",
    viewAll = "सभी देखें",
    bookNow = "अभी बुक करें",
    noUpcomingVisits = "कोई आगामी अपॉइंटमेंट नहीं है",
    noUpcomingVisitsSubtitle = "तेजपुर और विश्वनाथ में विशेषज्ञ डॉक्टर या लैब टेस्ट बुक करें",
    healthServiceCategories = "स्वास्थ्य सेवा श्रेणियाँ",
    popularSpecialties = "लोकप्रिय चिकित्सा विशेषज्ञताएँ",
    seeAll = "सभी देखें",
    nearbyHospitalsTitle = "नज़दीकी अस्पताल और डायग्नोस्टिक केंद्र",
    bookTestAction = "टेस्ट बुक करें",
    showingFacilitiesNear = "आपके क्षेत्र के पास की सुविधाएँ:",
    viewHospitalDoctors = "अस्पताल और डॉक्टर देखें",
    viewLabTests = "लैब और टेस्ट देखें",
    doctorsCountSuffix = "डॉक्टर",

    catDoctorsTitle = "डॉक्टर",
    catDoctorsSub = "OPD और वीडियो",
    catLabsTitle = "लैब टेस्ट",
    catLabsSub = "होम सैंपल",
    catHomeCareTitle = "होम केयर",
    catHomeCareSub = "नर्स व फ़िज़ियो",
    catAmbulanceTitle = "एम्बुलेंस",
    catAmbulanceSub = "108 आपात SOS",
    catInsuranceTitle = "बीमा",
    catInsuranceSub = "कैशलेस व खरीदें",
    catGuidesTitle = "गाइड",
    catGuidesSub = "सर्जरी देखभाल",

    bookingTabDoctors = "डॉक्टर",
    bookingTabLabs = "लैब टेस्ट",
    bookingTabHomeCare = "होम केयर",
    bookingTabMyBookings = "मेरी बुकिंग",
    filterDoctorsPlaceholder = "डॉक्टर, विशेषज्ञता या अस्पताल खोजें...",
    filterLabsPlaceholder = "ब्लड टेस्ट, MRI, ECG, अल्ट्रासाउंड खोजें...",
    videoConsult = "वीडियो परामर्श",
    feeLowToHigh = "शुल्क: कम से अधिक",
    bookSlot = "स्लॉट बुक करें",
    chatAction = "चैट",
    bookedOnly = "केवल बुकिंग पर",

    emergencySosTitle = "आपातकालीन एम्बुलेंस SOS",
    emergencySosDesc = "तेजपुर / विश्वनाथ 108 या अस्पताल एम्बुलेंस तुरंत अपने स्थान पर बुलाएँ।",
    dispatchAmbulanceCta = "एम्बुलेंस SOS भेजें",
    whichAmbulanceTitle = "कौन सी एम्बुलेंस चुनें?",
    nearbyEmergencyHospitals = "नज़दीकी 24/7 आपातकालीन अस्पताल",
    nearbyEmergencySubtitle = "आपके चुने हुए क्षेत्र से दूरी के अनुसार क्रमबद्ध",

    healthLockerTab = "हेल्थ लॉकर",
    procedureGuidesTab = "प्रक्रिया गाइड",
    scanStoreTitle = "मेडिकल रिकॉर्ड स्कैन और सेव करें",
    scanStoreSubtitle = "प्रिस्क्रिप्शन, लैब रिपोर्ट और स्कैन की फ़ोटो या PDF आसानी से अपलोड करें।",
    scanUploadButton = "प्रिस्क्रिप्शन / रिपोर्ट स्कैन या अपलोड करें",

    familyProfilesTitle = "परिवार के सदस्य",
    addMember = "सदस्य जोड़ें",
    paymentsReceiptsTitle = "भुगतान और रसीदें",
    myHealthInsurance = "मेरा स्वास्थ्य बीमा",
    addPolicy = "पॉलिसी जोड़ें",
    buyInsurers = "बीमा खरीदें",
    assistance = "सहायता",
    fileClaim = "क्लेम करें",
    openHub = "हब खोलें",
    helpAndSupport = "सहायता और समर्थन (FAQ व टिकट)",
    messageCareCoordinator = "निदानमित्र केयर कोऑर्डिनेटर को संदेश भेजें",
    signOut = "साइन आउट",
    insuranceHubTitle = "स्वास्थ्य बीमा हब",
    insuranceTabPolicies = "मेरी पॉलिसियाँ",
    insuranceTabPartners = "संबद्ध बीमा कंपनियाँ",
    insuranceTabAssistance = "बीमा सहायता",
    addMyPurchasedPolicy = "अपनी खरीदी हुई पॉलिसी जोड़ें",
    exploreBuyInsurance = "संबद्ध बीमा प्लान देखें और खरीदें",
    buyGetQuote = "खरीदें / कोट लें",
    checkEligibility = "पात्रता जाँचें",
    askAdvisor = "सलाहकार से पूछें",
    chatWithAdvisor = "बीमा सलाहकार से चैट करें",

    directionsButton = "दिशा-निर्देश",
    getDirectionsMaps = "Google Maps पर रास्ता देखें",
    aiAssistantTitle = "निदानमित्र AI सहायक",
    aiAssistantSubtitle = "English, हिन्दी और অসমীয়া में बुकिंग सहायता",
    aiAssistantFabLabel = "AI सहायक",
    aiAssistantBannerTitle = "बुकिंग में मदद चाहिए?",
    aiAssistantBannerDesc = "डॉक्टर, लैब टेस्ट या होम केयर आसानी से बुक करने के लिए हिन्दी, असमिया या अंग्रेज़ी में बोलकर या लिखकर AI से पूछें।",
    aiAssistantBannerCta = "AI से पूछें",
    aiAssistantInputPlaceholder = "लिखें या माइक दबाकर बोलें...",
    aiAssistantBookAction = "अभी बुक करें",
    aiAssistantNewChat = "नई चैट",
    voiceTapToSpeak = "बोलने के लिए माइक दबाएँ",
    voiceListeningHint = "सुन रहा हूँ... असमिया, हिन्दी या अंग्रेज़ी में बोलें",
    voiceStopAndSend = "भेजें",
    voiceCancel = "रद्द करें",
    voiceAutoSpeakOn = "वॉइस उत्तर चालू",
    voiceAutoSpeakOff = "वॉइस उत्तर बंद",
    voiceListenLabel = "सुनें",
    voiceStopSpeakingLabel = "रोकें",
    voiceMessageLabel = "वॉइस संदेश",
    voicePermissionDenied = "वॉइस सहायता के लिए माइक्रोफ़ोन अनुमति आवश्यक है।"
)

val AssameseAppStrings = AppStrings(
    appName = "নিদানমিত্ৰ",
    authSubtitle = "তেজপুৰ আৰু বিশ্বনাথ চাৰিআলি স্বাস্থ্য সেৱা",
    authDescription = "তেজপুৰ আৰু বিশ্বনাথ চাৰিআলিৰ ৰোগীসকলক স্থানীয় বিশেষজ্ঞ চিকিৎসক, এম্বুলেন্স, ডায়েগনষ্টিক লেব, স্বাস্থ্য বীমা আৰু চিকিৎসা নিৰ্দেশনাৰ সৈতে সংযোগ কৰা।",
    signInWithGoogle = "Google-ৰে ছাইন ইন কৰক",
    authenticating = "প্ৰমাণীকৰণ হৈ আছে...",
    selectLanguage = "ভাষা নিৰ্বাচন কৰক",
    languageChangedToast = { lang -> "ভাষা ${lang.nativeName}-লৈ সলনি কৰা হ'ল" },

    navHome = "হোম",
    navBooking = "বুকিং",
    navEmergency = "জৰুৰীকালীন",
    navRecords = "ৰেকৰ্ডছ",
    navProfile = "প্ৰফাইল",
    notificationsLabel = "জাননী",
    languageLabel = "ভাষা সলনি কৰক",
    darkModeLabel = "ডাৰ্ক ম'ড",
    darkModeSubtitle = "ৰাতি ব্যৱহাৰৰ বাবে চকুৰ আৰামদায়ক থীম",
    switchToDarkMode = "ডাৰ্ক ম'ড অন কৰক",
    switchToLightMode = "লাইট ম'ড অন কৰক",
    themeChangedToast = { isDark -> if (isDark) "ডাৰ্ক ম'ড অন কৰা হ'ল" else "লাইট ম'ড অন কৰা হ'ল" },

    helloPrefix = "নমস্কাৰ",
    homeSubtitle = "ডাক্তৰ, লেব টেষ্ট, হোম কেয়াৰ আৰু হাস্পতাল",
    searchPlaceholder = "ডাক্তৰ, টেষ্ট, হোম কেয়াৰ, হাস্পতাল বিচাৰক...",
    upcomingAppointments = "আগন্তুক এপইণ্টমেণ্ট",
    viewAll = "সকলো চাওক",
    bookNow = "বুক কৰক",
    noUpcomingVisits = "কোনো আগন্তুক ভ্ৰমণ নিৰ্ধাৰিত হোৱা নাই",
    noUpcomingVisitsSubtitle = "তেজপুৰ আৰু বিশ্বনাথত বিশেষজ্ঞ ডাক্তৰ বা লেব টেষ্ট বুক কৰক",
    healthServiceCategories = "স্বাস্থ্য সেৱাৰ শ্ৰেণীসমূহ",
    popularSpecialties = "জনপ্ৰিয় চিকিৎসা বিশেষজ্ঞতা",
    seeAll = "সকলো চাওক",
    nearbyHospitalsTitle = "ওচৰৰ হাস্পতাল আৰু ডায়েগনষ্টিক কেন্দ্ৰ",
    bookTestAction = "টেষ্ট বুক কৰক",
    showingFacilitiesNear = "আপোনাৰ অঞ্চলৰ ওচৰৰ সেৱাসমূহ:",
    viewHospitalDoctors = "হাস্পতাল আৰু ডাক্তৰ চাওক",
    viewLabTests = "লেব আৰু টেষ্ট চাওক",
    doctorsCountSuffix = "ডাক্তৰ",

    catDoctorsTitle = "ডাক্তৰ",
    catDoctorsSub = "OPD ও ভিডিঅ'",
    catLabsTitle = "লেব টেষ্ট",
    catLabsSub = "ঘৰৰ পৰা নমুনা",
    catHomeCareTitle = "হোম কেয়াৰ",
    catHomeCareSub = "নাৰ্ছ ও ফিজিঅ'",
    catAmbulanceTitle = "এম্বুলেন্স",
    catAmbulanceSub = "১০৮ জৰুৰী SOS",
    catInsuranceTitle = "স্বাস্থ্য বীমা",
    catInsuranceSub = "কেছলেছ ও ক্ৰয়",
    catGuidesTitle = "যত্ন গাইড",
    catGuidesSub = "অপাৰেচন যত্ন",

    bookingTabDoctors = "ডাক্তৰ",
    bookingTabLabs = "লেব টেষ্ট",
    bookingTabHomeCare = "হোম কেয়াৰ",
    bookingTabMyBookings = "মোৰ বুকিং",
    filterDoctorsPlaceholder = "ডাক্তৰ, বিশেষজ্ঞতা বা হাস্পতাল বিচাৰক...",
    filterLabsPlaceholder = "তেজ পৰীক্ষা, MRI, ECG, আল্ট্ৰাছাউণ্ড বিচাৰক...",
    videoConsult = "ভিডিঅ' পৰামৰ্শ",
    feeLowToHigh = "মাচুল: কমৰ পৰা বেছি",
    bookSlot = "শ্লট বুক কৰক",
    chatAction = "চ্যাট",
    bookedOnly = "কেৱল বুকিং কৰিলে",

    emergencySosTitle = "জৰুৰীকালীন এম্বুলেন্স SOS",
    emergencySosDesc = "তেজপুৰ / বিশ্বনাথ ১০৮ বা হাস্পতাল এম্বুলেন্স তৎক্ষণাত আপোনাৰ স্থানলৈ মাতক।",
    dispatchAmbulanceCta = "এম্বুলেন্স SOS পঠিয়াওক",
    whichAmbulanceTitle = "কোনখন এম্বুলেন্স বাছনি কৰিম?",
    nearbyEmergencyHospitals = "ওচৰৰ ২৪/৭ জৰুৰীকালীন হাস্পতাল",
    nearbyEmergencySubtitle = "আপোনাৰ নিৰ্বাচিত অঞ্চলৰ পৰা দূৰত্ব অনুসৰি সজোৱা হৈছে",

    healthLockerTab = "হেল্থ লকাৰ",
    procedureGuidesTab = "পদ্ধতি গাইড",
    scanStoreTitle = "স্বাস্থ্য ৰেকৰ্ড স্কেন ও সংৰক্ষণ কৰক",
    scanStoreSubtitle = "প্ৰেছক্ৰিপশ্যন, লেব ৰিপ'ৰ্ট আৰু স্কেনৰ ফটো বা PDF সহজে আপলোড কৰক।",
    scanUploadButton = "প্ৰেছক্ৰিপশ্যন / ৰিপ'ৰ্ট স্কেন বা আপলোড কৰক",

    familyProfilesTitle = "পৰিয়ালৰ সদস্যসকল",
    addMember = "সদস্য যোগ কৰক",
    paymentsReceiptsTitle = "পৰিশোধ আৰু ৰচিদ",
    myHealthInsurance = "মোৰ স্বাস্থ্য বীমা",
    addPolicy = "পলিচি যোগ কৰক",
    buyInsurers = "বীমা কিনক",
    assistance = "সহায়তা",
    fileClaim = "ক্লেইম কৰক",
    openHub = "হাব খোলক",
    helpAndSupport = "সহায় আৰু সমৰ্থন (FAQ ও টিকেট)",
    messageCareCoordinator = "নিদানমিত্ৰ কেয়াৰ সমন্বয়কলৈ বাৰ্তা পঠিয়াওক",
    signOut = "ছাইন আউট",
    insuranceHubTitle = "স্বাস্থ্য বীমা হাব",
    insuranceTabPolicies = "মোৰ পলিচি",
    insuranceTabPartners = "সংযুক্ত বীমা কোম্পানী",
    insuranceTabAssistance = "বীমা সহায়তা",
    addMyPurchasedPolicy = "আপোনাৰ ক্ৰয় কৰা পলিচি যোগ কৰক",
    exploreBuyInsurance = "সংযুক্ত বীমা প্লেন চাওক ও কিনক",
    buyGetQuote = "কিনক / কোট লওক",
    checkEligibility = "যোগ্যতা পৰীক্ষা",
    askAdvisor = "পৰামৰ্শদাতাক সোধক",
    chatWithAdvisor = "বীমা পৰামৰ্শদাতাৰ সৈতে চ্যাট কৰক",

    directionsButton = "দিশা-নিৰ্দেশ",
    getDirectionsMaps = "Google Maps-ত বাট চাওক",
    aiAssistantTitle = "নিদানমিত্ৰ AI সহায়ক",
    aiAssistantSubtitle = "অসমীয়া, हिन्दी আৰু English-ত বুকিং সহায়",
    aiAssistantFabLabel = "AI সহায়",
    aiAssistantBannerTitle = "বুকিং কৰাত সহায় লাগে নেকি?",
    aiAssistantBannerDesc = "নিজে বুক কৰিব নাজানিলে অসমীয়া, হিন্দী বা ইংৰাজীত কৈ বা লিখি নিদানমিত্ৰ AI-ৰ সহায়ত সহজে ডাক্তৰ বা লেব টেষ্ট বুক কৰক।",
    aiAssistantBannerCta = "AI-ক সোধক",
    aiAssistantInputPlaceholder = "লিখক বা মাইক টিপি কওক...",
    aiAssistantBookAction = "এতিয়াই বুক কৰক",
    aiAssistantNewChat = "নতুন চ্যাট",
    voiceTapToSpeak = "কথা ক'বলৈ মাইক টিপক",
    voiceListeningHint = "শুনি আছোঁ... অসমীয়া, হিন্দী বা ইংৰাজীত কওক",
    voiceStopAndSend = "পঠিয়াওক",
    voiceCancel = "বাতিল",
    voiceAutoSpeakOn = "ভইচ উত্তৰ অন",
    voiceAutoSpeakOff = "ভইচ উত্তৰ অফ",
    voiceListenLabel = "শুনক",
    voiceStopSpeakingLabel = "ৰখাওক",
    voiceMessageLabel = "ভইচ বাৰ্তা",
    voicePermissionDenied = "ভইচ সহায়তা ব্যৱহাৰ কৰিবলৈ মাইক্ৰ'ফোনৰ অনুমতি প্ৰয়োজন।"
)

fun stringsForLanguage(language: AppLanguage): AppStrings = when (language) {
    AppLanguage.ENGLISH -> EnglishAppStrings
    AppLanguage.HINDI -> HindiAppStrings
    AppLanguage.ASSAMESE -> AssameseAppStrings
}

val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.ENGLISH }
val LocalAppStrings = staticCompositionLocalOf { EnglishAppStrings }

@Composable
fun LanguageSelectorButton(
    selectedLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .semantics { contentDescription = strings.languageLabel }
                .testTag("top_bar_language_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Translate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = selectedLanguage.shortBadge,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.testTag("language_dropdown_menu")
        ) {
            Text(
                text = strings.selectLanguage,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            AppLanguage.entries.forEach { lang ->
                val isSelected = lang == selectedLanguage
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = lang.nativeName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            if (lang.nativeName != lang.englishName) {
                                Text(
                                    text = lang.englishName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    trailingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null,
                    onClick = {
                        expanded = false
                        onSelectLanguage(lang)
                    },
                    modifier = Modifier.testTag("language_option_${lang.code}")
                )
            }
        }
    }
}
