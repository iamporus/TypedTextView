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

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.google.common.base.Preconditions;

import java.util.Random;

@SuppressWarnings( "unused" )
public class TypedTextView extends AppCompatTextView
{
    private CharSequence mText;
    private OnCharacterTypedListener mOnCharacterTypedListener;
    private int mIndex;

    private static long DEFAULT_SENTENCE_PAUSE = 1500;
    private static long DEFAULT_CURSOR_BLINK_SPEED = 530;
    private static long DEFAULT_RANDOM_TYPING_SEED = 75;
    private static long DEFAULT_TYPING_SPEED = 175;
    private static boolean SHOW_CURSOR = false;
    private static boolean SPLIT_SENTENCES = false;
    private static boolean RANDOMIZE_TYPING = false;

    private long mSentencePauseMillis = DEFAULT_SENTENCE_PAUSE;
    private long mCursorBlinkSpeedMillis = DEFAULT_CURSOR_BLINK_SPEED;
    private long mRandomTypingSeedMillis = DEFAULT_RANDOM_TYPING_SEED;
    private long mTypingSpeedMillis = DEFAULT_TYPING_SPEED;
    private boolean mbShowCursor = SHOW_CURSOR;
    private boolean mbSplitSentences = SPLIT_SENTENCES;
    private boolean mbRandomizeTyping = RANDOMIZE_TYPING;

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

        String typedText = array.getString( R.styleable.TypedTextView_typed_text );
        if( typedText != null )
        {
            setTypedText( typedText );
        }

        array.recycle();
    }

    private Handler mHandler = new Handler();
    private Runnable mTypeWriter = new Runnable()
    {
        @Override
        public void run()
        {
            //extract characters by index
            CharSequence charSequence = mText.subSequence( 0, mIndex );

            //append cursor
            if( mbShowCursor && mIndex < mText.length() )
            {
                charSequence = charSequence + "|";
            }

            if( mbRandomizeTyping )
            {
                if( mTypingSpeedMillis == 0 )
                {
                    mTypingSpeedMillis = mRandomTypingSeedMillis;
                }
                mTypingSpeedMillis = mRandomTypingSeedMillis + new Random().nextInt( ( int ) ( mTypingSpeedMillis ) );
            }

            //set character by character
            setText( charSequence );

            if( mOnCharacterTypedListener != null && mIndex < mText.length() )
            {
                mOnCharacterTypedListener.onCharacterTyped( mText.charAt( mIndex ), mIndex );
            }

            if( mIndex < mText.length() )
            {
                mHandler.postDelayed( mTypeWriter, mTypingSpeedMillis );

                if( mIndex != 0 && ( mText.charAt( mIndex - 1 ) == '.' || mText.charAt( mIndex - 1 ) == ',' ) )
                {
                    mHandler.removeCallbacks( mTypeWriter );
                    mHandler.postDelayed( mTypeWriter, mSentencePauseMillis );
                }
                mIndex++;
            }
            else
            {
                mHandler.removeCallbacks( mTypeWriter );
                if( mbShowCursor )
                {
                    mHandler.postDelayed( mCursorProxyRunnable, mCursorBlinkSpeedMillis );
                }
            }
        }
    };

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

        mText = mbSplitSentences ? splitSentences( text ) : text;

        mIndex = 0;
        setText( "" );
        mHandler.removeCallbacks( mTypeWriter );
        if( mbShowCursor )
        {
            mHandler.removeCallbacks( mCursorProxyRunnable );
        }
        mHandler.postDelayed( mTypeWriter, mTypingSpeedMillis );
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
     * Set text to be typed with the TypeWriter effect.
     *
     * @param charSequence {@link CharSequence} to be typed character by character.
     */
    public void setTypedText( final CharSequence charSequence )
    {
        String text = charSequence.toString();
        setTypedText( text );
    }

    private String splitSentences( @NonNull String text )
    {
        Preconditions.checkNotNull( text );
        int index = text.indexOf( '.' );
        int lastIndex = text.lastIndexOf( '.' );
        if( index != lastIndex )
        {
            //multiple sentences found.
            //introduce new lines for every full stop except the last one terminating string.
            do
            {
                text = text.replaceFirst( "\\. ", ".\n" );

                index = text.indexOf( '.', index + 1 );
                lastIndex = text.lastIndexOf( '.' );

            } while( index != -1 && index != lastIndex );
        }

        return text;
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
    public void randomizeTypingSpeed( boolean bRandomizeTypeSpeed )
    {
        this.mbRandomizeTyping = bRandomizeTypeSpeed;
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        mHandler.removeCallbacks( mTypeWriter );
        if( mbShowCursor )
        {
            mHandler.removeCallbacks( mCursorProxyRunnable );
        }
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        if( mText != null && mIndex != 0 && mIndex != mText.length() )
        {
            mHandler.postDelayed( mTypeWriter, mTypingSpeedMillis );
        }
    }
}