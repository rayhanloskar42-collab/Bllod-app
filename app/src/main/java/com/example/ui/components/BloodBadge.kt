package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BloodRed
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonSecondary
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent

@Composable
fun BloodBadge(
    bloodGroup: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    fontSize: Int = 18
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(CrimsonSecondary, CrimsonPrimary)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = bloodGroup,
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = fontSize.sp,
            letterSpacing = (-0.5).sp
        )
    }
}

@Composable
fun DonorAvailabilityBadge(
    isAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isAvailable) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
    val textColor = if (isAvailable) EmeraldGreen else Color(0xFF757575)
    val text = if (isAvailable) "Available to Donate" else "Currently Resting"

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(textColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun UrgencyBadge(
    urgency: String,
    modifier: Modifier = Modifier
) {
    val isCritical = urgency.contains("Critical", ignoreCase = true) || urgency.contains("Immediate", ignoreCase = true)
    val bgColor = if (isCritical) Color(0xFFFFEBEE) else Color(0xFFFFF8E1)
    val textColor = if (isCritical) BloodRed else Color(0xFFE65100)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isCritical) Icons.Default.Warning else Icons.Default.CheckCircle,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = urgency,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
