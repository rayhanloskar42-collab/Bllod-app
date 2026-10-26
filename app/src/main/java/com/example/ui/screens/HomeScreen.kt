package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.BloodCompatibilityUtil
import com.example.data.model.BloodRequestEntity
import com.example.data.model.UserEntity
import com.example.ui.components.BloodBadge
import com.example.ui.components.BloodDonationVideoBanner
import com.example.ui.components.DonorAvailabilityBadge
import com.example.ui.components.UrgencyBadge
import com.example.ui.theme.BloodRed
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonSecondary
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent

@Composable
fun HomeScreen(
    currentUser: UserEntity?,
    urgentRequests: List<BloodRequestEntity>,
    totalDonorsCount: Int,
    videoUrl: String,
    onNavigateToDonors: (String?) -> Unit,
    onNavigateToRequests: () -> Unit,
    onNavigateToCompatibility: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onToggleAvailability: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFCF9F9)),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Top App Header & Greeting
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(CrimsonPrimary, CrimsonSecondary)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Welcome, ${currentUser?.firstName ?: "Donor"}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = currentUser?.location ?: "Bangladesh",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }

                        if (currentUser != null) {
                            BloodBadge(
                                bloodGroup = currentUser.bloodGroup,
                                size = 52.dp,
                                fontSize = 18
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Availability Card inside Header
                    if (currentUser != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.18f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(if (currentUser.isAvailable) EmeraldGreen else Color.White)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (currentUser.isAvailable) "Ready to Donate Blood" else "Currently Resting",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Switch(
                                    checked = currentUser.isAvailable,
                                    onCheckedChange = { onToggleAvailability(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = CrimsonPrimary,
                                        checkedTrackColor = Color.White,
                                        uncheckedThumbColor = Color.LightGray,
                                        uncheckedTrackColor = Color.White.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Blood Donation Video Banner from InsForge Storage
        item {
            BloodDonationVideoBanner(
                videoUrl = videoUrl,
                modifier = Modifier.padding(16.dp)
            )
        }

        // Key Stats Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Total Donors",
                    count = "$totalDonorsCount+",
                    icon = Icons.Default.People,
                    color = CrimsonPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Urgent Needs",
                    count = "${urgentRequests.count { !it.isFulfilled }}",
                    icon = Icons.Default.AddAlert,
                    color = BloodRed,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Blood Groups",
                    count = "8 Types",
                    icon = Icons.Default.Healing,
                    color = GoldAccent,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Action Shortcuts Grid
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionTile(
                        title = "Find Donors",
                        subtitle = "Search by group",
                        icon = Icons.Default.Search,
                        containerColor = Color(0xFFFFEBEE),
                        contentColor = CrimsonPrimary,
                        onClick = { onNavigateToDonors(null) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_find_donors")
                    )
                    ActionTile(
                        title = "Request Blood",
                        subtitle = "Post urgent need",
                        icon = Icons.Default.AddAlert,
                        containerColor = Color(0xFFFFF3E0),
                        contentColor = Color(0xFFE65100),
                        onClick = onNavigateToRequests,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_request_blood")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionTile(
                        title = "Compatibility",
                        subtitle = "Who can donate",
                        icon = Icons.Default.VerifiedUser,
                        containerColor = Color(0xFFE8F5E9),
                        contentColor = EmeraldGreen,
                        onClick = onNavigateToCompatibility,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_compatibility")
                    )
                    ActionTile(
                        title = "Donor ID Card",
                        subtitle = "My Donor Profile",
                        icon = Icons.Default.Favorite,
                        containerColor = Color(0xFFEDE7F6),
                        contentColor = Color(0xFF512DA8),
                        onClick = onNavigateToProfile,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_profile")
                    )
                }
            }
        }

        // Blood Group Quick Filter Chips
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Text(
                    text = "Find Donors by Blood Group",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(BloodCompatibilityUtil.BLOOD_GROUPS) { bg ->
                        OutlinedButton(
                            onClick = { onNavigateToDonors(bg) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (currentUser?.bloodGroup == bg) Color(0xFFFFECEC) else Color.White
                            )
                        ) {
                            Text(
                                text = bg,
                                fontWeight = FontWeight.Bold,
                                color = CrimsonPrimary,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // Urgent Blood Requests Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Emergency Requests (জরুরি রক্ত প্রয়োজন)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BloodRed
                )
                TextButton(onClick = onNavigateToRequests) {
                    Text("View All", color = CrimsonPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        val openRequests = urgentRequests.filter { !it.isFulfilled }
        if (openRequests.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No active emergency requests right now.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(openRequests.take(3)) { req ->
                EmergencyRequestCard(
                    request = req,
                    onCallClick = { phone ->
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    count: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1A1A)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF5A4848)
                )
            }
        }
    }
}

@Composable
fun EmergencyRequestCard(
    request: BloodRequestEntity,
    onCallClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
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
                    BloodBadge(bloodGroup = request.bloodGroup, size = 44.dp, fontSize = 16)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = request.patientName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${request.unitsNeeded} unit(s) blood needed",
                            style = MaterialTheme.typography.bodySmall,
                            color = CrimsonPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                UrgencyBadge(urgency = request.urgency)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${request.hospitalName}, ${request.hospitalLocation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (request.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = request.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B6464),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onCallClick(request.contactPhone) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BloodRed)
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call Contact",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Call Contact (${request.contactPhone})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
