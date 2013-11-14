/**
 * Mupen64PlusAE, an N64 emulator for the Android platform
 * 
 * Copyright (C) 2013 Paul Lamb
 * 
 * This file is part of Mupen64PlusAE.
 * 
 * Mupen64PlusAE is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * Mupen64PlusAE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Mupen64PlusAE. If
 * not, see <http://www.gnu.org/licenses/>.
 * 
 * Authors: Paul Lamb
 */
package paulscode.android.mupen64plusae;

import paulscode.android.mupen64plusae.util.SafeMethods;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

/**
 * The portion of the core interface that directly bridges Java and C code. Any function names
 * changed here should also be changed in the corresponding C code, and vice versa.
 * 
 * @see CoreInterface
 */
public class CoreInterfaceNative extends CoreInterface
{
    static
    {
        loadNativeLibName( "ae-imports" );
        //loadNativeLibName( "SDL" );
        loadNativeLibName( "SDL2" );
        loadNativeLibName( "core" );
        loadNativeLibName( "front-end" );
        loadNativeLibName( "ae-exports" );
    }
    
    /**
     * Loads the specified native library name (without "lib" and ".so").
     * 
     * @param libname absolute path to a native .so file (may optionally be in quotes)
     */
    public static void loadNativeLibName( String libname )
    {
        try
        {
            System.loadLibrary( libname );
        }
        catch( UnsatisfiedLinkError e )
        {
            Log.e( "FileUtil", "Unable to load native library '" + libname + "'" );
        }
    }
    
    /**
     * Loads the native .so file specified.
     * 
     * @param filepath absolute path to a native .so file (may optionally be in quotes)
     */
    public static void loadNativeLib( String filepath )
    {
        String filename = null;
        
        if( filepath != null && filepath.length() > 0 )
        {
            filename = filepath.replace( "\"", "" );
            if( filename.equalsIgnoreCase( "dummy" ) )
                return;
            
            try
            {
                System.load( filename );
            }
            catch( UnsatisfiedLinkError e )
            {
                Log.e( "FileUtil", "Unable to load native library '" + filename + "'", e );
            }
        }
    }
    
    // TODO: These should all have javadoc comments.
    // It would better document calls going in/out of native code.
    
    // ************************************************************************
    // ************************************************************************
    // Java <-> C communication (input-android)
    // jni/input-android/plugin.c
    // ************************************************************************
    // ************************************************************************
    
    public static native void jniInitInput();
    
    public static native void setControllerState( int controllerNum, boolean[] buttons, int axisX, int axisY );
    
    public static native void setControllerConfig( int controllerNum, boolean plugged, int pakType );
    
    public static void rumble( int controllerNum, boolean active )
    {
        if( sVibrators[controllerNum] == null )
            return;
        
        if( active )
            sVibrators[controllerNum].vibrate( VIBRATE_TIMEOUT );
        else
            sVibrators[controllerNum].cancel();
    }
    
    // ************************************************************************
    // ************************************************************************
    // Java <-> C communication (ae-bridge)
    // ************************************************************************
    // ************************************************************************
    
    // ------------------------------------------------------------------------
    // Call-outs made TO native code
    // jni/ae-bridge/ae_bridge_main.cpp
    // ------------------------------------------------------------------------
    
    public static native void sdlInit( Object[] args );
    
    // ------------------------------------------------------------------------
    // Call-outs made TO native code
    // jni/ae-bridge/ae_exports.cpp
    // ------------------------------------------------------------------------
    
    public static native void sdlOnResize( int x, int y, int format );
    
    public static native void sdlQuit();
    
    public static native void emuGameShark( boolean pressed );
    
    public static native void emuPause();
    
    public static native void emuResume();
    
    public static native void emuStop();
    
    public static native void emuAdvanceFrame();
    
    public static native void emuSetSpeed( int percent );
    
    public static native void emuSetSlot( int slotID );
    
    public static native void emuLoadSlot();
    
    public static native void emuSaveSlot();
    
    public static native void emuLoadFile( String filename );
    
    public static native void emuSaveFile( String filename );
    
    public static native int emuGetState();
    
    // ------------------------------------------------------------------------
    // Call-ins made FROM native code
    // jni/ae-bridge/ae_imports.cpp
    // ------------------------------------------------------------------------
    
    /**
     * Callback for when an emulator's state/parameter has changed.
     * 
     * @param paramChanged The changed paramter's ID.
     * @param newValue     The new value of the changed parameter.
     */
    public static void stateCallback( int paramChanged, int newValue )
    {
        synchronized( sStateCallbackLock )
        {
            if( sStateCallbackListener != null )
                sStateCallbackListener.onStateCallback( paramChanged, newValue );
        }
    }
    
    /**
     * Returns the hardware type of a device.
     * <p>
     * Note: This checks if the device is a device
     *       that has a custom profile for flicker reduction.
     *       If a device has a custom profile, this is returned
     *       instead.
     *       
     * @return The hardware type of the device, or the custom profile
     *         of the device (if it is a device that has one).
     */
    public static int getHardwareType()
    {
        int autoDetected = sAppData.hardwareInfo.hardwareType;
        int overridden = sUserPrefs.videoHardwareType;
        return (overridden < 0) ? autoDetected : overridden;
    }
    
    /**
     * Returns the custom polygon offset.
     */
    public static float getCustomPolygonOffset()
    {
        return sUserPrefs.customPolygonOffset;
    }

    /**
     * Checks if the emulator is using RGBA 8888.
     * 
     * @return True if RGBA 8888 is being used. False otherwise.
     */
    public static boolean useRGBA8888()
    {
        return sUserPrefs.isRgba8888;
    }
    
    // ************************************************************************
    // ************************************************************************
    // Java <-> C communication (SDL)
    // ************************************************************************
    // ************************************************************************
    
    // ------------------------------------------------------------------------
    // Call-ins made FROM native code
    // jni/SDL/src/core/android/SDL_android.cpp
    // ------------------------------------------------------------------------
    
    /**
     * Creates a GL context for SDL 2.0
     * <p>
     * Note: If the GL context creation fails the first time, this method
     *       will fall back to using legacy GL context creation, ignoring
     *       the specified configSpec, and attempt to make a valid context.
     * 
     * @param majorVersion The major GL version number.
     * @param minorVersion The minor GL version number.
     * @param configSpec   The configuration to use.
     * 
     * @return True if the context was able to be created. False if not.
     */
    public static boolean createGLContext( int majorVersion, int minorVersion, int[] configSpec )
    {
        return sSurface.createGLContext( majorVersion, minorVersion, configSpec );
    }
    
    public static void deleteGLContext()
    {
        sSurface.deleteGLContext();
    }
    
    /**
     * Swaps the GL buffers of the GameSurface in use.
     */
    public static void flipBuffers()
    {
        sSurface.flipBuffers();
        
        // Update frame rate info
        if( sFpsRecalcPeriod > 0 && sFpsListener != null )
        {
            sFrameCount++;
            if( sFrameCount >= sFpsRecalcPeriod )
            {
                long currentTime = System.currentTimeMillis();
                float fFPS = ( (float) sFrameCount / (float) ( currentTime - sLastFpsTime ) ) * 1000.0f;
                sFpsListener.onFpsChanged( Math.round( fFPS ) );
                sFrameCount = 0;
                sLastFpsTime = currentTime;
            }
        }
    }
    
    /**
     * Initializes the audio subsystem.
     * 
     * @param sampleRate    The sample rate for playback in hertz.
     * @param is16Bit       Whether or not the audio is 16 bits per sample.
     * @param isStereo      Whether or not the audio is stereo or mono.
     * @param desiredFrames The desired frames per sample.
     */
    public static int audioInit( int sampleRate, boolean is16Bit, boolean isStereo, int desiredFrames )
    {
        if( sAudioTrack == null )
        {
            // Audio configuration
            int channelConfig = isStereo ? AudioFormat.CHANNEL_OUT_STEREO : AudioFormat.CHANNEL_OUT_MONO;
            int audioFormat = is16Bit ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT;
            int frameSize = ( isStereo ? 2 : 1 ) * ( is16Bit ? 2 : 1 );
            
            // Let the user pick a larger buffer if they really want -- but ye gods they probably
            // shouldn't, the minimums are horrifyingly high latency already
            int minBufSize = AudioTrack.getMinBufferSize( sampleRate, channelConfig, audioFormat );
            int defaultFrames = ( minBufSize + frameSize - 1 ) / frameSize;
            desiredFrames = Math.max( desiredFrames, defaultFrames );
        
            sAudioTrack = new AudioTrack( AudioManager.STREAM_MUSIC, sampleRate, channelConfig, audioFormat, desiredFrames
                    * frameSize, AudioTrack.MODE_STREAM );
        
            // Instantiating AudioTrack can "succeed" without an exception and the track may still be invalid
            // Ref: https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/media/java/android/media/AudioTrack.java
            // Ref: http://developer.android.com/reference/android/media/AudioTrack.html#getState()
            
            if( sAudioTrack.getState() != AudioTrack.STATE_INITIALIZED )
            {
                Log.e( "CoreInterfaceNative", "Failed during initialization of audio track" );
                sAudioTrack = null;
                return -1;
            }
            
            sAudioTrack.play();
        }
        return 0;
    }
    
    /**
     * Writes audio data into a given short buffer.
     * 
     * @param buffer The short array which the audio data will be written to.
     */
    public static void audioWriteShortBuffer( short[] buffer )
    {
        for( int i = 0; i < buffer.length; )
        {
            int result = sAudioTrack.write( buffer, i, buffer.length - i );
            if( result > 0 )
            {
                i += result;
            }
            else if( result == 0 )
            {
                SafeMethods.sleep( 1 );
            }
            else
            {
                Log.w( "CoreInterfaceNative", "SDL Audio: Error returned from write(short[])" );
                return;
            }
        }
    }
    
    /**
     * Writes audio data into a given byte buffer.
     * 
     * @param buffer The byte array which the audio data will be written to.
     */
    public static void audioWriteByteBuffer( byte[] buffer )
    {
        for( int i = 0; i < buffer.length; )
        {
            int result = sAudioTrack.write( buffer, i, buffer.length - i );
            if( result > 0 )
            {
                i += result;
            }
            else if( result == 0 )
            {
                SafeMethods.sleep( 1 );
            }
            else
            {
                Log.w( "CoreInterfaceNative", "SDL Audio: Error returned from write(byte[])" );
                return;
            }
        }
    }

    /**
     * Shuts down the audio thread.
     */
    public static void audioQuit()
    {
        if( sAudioTrack != null )
        {
            sAudioTrack.stop();
            sAudioTrack.release();
            sAudioTrack = null;
        }
    }
    
    public static boolean setActivityTitle( String title )
    {
        // No-op implementation of SDL interface
        // See SDL2/src/core/android/SDL_android.cpp
        return true;
    }
    
    public static boolean sendMessage( int command, int param )
    {
        // No-op implementation of SDL interface
        // See SDL2/src/core/android/SDL_android.cpp
        return true;
    }
}
