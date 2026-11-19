package com.example.ui.screens.patient

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.DiagnosticCenter
import com.example.data.model.DiagnosticTest
import com.example.data.model.NearbyFacility
import com.example.data.model.Review
import com.example.data.model.Specialist
import com.example.ui.i18n.LocalAppStrings
import com.example.util.MapsNavigationHelper

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FacilityDetailScreen(
    facility: NearbyFacility,
    affiliatedDoctors: List<Specialist>,
    diagnosticCenter: DiagnosticCenter?,
    reviews: List<Review>,
    onBookDoctor: (Specialist) -> Unit,
    onOpenDoctorProfile: (Specialist) -> Unit,
    onBookTest: (DiagnosticCenter, DiagnosticTest) -> Unit,
    onRequestEmergencySos: () -> Unit,
    onSubmitReview: (rating: Int, comment: String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val isHospital = facility.facilityType == "HOSPITAL"
    var showReviewDialog by remember { mutableStateOf(false) }
    val facilityReviews = remember(reviews, facility.id) {
        reviews.filter { it.targetId == facility.id }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isHospital) "Hospital Details" else "Diagnostic Center",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("facility_detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
                .testTag("facility_detail_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isHospital) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isHospital) Icons.Default.LocalHospital else Icons.Default.Biotech,
                                    contentDescription = null,
                                    tint = if (isHospital) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = facility.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = facility.accreditation,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${facility.address} • ${facility.distanceKm} km away",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = facility.phone, style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surface) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("${facility.rating}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surface) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("~${facility.waitTimeMinutes} min wait", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            if (facility.acceptsCashlessInsurance) {
                                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Cashless", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { MapsNavigationHelper.openFacilityDirections(context, facility) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("facility_detail_directions_button")
                        ) {
                            Icon(Icons.Default.Directions, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.getDirectionsMaps, fontWeight = FontWeight.Bold)
                        }

                        if (facility.hasEmergencyUnit) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = onRequestEmergencySos,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Emergency, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Request 24/7 Emergency Ambulance Here", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Departments
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Departments & Capabilities",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        facility.departments.forEach { dept ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Text(
                                    text = dept,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Practicing Doctors (if Hospital)
            if (isHospital && affiliatedDoctors.isNotEmpty()) {
                item {
                    Text(
                        text = "Specialists Practicing Here (${affiliatedDoctors.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(affiliatedDoctors) { doc ->
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(doc.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("${doc.specialty} • Next: ${doc.nextAvailableSlot}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${doc.fee.toInt()} / visit", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(onClick = { onOpenDoctorProfile(doc) }) {
                                    Text("Profile")
                                }
                                Button(onClick = { onBookDoctor(doc) }) {
                                    Text("Book")
                                }
                            }
                        }
                    }
                }
            }

            // Offered Tests (if Diagnostic Center)
            if (!isHospital && diagnosticCenter != null) {
                item {
                    Text(
                        text = "Available Diagnostic Tests (${diagnosticCenter.testsOffered.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(diagnosticCenter.testsOffered) { test ->
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(test.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("${test.sampleType} • Report in ${test.turnaroundTime}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${test.price.toInt()}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Button(onClick = { onBookTest(diagnosticCenter, test) }) {
                                Text("Book")
                            }
                        }
                    }
                }
            }

            // Reviews Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Facility Reviews (${facilityReviews.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { showReviewDialog = true }) {
                        Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Write Review")
                    }
                }
            }

            if (facilityReviews.isEmpty()) {
                item {
                    Text(
                        text = "No reviews posted yet for ${facility.name}. Share your feedback after visiting.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(facilityReviews) { review ->
                    ReviewItemCard(review = review)
                }
            }
        }
    }

    if (showReviewDialog) {
        WriteReviewDialog(
            targetName = facility.name,
            onDismiss = { showReviewDialog = false },
            onSubmit = { rating, comment ->
                onSubmitReview(rating, comment)
                showReviewDialog = false
            }
        )
    }
}
