package com.example.myapplication

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import java.nio.ByteOrder
import kotlin.math.sqrt

/** Decodes only our bundled voice to 20 ms RMS windows, off the UI thread.
 * Playback remains MediaPlayer; its actual position selects the mouth window.
 * No microphone, recording permission, network, or wall-clock approximation.
 */
class EggVoiceEnvelope(context: Context) {
    @Volatile private var levels = FloatArray(0)
    @Volatile private var cancelled = false
    private val worker = Thread({
        val extractor = MediaExtractor()
        var codec: MediaCodec? = null
        try {
            context.resources.openRawResourceFd(R.raw.easter_egg_audio).use {
                extractor.setDataSource(it.fileDescriptor,it.startOffset,it.length)
            }
            val index=(0 until extractor.trackCount).first { extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/")==true }
            extractor.selectTrack(index)
            val format=extractor.getTrackFormat(index)
            var rate=format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            var channels=format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            val duration=format.getLong(MediaFormat.KEY_DURATION)
            val squares=DoubleArray((duration/20_000+2).toInt())
            val counts=IntArray(squares.size)
            val decoder=MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME)!!)
            codec=decoder
            decoder.configure(format,null,null,0); decoder.start()
            val info=MediaCodec.BufferInfo()
            var inputDone=false
            var outputDone=false
            var floatPcm=false
            while(!cancelled && !outputDone) {
                if(!inputDone) {
                    val input=decoder.dequeueInputBuffer(10_000)
                    if(input>=0) {
                        val buffer=decoder.getInputBuffer(input)!!
                        val count=extractor.readSampleData(buffer,0)
                        if(count<0) {
                            decoder.queueInputBuffer(input,0,0,0,MediaCodec.BUFFER_FLAG_END_OF_STREAM); inputDone=true
                        } else {
                            decoder.queueInputBuffer(input,0,count,extractor.sampleTime,0); extractor.advance()
                        }
                    }
                }
                val output=decoder.dequeueOutputBuffer(info,10_000)
                if(output==MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    val decoded=decoder.outputFormat
                    rate=decoded.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                    channels=decoded.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                    floatPcm=decoded.containsKey(MediaFormat.KEY_PCM_ENCODING) && decoded.getInteger(MediaFormat.KEY_PCM_ENCODING)==android.media.AudioFormat.ENCODING_PCM_FLOAT
                }
                if(output>=0) {
                    val buffer=decoder.getOutputBuffer(output)!!.order(ByteOrder.LITTLE_ENDIAN)
                    buffer.position(info.offset); buffer.limit(info.offset+info.size)
                    var sample=0
                    val bytes=if(floatPcm) 4 else 2
                    while(buffer.remaining()>=bytes) {
                        val value=if(floatPcm) buffer.float.toDouble() else buffer.short/32768.0
                        val us=info.presentationTimeUs+(sample/channels)*1_000_000L/rate
                        val bucket=(us/20_000).toInt()
                        if(bucket in squares.indices) { squares[bucket]+=value*value; counts[bucket]++ }
                        sample++
                    }
                    outputDone=info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM!=0
                    decoder.releaseOutputBuffer(output,false)
                }
            }
            if(!cancelled) levels=FloatArray(squares.size) { i -> if(counts[i]==0) 0f else sqrt(squares[i]/counts[i]).toFloat() }
        } catch (_: Exception) {
            // Silent fallback closes the mouth rather than moving it independently of audio.
        } finally {
            codec?.release(); extractor.release()
        }
    },"egg-voice-envelope").apply { isDaemon=true; start() }

    fun levelAt(positionMs:Int):Float {
        val data=levels
        val i=(positionMs/20).coerceAtLeast(0)
        return data.getOrElse(i) { 0f }
    }
    fun close() { cancelled=true; worker.interrupt() }
}
