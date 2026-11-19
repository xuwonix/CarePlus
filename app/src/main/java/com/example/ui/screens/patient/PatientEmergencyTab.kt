package com.example.ui.screens.patient

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.AmbulanceRequest
import com.example.data.model.NearbyFacility
import com.example.ui.i18n.LocalAppStrings
import com.example.util.MapsNavigationHelper

@Composable
fun PatientEmergencyTab(
    activeAmbulance: AmbulanceRequest?,
    allRequests: List<AmbulanceRequest>,
    emergencyHospitals: List<NearbyFacility>,
    onTriggerSOS: () -> Unit,
    onResolveAmbulance: (String) -> Unit
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("patient_emergency_tab"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        // 1. Emergency Alert SOS Hero or Active Tracker
        item {
            if (activeAmbulance != null) {
                ActiveAmbulanceFullCard(
                    ambulance = activeAmbulance,
                    onResolve = { onResolveAmbulance(activeAmbulance.id) }
                )
            } else {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.72f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Emergency Alert SOS",
                                tint = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = strings.emergencySosTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = strings.emergencySosDesc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.88f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onTriggerSOS,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("emergency_sos_dispatch_button")
                        ) {
                            Icon(imageVector = Icons.Default.Emergency, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.dispatchAmbulanceCta,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        // 2. Ambulance Guide (Plain-English explanation for normal users)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = strings.whichAmbulanceTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                AmbulanceTypeGuideCard(
                    icon = Icons.Default.Favorite,
                    title = "ICU / Cardiac Ambulance (ACLS)",
                    badge = "Critical Emergencies",
                    description = "Like a moving hospital ICU — has ventilator, heart monitor, defibrillator & paramedic. Choose for chest pain, heart attack, stroke, or unconsciousness."
                )
                AmbulanceTypeGuideCard(
                    icon = Icons.Default.MedicalServices,
                    title = "Basic Medical Ambulance (BLS)",
                    badge = "Standard Transport",
                    description = "Equipped with oxygen cylinder, stretcher, first-aid & EMT helper. Choose for fractures, high fever, maternity transport, or non-critical transfers."
                )
            }
        }

        // 3. Nearby 24/7 Emergency Hospitals
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = strings.nearbyEmergencyHospitals,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = strings.nearbyEmergencySubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(emergencyHospitals) { hospital ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.errorContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(hospital.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("${hospital.distanceKm} km • ${hospital.address}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "ER Wait: ~${hospital.waitTimeMinutes} min • ${hospital.phone}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = { MapsNavigationHelper.openFacilityDirections(context, hospital) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("emergency_hospital_directions_${hospital.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Directions,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.getDirectionsMaps, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // 4. Collapsible Critical Emergency First-Aid Protocols (Dropdown Accordions)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Column {
                    Text(
                        text = "Critical Emergency First-Aid Protocols",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap any protocol below to expand life-saving steps",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                EmergencyFirstAidDropdownCard(
                    icon = Icons.Default.Favorite,
                    title = "Chest Pain & Cardiac Warning",
                    subtitle = "Heart attack or sudden breathing distress",
                    steps = listOf(
                        "Keep the person seated upright calmly; loosen tight collar or belt.",
                        "If prescribed nitroglycerin is available, assist them in taking it.",
                        "Have them chew 1 regular aspirin (300–325mg) unless allergic.",
                        "If unresponsive and not breathing, start hands-only CPR (100–120 chest pushes/min)."
                    ),
                    testTag = "first_aid_dropdown_cardiac"
                )

                EmergencyFirstAidDropdownCard(
                    icon = Icons.Default.Psychology,
                    title = "Stroke F.A.S.T. Assessment",
                    subtitle = "Sudden numbness, confusion, or slurred speech",
                    steps = listOf(
                        "F — Face: Ask them to smile; check if one side droops.",
                        "A — Arms: Ask them to raise both arms; check if one drifts downward.",
                        "S — Speech: Ask them to repeat a simple phrase; listen for slurring.",
                        "T — Time: Dispatch an ICU/Cardiac ambulance immediately and note the exact time symptoms started."
                    ),
                    testTag = "first_aid_dropdown_stroke"
                )

                EmergencyFirstAidDropdownCard(
                    icon = Icons.Default.Warning,
                    title = "Heavy Bleeding & Road Trauma",
                    subtitle = "Accidents, deep cuts, or suspected fractures",
                    steps = listOf(
                        "Apply firm, steady direct pressure on the wound using a clean cloth or sterile gauze.",
                        "Do not remove a cloth that soaks through—layer another clean cloth directly on top.",
                        "Keep an injured neck, spine, or broken limb completely still; do not force it straight.",
                        "Keep the patient warm and avoid giving food or water before hospital triage."
                    ),
                    testTag = "first_aid_dropdown_trauma"
                )
            }
        }

        if (allRequests.isNotEmpty()) {
            item {
                Text(
                    text = "Recent Emergency Dispatches",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(allRequests) { req ->
                val friendlyType = when (req.ambulanceType) {
                    "ADVANCED_CARDIAC" -> "ICU / Cardiac (ACLS)"
                    "BASIC_LIFE_SUPPORT" -> "Basic Medical (BLS)"
                    else -> req.ambulanceType.replace("_", " ")
                }
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text("${req.vehicleNumber} • $friendlyType", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Pickup: ${req.pickupAddress.ifBlank { "Saved Location" }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        SuggestionChip(onClick = {}, label = { Text(req.status) })
                    }
                }
            }
        }
    }
}

@Composable
private fun AmbulanceTypeGuideCard(
    icon: ImageVector,
    title: String,
    badge: String,
    description: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ActiveAmbulanceFullCard(
    ambulance: AmbulanceRequest,
    onResolve: () -> Unit
) {
    val friendlyType = if (ambulance.ambulanceType == "ADVANCED_CARDIAC") {
        "ICU / Cardiac Ambulance (ACLS)"
    } else {
        "Basic Medical Ambulance (BLS)"
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_ambulance_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Emergency, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Unit ${ambulance.vehicleNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                SuggestionChip(onClick = {}, label = { Text(ambulance.status) })
            }

            Text(
                text = friendlyType,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            val progress = when (ambulance.status) {
                "DISPATCHED" -> 0.3f
                "EN_ROUTE" -> 0.65f
                "ARRIVED" -> 0.95f
                else -> 1f
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.error,
                trackColor = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(14.dp))
            Text("Responder: ${ambulance.driverName} (${ambulance.driverPhone})", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text("Pickup: ${ambulance.pickupAddress}", style = MaterialTheme.typography.bodySmall)
            Text("Destination: ${ambulance.hospitalDestination}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Estimated Arrival: ${ambulance.etaMinutes} minutes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
                onClick = onResolve,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Mark Completed / Cancel Alert")
            }
        }
    }
}

@Composable
private fun EmergencyFirstAidDropdownCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    steps: List<String>,
    testTag: String
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag(testTag)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse steps" else "Expand steps",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        steps.forEachIndexed { idx, step ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "${idx + 1}.",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.width(20.dp)
                                )
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
