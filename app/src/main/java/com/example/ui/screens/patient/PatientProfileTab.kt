package com.example.ui.screens.patient

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.HelpCenter
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.FamilyMember
import com.example.data.model.InsuranceClaim
import com.example.data.model.PaymentRecord
import com.example.data.model.UserInsurancePolicy
import com.example.data.model.UserProfile
import com.example.ui.i18n.LocalAppStrings
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PatientProfileTab(
    profile: UserProfile?,
    familyMembers: List<FamilyMember>,
    paymentRecords: List<PaymentRecord>,
    userPolicies: List<UserInsurancePolicy>,
    tiedUpInsurerCount: Int,
    claims: List<InsuranceClaim>,
    onEditProfile: () -> Unit,
    onOpenLocationPicker: () -> Unit,
    onAddFamilyMember: () -> Unit,
    onDeleteFamilyMember: (String) -> Unit,
    onOpenInsuranceHub: (initialTab: Int) -> Unit,
    onAddInsurancePolicy: () -> Unit,
    onSubmitClaim: (UserInsurancePolicy?) -> Unit,
    onOpenHelpSupport: () -> Unit,
    onOpenSupportChat: () -> Unit,
    onSignOut: () -> Unit
) {
    val strings = LocalAppStrings.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("patient_profile_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Patient Identity & Medical Summary Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            val initial = profile?.displayName?.firstOrNull()?.uppercase() ?: "P"
                            Text(
                                text = initial,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile?.displayName?.ifBlank { "Nidanmitra Patient" } ?: "Nidanmitra Patient",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = profile?.email ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = profile?.phone?.ifBlank { "Add phone number" } ?: "Add phone number",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        IconButton(
                            onClick = onEditProfile,
                            modifier = Modifier.testTag("edit_profile_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val blood = profile?.bloodGroup?.ifBlank { "Not set" } ?: "Not set"
                    val age = profile?.age?.ifBlank { "Not set" } ?: "Not set"
                    val allergies = profile?.allergies?.ifBlank { "None reported" } ?: "None reported"
                    val conditions = profile?.chronicConditions?.ifBlank { "None" } ?: "None"
                    val iceName = profile?.emergencyContactName.orEmpty()
                    val icePhone = profile?.emergencyContactPhone.orEmpty()
                    val iceLabel = if (iceName.isBlank() && icePhone.isBlank()) {
                        "Not added yet (tap Edit to add emergency contact)"
                    } else {
                        listOf(iceName, icePhone).filter { it.isNotBlank() }.joinToString(" • ")
                    }

                    Text(
                        text = "Blood Group: $blood • Age: $age • ${profile?.gender ?: "Patient"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Allergies: $allergies • Conditions: $conditions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "ICE Contact: $iceLabel",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onOpenLocationPicker,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        val areaText = profile?.locationArea?.ifBlank { "Mission Chariali, Tezpur" } ?: "Mission Chariali, Tezpur"
                        val addrText = profile?.savedAddress?.ifBlank { "Tap to set home address" } ?: "Tap to set home address"
                        Text(
                            text = "$areaText • ${addrText.take(34)}",
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 2. Family Profiles Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${strings.familyProfilesTitle} (${familyMembers.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                TextButton(
                    onClick = onAddFamilyMember,
                    modifier = Modifier.testTag("add_family_member_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.addMember)
                }
            }
        }

        if (familyMembers.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Add family members (spouse, children, parents) to book doctor visits, lab tests, and home care on their behalf.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(familyMembers) { member ->
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
                            Text("${member.name} (${member.relationship})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Age: ${member.age} • ${member.gender} • Blood: ${member.bloodGroup}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Allergies: ${member.allergies}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        IconButton(onClick = { onDeleteFamilyMember(member.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove family member", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        // 3. Payments & Receipts
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${strings.paymentsReceiptsTitle} (${paymentRecords.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (paymentRecords.isEmpty()) {
            item {
                Text(
                    text = "No payments recorded yet. Receipts appear automatically when booking appointments, tests, or home care.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(paymentRecords) { pay ->
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
                            Text(pay.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("${pay.providerName} • For: ${pay.billingForName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${pay.paymentMethod} • Ref: ${pay.transactionRef}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("₹${pay.amount.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            SuggestionChip(onClick = {}, label = { Text(pay.status.replace("_", " ")) })
                        }
                    }
                }
            }
        }

        // 4. Health Insurance (Consumer-Added Policies + Tied-Up Partners + Assistance)
        item {
            val inrFormatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.myHealthInsurance,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "$tiedUpInsurerCount tied-up insurers • Cashless TPA Desk",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    }

                    if (userPolicies.isEmpty()) {
                        Text(
                            text = "No personal insurance policy added yet. Add your own purchased health plan (or PM-JAY / MMAAY card), or compare & purchase from Nidanmitra's tied-up insurers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    } else {
                        val primaryPolicy = userPolicies.first()
                        Text(
                            text = "${primaryPolicy.insurerName} — ${primaryPolicy.planName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Policy #${primaryPolicy.policyNumber} • Cover: ₹${inrFormatter.format(primaryPolicy.sumInsured.toLong())}" +
                                if (userPolicies.size > 1) " (+${userPolicies.size - 1} more)" else "",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onAddInsurancePolicy,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_add_policy_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.addPolicy, maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = { onOpenInsuranceHub(1) },
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("profile_tied_up_insurers_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.buyInsurers, maxLines = 1)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onOpenInsuranceHub(2) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_insurance_assistance_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.assistance, maxLines = 1)
                        }
                        if (userPolicies.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { onSubmitClaim(userPolicies.first()) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("submit_claim_banner_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(strings.fileClaim, maxLines = 1)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onOpenInsuranceHub(0) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("submit_claim_banner_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(strings.openHub, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        if (claims.isNotEmpty()) {
            item {
                Text("Insurance Claims (${claims.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(claims) { claim ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(claim.procedureName.ifBlank { "Hospital Claim" }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            SuggestionChip(onClick = {}, label = { Text(claim.status) })
                        }
                        Text("${claim.hospitalName} • Claimed: ₹${claim.claimedAmount.toInt()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // 5. Help & Support + Care Chat + Sign Out
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onOpenHelpSupport,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("profile_help_support_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.HelpCenter, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.helpAndSupport)
                }

                OutlinedButton(
                    onClick = onOpenSupportChat,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("profile_care_chat_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.messageCareCoordinator)
                }

                OutlinedButton(
                    onClick = onSignOut,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("profile_sign_out_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.signOut, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
