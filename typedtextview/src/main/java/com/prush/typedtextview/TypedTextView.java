/*
 * Copyright 2019 Purushottam Pawar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.prush.typedtextview;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.content.res.TypedArray;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;
import android.support.annotation.StringRes;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.google.common.base.Preconditions;

import java.util.Random;

@SuppressWarnings( "unused" )
public class TypedTextView extends AppCompatTextView implements LifecycleObserver
{
    private CharSequence mText;
    private OnCharacterTypedListener mOnCharacterTypedListener;
    private int mIndex;

    private static long DEFAULT_SENTENCE_PAUSE = 1500;
    private static long DEFAULT_CURSOR_BLINK_SPEED = 530;
    private static long DEFAULT_RANDOM_TYPING_SEED = 75;
    private static long DEFAULT_TYPING_SPEED = 175;
    private static int DEFAULT_KEYSTROKES_AUDIO_RES = R.raw.keystrokes;

    private static boolean SHOW_CURSOR = true;
    private static boolean SPLIT_SENTENCES = true;
    private static boolean RANDOMIZE_TYPING = true;
    private static boolean PLAY_KEYSTROKES_AUDIO = true;

    private long mSentencePauseMillis = DEFAULT_SENTENCE_PAUSE;
    private long mCursorBlinkSpeedMillis = DEFAULT_CURSOR_BLINK_SPEED;
    private long mRandomTypingSeedMillis = DEFAULT_RANDOM_TYPING_SEED;
    private long mTypingSpeedMillis = DEFAULT_TYPING_SPEED;
    private boolean mbShowCursor = SHOW_CURSOR;
    private boolean mbSplitSentences = SPLIT_SENTENCES;
    private boolean mbRandomizeTyping = RANDOMIZE_TYPING;
    private boolean mbPlayKeyStrokesAudio = PLAY_KEYSTROKES_AUDIO;
    private int mKeyStrokeAudioRes = DEFAULT_KEYSTROKES_AUDIO_RES;

    private MediaPlayer mMediaPlayer;
    private Handler mHandler = new Handler();


    /**
     * Callback to be invoked when typing is started.
     */
    public interface OnCharacterTypedListener
    {
        /**
         * Provides current index from the passed String.
         *
         * @param index The index of last typed character on screen.
         */
        void onCharacterTyped( final char character, final int index );
    }

    public TypedTextView( Context context )
    {
        super( context );
    }

    public TypedTextView( Context context, AttributeSet attrs )
    {
        this( context, attrs, 0 );
    }

    public TypedTextView( Context context, AttributeSet attrs, int defStyle )
    {
        super( context, attrs, defStyle );

        TypedArray array = context.obtainStyledAttributes( attrs, R.styleable.TypedTextView, defStyle, 0 );

        mSentencePauseMillis = array.getInteger( R.styleable.TypedTextView_sentence_pause, ( int ) DEFAULT_SENTENCE_PAUSE );
        mCursorBlinkSpeedMillis = array.getInteger( R.styleable.TypedTextView_cursor_blink_speed, ( int ) DEFAULT_CURSOR_BLINK_SPEED );
        mRandomTypingSeedMillis = array.getInteger( R.styleable.TypedTextView_randomize_type_seed, ( int ) DEFAULT_RANDOM_TYPING_SEED );
        mTypingSpeedMillis = array.getInteger( R.styleable.TypedTextView_typing_speed, ( int ) DEFAULT_TYPING_SPEED );
        mbShowCursor = array.getBoolean( R.styleable.TypedTextView_show_cursor, SHOW_CURSOR );
        mbSplitSentences = array.getBoolean( R.styleable.TypedTextView_split_sentences, SPLIT_SENTENCES );
        mbRandomizeTyping = array.getBoolean( R.styleable.TypedTextView_randomize_typing_speed, RANDOMIZE_TYPING );
        mbPlayKeyStrokesAudio = array.getBoolean( R.styleable.TypedTextView_play_keystrokes_audio, PLAY_KEYSTROKES_AUDIO );
        mKeyStrokeAudioRes = array.getResourceId( R.styleable.TypedTextView_play_keystrokes_audio_res, -1 );

        if( mKeyStrokeAudioRes == -1 )
        {
            mKeyStrokeAudioRes = DEFAULT_KEYSTROKES_AUDIO_RES;
        }
        else
        {
            playKeyStrokesAudioWith( mKeyStrokeAudioRes );
        }

        String typedText = array.getString( R.styleable.TypedTextView_typed_text );
        if( typedText != null )
        {
            setTypedText( typedText );
        }

        array.recycle();
    }

    private Runnable mTypeWriter = new Runnable()
    {
        @Override
        public void run()
        {
            if( mIndex < mText.length() )
            {
                //extract characters by index
                CharSequence charSequence = mText.subSequence( 0, mIndex );

                //append cursor
                if( mbShowCursor )
                {
                    charSequence = charSequence + "|";
                }

                randomizeTyping();

                //play keystrokes
                playKeystrokes();

                //set character by character
                setText( charSequence );

                if( mOnCharacterTypedListener != null )
                {
                    mOnCharacterTypedListener.onCharacterTyped( mText.charAt( mIndex ), mIndex );
                }

                mHandler.postDelayed( mTypeWriter, mTypingSpeedMillis );

                addSentencePause();

                mIndex++;
            }
            else
            {
                //end of text.
                mHandler.removeCallbacks( mTypeWriter );

                //stop playing keystrokes
                stopKeystrokes();

                //typing completed. show blinking cursor.
                if( mbShowCursor )
                {
                    mHandler.postDelayed( mCursorProxyRunnable, mCursorBlinkSpeedMillis );
                }
            }
        }
    };

    private void stopKeystrokes()
    {
        if( mbPlayKeyStrokesAudio )
        {
            mMediaPlayer.stop();
        }
    }

    private void playKeystrokes()
    {
        if( mbPlayKeyStrokesAudio )
        {
            mMediaPlayer.start();
        }
    }

    private void prepareMediaPlayer()
    {
        if( mbPlayKeyStrokesAudio )
        {
            mMediaPlayer = MediaPlayer.create( getContext(), mKeyStrokeAudioRes );
        }
    }

    private void removeCallbacks()
    {
        mHandler.removeCallbacks( mTypeWriter );
        if( mbShowCursor )
        {
            mHandler.removeCallbacks( mCursorProxyRunnable );
        }
    }

    private void pauseKeyStrokes()
    {
        if( mbPlayKeyStrokesAudio )
        {
            mMediaPlayer.pause();
        }
    }

    private void randomizeTyping()
    {
        if( mbRandomizeTyping )
        {
            if( mTypingSpeedMillis == 0 )
            {
                mTypingSpeedMillis = mRandomTypingSeedMillis;
            }
            mTypingSpeedMillis = mRandomTypingSeedMillis + new Random().nextInt( ( int ) ( mTypingSpeedMillis ) );
        }
    }

    private void addSentencePause()
    {
        //introduce sentence pause
        if( mIndex != 0 && ( mText.charAt( mIndex - 1 ) == '.' || mText.charAt( mIndex - 1 ) == ',' ) )
        {
            //pause keystrokes as well
            pauseKeyStrokes();

            mHandler.removeCallbacks( mTypeWriter );
            mHandler.postDelayed( mTypeWriter, mSentencePauseMillis );
        }
    }

    private Runnable mCursorProxyRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            /*

            If TextView gravity is set to center, appending and removing pipe in each execution,
            re-aligns the text in order to keep it centered.

            To overcome this, an empty space is added which replaces pipe | in order to keep the text in same position.

            if cursor is not shown and empty space is not shown, append cursor.
            else Replace empty space with cursor/pipe.
            else Replace cursor/pipe with empty space.

            */
            CharSequence charSequence = mText;

            if( charSequence.charAt( charSequence.length() - 1 ) != '|' && charSequence.charAt( charSequence.length() - 1 ) != ' ' )
            {
                charSequence = charSequence + "|";
            }
            else if( charSequence.charAt( charSequence.length() - 1 ) == ' ' )
            {
                charSequence = charSequence.subSequence( 0, charSequence.length() - 1 );
                charSequence = charSequence + "|";
            }
            else
            {
                charSequence = charSequence.subSequence( 0, charSequence.length() - 1 );
                charSequence = charSequence + " ";
            }
            mText = charSequence;
            setText( charSequence );
            mHandler.postDelayed( mCursorProxyRunnable, mCursorBlinkSpeedMillis );
        }
    };

    /**
     * Set text to be typed with the TypeWriter effect.
     *
     * @param text {@link String} text to be typed character by character.
     */
    public void setTypedText( @NonNull final String text )
    {
        Preconditions.checkNotNull( text );

        //split sentences on new line
        mText = mbSplitSentences ? splitSentences( text ) : text;

        mIndex = 0;
        setText( "" );

        removeCallbacks();

        prepareMediaPlayer();

        //start typing
        mHandler.postDelayed( mTypeWriter, mTypingSpeedMillis );
    }

    private static void startTyping( @NonNull final String text )
    {
        Preconditions.checkNotNull( text );

    }

    /**
     * Set text to be typed with the TypeWriter effect.
     *
     * @param resId int resource Id of String to be typed.
     */
    public void setTypedText( @StringRes final int resId )
    {
        String text = getContext().getString( resId );
        setTypedText( text );
    }

    /**
     * Get the text to be typed.
     *
     * @return CharSequence text being/to be typed by the view.
     */
    @Override
    public CharSequence getText()
    {
        return mText;
    }

    /**
     * Set text to be typed with the TypeWriter effect.
     *
     * @param charSequence {@link CharSequence} to be typed character by character.
     */
    public void setTypedText( final CharSequence charSequence )
    {
        String text = charSequence.toString();
        setTypedText( text );
    }

    private String splitSentences( @NonNull final String text )
    {
        Preconditions.checkNotNull( text );
        String modifiedText = text;
        int index = modifiedText.indexOf( '.' );
        int lastIndex = modifiedText.lastIndexOf( '.' );
        if( index != lastIndex )
        {
            //multiple sentences found.
            //introduce new lines for every full stop except the last one terminating string.
            do
            {
                modifiedText = modifiedText.replaceFirst( "\\. ", ".\n" );

                index = modifiedText.indexOf( '.', index + 1 );
                lastIndex = modifiedText.lastIndexOf( '.' );

            } while( index != -1 && index != lastIndex );
        }

        return modifiedText;
    }

    /**
     * Register a callback to be invoked when typing is started.
     *
     * @param onCharacterTypedListener {@link OnCharacterTypedListener}
     */
    public void setOnCharacterTypedListener( final OnCharacterTypedListener onCharacterTypedListener )
    {
        mOnCharacterTypedListener = onCharacterTypedListener;
    }

    /**
     * Show cursor while typing
     *
     * @param bShowCursor boolean display blinking cursor while typing.
     */
    public void showCursor( final boolean bShowCursor )
    {
        this.mbShowCursor = bShowCursor;
    }

    /**
     * Split sentences on a new line.
     *
     * @param bSplitSentences boolean Type Writer splits sentences onto new line based on fullstops
     *                        found in the passed string
     */
    public void splitSentences( final boolean bSplitSentences )
    {
        this.mbSplitSentences = bSplitSentences;
    }

    /**
     * Set duration to wait after every sentence
     *
     * @param sentencePauseMillis long duration in milliseconds to wait after every sentence
     */
    public void setSentencePause( final long sentencePauseMillis )
    {
        mSentencePauseMillis = sentencePauseMillis;
    }

    /**
     * Set duration to wait after every cursor blink
     *
     * @param cursorBlinkSpeedMillis long duration in milliseconds between every cursor blink
     */
    public void setCursorBlinkSpeed( final long cursorBlinkSpeedMillis )
    {
        showCursor( true );
        mCursorBlinkSpeedMillis = cursorBlinkSpeedMillis;
    }

    /**
     * Set duration to wait after every character typed
     *
     * @param typingSpeedMillis long duration in milliseconds to wait after every character typed
     */
    public void setTypingSpeed( final long typingSpeedMillis )
    {
        mTypingSpeedMillis = typingSpeedMillis;
    }

    /**
     * Randomize Typing delay
     *
     * @param seed long seed to randomize the default typing speed
     */
    public void randomizeTypeSeed( final long seed )
    {
        randomizeTypingSpeed( true );
        mRandomTypingSeedMillis = seed;
    }

    /**
     * Simulate human typing by randomize typing speed
     *
     * @param bRandomizeTypeSpeed boolean enable random typing speed.
     */
    public void randomizeTypingSpeed( final boolean bRandomizeTypeSpeed )
    {
        mbRandomizeTyping = bRandomizeTypeSpeed;
    }

    /**
     * Play default keystrokes sound along with typing characters
     *
     * @param bPlayKeystrokesAudio boolean
     */
    public void playKeyStrokesAudio( final boolean bPlayKeystrokesAudio )
    {
        mbPlayKeyStrokesAudio = bPlayKeystrokesAudio;
    }

    /**
     * Play specified keystrokes sound along with typing characters
     *
     * @param keyStrokeAudioRes @RawRes int resourceId of audio resource
     */
    public void playKeyStrokesAudioWith( @RawRes final int keyStrokeAudioRes )
    {
        playKeyStrokesAudio( true );
        mKeyStrokeAudioRes = keyStrokeAudioRes;
    }

    public static class Builder
    {
        private TypedTextView mTypedTextView;
        private long mSentencePauseMillis = DEFAULT_SENTENCE_PAUSE;
        private long mCursorBlinkSpeedMillis = DEFAULT_CURSOR_BLINK_SPEED;
        private long mRandomTypingSeedMillis = DEFAULT_RANDOM_TYPING_SEED;
        private long mTypingSpeedMillis = DEFAULT_TYPING_SPEED;
        private boolean mbShowCursor = SHOW_CURSOR;
        private boolean mbSplitSentences = SPLIT_SENTENCES;
        private boolean mbRandomizeTyping = RANDOMIZE_TYPING;
        private boolean mbPlayKeyStrokesAudio = PLAY_KEYSTROKES_AUDIO;
        private int mKeyStrokeAudioRes = DEFAULT_KEYSTROKES_AUDIO_RES;

        public Builder( TypedTextView typedTextView )
        {
            this.mTypedTextView = typedTextView;
        }

        /**
         * Randomize Typing delay
         *
         * @param seed long seed to randomize the default typing speed
         */
        public Builder randomizeTypeSeed( final long seed )
        {
            this.mRandomTypingSeedMillis = seed;
            return this;
        }

        /**
         * Show cursor while typing
         *
         * @param bShowCursor boolean display blinking cursor while typing.
         */
        public Builder showCursor( boolean bShowCursor )
        {
            this.mbShowCursor = bShowCursor;
            return this;
        }

        /**
         * Simulate human typing by randomize typing speed
         *
         * @param bRandomizeTypeSpeed boolean enable random typing speed.
         */
        public Builder randomizeTypingSpeed( final boolean bRandomizeTypeSpeed )
        {
            mTypedTextView.randomizeTypingSpeed( bRandomizeTypeSpeed );
            return this;
        }

        /**
         * Play specified keystrokes sound along with typing characters
         *
         * @param keyStrokeAudioRes @RawRes int resourceId of audio resource
         */
        public Builder playKeyStrokesAudioWith( @RawRes final int keyStrokeAudioRes )
        {
            mTypedTextView.playKeyStrokesAudioWith( keyStrokeAudioRes );
            return this;
        }

        /**
         * Play default keystrokes sound along with typing characters
         *
         * @param bPlayKeystrokesAudio boolean
         */
        public Builder playKeyStrokesAudio( final boolean bPlayKeystrokesAudio )
        {
            mTypedTextView.playKeyStrokesAudio( bPlayKeystrokesAudio );
            return this;
        }

        /**
         * Split sentences on a new line.
         *
         * @param bSplitSentences boolean Type Writer splits sentences onto new line based on fullstops
         *                        found in the passed string
         */
        public Builder splitSentences( final boolean bSplitSentences )
        {
            mTypedTextView.splitSentences( bSplitSentences );
            return this;
        }

        /**
         * Set duration to wait after every sentence
         *
         * @param sentencePauseMillis long duration in milliseconds to wait after every sentence
         */
        public Builder setSentencePause( final long sentencePauseMillis )
        {
            mTypedTextView.setSentencePause( sentencePauseMillis );
            return this;
        }

        /**
         * Set duration to wait after every cursor blink
         *
         * @param cursorBlinkSpeedMillis long duration in milliseconds between every cursor blink
         */
        public Builder setCursorBlinkSpeed( final long cursorBlinkSpeedMillis )
        {
            mTypedTextView.setCursorBlinkSpeed( cursorBlinkSpeedMillis );
            return this;
        }

        /**
         * Set duration to wait after every character typed
         *
         * @param typingSpeedMillis long duration in milliseconds to wait after every character typed
         */
        public Builder setTypingSpeed( final long typingSpeedMillis )
        {
            mTypedTextView.setTypingSpeed( typingSpeedMillis );
            return this;
        }

        public TypedTextView build()
        {
            return mTypedTextView;
        }
    }

    /**
     * Returns a LifecycleObserver that expects to be notified when the LifecycleOwner changes state.
     * Add this as a {@link LifecycleObserver} to {@link android.support.v7.app.AppCompatActivity} or
     * {@link android.support.v4.app.Fragment}
     *
     * @return LifecycleObserver
     */
    public LifecycleObserver getLifecycleObserver()
    {
        return this;
    }

    @OnLifecycleEvent( Lifecycle.Event.ON_START )
    void onViewStarted()
    {
        //resume typing if view was stopped before entire text was displayed.
        if( mText != null && mIndex != 0 && mIndex != mText.length() )
        {
            //resume playing keystrokes
            playKeystrokes();
            mHandler.postDelayed( mTypeWriter, mTypingSpeedMillis );
        }
    }

    @OnLifecycleEvent( Lifecycle.Event.ON_STOP )
    void onViewStopped()
    {
        //stop typing as view is now in stopped state.
        removeCallbacks();

        //pause playing keystrokes
        pauseKeyStrokes();
    }

    @Override
    public Parcelable onSaveInstanceState()
    {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState( superState );
        savedState.setCurrentIndex( mIndex );
        return savedState;
    }

    @Override
    public void onRestoreInstanceState( Parcelable state )
    {
        SavedState savedState = ( SavedState ) state;
        super.onRestoreInstanceState( savedState.getSuperState() );
        mIndex = savedState.getCurrentIndex();
    }

    /**
     * Class to save view's internal state across lifecycle owner's state changes.
     */
    private static class SavedState extends BaseSavedState
    {
        private int mCurrentIndex;

        private SavedState( Parcel source )
        {
            super( source );
            mCurrentIndex = source.readInt();
        }

        private SavedState( Parcelable superState )
        {
            super( superState );
        }

        private void setCurrentIndex( int currentIndex )
        {
            mCurrentIndex = currentIndex;
        }

        private int getCurrentIndex()
        {
            return mCurrentIndex;
        }

        @Override
        public void writeToParcel( Parcel out, int flags )
        {
            super.writeToParcel( out, flags );
            out.writeInt( mCurrentIndex );
        }

        public static final Parcelable.Creator< SavedState > CREATOR = new Creator< SavedState >()
        {
            @Override
            public SavedState createFromParcel( Parcel parcel )
            {
                return new SavedState( parcel );
            }

            @Override
            public SavedState[] newArray( int size )
            {
                return new SavedState[ size ];
            }
        };
    }
}