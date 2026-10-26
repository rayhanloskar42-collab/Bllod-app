package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BloodCompatibilityUtil
import com.example.data.model.BloodRequestEntity
import com.example.ui.components.BloodBadge
import com.example.ui.components.UrgencyBadge
import com.example.ui.theme.BloodRed
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.EmeraldGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodRequestsScreen(
    requests: List<BloodRequestEntity>,
    onCreateRequest: (
        patientName: String,
        bloodGroup: String,
        unitsNeeded: Int,
        hospitalName: String,
        hospitalLocation: String,
        contactPhone: String,
        urgency: String,
        notes: String
    ) -> Unit,
    onMarkFulfilled: (Int) -> Unit,
    defaultContactPhone: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Active, 1: Fulfilled
    var selectedBloodGroupFilter by remember { mutableStateOf<String?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val filteredRequests = requests.filter { req ->
        val matchesTab = if (selectedTab == 0) !req.isFulfilled else req.isFulfilled
        val matchesGroup = selectedBloodGroupFilter == null || req.bloodGroup.equals(selectedBloodGroupFilter, ignoreCase = true)
        matchesTab && matchesGroup
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = CrimsonPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .testTag("fab_create_request")
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Post Request")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Request Blood", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = Color(0xFFFCF9F9),
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Emergency Blood Requests",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = CrimsonPrimary
                )
                Text(
                    text = "Patients in urgent need of blood transfusion",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Blood Group horizontal filter
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedBloodGroupFilter == null,
                            onClick = { selectedBloodGroupFilter = null },
                            label = { Text("All") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrimsonPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    items(BloodCompatibilityUtil.BLOOD_GROUPS) { bg ->
                        val isSelected = selectedBloodGroupFilter == bg
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedBloodGroupFilter = if (isSelected) null else bg },
                            label = { Text(bg, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrimsonPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tabs: Active vs Fulfilled
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = CrimsonPrimary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "Urgent Needs (${requests.count { !it.isFulfilled }})",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Fulfilled (${requests.count { it.isFulfilled }})",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Requests List
            if (filteredRequests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Default.DoneAll else Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (selectedTab == 0) "No active requests for this filter" else "No fulfilled requests yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredRequests, key = { it.id }) { req ->
                        BloodRequestItemCard(
                            request = req,
                            onCall = { phone ->
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                context.startActivity(intent)
                            },
                            onMarkFulfilled = { onMarkFulfilled(req.id) }
                        )
                    }
                }
            }
        }
    }

    // Create Blood Request Dialog
    if (showCreateDialog) {
        CreateBloodRequestDialog(
            defaultPhone = defaultContactPhone,
            onDismiss = { showCreateDialog = false },
            onSubmit = { pName, bg, units, hName, hLoc, phone, urgency, notes ->
                onCreateRequest(pName, bg, units, hName, hLoc, phone, urgency, notes)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun BloodRequestItemCard(
    request: BloodRequestEntity,
    onCall: (String) -> Unit,
    onMarkFulfilled: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (request.isFulfilled) Color(0xFFF9F9F9) else Color.White
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (request.isFulfilled) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    BloodBadge(bloodGroup = request.bloodGroup, size = 48.dp, fontSize = 18)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = request.patientName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${request.unitsNeeded} Bag(s) Needed",
                            style = MaterialTheme.typography.bodySmall,
                            color = CrimsonPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (request.isFulfilled) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Fulfilled",
                            color = EmeraldGreen,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    UrgencyBadge(urgency = request.urgency)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalHospital,
                    contentDescription = null,
                    tint = CrimsonPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${request.hospitalName}, ${request.hospitalLocation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (request.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Note: ${request.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B6464)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (!request.isFulfilled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onCall(request.contactPhone) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BloodRed)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Call (${request.contactPhone})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = onMarkFulfilled,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Mark Fulfilled",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Fulfilled", color = EmeraldGreen, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBloodRequestDialog(
    defaultPhone: String,
    onDismiss: () -> Unit,
    onSubmit: (
        patientName: String,
        bloodGroup: String,
        unitsNeeded: Int,
        hospitalName: String,
        hospitalLocation: String,
        contactPhone: String,
        urgency: String,
        notes: String
    ) -> Unit
) {
    var patientName by remember { mutableStateOf("") }
    var selectedBloodGroup by remember { mutableStateOf("A+") }
    var unitsText by remember { mutableStateOf("1") }
    var hospitalName by remember { mutableStateOf("") }
    var hospitalLocation by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf(defaultPhone) }
    var selectedUrgency by remember { mutableStateOf("Critical / Immediate") }
    var notes by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Post Request & Notify Donors",
                    fontWeight = FontWeight.Bold,
                    color = CrimsonPrimary
                )
                Text(
                    text = "রক্তের আবেদন ও পুশ নোটিফিকেশন",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFECEC))
                ) {
                    Text(
                        text = "🚨 $selectedBloodGroup রক্তদাতাদের ফোনে সরাসরি পুশ নোটিফিকেশন ও অ্যালার্ট যাবে।",
                        color = CrimsonPrimary,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                if (errorText != null) {
                    Text(
                        text = errorText ?: "",
                        color = BloodRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                val dialogInputColors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = CrimsonPrimary,
                    focusedLabelColor = CrimsonPrimary,
                    unfocusedLabelColor = Color(0xFF4A4A4A),
                    cursorColor = CrimsonPrimary
                )

                OutlinedTextField(
                    value = patientName,
                    onValueChange = { patientName = it },
                    label = { Text("Patient Name (রোগীর নাম) *") },
                    singleLine = true,
                    colors = dialogInputColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("req_patient_name")
                )

                // Blood Group
                Column {
                    Text("Blood Group Needed *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(BloodCompatibilityUtil.BLOOD_GROUPS) { bg ->
                            FilterChip(
                                selected = selectedBloodGroup == bg,
                                onClick = { selectedBloodGroup = bg },
                                label = { Text(bg, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CrimsonPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = unitsText,
                    onValueChange = { if (it.length <= 2) unitsText = it.filter { c -> c.isDigit() } },
                    label = { Text("Units / Bags Needed *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = dialogInputColors,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = hospitalName,
                    onValueChange = { hospitalName = it },
                    label = { Text("Hospital Name (হাসপাতাল) *") },
                    placeholder = { Text("e.g. Dhaka Medical College") },
                    singleLine = true,
                    colors = dialogInputColors,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = hospitalLocation,
                    onValueChange = { hospitalLocation = it },
                    label = { Text("Hospital City / Area *") },
                    placeholder = { Text("e.g. Dhaka, Bakshibazar") },
                    singleLine = true,
                    colors = dialogInputColors,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    label = { Text("Contact Phone (যোগাযোগের নম্বর) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = dialogInputColors,
                    modifier = Modifier.fillMaxWidth()
                )

                // Urgency selector
                Column {
                    Text("Urgency Level *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Critical / Immediate", "Within 24 Hours", "Scheduled").forEach { u ->
                            FilterChip(
                                selected = selectedUrgency == u,
                                onClick = { selectedUrgency = u },
                                label = { Text(u.substringBefore(" /"), fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CrimsonPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (e.g. Ward / Bed no.)") },
                    colors = dialogInputColors,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (patientName.isBlank() || hospitalName.isBlank() || hospitalLocation.isBlank() || contactPhone.isBlank()) {
                        errorText = "Please fill in all required fields."
                        return@Button
                    }
                    val units = unitsText.toIntOrNull() ?: 1
                    onSubmit(
                        patientName,
                        selectedBloodGroup,
                        units,
                        hospitalName,
                        hospitalLocation,
                        contactPhone,
                        selectedUrgency,
                        notes
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                modifier = Modifier.testTag("submit_blood_request_btn")
            ) {
                Text("Post Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
