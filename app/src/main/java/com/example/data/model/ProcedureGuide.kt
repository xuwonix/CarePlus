package com.example.data.model

data class ProcedureChecklistStep(
    val stepId: String,
    val title: String,
    val detail: String,
    val timing: String, // e.g. "7 Days Before", "24 Hours Before", "Morning of Procedure", "Post-Op Day 1"
    var isCompleted: Boolean = false
)

data class ProcedureGuide(
    val id: String,
    val title: String,
    val category: String, // SURGICAL, DIAGNOSTIC_IMAGING, ENDOSCOPIC, CARDIOLOGY
    val summary: String,
    val estimatedDuration: String,
    val anesthesiaType: String,
    val recoveryDays: Int,
    val preparationChecklist: List<ProcedureChecklistStep>,
    val procedureOverview: String,
    val postCareInstructions: List<String>,
    val warningRedFlags: List<String>
)
