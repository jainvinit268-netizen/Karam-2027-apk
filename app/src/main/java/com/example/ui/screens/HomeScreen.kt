package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.JeeViewModel

enum class HomeSection(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    TEST_LIBRARY("Tests", Icons.Default.LibraryBooks),
    LINK_TO_CBT("Link to CBT", Icons.Default.Link),
    MISTAKE_BOOK("Mistakes", Icons.Default.MenuBook),
    ANALYTICS("Analytics", Icons.Default.AutoGraph),
    ATTEMPTS("History", Icons.Default.Assessment)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableStateOf(HomeSection.TEST_LIBRARY) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    KaramTopBarBrand(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.navigateTo(AppScreen.SplashInvocation) }
                            .padding(vertical = 4.dp)
                            .testTag("home_karam_branding_header")
                    )
                },
                actions = {
                    AiStatusTopBarChip(viewModel = viewModel)
                    UserAccountTopBarAction(viewModel = viewModel)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("home_bottom_nav")
            ) {
                HomeSection.values().forEach { section ->
                    val isSelected = selectedSection == section
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedSection = section },
                        icon = {
                            Icon(
                                imageVector = section.icon,
                                contentDescription = section.title
                            )
                        },
                        label = {
                            Text(
                                text = section.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = JeeNavyDark,
                            selectedTextColor = JeeCyan,
                            indicatorColor = JeeCyan,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_${section.name.lowercase()}")
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Surface(
                onClick = { viewModel.navigateTo(AppScreen.SplashInvocation) },
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF14141E),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2E42)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("sacred_blessings_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "🕉️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "जय गणपति बप्पा • राधे कृष्ण • जय प्रेमानंद जी",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFFFFD54F),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "❤️", fontSize = 11.sp)
                            }
                            Text(
                                text = "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन • KARAM 2027 🔥🔥",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFA5A5B5)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "View Blessings",
                        tint = JeeCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            AnimatedContent(
                targetState = selectedSection,
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        (slideInHorizontally { width -> width / 3 } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width / 3 } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> -width / 3 } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width / 3 } + fadeOut()
                        )
                    }
                },
                label = "home_section_animation",
                modifier = Modifier.weight(1f)
            ) { section ->
                when (section) {
                    HomeSection.TEST_LIBRARY -> TestLibrarySection(viewModel = viewModel)
                    HomeSection.LINK_TO_CBT -> LinkToCbtSection(viewModel = viewModel)
                    HomeSection.MISTAKE_BOOK -> MistakeBookSection(viewModel = viewModel)
                    HomeSection.ANALYTICS -> AnalyticsCoachSection(viewModel = viewModel)
                    HomeSection.ATTEMPTS -> AttemptsHistorySection(viewModel = viewModel)
                }
            }
        }
    }
}
