package com.example

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ClockAppMainScreen()
            }
        }
    }
}

// Data holder for World Cities
data class WorldCity(
    val englishName: String,
    val arabicName: String,
    val timezoneId: String,
    val flagEmoji: String
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ClockAppMainScreen() {
    val context = LocalContext.current
    
    // Theme options: 0-Cosmic Charcoal, 1-Cyber Neon, 2-Crimson Orchid
    var appThemeMode by remember { mutableStateOf(0) }
    
    val baseBackground = when (appThemeMode) {
        0 -> listOf(Color(0xFF030712), Color(0xFF0F172A), Color(0xFF1E293B))
        1 -> listOf(Color(0xFF000511), Color(0xFF050E28), Color(0xFF011627))
        else -> listOf(Color(0xFF0B0106), Color(0xFF220511), Color(0xFF3F0B23))
    }
    
    val primaryColor = when (appThemeMode) {
        0 -> Color(0xFF06B6D4) // Cyan
        1 -> Color(0xFF39FF14) // Electric Green
        else -> Color(0xFFEC4899) // Hot Pink
    }
    
    val secondaryColor = when (appThemeMode) {
        0 -> Color(0xFF3B82F6) // Electric Blue
        1 -> Color(0xFF00E5FF) // Cyber Cyan
        else -> Color(0xFFEF4444) // Bright Red
    }

    var selectedTab by remember { mutableStateOf(0) } // 0: الساعة, 1: ساعة إيقاف, 2: المؤقت

    // Timer Active States (globalized to continue ticking even if tab changes)
    var isTimerRunning by remember { mutableStateOf(false) }
    var timerRemainingMs by remember { mutableStateOf(0L) }
    var timerTotalMs by remember { mutableStateOf(0L) }
    var isTimerAlarmActive by remember { mutableStateOf(false) }

    // Coroutine for the Timer Countdown
    LaunchedEffect(isTimerRunning, timerRemainingMs) {
        if (isTimerRunning && timerRemainingMs > 0) {
            val startTime = System.currentTimeMillis()
            val initialRemaining = timerRemainingMs
            while (isTimerRunning && timerRemainingMs > 0) {
                delay(100)
                val elapsed = System.currentTimeMillis() - startTime
                timerRemainingMs = (initialRemaining - elapsed).coerceAtLeast(0L)
                if (timerRemainingMs == 0L) {
                    isTimerRunning = false
                    isTimerAlarmActive = true
                    // Vibrate alert
                    triggerDeviceVibrator(context)
                }
            }
        }
    }

    // Main layout
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_scaffold"),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(baseBackground))
                .padding(innerPadding)
                .safeDrawingPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Beautiful Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo and Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Clock Logo",
                            tint = primaryColor,
                            modifier = Modifier
                                .size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "الساعة الذكية",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                    }

                    // Theme Picker Toggle Button
                    Row(
                        modifier = Modifier
                            .background(Color(0x1F27272A), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("🌌", "📟", "🌺").forEachIndexed { index, emoji ->
                            Text(
                                text = emoji,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (appThemeMode == index) Color(0x33FFFFFF) else Color.Transparent)
                                    .clickable { appThemeMode = index }
                                    .padding(vertical = 4.dp, horizontal = 10.dp),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Modern Segmented Pill Tabs selector (Pure Arabized)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(Color(0x12FFFFFF), RoundedCornerShape(100.dp))
                        .border(1.dp, Color(0x17FFFFFF), RoundedCornerShape(100.dp))
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val tabs = listOf(
                        Triple(0, "الساعة", Icons.Default.Home),
                        Triple(1, "ساعة الإيقاف", Icons.Default.List),
                        Triple(2, "المؤقت", Icons.Default.Notifications)
                    )

                    tabs.forEach { (index, title, icon) ->
                        val isSelected = selectedTab == index
                        val bgBrush = if (isSelected) {
                            Brush.horizontalGradient(listOf(primaryColor, secondaryColor))
                        } else {
                            Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        }

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(100.dp))
                                .background(bgBrush)
                                .clickable { selectedTab = index }
                                .padding(horizontal = 4.dp)
                                .testTag("tab_button_$index"),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                tint = if (isSelected) Color.White else Color(0xB2FFFFFF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = title,
                                color = if (isSelected) Color.White else Color(0xCCFFFFFF),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Tab Content with Animated Content Triggers
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInHorizontally { width -> width } + fadeIn() with
                                        slideOutHorizontally { width -> -width } + fadeOut()
                            } else {
                                slideInHorizontally { width -> -width } + fadeIn() with
                                        slideOutHorizontally { width -> width } + fadeOut()
                            }.using(
                                SizeTransform(clip = false)
                            )
                        },
                        label = "tab_content_transition"
                    ) { targetTab ->
                        when (targetTab) {
                            0 -> MainTimeTab(primaryColor = primaryColor, secondaryColor = secondaryColor)
                            1 -> StopwatchTab(primaryColor = primaryColor, secondaryColor = secondaryColor)
                            2 -> TimerTab(
                                primaryColor = primaryColor,
                                secondaryColor = secondaryColor,
                                isRunning = isTimerRunning,
                                remainingMs = timerRemainingMs,
                                totalMs = timerTotalMs,
                                onRunningChange = { isTimerRunning = it },
                                onRemainingChange = { timerRemainingMs = it },
                                onTotalChange = { timerTotalMs = it }
                            )
                        }
                    }
                }
            }

            // High-priority full screen Alarm Trigger Event Overlay if Timer finished
            if (isTimerAlarmActive) {
                TimerAlertOverlay(
                    primaryColor = primaryColor,
                    onDismiss = {
                        isTimerAlarmActive = false
                    }
                )
            }
        }
    }
}

// -----------------------------------------------------------------
// TAB 0: THE PRIMARY CLOCK + WORLD CLOCK
// -----------------------------------------------------------------
@Composable
fun MainTimeTab(primaryColor: Color, secondaryColor: Color) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    
    // Coroutine update tick
    LaunchedEffect(Unit) {
        while (isActive) {
            currentTime = System.currentTimeMillis()
            delay(16) // 60 FPS update speed for liquid analogue sweeping
        }
    }

    val calendar = Calendar.getInstance().apply { timeInMillis = currentTime }
    val hour = calendar.get(Calendar.HOUR)
    val minute = calendar.get(Calendar.MINUTE)
    val second = calendar.get(Calendar.SECOND)
    val millisecond = calendar.get(Calendar.MILLISECOND)

    // Liquids sweep hands calculations
    val sweepSecond = second + (millisecond / 1000f)
    val sweepMinute = minute + (sweepSecond / 60f)
    val sweepHour = (hour % 12) + (sweepMinute / 60f)

    // Formatter for time and Arabic Date
    val arabicLocale = Locale("ar")
    val dayNameFormat = SimpleDateFormat("EEEE", arabicLocale)
    val dateFormat = SimpleDateFormat("d MMMM yyyy", arabicLocale)
    val digitalTimeFormat = SimpleDateFormat("hh:mm:ss", arabicLocale)
    val amPmFormat = SimpleDateFormat("a", arabicLocale)

    val formattedDay = dayNameFormat.format(Date(currentTime))
    val formattedDate = dateFormat.format(Date(currentTime))
    val formattedDigital = digitalTimeFormat.format(Date(currentTime))
    val formattedAmPm = amPmFormat.format(Date(currentTime))

    // World Cities Definition
    val worldCities = listOf(
        WorldCity("Mecca", "مكة المكرمة", "Asia/Riyadh", "🕋"),
        WorldCity("Dubai", "دبي", "Asia/Dubai", "🇦🇪"),
        WorldCity("Paris", "باريس", "Europe/Paris", "🗼"),
        WorldCity("Tokyo", "طوكيو", "Asia/Tokyo", "🇯🇵"),
        WorldCity("New York", "نيويورك", "America/New_York", "🗽")
    )
    
    var selectedCityIdx by remember { mutableStateOf(0) }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Section: Gorgeous Custom Drawn Canvas Analog Clock
        item {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                        )
                    )
                    .border(
                        BorderStroke(
                            2.5.dp,
                            Brush.linearGradient(listOf(primaryColor, Color(0x33FFFFFF), secondaryColor))
                        ),
                        CircleShape
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerOffset = Offset(size.width / 2, size.height / 2)
                    val radius = size.minDimension / 2

                    // 12 Ticks drawing
                    for (i in 0 until 12) {
                        val angleDeg = i * 30f
                        val angleRad = Math.toRadians(angleDeg.toDouble())
                        val isMajor = i % 3 == 0

                        val tickStartLen = if (isMajor) radius * 0.82f else radius * 0.88f
                        val tickEndLen = radius * 0.94f

                        val startOffset = centerOffset + Offset(
                            (tickStartLen * sin(angleRad)).toFloat(),
                            (-tickStartLen * cos(angleRad)).toFloat()
                        )
                        val endOffset = centerOffset + Offset(
                            (tickEndLen * sin(angleRad)).toFloat(),
                            (-tickEndLen * cos(angleRad)).toFloat()
                        )

                        drawLine(
                            color = if (isMajor) primaryColor else Color(0x66FFFFFF),
                            start = startOffset,
                            end = endOffset,
                            strokeWidth = if (isMajor) 3.5.dp.toPx() else 1.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // Hands Geometry calculations
                    val hourAngleRad = Math.toRadians(sweepHour * 30.0)
                    val minuteAngleRad = Math.toRadians(sweepMinute * 6.0)
                    val secondAngleRad = Math.toRadians(sweepSecond * 6.0)

                    val hourLength = radius * 0.50f
                    val minuteLength = radius * 0.73f
                    val secondLength = radius * 0.84f
                    val secondTailLength = radius * 0.18f

                    // 1. Hour Hand (Thick, Grey styled)
                    val hourEnd = centerOffset + Offset(
                        (hourLength * sin(hourAngleRad)).toFloat(),
                        (-hourLength * cos(hourAngleRad)).toFloat()
                    )
                    drawLine(
                        color = Color.White,
                        start = centerOffset,
                        end = hourEnd,
                        strokeWidth = 6.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // 2. Minute Hand (Medium, White styled)
                    val minuteEnd = centerOffset + Offset(
                        (minuteLength * sin(minuteAngleRad)).toFloat(),
                        (-minuteLength * cos(minuteAngleRad)).toFloat()
                    )
                    drawLine(
                        color = Color(0xFFE2E8F0),
                        start = centerOffset,
                        end = minuteEnd,
                        strokeWidth = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // 3. Second Hand (Thin neon highlight sweep + reverse balance weight)
                    val secondStart = centerOffset - Offset(
                        (secondTailLength * sin(secondAngleRad)).toFloat(),
                        (-secondTailLength * cos(secondAngleRad)).toFloat()
                    )
                    val secondEnd = centerOffset + Offset(
                        (secondLength * sin(secondAngleRad)).toFloat(),
                        (-secondLength * cos(secondAngleRad)).toFloat()
                    )
                    drawLine(
                        color = Color(0xFFF97316), // Coral Orange for distinct scanning
                        start = secondStart,
                        end = secondEnd,
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // 4. Center PIN core caps
                    drawCircle(color = Color.White, radius = 5.dp.toPx(), center = centerOffset)
                    drawCircle(color = Color(0xFFF97316), radius = 2.dp.toPx(), center = centerOffset)
                }
            }
        }

        // Section: Elegant Pulsing Digital Clock
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x0FFFFFFF))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Digital Time Text
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = formattedDigital,
                            color = primaryColor,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formattedAmPm,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Arabic Calendar Date Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$formattedDay، $formattedDate",
                            color = Color(0xCCFFFFFF),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Section: World Clock Explorer (المنظار العالمي للبلدان)
        item {
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "التوقيت العالمي للبلدان",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "قارن المناطق",
                    color = primaryColor,
                    fontSize = 12.sp,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        itemsIndexed(worldCities) { index, city ->
            val cityCal = Calendar.getInstance(TimeZone.getTimeZone(city.timezoneId))
            val worldTimeFormat = SimpleDateFormat("hh:mm a", arabicLocale).apply {
                timeZone = TimeZone.getTimeZone(city.timezoneId)
            }
            val localTimeStr = worldTimeFormat.format(cityCal.time)

            // Calculate timezone offset difference compared to user home zone
            val systemZone = TimeZone.getDefault()
            val targetZone = TimeZone.getTimeZone(city.timezoneId)
            val diffMs = targetZone.getOffset(System.currentTimeMillis()) - systemZone.getOffset(System.currentTimeMillis())
            val diffHours = diffMs / 3600000

            val relativeTimeLabel = when {
                diffHours == 0 -> "مطابق لتوقيتك"
                diffHours > 0 -> "متقدم بـ +$diffHours ساعات"
                else -> "متأخر بـ $diffHours ساعات"
            }

            val isSelected = selectedCityIdx == index

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clickable { selectedCityIdx = index }
                    .border(
                        1.dp,
                        if (isSelected) primaryColor.copy(alpha = 0.5f) else Color(0x1F27272A),
                        RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) primaryColor.copy(alpha = 0.08f) else Color(0x11FFFFFF)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = city.flagEmoji,
                            fontSize = 24.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = city.arabicName,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = relativeTimeLabel,
                                color = Color(0x99FFFFFF),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Text(
                        text = localTimeStr,
                        color = if (isSelected) primaryColor else Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------
// TAB 1: EXPERT CHRONOGRAPH STOPWATCH WITH LAP HISTORIES
// -----------------------------------------------------------------
@Composable
fun StopwatchTab(primaryColor: Color, secondaryColor: Color) {
    var accumulatedTimeMs by remember { mutableStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    val laps = remember { mutableStateListOf<Long>() }
    val listState = rememberLazyListState()

    // High Precision Stopwatch ticker (updates every 10ms)
    LaunchedEffect(isRunning) {
        if (isRunning) {
            var lastTime = System.currentTimeMillis()
            while (isRunning && isActive) {
                delay(10)
                val current = System.currentTimeMillis()
                accumulatedTimeMs += (current - lastTime)
                lastTime = current
            }
        }
    }

    // Format Chronometer
    val minutes = (accumulatedTimeMs / 60000) % 60
    val seconds = (accumulatedTimeMs / 1000) % 60
    val centiseconds = (accumulatedTimeMs / 10) % 100

    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    val centiFormatted = String.format(Locale.getDefault(), ".%02d", centiseconds)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Stopwatch Visual Circular Face Plate
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(CircleShape)
                .background(Color(0x0EFFFFFF))
                .border(2.dp, Color(0x1AFFFFFF), CircleShape)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            // Visual dynamic rotating wheel indicator inside stopwatch
            val infiniteTransition = rememberInfiniteTransition(label = "stopwatch_ring")
            val rotationAngle by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(4000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "ring_angle"
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2

                if (isRunning) {
                    drawArc(
                        brush = Brush.sweepGradient(listOf(primaryColor, secondaryColor, primaryColor)),
                        startAngle = rotationAngle,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                } else {
                    drawCircle(
                        color = Color(0x33FFFFFF),
                        radius = radius,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }

            // Visual digital stopwatch numerical displays
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = timeFormatted,
                        color = Color.White,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = centiFormatted,
                        color = secondaryColor,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }
                
                Text(
                    text = if (isRunning) "قيد التشغيل" else "متوقف موقتاً",
                    color = if (isRunning) primaryColor else Color(0x99FFFFFF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Large Premium Dashboard Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset button
            IconButton(
                onClick = {
                    accumulatedTimeMs = 0L
                    isRunning = false
                    laps.clear()
                },
                enabled = accumulatedTimeMs > 0L,
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        if (accumulatedTimeMs > 0L) Color(0x22EF4444) else Color(0x11FFFFFF),
                        CircleShape
                    )
                    .border(
                        BorderStroke(1.dp, if (accumulatedTimeMs > 0L) Color(0xFFEF4444) else Color(0x1F27272A)),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Stopwatch",
                    tint = if (accumulatedTimeMs > 0L) Color(0xFFEF4444) else Color(0x66FFFFFF),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Big Start/Pause pill shaped glow button
            val playBg = if (isRunning) Color(0xFFEF4444) else primaryColor
            Button(
                onClick = { isRunning = !isRunning },
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(containerColor = playBg),
                contentPadding = PaddingValues(horizontal = 36.dp, vertical = 14.dp),
                modifier = Modifier
                    .height(56.dp)
                    .testTag("stopwatch_trigger_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Close else Icons.Default.PlayArrow,
                        contentDescription = "Trigger Status",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRunning) "إيقاف مؤقت" else "بدء العداد",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Lap register button
            IconButton(
                onClick = {
                    laps.add(0, accumulatedTimeMs) // Add lap to top of list
                },
                enabled = isRunning,
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        if (isRunning) Color(0x2206B6D4) else Color(0x11FFFFFF),
                        CircleShape
                    )
                    .border(
                        BorderStroke(1.dp, if (isRunning) primaryColor.copy(alpha = 0.4f) else Color(0x1F27272A)),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Record Lap",
                    tint = if (isRunning) primaryColor else Color(0x66FFFFFF),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Professional Lap History display board
        Text(
            text = if (laps.isNotEmpty()) "سجل الجولات (${laps.size})" else "لا يوجد جولات مسجلة بعد",
            color = Color(0xB2FFFFFF),
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.fillMaxWidth().padding(start = 12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0x0BFFFFFF), RoundedCornerShape(20.dp))
                .border(1.dp, Color(0x11FFFFFF), RoundedCornerShape(20.dp))
                .padding(8.dp)
        ) {
            if (laps.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0x33FFFFFF),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "اضغط على النجم لتسجيل دورة جديدة أثناء تشغيل ساعة الإيقاف",
                        color = Color(0x66FFFFFF),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 30.dp)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(laps) { index, lapTotalTime ->
                        val lapNum = laps.size - index
                        
                        // Calculate differential split
                        val diffStr = if (index < laps.size - 1) {
                            val previousLap = laps[index + 1]
                            val diff = lapTotalTime - previousLap
                            val diffSec = (diff / 1000) % 60
                            val diffCen = (diff / 10) % 100
                            String.format(Locale.getDefault(), "+%d.%02d ث", diffSec, diffCen)
                        } else {
                            "بداية"
                        }

                        // Formatter for this lap time
                        val lapMinutes = (lapTotalTime / 60000) % 60
                        val lapSeconds = (lapTotalTime / 1000) % 60
                        val lapCentiseconds = (lapTotalTime / 10) % 100
                        val lapDisplayString = String.format(Locale.getDefault(), "%02d:%02d.%02d", lapMinutes, lapSeconds, lapCentiseconds)

                        // Wrapped safely inside a local Column Scope to avoid LazyColumn compilation issues
                        Column(modifier = Modifier.fillMaxWidth()) {
                            AnimatedVisibility(
                                visible = true,
                                enter = slideInVertically { -40 } + fadeIn()
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "#$lapNum",
                                                color = primaryColor,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                modifier = Modifier.width(36.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = "جولة $lapNum",
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = diffStr,
                                                    color = if (diffStr == "بداية") Color(0x99FFFFFF) else secondaryColor,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }

                                        Text(
                                            text = lapDisplayString,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.testTag("lap_value_$index")
                                        )
                                    }
                                    
                                    if (index < laps.size - 1) {
                                        Divider(color = Color(0x14FFFFFF), thickness = 1.dp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------
// TAB 2: ELITE HIGH COUNTDOWN TIMER SECTION WITH COMPACT SLIDER SELECTORS (M3)
// -----------------------------------------------------------------
@Suppress("PrimitiveInCollection")
@Composable
fun TimerTab(
    primaryColor: Color,
    secondaryColor: Color,
    isRunning: Boolean,
    remainingMs: Long,
    totalMs: Long,
    onRunningChange: (Boolean) -> Unit,
    onRemainingChange: (Long) -> Unit,
    onTotalChange: (Long) -> Unit
) {
    // Local picker states
    var selectHours by remember { mutableStateOf(0) }
    var selectMinutes by remember { mutableStateOf(10) }
    var selectSeconds by remember { mutableStateOf(0) }

    val formattedRemaining = if (remainingMs > 0) {
        val hrs = (remainingMs / 3600000)
        val mins = (remainingMs / 60000) % 60
        val secs = (remainingMs / 1000) % 60
        if (hrs > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
        }
    } else {
        "00:00"
    }

    // Preset options in minutes
    val presets = listOf(1, 3, 5, 10, 15, 30, 45, 60)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Section A: Active running visual disk, or the pickers if stopped
        item {
            if (remainingMs > 0) {
                // Circular active countdown graphic sweep bar
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val angleProgress = if (totalMs > 0) (remainingMs.toFloat() / totalMs.toFloat()) else 0f

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = size.minDimension / 2 * 0.95f

                        // Outer track ring background
                        drawCircle(
                            color = Color(0x13FFFFFF),
                            radius = radius,
                            style = Stroke(width = 8.dp.toPx())
                        )

                        // Glowing remaining active sweep
                        drawArc(
                            color = primaryColor,
                            startAngle = -90f,
                            sweepAngle = 360f * angleProgress,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = formattedRemaining,
                            color = Color.White,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        val percentage = (angleProgress * 100).toInt()
                        Text(
                            text = "$percentage%",
                            color = secondaryColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                // Interactive manual sliders & pickers layout (Visible when timer is ready to set)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x0EFFFFFF))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "تعديل مدة المؤقت",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Sliders columns
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // 1. Selector Hour
                            SliderValueRow(
                                title = "الساعات",
                                value = selectHours,
                                maxValue = 23,
                                primaryColor = primaryColor,
                                onValueChange = { selectHours = it }
                            )

                            // 2. Selector Minutes
                            SliderValueRow(
                                title = "الدقائق",
                                value = selectMinutes,
                                maxValue = 59,
                                primaryColor = primaryColor,
                                onValueChange = { selectMinutes = it }
                            )

                            // 3. Selector Seconds
                            SliderValueRow(
                                title = "الثواني",
                                value = selectSeconds,
                                maxValue = 59,
                                primaryColor = primaryColor,
                                onValueChange = { selectSeconds = it }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Selected Overview Target Output Display
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .background(Color(0x15FFFFFF), RoundedCornerShape(12.dp))
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "⏳ " + String.format(Locale.getDefault(), "%02d:%02d:%02d", selectHours, selectMinutes, selectSeconds),
                                color = Color.White,
                                fontSize = 20.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Section B: Controller Button Triggers (Start, Pause, Cancel/Revert)
        item {
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cancel/Reset Button (takes us back to picker mode or resets timer)
                IconButton(
                    onClick = {
                        onRunningChange(false)
                        onRemainingChange(0L)
                        onTotalChange(0L)
                    },
                    enabled = remainingMs > 0,
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            if (remainingMs > 0) Color(0x1AEF4444) else Color(0x11FFFFFF),
                            CircleShape
                        )
                        .border(
                            BorderStroke(1.dp, if (remainingMs > 0) Color(0x33EF4444) else Color(0x1F27272A)),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel countdown",
                        tint = if (remainingMs > 0) Color(0xFFEF4444) else Color(0x55FFFFFF)
                    )
                }

                // Primary trigger start / pause
                val totalCalculateVal = (selectHours * 3600L + selectMinutes * 60L + selectSeconds) * 1000L
                val canStart = remainingMs > 0 || totalCalculateVal > 0

                Button(
                    onClick = {
                        if (remainingMs > 0) {
                            // Toggle running status
                            onRunningChange(!isRunning)
                        } else {
                            // Initialize new timer
                            onTotalChange(totalCalculateVal)
                            onRemainingChange(totalCalculateVal)
                            onRunningChange(true)
                        }
                    },
                    enabled = canStart,
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) Color(0xFFE11D48) else primaryColor
                    ),
                    contentPadding = PaddingValues(horizontal = 40.dp, vertical = 14.dp),
                    modifier = Modifier
                        .height(56.dp)
                        .testTag("timer_trigger_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Close else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRunning) "إيقاف مؤقت" else if (remainingMs > 0) "متابعة" else "بدء المؤقت",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Section C: Grid of Quick Presets (Only displayed when timer is not currently counting down)
        if (remainingMs == 0L) {
            item {
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = "مؤقتات سريعة جاهزة",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Chunk presets into rows of 4 buttons
                    presets.chunked(4).forEach { chunk ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            chunk.forEach { mins ->
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            // Start countdown instantly under preset
                                            val tMs = mins * 60L * 1000L
                                            onTotalChange(tMs)
                                            onRemainingChange(tMs)
                                            onRunningChange(true)
                                        }
                                        .border(BorderStroke(1.dp, Color(0x14FFFFFF)), RoundedCornerShape(12.dp)),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0x0FFFFFFF))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "+$mins",
                                            color = primaryColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "دقيقة",
                                            color = Color(0xCCFFFFFF),
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Internal reusable Slider picker template
@Composable
fun SliderValueRow(
    title: String,
    value: Int,
    maxValue: Int,
    primaryColor: Color,
    onValueChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color(0xCCFFFFFF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$value",
                color = primaryColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..maxValue.toFloat(),
            steps = if (maxValue > 1) maxValue - 1 else 1,
            colors = SliderDefaults.colors(
                thumbColor = primaryColor,
                activeTrackColor = primaryColor,
                inactiveTrackColor = Color(0x1EFFFFFF)
            )
        )
    }
}

// -----------------------------------------------------------------
// SATISFYING FULLSCREEN TIMER DONE SUCCESS DIALOG OVERLAY (ALARM)
// -----------------------------------------------------------------
@Composable
fun TimerAlertOverlay(
    primaryColor: Color,
    onDismiss: () -> Unit
) {
    // Beautiful Pulsing glow circular animation for the alert
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_alarm")
    val alertScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE6090D16))
            .clickable(enabled = false) {}, // Intercepts taps
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // Alarm Ring Symbol under dynamic sweep scale glow
            Box(
                modifier = Modifier
                    .size(130.dp * alertScale)
                    .clip(CircleShape)
                    .background(Color(0x2BFF2222))
                    .border(BorderStroke(2.dp, Color(0xFFFF4848)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Ringing Tracker",
                    tint = Color(0xFFFF4848),
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "انتهى الوقت والمؤقت!",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "دق منبه الدقائق التنازلي لإتمام المهمة المحددة بنجاح.",
                color = Color(0xA8FFFFFF),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Action stop button
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                contentPadding = PaddingValues(horizontal = 36.dp, vertical = 14.dp),
                modifier = Modifier
                    .height(52.dp)
                    .width(180.dp)
                    .testTag("dismiss_alarm_btn")
            ) {
                Text(
                    text = "إيقاف التنبيه",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// -----------------------------------------------------------------
// CORE SYSTEM HELPER: VIBRATION CONTROLLER SENSORY ACTUATIONS
// -----------------------------------------------------------------
private fun triggerDeviceVibrator(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    vibrator?.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibratorPattern = longArrayOf(0, 400, 200, 400, 200, 400)
            it.vibrate(VibrationEffect.createWaveform(vibratorPattern, -1))
        } else {
            @Suppress("DEPRECATION")
            it.vibrate(600)
        }
    }
}
