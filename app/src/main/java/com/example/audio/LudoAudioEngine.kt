package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import android.util.Log
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ConcurrentHashMap

enum class WaveType {
    SINE, SQUARE, TRIANGLE
}

object LudoAudioEngine {
    private const val TAG = "LudoAudioEngine"
    private const val SAMPLE_RATE = 44100

    private val audioScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var soundPool: SoundPool? = null
    private val soundMap = ConcurrentHashMap<String, Int>()
    private val pcmCache = ConcurrentHashMap<String, ShortArray>()

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

    var isSoundEnabled: Boolean = true

    @Synchronized
    fun init(context: Context) {
        if (soundPool != null) return
        try {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(attributes)
                .build()

            audioScope.launch(Dispatchers.IO) {
                prewarmInternal(context.applicationContext)
            }
        } catch (e: Exception) {
            Log.e(TAG, "SoundPool init failed", e)
        }
    }

    fun prewarm(context: Context? = null) {
        if (context != null) {
            init(context)
        } else {
            audioScope.launch(Dispatchers.Default) {
                generateAllSfxPcm()
            }
        }
    }

    private fun generateAllSfxPcm() {
        if (pcmCache.containsKey("token_move")) return
        pcmCache["token_move"] = generateSequenceSamples(listOf(783.99, 1046.50, 880.00), listOf(25, 25, 35), 0.14f, WaveType.SINE, 0)
        pcmCache["token_hop"] = generateSequenceSamples(listOf(880.00, 1174.66), listOf(20, 25), 0.14f, WaveType.SINE, 0)
        pcmCache["turn_pass"] = generateSequenceSamples(listOf(783.99, 880.00), listOf(30, 40), 0.05f, WaveType.SINE, 0)
        pcmCache["alert"] = generateSequenceSamples(listOf(783.99, 880.00), listOf(80, 100), 0.10f, WaveType.SINE, 30)
        pcmCache["dice_roll"] = generateSequenceSamples(listOf(120.0, 200.0, 160.0, 240.0, 180.0, 280.0), listOf(18, 18, 18, 18, 18, 22), 0.09f, WaveType.TRIANGLE, 0)
        pcmCache["token_captured"] = generateSequenceSamples(listOf(587.33, 493.88, 392.00, 440.00, 587.33), listOf(45, 45, 45, 45, 120), 0.12f, WaveType.SINE, 0)
        pcmCache["token_reached_home"] = generateSequenceSamples(listOf(523.25, 659.25, 783.99, 1046.50, 1318.51), listOf(50, 50, 50, 50, 250), 0.13f, WaveType.SINE, 0)
        pcmCache["victory"] = generateSequenceSamples(listOf(523.25, 659.25, 783.99, 1046.50, 783.99, 1046.50, 1318.51, 1567.98), listOf(60, 60, 60, 60, 60, 60, 60, 400), 0.12f, WaveType.SINE, 0)
    }

    private fun prewarmInternal(context: Context) {
        generateAllSfxPcm()
        val pool = soundPool ?: return

        pcmCache.forEach { (key, pcmData) ->
            try {
                val file = File(context.cacheDir, "sfx_$key.wav")
                writePcmToWavFile(file, pcmData, SAMPLE_RATE)
                val soundId = pool.load(file.absolutePath, 1)
                soundMap[key] = soundId
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load SFX $key into SoundPool", e)
            }
        }
    }

    private fun writePcmToWavFile(file: File, pcmData: ShortArray, sampleRate: Int) {
        val totalAudioLen = pcmData.size * 2
        val totalDataLen = totalAudioLen + 36
        val byteRate = sampleRate * 1 * 2

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1
        header[21] = 0
        header[22] = 1
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = 2
        header[33] = 0
        header[34] = 16
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

        FileOutputStream(file).use { fos ->
            fos.write(header)
            val byteBuffer = ByteBuffer.allocate(pcmData.size * 2).order(ByteOrder.LITTLE_ENDIAN)
            for (sample in pcmData) {
                byteBuffer.putShort(sample)
            }
            fos.write(byteBuffer.array())
        }
    }

    private fun playSfx(key: String, volume: Float = 1.0f) {
        if (!isSoundEnabled) return
        val pool = soundPool
        val soundId = soundMap[key]
        if (pool != null && soundId != null && soundId != 0) {
            pool.play(soundId, volume, volume, 1, 0, 1.0f)
        } else {
            // Fallback: play directly using AudioTrack without clicks
            playPcmDirect(key)
        }
    }

    private fun playPcmDirect(key: String) {
        if (!isSoundEnabled) return
        val pcm = pcmCache[key] ?: return
        audioScope.launch(Dispatchers.Default) {
            try {
                val minBuf = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val track = AudioTrack.Builder()
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
                    .setBufferSizeInBytes((minBuf * 2).coerceAtLeast(pcm.size * 2))
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(pcm, 0, pcm.size)
                track.play()
                delay((pcm.size * 1000L / SAMPLE_RATE) + 100L)
                track.stop()
                track.release()
            } catch (e: Exception) {
                Log.e(TAG, "AudioTrack direct play error", e)
            }
        }
    }

    fun releaseSfxTrack() {
        soundPool?.autoPause()
    }

    fun startBgm() {
        if (!isMusicEnabled) return
        if (bgmJob != null && bgmJob?.isActive == true) return

        bgmJob = audioScope.launch(Dispatchers.Default) {
            try {
                val minBuffer = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = (minBuffer * 4).coerceAtLeast(8192)

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

                val melody = listOf(
                    Note(523.25, 900),  // C5
                    Note(587.33, 900),  // D5
                    Note(659.25, 1100), // E5
                    Note(783.99, 1400), // G5
                    Note(659.25, 900),  // E5
                    Note(587.33, 900),  // D5
                    Note(523.25, 1600), // C5
                    Note(440.00, 900),  // A4
                    Note(523.25, 900),  // C5
                    Note(587.33, 900),  // D5
                    Note(783.99, 1100), // G5
                    Note(880.00, 1600), // A5
                    Note(783.99, 1100), // G5
                    Note(659.25, 900),  // E5
                    Note(587.33, 1600)  // D5
                )

                var noteIndex = 0
                while (isActive && isMusicEnabled) {
                    val note = melody[noteIndex]
                    
                    // Generate note samples with smooth 0-crossing envelope
                    val noteSamples = generateToneSamples(note.frequency, note.durationMs, volume = 0.025f, WaveType.SINE)
                    if (noteSamples.isNotEmpty() && isActive) {
                        track.write(noteSamples, 0, noteSamples.size)
                    }

                    // Write smooth silence frames instead of delay to PREVENT buffer underrun popping!
                    val silenceSamplesCount = (SAMPLE_RATE * 0.25).toInt()
                    val silenceSamples = ShortArray(silenceSamplesCount)
                    if (isActive) {
                        track.write(silenceSamples, 0, silenceSamples.size)
                    }

                    noteIndex = (noteIndex + 1) % melody.size
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

    private fun generateToneSamples(
        frequency: Double,
        durationMs: Int,
        volume: Float,
        type: WaveType
    ): ShortArray {
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        if (numSamples <= 0) return ShortArray(0)
        val samples = ShortArray(numSamples)

        val attackSamples = (numSamples * 0.15).toInt().coerceAtLeast(32)
        val decaySamples = (numSamples - attackSamples).coerceAtLeast(32)

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

            // Smooth Hann envelope
            val env = when {
                i < attackSamples -> 0.5f * (1.0f - Math.cos(Math.PI * i / attackSamples).toFloat())
                else -> 0.5f * (1.0f + Math.cos(Math.PI * (i - attackSamples) / decaySamples).toFloat())
            }

            samples[i] = (waveVal * Short.MAX_VALUE * volume * env).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

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

            val attackSamples = (numSamples * 0.10).toInt().coerceAtLeast(16)
            val decaySamples = (numSamples - attackSamples).coerceAtLeast(16)

            for (j in 0 until numSamples) {
                if (currentIndex >= totalSamples) break
                val t = j.toDouble() / SAMPLE_RATE
                val angle = 2.0 * Math.PI * freq * t
                val waveVal = when (type) {
                    WaveType.SINE -> Math.sin(angle)
                    WaveType.SQUARE -> if (Math.sin(angle) >= 0) 0.8 else -0.8
                    WaveType.TRIANGLE -> {
                        val x = angle / (2.0 * Math.PI)
                        2.0 * Math.abs(2.0 * (x - Math.floor(x + 0.5))) - 1.0
                    }
                }

                // Raised cosine envelope for smooth click-free audio start and end
                val env = when {
                    j < attackSamples -> 0.5f * (1.0f - Math.cos(Math.PI * j / attackSamples).toFloat())
                    else -> 0.5f * (1.0f + Math.cos(Math.PI * (j - attackSamples) / decaySamples).toFloat())
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

    fun playTokenMove() {
        playSfx("token_move")
    }

    fun playTokenHop() {
        playSfx("token_hop")
    }

    fun playTurnPass() {
        playSfx("turn_pass", volume = 0.5f)
    }

    fun playAlert() {
        playSfx("alert")
    }

    fun playDiceRoll() {
        playSfx("dice_roll")
    }

    fun playTokenCaptured() {
        playSfx("token_captured")
    }

    fun playTokenReachedHome() {
        playSfx("token_reached_home")
    }

    fun playVictory() {
        playSfx("victory")
    }

    private data class Note(val frequency: Double, val durationMs: Int)
}
