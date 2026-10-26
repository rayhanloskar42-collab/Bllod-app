package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.AddAlert
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonSecondary
import com.example.viewmodel.BloodDonorViewModel

enum class MainTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_tab_home"),
    DONORS("Donors", Icons.Filled.People, Icons.Outlined.People, "nav_tab_donors"),
    REQUESTS("Requests", Icons.Filled.AddAlert, Icons.Outlined.AddAlert, "nav_tab_requests"),
    COMPATIBILITY("Guide", Icons.Filled.VerifiedUser, Icons.Outlined.VerifiedUser, "nav_tab_guide"),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person, "nav_tab_profile")
}

@Composable
fun MainAppContainer(
    viewModel: BloodDonorViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val currentUser by viewModel.currentUserProfile.collectAsStateWithLifecycle()
    val donors by viewModel.filteredDonors.collectAsStateWithLifecycle()
    val allDonors by viewModel.allDonors.collectAsStateWithLifecycle()
    val allRequests by viewModel.allBloodRequests.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val videoUrl by viewModel.videoUrl.collectAsStateWithLifecycle()

    // Handle back button on secondary tabs to return to Home
    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_bottom_nav"),
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                MainTab.values().forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(tab.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CrimsonPrimary,
                            selectedTextColor = CrimsonPrimary,
                            indicatorColor = Color(0xFFFFECEC),
                            unselectedIconColor = Color(0xFF757575),
                            unselectedTextColor = Color(0xFF757575)
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        },
        containerColor = Color(0xFFFCF9F9),
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (selectedTab) {
                0 -> HomeScreen(
                    currentUser = currentUser,
                    urgentRequests = allRequests,
                    totalDonorsCount = allDonors.size,
                    videoUrl = videoUrl,
                    onNavigateToDonors = { bloodGroup ->
                        viewModel.setBloodGroupFilter(bloodGroup)
                        selectedTab = 1
                    },
                    onNavigateToRequests = { selectedTab = 2 },
                    onNavigateToCompatibility = { selectedTab = 3 },
                    onNavigateToProfile = { selectedTab = 4 },
                    onToggleAvailability = { viewModel.toggleAvailability(it) }
                )

                1 -> DonorsScreen(
                    donors = donors,
                    filterState = filterState,
                    onSelectBloodGroup = { viewModel.setBloodGroupFilter(it) },
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onToggleAvailableOnly = { viewModel.toggleAvailableOnly(it) },
                    onClearFilters = { viewModel.clearFilters() }
                )

                2 -> BloodRequestsScreen(
                    requests = allRequests,
                    onCreateRequest = { pName, bg, units, hName, hLoc, phone, urgency, notes ->
                        viewModel.createBloodRequest(pName, bg, units, hName, hLoc, phone, urgency, notes)
                    },
                    onMarkFulfilled = { viewModel.markRequestFulfilled(it) },
                    defaultContactPhone = currentUser?.phoneNumber ?: ""
                )

                3 -> CompatibilityScreen(
                    userBloodGroup = currentUser?.bloodGroup
                )

                4 -> ProfileScreen(
                    user = currentUser,
                    onToggleAvailability = { viewModel.toggleAvailability(it) },
                    onLogDonation = { viewModel.logDonation(it) },
                    onUpdateProfile = { phone, location, age ->
                        viewModel.updateProfile(phone, location, age)
                    },
                    onLogout = onLogout
                )
            }
        }
    }
}
