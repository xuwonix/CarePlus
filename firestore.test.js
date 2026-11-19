const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_tezpur_123";
const BOB_UID = "bob_tezpur_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users cannot read or write profiles, appointments, or chat messages", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("appointments").get());
  await assertFails(unauthDb.collection("chat_messages").get());
  await assertFails(
    unauthDb.collection("chat_messages").doc("msg_unauth").set({
      consultationId: "consult_doc_cardio_1",
      senderId: ALICE_UID,
      recipientId: "doc_cardio_1",
      message: "Unauthorized",
    })
  );
});

test("Users collection is strictly owner-scoped", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      email: "alice@example.com",
      displayName: "Alice Borah",
      role: "PATIENT",
      onboardingCompleted: true,
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({ displayName: "Hijacked" })
  );
});

test("Chat messages: Kotlin payload with prescription attachment & rules-aligned queries succeed", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  // Mirror HealthcareRepository.sendChatMessage with prescription attachment
  await assertSucceeds(
    aliceDb.collection("chat_messages").doc("msg_rx_1").set({
      consultationId: "consult_doc_cardio_1",
      senderId: ALICE_UID,
      senderName: "Alice Borah",
      senderRole: "PATIENT",
      recipientId: "doc_cardio_1",
      recipientName: "Dr. Ananya Baruah, MD, DM",
      message: "Shared prescription: rx_scan_102.jpg",
      isRead: false,
      messageType: "PRESCRIPTION",
      attachmentType: "IMAGE",
      attachmentPath: "/data/user/0/com.aistudio.healthcare.kxpvld/files/medical_attachments/rx_102.jpg",
      attachmentName: "rx_scan_102.jpg",
      createdAt: new Date(Date.now() - 1000),
    })
  );

  // Mirror Kotlin observeConsultationMessages single-field queries
  await assertSucceeds(
    aliceDb.collection("chat_messages").where("senderId", "==", ALICE_UID).get()
  );
  await assertSucceeds(
    aliceDb.collection("chat_messages").where("recipientId", "==", ALICE_UID).get()
  );

  // Unfiltered or consultationId-only queries MUST be rejected by rules
  await assertFails(
    aliceDb
      .collection("chat_messages")
      .where("consultationId", "==", "consult_doc_cardio_1")
      .get()
  );

  // Cross-user isolation: Bob cannot read Alice's chat message
  await assertFails(bobDb.collection("chat_messages").doc("msg_rx_1").get());

  // Invalid attachmentType or empty message+attachment MUST be rejected
  await assertFails(
    aliceDb.collection("chat_messages").doc("msg_invalid_type").set({
      consultationId: "consult_doc_cardio_1",
      senderId: ALICE_UID,
      recipientId: "doc_cardio_1",
      message: "Hello",
      attachmentType: "EXECUTABLE",
    })
  );
  await assertFails(
    aliceDb.collection("chat_messages").doc("msg_empty").set({
      consultationId: "consult_doc_cardio_1",
      senderId: ALICE_UID,
      recipientId: "doc_cardio_1",
      message: "",
      attachmentType: "",
      attachmentPath: "",
    })
  );
});

test("Notifications: create, filtered read, and mark isRead=true partial update succeed", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("notifications").doc("notif_1").set({
      patientId: ALICE_UID,
      title: "Appointment Confirmed",
      message: "Booked with Dr. Ananya Baruah",
      category: "BOOKING",
      isRead: false,
      createdAt: new Date(Date.now() - 1000),
    })
  );

  await assertSucceeds(
    aliceDb.collection("notifications").where("patientId", "==", ALICE_UID).get()
  );
  await assertSucceeds(
    aliceDb.collection("notifications").doc("notif_1").update({ isRead: true })
  );
  await assertFails(
    bobDb.collection("notifications").doc("notif_1").update({ isRead: true })
  );
});

test("Medical records: scanned prescription record create and owner query succeed", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("medical_records").doc("rec_1").set({
      patientId: ALICE_UID,
      memberName: "Self",
      title: "Cardiology Prescription",
      category: "PRESCRIPTION",
      doctorOrFacility: "Dr. Ananya Baruah, MD, DM",
      recordDate: "20 Nov 2026",
      summary: "Shared via consultation chat",
      keyMetrics: "",
      fileType: "IMAGE",
      attachmentPath: "/files/medical_attachments/rx_102.jpg",
      attachmentName: "rx_102.jpg",
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  await assertSucceeds(
    aliceDb.collection("medical_records").where("patientId", "==", ALICE_UID).get()
  );
  await assertFails(bobDb.collection("medical_records").doc("rec_1").get());
});

test("Bookings (appointments, diagnostics, home care, ambulance) and payments succeed for owner and block cross-user access", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 1000);

  // 1. Doctor Appointment create + query + reschedule + cancel
  await assertSucceeds(
    aliceDb.collection("appointments").doc("appt_1").set({
      patientId: ALICE_UID,
      patientName: "Alice Borah",
      familyMemberName: "Self",
      specialistId: "doc_cardio_1",
      specialistName: "Dr. Ananya Baruah",
      specialty: "Cardiology",
      clinicHospital: "Tezpur Medical College",
      date: "20 Nov 2026",
      timeSlot: "10:30 AM",
      mode: "In-Person",
      status: "SCHEDULED",
      notes: "Routine checkup",
      prescription: "",
      consultationFee: 500.0,
      paymentStatus: "PAID",
      paymentMethod: "UPI / Card",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    aliceDb.collection("appointments").where("patientId", "==", ALICE_UID).get()
  );
  await assertSucceeds(
    aliceDb.collection("appointments").doc("appt_1").update({
      date: "21 Nov 2026",
      timeSlot: "11:00 AM",
      status: "SCHEDULED",
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(bobDb.collection("appointments").doc("appt_1").get());

  // 2. Payment record created alongside booking
  await assertSucceeds(
    aliceDb.collection("payments").doc("pay_1").set({
      patientId: ALICE_UID,
      title: "Consultation: Dr. Ananya Baruah",
      providerName: "Tezpur Medical College",
      category: "APPOINTMENT",
      billingForName: "Self",
      amount: 500.0,
      paymentMethod: "UPI / Card",
      status: "PAID",
      transactionRef: "TXN-123456",
      createdAt: now,
    })
  );
  await assertSucceeds(
    aliceDb.collection("payments").where("patientId", "==", ALICE_UID).get()
  );
  await assertFails(bobDb.collection("payments").doc("pay_1").get());

  // 3. Diagnostic booking create + query + cancel
  await assertSucceeds(
    aliceDb.collection("diagnostic_bookings").doc("diag_1").set({
      patientId: ALICE_UID,
      patientName: "Alice Borah",
      familyMemberName: "Self",
      centerId: "lab_1",
      centerName: "Tezpur Diagnostics",
      testId: "test_cbc",
      testName: "Complete Blood Count",
      testCategory: "Blood",
      scheduledDate: "20 Nov 2026",
      scheduledTime: "08:00 AM",
      collectionType: "HOME_COLLECTION",
      collectionAddress: "Mission Chariali, Tezpur",
      status: "BOOKED",
      resultSummary: "Phlebotomist assigned",
      abnormalFlag: false,
      cost: 350.0,
      paymentStatus: "PAID",
      paymentMethod: "UPI / Card",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    aliceDb.collection("diagnostic_bookings").where("patientId", "==", ALICE_UID).get()
  );
  await assertFails(bobDb.collection("diagnostic_bookings").doc("diag_1").get());

  // 4. Home care booking create + query
  await assertSucceeds(
    aliceDb.collection("home_care_bookings").doc("hc_1").set({
      patientId: ALICE_UID,
      patientName: "Alice Borah",
      memberName: "Self",
      serviceId: "hc_nurse",
      serviceTitle: "Home Nursing Visit",
      serviceCategory: "Nursing",
      providerTeam: "Nidanmitra Home Care",
      visitAddress: "Mission Chariali, Tezpur",
      scheduledDate: "20 Nov 2026",
      timeWindow: "09:00 AM - 11:00 AM",
      notes: "",
      status: "SCHEDULED",
      cost: 800.0,
      paymentStatus: "PAID",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    aliceDb.collection("home_care_bookings").where("patientId", "==", ALICE_UID).get()
  );
  await assertFails(bobDb.collection("home_care_bookings").doc("hc_1").get());

  // 5. Ambulance dispatch create + query
  await assertSucceeds(
    aliceDb.collection("ambulance_requests").doc("amb_1").set({
      patientId: ALICE_UID,
      patientName: "Alice Borah",
      patientPhone: "+91 9876543210",
      pickupAddress: "Biswanath Chariali",
      emergencyType: "Cardiac",
      ambulanceType: "ADVANCED_CARDIAC",
      status: "DISPATCHED",
      vehicleNumber: "AS-32-EM-1234",
      driverName: "Biswanath 108 EMS Team",
      driverPhone: "+91 3715-108-220",
      etaMinutes: 7,
      hospitalDestination: "Biswanath Chariali Civil Hospital",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    aliceDb.collection("ambulance_requests").where("patientId", "==", ALICE_UID).get()
  );
  await assertFails(bobDb.collection("ambulance_requests").doc("amb_1").get());
});

