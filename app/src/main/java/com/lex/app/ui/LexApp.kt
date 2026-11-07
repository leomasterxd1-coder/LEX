package com.lex.app.ui

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lex.app.LexViewModel
import com.lex.app.data.JobKind
import com.lex.app.ui.components.LexBottomBar
import com.lex.app.ui.screens.AboutScreen
import com.lex.app.ui.screens.CopyScreen
import com.lex.app.ui.screens.DataCollectionScreen
import com.lex.app.ui.screens.DeviceDetailScreen
import com.lex.app.ui.screens.DevicesScreen
import com.lex.app.ui.screens.HomeScreen
import com.lex.app.ui.screens.ImportDataScreen
import com.lex.app.ui.screens.MoreScreen
import com.lex.app.ui.screens.OnboardingScreen
import com.lex.app.ui.screens.OpenSourceScreen
import com.lex.app.ui.screens.PrintScreen
import com.lex.app.ui.screens.QueueScreen
import com.lex.app.ui.screens.ScanScreen
import com.lex.app.ui.screens.SettingsScreen
import com.lex.app.ui.screens.SourcesScreen
import com.lex.app.ui.screens.WebPrintScreen
import com.lex.app.ui.screens.WhatsNewScreen

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val PRINT = "print"
    const val SCAN = "scan"
    const val COPY = "copy"
    const val WEB = "web"
    const val DEVICES = "devices"
    const val QUEUE = "queue"
    const val MORE = "more"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val WHATS_NEW = "whatsnew"
    const val DATA_COLLECTION = "datacollection"
    const val OPEN_SOURCE = "opensource"
    const val IMPORT_DATA = "importdata"
    const val SOURCES = "sources"
    const val DEVICE = "device/{id}"

    fun device(id: String) = "device/$id"
}

@Composable
fun LexApp(viewModel: LexViewModel = viewModel()) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val state by viewModel.data.collectAsStateWithLifecycle()
    val discovered by viewModel.discovered.collectAsStateWithLifecycle()
    val discovering by viewModel.discovering.collectAsStateWithLifecycle()
    val discoveryStatus by viewModel.discoveryStatus.collectAsStateWithLifecycle()

    val startDestination = remember { if (state.onboardingDone) Routes.HOME else Routes.ONBOARDING }

    val notificationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    val nearbyWifiPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startDiscovery()
        } else {
            Toast.makeText(context, "A permissão de dispositivos Wi-Fi é necessária para a busca.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!granted) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val bottomDestinations = listOf(
        Triple(Routes.HOME, "Início", Icons.Filled.Home),
        Triple(Routes.DEVICES, "Localizar\ndispositivo", Icons.Filled.Search),
        Triple(Routes.QUEUE, "Status", Icons.Filled.History),
        Triple(Routes.MORE, "Mais", Icons.Filled.MoreHoriz)
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomDestinations.map { it.first }

    fun startDiscoveryWithPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            nearbyWifiPermission.launch(Manifest.permission.NEARBY_WIFI_DEVICES)
        } else {
            viewModel.startDiscovery()
        }
    }

    fun navigateTopLevel(route: String) {
        if (currentRoute == route) return
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    composable(Routes.ONBOARDING) {
                        OnboardingScreen(
                            onFinish = { addDemoDevice, dataCollection ->
                                viewModel.setDataCollection(dataCollection)
                                if (addDemoDevice) viewModel.addDemoDevice()
                                viewModel.completeOnboarding()
                                navController.navigate(Routes.HOME) {
                                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable(Routes.HOME) {
                        HomeScreen(
                            device = state.defaultDevice,
                            onOpenDevice = { id -> navController.navigate(Routes.device(id)) },
                            onFindDevices = { navigateTopLevel(Routes.DEVICES) },
                            onOpenCamera = { navController.navigate(Routes.SCAN) },
                            onOpenPhotos = { navController.navigate(Routes.SCAN) },
                            onOpenFiles = { navController.navigate(Routes.PRINT) },
                            onOpenWeb = { navController.navigate(Routes.WEB) },
                            onOpenScanner = { navController.navigate(Routes.SCAN) },
                            onEditSources = { navController.navigate(Routes.SOURCES) }
                        )
                    }

                    composable(Routes.PRINT) {
                        PrintScreen(
                            state = state,
                            onCreateJob = { name, kind, pages, color, duplex, detail ->
                                viewModel.createJob(
                                    name = name,
                                    kind = kind,
                                    device = state.defaultDevice,
                                    pages = pages,
                                    color = color,
                                    duplex = duplex,
                                    detail = detail
                                )
                            },
                            onPrintSettingsChange = viewModel::setPrintSettings,
                            onSelectDevice = viewModel::setDefaultDevice,
                            onBack = { navController.popBackStack() },
                            onOpenDevices = { navigateTopLevel(Routes.DEVICES) },
                            onOpenWeb = { navController.navigate(Routes.WEB) },
                            onOpenScan = { navigateTopLevel(Routes.SCAN) }
                        )
                    }

                    composable(Routes.SCAN) {
                        ScanScreen(
                            state = state,
                            scanSettings = state.scanSettings,
                            onCreateJob = { name, kind, pages, color, duplex, detail ->
                                viewModel.createJob(
                                    name = name,
                                    kind = kind,
                                    device = state.defaultDevice,
                                    pages = pages,
                                    color = color,
                                    duplex = duplex,
                                    detail = detail
                                )
                            },
                            onScanSettingsChange = viewModel::setScanSettings,
                            onBack = { navController.popBackStack() },
                            onOpenDevices = { navigateTopLevel(Routes.DEVICES) }
                        )
                    }

                    composable(Routes.COPY) {
                        CopyScreen(
                            state = state,
                            onCreateJob = { name, kind, pages, color, duplex, detail ->
                                viewModel.createJob(
                                    name = name,
                                    kind = kind,
                                    device = state.defaultDevice,
                                    pages = pages,
                                    color = color,
                                    duplex = duplex,
                                    detail = detail
                                )
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Routes.WEB) {
                        WebPrintScreen(
                            state = state,
                            onCreateJob = { name, kind, pages, color, duplex, detail ->
                                viewModel.createJob(
                                    name = name,
                                    kind = kind,
                                    device = state.defaultDevice,
                                    pages = pages,
                                    color = color,
                                    duplex = duplex,
                                    detail = detail
                                )
                            },
                            onAddHistory = viewModel::addWebHistory,
                            onClearHistory = viewModel::clearWebHistory,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Routes.DEVICES) {
                        DevicesScreen(
                            state = state,
                            discovered = discovered,
                            discovering = discovering,
                            discoveryStatus = discoveryStatus,
                            onStartDiscovery = ::startDiscoveryWithPermission,
                            onStopDiscovery = viewModel::stopDiscovery,
                            onAddDevice = viewModel::addDevice,
                            onSetDefault = viewModel::setDefaultDevice,
                            onOpenDevice = { id -> navController.navigate(Routes.device(id)) },
                            onOpenQueue = { navController.navigate(Routes.QUEUE) },
                            onAddDemoDevice = viewModel::addDemoDevice
                        )
                    }

                    composable(Routes.QUEUE) {
                        QueueScreen(
                            state = state,
                            onBack = null,
                            onReleaseAll = viewModel::releaseAll,
                            onRelease = viewModel::releaseJob,
                            onDelete = viewModel::deleteJob,
                            onClearFinished = viewModel::clearFinishedJobs,
                            onOpenDevices = { navigateTopLevel(Routes.DEVICES) }
                        )
                    }

                    composable(Routes.MORE) {
                        MoreScreen(
                            state = state,
                            onOpenDevices = { navigateTopLevel(Routes.DEVICES) },
                            onOpenSources = { navController.navigate(Routes.SOURCES) },
                            onOpenQueue = { navController.navigate(Routes.QUEUE) },
                            onOpenImport = { navController.navigate(Routes.IMPORT_DATA) },
                            onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                            onOpenWhatsNew = { navController.navigate(Routes.WHATS_NEW) },
                            onOpenAbout = { navController.navigate(Routes.ABOUT) },
                            onOpenDataCollection = { navController.navigate(Routes.DATA_COLLECTION) },
                            onOpenOpenSource = { navController.navigate(Routes.OPEN_SOURCE) }
                        )
                    }

                    composable(Routes.SETTINGS) {
                        SettingsScreen(
                            state = state,
                            onBack = { navController.popBackStack() },
                            onPrintSettingsChange = viewModel::setPrintSettings,
                            onScanSettingsChange = viewModel::setScanSettings,
                            onNotificationsChange = viewModel::setNotifications,
                            onDataCollectionChange = viewModel::setDataCollection,
                            onReset = {
                                viewModel.resetApp()
                                navController.navigate(Routes.ONBOARDING) {
                                    popUpTo(Routes.HOME) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable(Routes.ABOUT) {
                        AboutScreen(
                            onBack = { navController.popBackStack() },
                            onOpenOpenSource = { navController.navigate(Routes.OPEN_SOURCE) }
                        )
                    }

                    composable(Routes.WHATS_NEW) {
                        WhatsNewScreen(onBack = { navController.popBackStack() })
                    }

                    composable(Routes.DATA_COLLECTION) {
                        DataCollectionScreen(
                            state = state,
                            onBack = { navController.popBackStack() },
                            onDataCollectionChange = viewModel::setDataCollection
                        )
                    }

                    composable(Routes.OPEN_SOURCE) {
                        OpenSourceScreen(onBack = { navController.popBackStack() })
                    }

                    composable(Routes.IMPORT_DATA) {
                        ImportDataScreen(
                            onBack = { navController.popBackStack() },
                            importJson = viewModel::importDevices,
                            exportJson = viewModel::devicesAsJson
                        )
                    }

                    composable(Routes.SOURCES) {
                        SourcesScreen(
                            state = state,
                            onBack = { navController.popBackStack() },
                            onToggleSource = viewModel::toggleSource
                        )
                    }

                    composable(
                        route = Routes.DEVICE,
                        arguments = listOf(navArgument("id") { type = NavType.StringType })
                    ) { entry ->
                        val id = entry.arguments?.getString("id").orEmpty()
                        DeviceDetailScreen(
                            state = state,
                            deviceId = id,
                            onBack = { navController.popBackStack() },
                            onSetDefault = viewModel::setDefaultDevice,
                            onRemove = viewModel::removeDevice,
                            onOpenPrint = { navController.navigate(Routes.PRINT) },
                            onOpenScan = { navController.navigate(Routes.SCAN) }
                        )
                    }
                }
            }

            if (showBottomBar) {
                LexBottomBar(
                    currentRoute = currentRoute,
                    onSelect = ::navigateTopLevel,
                    destinations = bottomDestinations
                )
            }
        }
    }
}
