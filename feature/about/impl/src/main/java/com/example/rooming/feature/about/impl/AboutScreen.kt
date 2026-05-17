package com.example.rooming.feature.about.impl

import android.annotation.SuppressLint
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.rooming.core.common.CrashReporter
import com.example.rooming.core.ui.InfoChipRow
import com.example.rooming.core.ui.SectionCard
import com.example.rooming.feature.about.api.BuildVariantConfig
import com.example.rooming.feature.about.api.MapConfig
import com.example.rooming.feature.about.api.RemoteAppConfig
import com.example.rooming.feature.about.api.RemoteConfigService
import com.example.rooming.feature.about.api.UserProfile
import com.example.rooming.feature.about.api.UserProfileService
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView

private const val OFFICE_LAT = 53.7557
private const val OFFICE_LON = 87.1099
private val DemoStartPoint = Point(53.7574, 87.1364)

@Composable
fun AboutRoute(
    mapConfig: MapConfig,
    buildVariantConfig: BuildVariantConfig,
    remoteConfigService: RemoteConfigService,
    userProfileService: UserProfileService,
    crashReporter: CrashReporter,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        remoteConfigService.start()
        userProfileService.start()
    }

    val remoteConfig by remoteConfigService.config.collectAsStateWithLifecycle()
    val userProfile by userProfileService.profile.collectAsStateWithLifecycle()
    val profileError by userProfileService.errorMessage.collectAsStateWithLifecycle()

    AboutScreen(
        hasMapKey = mapConfig.yandexMapKitApiKey.isNotBlank(),
        buildVariantConfig = buildVariantConfig,
        remoteConfig = remoteConfig,
        userProfile = userProfile,
        profileError = profileError,
        onGenerateCrashClick = {
            crashReporter.log("Generate crash button clicked")
            crashReporter.setKey("screen", "about")
            crashReporter.setKey("remote_experiment_enabled", remoteConfig.isExperimentalEnabled.toString())
            crashReporter.setUserId(userProfile?.userId)
            throw RuntimeException("Manual crash from laboratory work 8")
        },
        onLogoutClick = onLogoutClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    hasMapKey: Boolean,
    buildVariantConfig: BuildVariantConfig,
    remoteConfig: RemoteAppConfig,
    userProfile: UserProfile?,
    profileError: String?,
    onGenerateCrashClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var routeStartPoint by remember { mutableStateOf<Point?>(null) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        routeStartPoint = if (granted) {
            findCurrentPoint(context) ?: DemoStartPoint
        } else {
            DemoStartPoint
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("О нас") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF082B66),
                                Color(0xFF0B5FFF),
                                Color(0xFF8EC5FF),
                            ),
                        ),
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.BottomStart,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Rooming Labs",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Text(
                        text = "Мы помогаем университетам бронировать аудитории без бумажных журналов и бесконечных чатов.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.86f),
                    )
                }
            }

            SectionCard(
                title = remoteConfig.welcomeMessage,
                subtitle = if (remoteConfig.isExperimentalEnabled) {
                    "Экспериментальная функция включена"
                } else {
                    "Экспериментальная функция выключена"
                },
            ) {
                InfoChipRow(
                    labels = listOf(
                        "Remote Config",
                        "variant=${buildVariantConfig.environmentName}",
                        if (remoteConfig.isExperimentalEnabled) "flag=true" else "flag=false",
                    ),
                )
            }

            SectionCard(
                title = "Профиль",
                subtitle = profileError ?: "Данные синхронизируются через Cloud Firestore.",
            ) {
                val profile = userProfile
                if (profile == null) {
                    Text(
                        text = "Профиль загружается...",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    InfoChipRow(
                        labels = listOf(
                            "UID: ${profile.userId.take(8)}",
                            profile.email.ifBlank { "email не задан" },
                        ),
                    )
                    Text(
                        text = "Имя: ${profile.name.ifBlank { "Пользователь Rooming" }}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "FCM-токен: ${profile.fcmToken.asShortToken()}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (profile.updatedAt.isNotBlank()) {
                        Text(
                            text = "Обновлено: ${profile.updatedAt}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            if (buildVariantConfig.labToolsEnabled) {
                SectionCard(
                    title = "Crash reporting",
                    subtitle = "Тестовый сценарий для Firebase Crashlytics и AppMetrica.",
                ) {
                    InfoChipRow(
                        labels = listOf(
                            "Crashlytics",
                            "AppMetrica",
                            "non-fatal context",
                        ),
                    )
                    Button(
                        onClick = onGenerateCrashClick,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(imageVector = Icons.Outlined.BugReport, contentDescription = null)
                        Text(
                            text = "Сгенерировать тестовый краш",
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }

            SectionCard(
                title = "Компания",
                subtitle = "Вымышленная команда для лабораторной работы 6.",
            ) {
                InfoChipRow(
                    labels = listOf(
                        "Новокузнецк",
                        "EdTech",
                        "Бронирование аудиторий",
                    ),
                )
                Text(
                    text = "Офис: г. Новокузнецк, ул. Кирова, 42. Здесь мы проектируем сервисы, которые экономят время преподавателей, студентов и администраторов.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(
                    onClick = onLogoutClick,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Outlined.Logout, contentDescription = null)
                    Text(
                        text = "Выйти из аккаунта",
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }

            SectionCard(
                title = "Карта офиса",
                subtitle = "Наш офис отмечен на карте. Можно сразу построить маршрут от текущего местоположения.",
            ) {
                if (hasMapKey) {
                    OfficeMap(routeStartPoint = routeStartPoint)
                } else {
                    MapUnavailableState()
                }
                Button(
                    onClick = {
                        if (context.hasLocationPermission()) {
                            routeStartPoint = findCurrentPoint(context) ?: DemoStartPoint
                        } else {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    enabled = hasMapKey,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(imageVector = Icons.Outlined.Route, contentDescription = null)
                    Text(
                        text = "Показать маршрут на карте",
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun OfficeMap(routeStartPoint: Point?) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val officePoint = remember { Point(OFFICE_LAT, OFFICE_LON) }
    val mapView = rememberMapView(officePoint)

    DisposableEffect(lifecycleOwner, mapView) {
        var isStarted = false
        fun startMap() {
            if (!isStarted) {
                MapKitFactory.getInstance().onStart()
                mapView.onStart()
                isStarted = true
            }
        }

        fun stopMap() {
            if (isStarted) {
                mapView.onStop()
                MapKitFactory.getInstance().onStop()
                isStarted = false
            }
        }

        startMap()

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> startMap()
                Lifecycle.Event.ON_STOP -> stopMap()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            stopMap()
        }
    }

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(24.dp)),
        factory = { mapView },
        update = { view ->
            view.renderOfficeMap(officePoint = officePoint, routeStartPoint = routeStartPoint)
        },
    )
}

@Composable
private fun rememberMapView(officePoint: Point): MapView {
    val context = LocalContext.current
    return remember(context, officePoint) {
        MapView(context).apply {
            renderOfficeMap(officePoint = officePoint, routeStartPoint = null)
        }
    }
}

@Composable
private fun MapUnavailableState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFE7F0FF))
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Добавьте YANDEX_MAPKIT_API_KEY, чтобы показать карту офиса внутри приложения.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF10213A),
        )
    }
}

private fun MapView.renderOfficeMap(
    officePoint: Point,
    routeStartPoint: Point?,
) {
    val map = mapWindow.map
    val objects = map.mapObjects
    objects.clear()
    objects.addPlacemark(officePoint)

    if (routeStartPoint == null) {
        map.move(CameraPosition(officePoint, 16f, 0f, 0f))
        return
    }

    objects.addPlacemark(routeStartPoint)
    objects.addPolyline(Polyline(listOf(routeStartPoint, officePoint)))
    map.move(CameraPosition(routeStartPoint.centerWith(officePoint), 12f, 0f, 0f))
}

private fun Point.centerWith(other: Point): Point =
    Point(
        (latitude + other.latitude) / 2,
        (longitude + other.longitude) / 2,
    )

private fun Context.hasLocationPermission(): Boolean =
    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

@SuppressLint("MissingPermission")
private fun findCurrentPoint(context: Context): Point? {
    if (!context.hasLocationPermission()) return null

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.getProviders(true)
        .asSequence()
        .mapNotNull { provider ->
            runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
        }
        .maxByOrNull { location -> location.time }
        ?.let { location -> Point(location.latitude, location.longitude) }
}

private fun String.asShortToken(): String =
    if (isBlank()) {
        "ещё не получен"
    } else {
        "${take(18)}..."
    }
