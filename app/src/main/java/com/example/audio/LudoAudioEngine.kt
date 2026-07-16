package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.Executors

enum class WaveType {
    SINE, SQUARE, TRIANGLE
}

object LudoAudioEngine {
    private const val TAG = "LudoAudioEngine"
    private const val SAMPLE_RATE = 22050

    // Shared Coroutine Scope for BGM and SFX
    private val audioScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val sfxExecutor = Executors.newSingleThreadExecutor()
    private val sfxDispatcher = sfxExecutor.asCoroutineDispatcher()

    private var bgmTrack: AudioTrack? = null
    private var bgmJob: Job? = null

    var isMusicEnabled: Boolean = true
        set(value) {
            field = value
            if (value) {
                startBgm()
            } else {
                stopBgm()
            }
        }

    private var sfxTrack: AudioTrack? = null

    var isSoundEnabled: Boolean = true
        set(value) {
            field = value
            if (!value) {
                releaseSfxTrack()
            }
        }

    private fun getSfxTrack(): AudioTrack? {
        if (!isSoundEnabled) {
            releaseSfxTrack()
            return null
        }
        var track = sfxTrack
        if (track == null || track.state != AudioTrack.STATE_INITIALIZED) {
            try {
                track?.release()
            } catch (e: Exception) {}
            try {
                val bufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(4096)

                track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
                
                track.play()
                sfxTrack = track
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create SFX track", e)
                track = null
            }
        } else {
            try {
                if (track.playState != AudioTrack.PLAYSTATE_PLAYING) {
                    track.play()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to play SFX track", e)
            }
        }
        return track
    }

    fun releaseSfxTrack() {
        sfxTrack?.let {
            try {
                it.stop()
                it.release()
            } catch (e: Exception) {}
        }
        sfxTrack = null
    }

    fun prewarm() {
        audioScope.launch(sfxDispatcher) {
            try {
                getSfxTrack()
            } catch (e: Exception) {
                Log.e(TAG, "Prewarm failed", e)
            }
        }
    }

    fun startBgm() {
        if (!isMusicEnabled) return
        if (bgmJob != null && bgmJob?.isActive == true) return

        bgmJob = audioScope.launch {
            try {
                val bufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(4096)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                bgmTrack = track
                track.play()

                // Ultra-soft, slow, calming music-box ambient progression using pure Sine waves
                val melody = listOf(
                    Note(523.25, 1000), // C5
                    Note(587.33, 1000), // D5
                    Note(659.25, 1200), // E5
                    Note(783.99, 1500), // G5
                    Note(659.25, 1000), // E5
                    Note(587.33, 1000), // D5
                    Note(523.25, 1800), // C5
                    Note(440.00, 1000), // A4
                    Note(523.25, 1000), // C5
                    Note(587.33, 1000), // D5
                    Note(783.99, 1200), // G5
                    Note(880.00, 1800), // A5 (sweet bell)
                    Note(783.99, 1200), // G5
                    Note(659.25, 1000), // E5
                    Note(587.33, 1800)  // D5
                )

                var noteIndex = 0
                while (isActive && isMusicEnabled) {
                    val note = melody[noteIndex]
                    writeToneToTrack(track, note.frequency, note.durationMs, volume = 0.025f, type = WaveType.SINE)
                    noteIndex = (noteIndex + 1) % melody.size
                    delay(note.durationMs.toLong() + 300L) // Beautiful spacious pause after note completes
                }
            } catch (e: Exception) {
                Log.e(TAG, "BGM Error", e)
            } finally {
                try {
                    bgmTrack?.stop()
                    bgmTrack?.release()
                } catch (e: Exception) { /* ignore */ }
                bgmTrack = null
            }
        }
    }

    fun stopBgm() {
        bgmJob?.cancel()
        bgmJob = null
        bgmTrack?.let {
            try {
                it.stop()
                it.release()
            } catch (e: Exception) { /* ignore */ }
        }
        bgmTrack = null
    }

    private fun writeToneToTrack(
        track: AudioTrack,
        frequency: Double,
        durationMs: Int,
        volume: Float,
        type: WaveType
    ) {
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)

        val attackSamples = (numSamples * 0.15).toInt()
        val decaySamples = (numSamples * 0.85).toInt()

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val angle = 2.0 * Math.PI * frequency * t
            val waveVal = when (type) {
                WaveType.SINE -> Math.sin(angle)
                WaveType.SQUARE -> if (Math.sin(angle) >= 0) 1.0 else -1.0
                WaveType.TRIANGLE -> {
                    val x = angle / (2.0 * Math.PI)
                    2.0 * Math.abs(2.0 * (x - Math.floor(x + 0.5))) - 1.0
                }
            }

            // Envelope
            val env = when {
                i < attackSamples -> (i.toFloat() / attackSamples)
                else -> (1.0f - (i - attackSamples).toFloat() / decaySamples).coerceIn(0f, 1f)
            }

            samples[i] = (waveVal * Short.MAX_VALUE * volume * env).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        track.write(samples, 0, numSamples)
    }

    private val sfxMutex = kotlinx.coroutines.sync.Mutex()

    private fun generateSequenceSamples(
        frequencies: List<Double>,
        durationsMs: List<Int>,
        volume: Float,
        type: WaveType,
        gapMs: Long
    ): ShortArray {
        var totalSamples = 0
        for (dur in durationsMs) {
            totalSamples += (SAMPLE_RATE * (dur / 1000.0)).toInt()
            if (gapMs > 0) {
                totalSamples += (SAMPLE_RATE * (gapMs / 1000.0)).toInt()
            }
        }

        if (totalSamples <= 0) return ShortArray(0)
        val samples = ShortArray(totalSamples)
        var currentIndex = 0

        for (i in frequencies.indices) {
            if (i >= durationsMs.size) break
            val freq = frequencies[i]
            val dur = durationsMs[i]
            val numSamples = (SAMPLE_RATE * (dur / 1000.0)).toInt()

            val attackSamples = (numSamples * 0.15).toInt()
            val decaySamples = (numSamples * 0.85).toInt()

            for (j in 0 until numSamples) {
                if (currentIndex >= totalSamples) break
                val t = j.toDouble() / SAMPLE_RATE
                val angle = 2.0 * Math.PI * freq * t
                val waveVal = when (type) {
                    WaveType.SINE -> Math.sin(angle)
                    WaveType.SQUARE -> if (Math.sin(angle) >= 0) 1.0 else -1.0
                    WaveType.TRIANGLE -> {
                        val x = angle / (2.0 * Math.PI)
                        2.0 * Math.abs(2.0 * (x - Math.floor(x + 0.5))) - 1.0
                    }
                }

                // Envelope
                val env = when {
                    j < attackSamples -> (j.toFloat() / attackSamples)
                    else -> (1.0f - (j - attackSamples).toFloat() / decaySamples).coerceIn(0f, 1f)
                }

                samples[currentIndex] = (waveVal * Short.MAX_VALUE * volume * env).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                currentIndex++
            }

            if (gapMs > 0) {
                val gapSamplesCount = (SAMPLE_RATE * (gapMs / 1000.0)).toInt()
                for (g in 0 until gapSamplesCount) {
                    if (currentIndex >= totalSamples) break
                    samples[currentIndex] = 0
                    currentIndex++
                }
            }
        }

        return samples
    }

    private val sfxCache = java.util.concurrent.ConcurrentHashMap<String, ShortArray>()

    private fun playSequenceCached(
        key: String,
        frequencies: List<Double>,
        durationsMs: List<Int>,
        volume: Float = 0.25f,
        type: WaveType = WaveType.TRIANGLE,
        gapMs: Long = 0
    ) {
        if (!isSoundEnabled) return
        audioScope.launch(sfxDispatcher) {
            val samples = sfxCache.getOrPut(key) {
                generateSequenceSamples(frequencies, durationsMs, volume, type, gapMs)
            }
            if (samples.isEmpty()) return@launch

            sfxMutex.withLock {
                val track = getSfxTrack() ?: return@withLock
                try {
                    track.flush()
                    track.write(samples, 0, samples.size)
                } catch (e: Exception) {
                    Log.e(TAG, "SFX Error playing sequence: $key", e)
                }
            }
        }
    }

    fun playTokenMove() {
        // High, cozy, soft bubble-pop sound on a pure sine wave (sounds like a high-end physical hop)
        playSequenceCached("token_move", listOf(523.25, 783.99, 659.25), listOf(35, 35, 50), volume = 0.10f, type = WaveType.SINE)
    }

    fun playTokenHop() {
        // Very short, crisp single hop sound (pure sine wave, 45ms)
        playSequenceCached("token_hop", listOf(659.25, 880.00), listOf(20, 25), volume = 0.08f, type = WaveType.SINE)
    }

    fun playTurnPass() {
        // Extremely subtle, polite double-chime to signify next turn
        playSequenceCached("turn_pass", listOf(783.99, 880.00), listOf(30, 40), volume = 0.05f, type = WaveType.SINE)
    }

    fun playAlert() {
        // Soft digital double-chime
        playSequenceCached("alert", listOf(783.99, 880.00), listOf(80, 100), volume = 0.10f, type = WaveType.SINE, gapMs = 30)
    }

    fun playDiceRoll() {
        // Fast, satisfyingly lightweight tactile wooden/marble click-clack roll
        playSequenceCached("dice_roll", listOf(120.0, 200.0, 160.0, 240.0, 180.0, 280.0), listOf(18, 18, 18, 18, 18, 22), volume = 0.09f, type = WaveType.TRIANGLE)
    }

    fun playTokenCaptured() {
        // Playful, cute bouncy slide-back arpeggio - soft, comforting, and absolutely non-irritating!
        playSequenceCached(
            "token_captured",
            listOf(587.33, 493.88, 392.00, 440.00, 587.33),
            listOf(45, 45, 45, 45, 120),
            volume = 0.12f,
            type = WaveType.SINE
        )
    }

    fun playTokenReachedHome() {
        // A magical, sparkling crystal chime cascade when a token successfully reaches home - highly rewarding!
        playSequenceCached(
            "token_reached_home",
            listOf(523.25, 659.25, 783.99, 1046.50, 1318.51),
            listOf(50, 50, 50, 50, 250),
            volume = 0.13f,
            type = WaveType.SINE
        )
    }

    fun playVictory() {
        // Grand, elegant, soft celebratory chime song when a player completely wins the match
        playSequenceCached(
            "victory",
            listOf(523.25, 659.25, 783.99, 1046.50, 783.99, 1046.50, 1318.51, 1567.98),
            listOf(60, 60, 60, 60, 60, 60, 60, 400),
            volume = 0.12f,
            type = WaveType.SINE
        )
    }

    private data class Note(val frequency: Double, val durationMs: Int)
}
