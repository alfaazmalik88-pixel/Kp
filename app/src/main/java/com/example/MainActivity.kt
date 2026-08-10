package com.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.model.AdType
import com.example.model.GamePhase
import com.example.model.LudoViewModel
import com.example.audio.LudoAudioEngine
import com.example.ui.LudoBoard
import com.example.ui.LudoMenu
import com.example.ui.isInternetAvailable
import com.example.ui.theme.MyApplicationTheme
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun InternetStatusGate(content: @Composable () -> Unit) {
  val context = LocalContext.current
  var isConnected by remember { mutableStateOf(isInternetAvailable(context)) }

  LaunchedEffect(Unit) {
    while (true) {
      isConnected = isInternetAvailable(context)
      delay(1500)
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    content()

    if (!isConnected) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.94f)),
        contentAlignment = Alignment.Center
      ) {
        Card(
          modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(16.dp),
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
          border = BorderStroke(2.dp, Color(0xFFEF4444))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(72.dp)
                .background(Color(0xFFEF4444).copy(alpha = 0.2f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text("📡", fontSize = 38.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
              text = "⚠️ इंटरनेट कनेक्शन बंद है! (No Internet)",
              style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
              )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = "Ludo Prime खेलने के लिए इंटरनेट ऑन होना अनिवार्य है। कृपया अपना Mobile Data या Wi-Fi कनेक्शन चालू करें।",
              style = TextStyle(
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
              )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
              onClick = {
                isConnected = isInternetAvailable(context)
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF10B981)
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "रीफ्रेश करें (REFRESH) 🔄",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.padding(vertical = 4.dp)
              )
            }
          }
        }
      }
    }
  }
}

class MainActivity : ComponentActivity() {
  private val viewModel: LudoViewModel by viewModels()

  private var mInterstitialAd: InterstitialAd? = null
  private var mRewardedAd: RewardedAd? = null

  private var isInterstitialLoading = false
  private var isRewardedLoading = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Initialize user profile preferences and daily rewards
    viewModel.initPrefs(this)
    
    // Pre-warm audio engine with SoundPool for instant zero-latency sound effects
    LudoAudioEngine.init(applicationContext)
    LudoAudioEngine.prewarm(applicationContext)
    
    // Initialize Mobile Ads SDK
    MobileAds.initialize(this) {
      loadInterstitialAd()
      loadRewardedAd()
    }

    // Collect uiState changes to dynamically show loaded ads
    lifecycleScope.launch {
      viewModel.uiState.collectLatest { state ->
        val adType = state.adType
        if (adType != null) {
          if (adType == AdType.GAME_FINISH || adType == AdType.RESET || adType == AdType.GAME_START) {
            val ad = mInterstitialAd
            if (ad != null) {
              mInterstitialAd = null // Consume immediately
              viewModel.onRealAdStarted()
              runOnUiThread {
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                  override fun onAdDismissedFullScreenContent() {
                    loadInterstitialAd() // Preload next
                    viewModel.onRealAdCompleted(adType)
                  }

                  override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    mInterstitialAd = null
                    Log.e("AdMob", "Interstitial ad failed to show: ${adError.message}")
                    // Fallback to simulated countdown
                  }
                }
                ad.show(this@MainActivity)
              }
            }
          } else if (adType == AdType.GUARANTEED_SIX || adType == AdType.EXTEND_TIME || adType == AdType.WATCH_AD) {
            val ad = mRewardedAd
            if (ad != null) {
              mRewardedAd = null // Consume immediately
              viewModel.onRealAdStarted()
              runOnUiThread {
                var rewardEarned = false
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                  override fun onAdDismissedFullScreenContent() {
                    loadRewardedAd() // Preload next
                    if (rewardEarned) {
                      viewModel.onRealAdCompleted(adType)
                    } else {
                      viewModel.dismissAd()
                    }
                  }

                  override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    mRewardedAd = null
                    Log.e("AdMob", "Rewarded ad failed to show: ${adError.message}")
                    // Fallback to simulated countdown
                  }
                }
                ad.show(this@MainActivity) { rewardItem ->
                  rewardEarned = true
                }
              }
            }
          }
        }
      }
    }

    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val state by viewModel.uiState.collectAsState()

        InternetStatusGate {
          Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            when (state.gamePhase) {
              GamePhase.MODE_SELECT, GamePhase.SETUP -> {
                LudoMenu(
                  viewModel = viewModel,
                  modifier = Modifier.padding(innerPadding)
                )
              }
              GamePhase.PLAYING, GamePhase.FINISHED -> {
                LudoBoard(
                  viewModel = viewModel,
                  onBack = { viewModel.triggerAd(AdType.GAME_FINISH) },
                  modifier = Modifier.padding(innerPadding)
                )
              }
            }
          }
        }
      }
    }
  }

  override fun onStart() {
    super.onStart()
    LudoAudioEngine.startBgm()
  }

  override fun onStop() {
    super.onStop()
    LudoAudioEngine.stopBgm()
  }

  private fun loadInterstitialAd() {
    if (mInterstitialAd != null || isInterstitialLoading) return
    isInterstitialLoading = true
    val adRequest = AdRequest.Builder().build()
    InterstitialAd.load(
      this,
      "ca-app-pub-3940256099942544/1033173712", // Google AdMob Test Interstitial ID
      adRequest,
      object : InterstitialAdLoadCallback() {
        override fun onAdFailedToLoad(adError: LoadAdError) {
          mInterstitialAd = null
          isInterstitialLoading = false
          Log.e("AdMob", "Interstitial ad failed to load: ${adError.message}. Retrying in 15 seconds...")
          lifecycleScope.launch {
            kotlinx.coroutines.delay(15000)
            loadInterstitialAd()
          }
        }

        override fun onAdLoaded(interstitialAd: InterstitialAd) {
          mInterstitialAd = interstitialAd
          isInterstitialLoading = false
          Log.d("AdMob", "Interstitial ad loaded successfully.")
        }
      }
    )
  }

  private fun loadRewardedAd() {
    if (mRewardedAd != null || isRewardedLoading) return
    isRewardedLoading = true
    val adRequest = AdRequest.Builder().build()
    RewardedAd.load(
      this,
      "ca-app-pub-3940256099942544/5224354917", // Google AdMob Test Rewarded ID
      adRequest,
      object : RewardedAdLoadCallback() {
        override fun onAdFailedToLoad(adError: LoadAdError) {
          mRewardedAd = null
          isRewardedLoading = false
          Log.e("AdMob", "Rewarded ad failed to load: ${adError.message}. Retrying in 15 seconds...")
          lifecycleScope.launch {
            kotlinx.coroutines.delay(15000)
            loadRewardedAd()
          }
        }

        override fun onAdLoaded(rewardedAd: RewardedAd) {
          mRewardedAd = rewardedAd
          isRewardedLoading = false
          Log.d("AdMob", "Rewarded ad loaded successfully.")
        }
      }
    )
  }
}

