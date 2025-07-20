package com.example.alphabetapp

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alphabetapp.ui.theme.AlphabetAppTheme
import java.util.*

data class AlphabetItem(
    val letter: String,
    val word: String,
    val emoji: String,
    val imageRes: Int
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlphabetAppTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF667eea),
                                    Color(0xFF764ba2),
                                    Color(0xFFf093fb)
                                )
                            )
                        )
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent
                    ) { innerPadding ->
                        AlphabetLearningApp(
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlphabetLearningApp(modifier: Modifier = Modifier) {
    val alphabetData = remember { getAlphabetData() }
    var currentIndex by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    
    // Text-to-Speech setup
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    
    DisposableEffect(context) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setSpeechRate(0.8f) // Slower speech for kids
            }
        }
        onDispose {
            tts?.shutdown()
        }
    }
    
    // Function to speak letter and word
    fun speakLetterAndWord(item: AlphabetItem) {
        tts?.let { textToSpeech ->
            val textToSpeak = "Letter ${item.letter}. ${item.word}"
            textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
        }
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    
    // Add state for LazyRow controller
    val lazyRowState = rememberLazyListState()
    
    // Use LaunchedEffect to sync LazyRow position with currentIndex
    LaunchedEffect(currentIndex) {
        lazyRowState.animateScrollToItem(
            index = currentIndex.coerceAtMost(alphabetData.lastIndex)
        )
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2),
                        Color(0xFFf093fb)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        // App Logo
        AppLogo(
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Letter selection row with state
        LazyRow(
            state = lazyRowState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            itemsIndexed(alphabetData) { index, item ->
                LetterButton(
                    letter = item.letter,
                    isSelected = index == currentIndex,
                    onClick = { 
                        currentIndex = index
                        speakLetterAndWord(alphabetData[currentIndex])
                    }
                )
            }
        }
        
        // Main letter card with swipe functionality
        AlphabetCard(
            alphabetItem = alphabetData[currentIndex],
            onSwipeLeft = {
                if (currentIndex < alphabetData.size - 1) {
                    currentIndex++
                    speakLetterAndWord(alphabetData[currentIndex])
                }
            },
            onSwipeRight = {
                if (currentIndex > 0) {
                    currentIndex--
                    speakLetterAndWord(alphabetData[currentIndex])
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        
        // Navigation buttons with audio - Modern Design
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Button
            IconButton(
                onClick = { 
                    if (currentIndex > 0) {
                        currentIndex--
                        speakLetterAndWord(alphabetData[currentIndex])
                    }
                },
                enabled = currentIndex > 0,
                modifier = Modifier
                    .background(
                        if (currentIndex > 0) {
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF4ECDC4),
                                    Color(0xFF44A08D)
                                )
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.3f),
                                    Color.White.copy(alpha = 0.1f)
                                )
                            )
                        },
                        CircleShape
                    )
                    .size(64.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous letter",
                    tint = if (currentIndex > 0) Color.White else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(36.dp)
                )
            }
            
            // Audio button for current letter - Center
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = { 
                        speakLetterAndWord(alphabetData[currentIndex])
                    },
                    modifier = Modifier
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFF6B6B),
                                    Color(0xFFFF8E53)
                                )
                            ),
                            CircleShape
                        )
                        .size(72.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play sound",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Text(
                    text = "${currentIndex + 1} / ${alphabetData.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // Next Button
            IconButton(
                onClick = { 
                    if (currentIndex < alphabetData.size - 1) {
                        currentIndex++
                        speakLetterAndWord(alphabetData[currentIndex])
                    }
                },
                enabled = currentIndex < alphabetData.size - 1,
                modifier = Modifier
                    .background(
                        if (currentIndex < alphabetData.size - 1) {
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF45B7D1),
                                    Color(0xFF2980B9)
                                )
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.3f),
                                    Color.White.copy(alpha = 0.1f)
                                )
                            )
                        },
                        CircleShape
                    )
                    .size(64.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next letter",
                    tint = if (currentIndex < alphabetData.size - 1) Color.White else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        
        // Footer text
        Text(
            text = "Reet Kumar Bind",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp)
        )
        }
    }
}

@Composable
fun AppLogo(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFF6B6B),
                            Color(0xFF4ECDC4),
                            Color(0xFF45B7D1),
                            Color(0xFFFECA57)
                        )
                    ),
                    RoundedCornerShape(20.dp)
                )
                .padding(20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo with colorful letters
            Text(
                text = "🌟",
                fontSize = 36.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
            
            Text(
                text = "Alphabet",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            
            Text(
                text = "App",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            
            Text(
                text = "🎨",
                fontSize = 36.sp,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

@Composable
fun LetterButton(
    letter: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "button_scale"
    )
    
    Box(
        modifier = modifier
            .size(52.dp)
            .scale(animatedScale)
            .clip(CircleShape)
            .background(
                if (isSelected) {
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF6B6B),
                            Color(0xFFFF8E53)
                        )
                    )
                } else {
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.9f),
                            Color.White.copy(alpha = 0.7f)
                        )
                    )
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            color = if (isSelected) Color.White else Color(0xFF2D3748),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp
        )
    }
}

@Composable
fun AlphabetCard(
    alphabetItem: AlphabetItem,
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Animation for card entrance
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_scale"
    )
    
    // Color-coded letters for better learning
    val letterColor = getLetterColor(alphabetItem.letter)
    
    Card(
        modifier = modifier
            .scale(scale)
            .semantics {
                contentDescription = "Letter ${alphabetItem.letter} for ${alphabetItem.word}"
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated letter display with color coding
            AnimatedContent(
                targetState = alphabetItem.letter,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                    slideOutHorizontally { width -> -width } + fadeOut()
                },
                label = "letter_animation"
            ) { letter ->
                Text(
                    text = letter,
                    fontSize = 120.sp,
                    fontWeight = FontWeight.Bold,
                    color = letterColor,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Animated emoji display area with pulse effect
            val pulseScale by animateFloatAsState(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "emoji_pulse"
            )
            
            AnimatedContent(
                targetState = alphabetItem.emoji,
                transitionSpec = {
                    scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut()
                },
                label = "emoji_animation"
            ) { emoji ->
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .scale(pulseScale * 0.95f + 0.05f)
                        .background(
                            Color.Gray.copy(alpha = 0.2f),
                            CircleShape
                        )
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragEnd = {
                                    // Gesture completed
                                }
                            ) { change, dragAmount ->
                                val (x, y) = dragAmount
                                when {
                                    x > 50 -> {
                                        // Swipe right - go to previous letter
                                        onSwipeRight()
                                    }
                                    x < -50 -> {
                                        // Swipe left - go to next letter
                                        onSwipeLeft()
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (alphabetItem.imageRes != 0) {
                        Image(
                            painter = painterResource(id = alphabetItem.imageRes),
                            contentDescription = alphabetItem.word,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text(
                            text = emoji,
                            fontSize = 180.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Animated word display
            AnimatedContent(
                targetState = alphabetItem.word,
                transitionSpec = {
                    slideInVertically { height -> height } + fadeIn() togetherWith
                    slideOutVertically { height -> -height } + fadeOut()
                },
                label = "word_animation"
            ) { word ->
                Text(
                    text = word,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Function to get color-coded letters for better learning
@Composable
fun getLetterColor(letter: String): Color {
    return when (letter) {
        "A", "E", "I", "O", "U" -> Color(0xFFFF6B6B) // Red for vowels
        in "B".."F" -> Color(0xFF4ECDC4) // Teal for B-F
        in "G".."L" -> Color(0xFF45B7D1) // Blue for G-L
        in "M".."R" -> Color(0xFF96CEB4) // Green for M-R
        in "S".."Z" -> Color(0xFFFECA57) // Yellow for S-Z
        else -> Color.Cyan
    }
}

@Composable
fun ImagePlaceholder(letter: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🖼️",
            fontSize = 64.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Image for '$letter'",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

fun getAlphabetData(): List<AlphabetItem> {
    return listOf(
        AlphabetItem("A", "Apple", "🍎", 0),
        AlphabetItem("B", "Ball", "⚽", 0),
        AlphabetItem("C", "Cat", "🐱", 0),
        AlphabetItem("D", "Dog", "🐶", 0),
        AlphabetItem("E", "Elephant", "🐘", 0),
        AlphabetItem("F", "Fish", "🐠", 0),
        AlphabetItem("G", "Giraffe", "🦒", 0),
        AlphabetItem("H", "House", "🏠", 0),
        AlphabetItem("I", "Ice Cream", "🍦", 0),
        AlphabetItem("J", "Juice", "🧃", 0),
        AlphabetItem("K", "Kite", "🪁", 0),
        AlphabetItem("L", "Lion", "🦁", 0),
        AlphabetItem("M", "Monkey", "🐵", 0),
        AlphabetItem("N", "Nest", "🪺", 0),
        AlphabetItem("O", "Orange", "🍊", 0),
        AlphabetItem("P", "Penguin", "🐧", 0),
        AlphabetItem("Q", "Queen", "👸", 0),
        AlphabetItem("R", "Rabbit", "🐰", 0),
        AlphabetItem("S", "Sun", "☀️", 0),
        AlphabetItem("T", "Tiger", "🐅", 0),
        AlphabetItem("U", "Umbrella", "☂️", 0),
        AlphabetItem("V", "Violin", "🎻", 0),
        AlphabetItem("W", "Whale", "🐋", 0),
        AlphabetItem("X", "Xylophone", "🎵", 0),
        AlphabetItem("Y", "Yacht", "🛥️", 0),
        AlphabetItem("Z", "Zebra", "🦓", 0)
    )
}

@Preview(showBackground = true)
@Composable
fun AlphabetLearningAppPreview() {
    AlphabetAppTheme {
        AlphabetLearningApp()
    }
}