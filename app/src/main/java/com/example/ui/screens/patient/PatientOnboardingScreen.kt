package com.example.ui.screens.patient

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.UserProfile
import com.example.data.repository.HealthcareCatalog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientOnboardingScreen(
    initialProfile: UserProfile,
    onSaveProfile: (UserProfile) -> Unit,
    onSkip: () -> Unit
) {
    var displayName by remember(initialProfile.userId) { mutableStateOf(initialProfile.displayName) }
    var phone by remember(initialProfile.userId) { mutableStateOf(initialProfile.phone) }
    var age by remember(initialProfile.userId) { mutableStateOf(initialProfile.age) }
    var gender by remember(initialProfile.userId) {
        mutableStateOf(initialProfile.gender.ifBlank { "Prefer not to say" })
    }
    var bloodGroup by remember(initialProfile.userId) {
        mutableStateOf(initialProfile.bloodGroup.ifBlank { "O+" })
    }
    var allergies by remember(initialProfile.userId) { mutableStateOf(initialProfile.allergies) }
    var chronicConditions by remember(initialProfile.userId) { mutableStateOf(initialProfile.chronicConditions) }
    var emergencyContactName by remember(initialProfile.userId) { mutableStateOf(initialProfile.emergencyContactName) }
    var emergencyContactPhone by remember(initialProfile.userId) { mutableStateOf(initialProfile.emergencyContactPhone) }
    var locationArea by remember(initialProfile.userId) {
        mutableStateOf(initialProfile.locationArea.ifBlank { "Mission Chariali, Tezpur" })
    }
    var savedAddress by remember(initialProfile.userId) { mutableStateOf(initialProfile.savedAddress) }

    val genders = listOf("Female", "Male", "Non-binary", "Prefer not to say")
    val bloodGroups = listOf("O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-", "Unknown")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Medical Profile Setup",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    TextButton(
                        onClick = onSkip,
                        modifier = Modifier.testTag("onboarding_skip_button")
                    ) {
                        Text(
                            text = "Skip",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("patient_onboarding_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Welcome to Nidanmita",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your Google account only provides your name and email. Add your personal health details and emergency contact now—or skip and fill them in later.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Personal & Contact Details
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Personal & Address Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_name_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = age,
                                onValueChange = { age = it },
                                label = { Text("Age") },
                                placeholder = { Text("e.g. 30") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("onboarding_age_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Mobile Phone") },
                                placeholder = { Text("+91 98640 ...") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier
                                    .weight(1.7f)
                                    .testTag("onboarding_phone_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Text("Gender:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(genders) { option ->
                                FilterChip(
                                    selected = gender == option,
                                    onClick = { gender = option },
                                    label = { Text(option) }
                                )
                            }
                        }

                        Text("Service Zone:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(HealthcareCatalog.metroAreas) { area ->
                                FilterChip(
                                    selected = locationArea == area,
                                    onClick = { locationArea = area },
                                    label = { Text(area) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = savedAddress,
                            onValueChange = { savedAddress = it },
                            label = { Text("Home / Pickup Address") },
                            placeholder = { Text("Street, Apt, City (for home lab & ambulance)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_address_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Medical Information
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Medical Essentials", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Text("Blood Group:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(bloodGroups) { bg ->
                                FilterChip(
                                    selected = bloodGroup == bg,
                                    onClick = { bloodGroup = bg },
                                    label = { Text(bg) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = allergies,
                            onValueChange = { allergies = it },
                            label = { Text("Known Allergies") },
                            placeholder = { Text("e.g. Penicillin, Sulfa, Peanuts, or None") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_allergies_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = chronicConditions,
                            onValueChange = { chronicConditions = it },
                            label = { Text("Existing Conditions / Medications") },
                            placeholder = { Text("e.g. Asthma, Diabetes, Hypertension, or None") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_conditions_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Emergency Contact (ICE)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Emergency, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Emergency Contact (ICE)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        OutlinedTextField(
                            value = emergencyContactName,
                            onValueChange = { emergencyContactName = it },
                            label = { Text("Emergency Contact Name & Relation") },
                            placeholder = { Text("e.g. Ankur Das (Spouse)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_ice_name_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = emergencyContactPhone,
                            onValueChange = { emergencyContactPhone = it },
                            label = { Text("Emergency Contact Phone") },
                            placeholder = { Text("+91 98640 ...") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_ice_phone_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Action Buttons
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            onSaveProfile(
                                initialProfile.copy(
                                    displayName = displayName.trim().ifBlank { initialProfile.displayName },
                                    phone = phone.trim(),
                                    age = age.trim(),
                                    gender = gender,
                                    bloodGroup = bloodGroup,
                                    allergies = allergies.trim().ifBlank { "None reported" },
                                    chronicConditions = chronicConditions.trim().ifBlank { "None" },
                                    emergencyContactName = emergencyContactName.trim(),
                                    emergencyContactPhone = emergencyContactPhone.trim(),
                                    locationArea = locationArea,
                                    savedAddress = savedAddress.trim(),
                                    onboardingCompleted = true
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("onboarding_save_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Save Medical Profile & Continue", fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = onSkip,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("onboarding_skip_bottom_button")
                    ) {
                        Text("Skip for Now — I'll add details later")
                    }
                }
            }
        }
    }
}
