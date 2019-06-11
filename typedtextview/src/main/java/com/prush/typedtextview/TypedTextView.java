package com.prush.typedtextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.support.annotation.NonNull;
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

    private static long DEFAULT_SENTENCE_DELAY = 1500;
    private static long DEFAULT_CURSOR_BLINK_DELAY = 530;
    private static long DEFAULT_TYPING_RANDOM_SEED = 75;
    private static long DEFAULT_TYPING_SPEED = 175;
    private static boolean SHOW_CURSOR = false;
    private static boolean SPLIT_SENTENCES = false;
    private static boolean RANDOMIZE_TYPE_DELAY = false;

    private long mSentenceDelayMillis = DEFAULT_SENTENCE_DELAY;
    private long mCursorBlinkDelayMillis = DEFAULT_CURSOR_BLINK_DELAY;
    private long mTypingSeed = DEFAULT_TYPING_RANDOM_SEED;
    private long mTypingDelayMillis = DEFAULT_TYPING_SPEED;
    private boolean mbShowCursor = SHOW_CURSOR;
    private boolean mbSplitSentences = SPLIT_SENTENCES;
    private boolean mbRandomizeTypeDelay = RANDOMIZE_TYPE_DELAY;

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
        void onCharacterTyped( final int index );
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

        mSentenceDelayMillis = array.getInteger( R.styleable.TypedTextView_sentence_delay, ( int ) DEFAULT_SENTENCE_DELAY );
        mCursorBlinkDelayMillis = array.getInteger( R.styleable.TypedTextView_cursor_blink_delay, ( int ) DEFAULT_CURSOR_BLINK_DELAY );
        mTypingSeed = array.getInteger( R.styleable.TypedTextView_typing_seed, ( int ) DEFAULT_TYPING_RANDOM_SEED );
        mTypingDelayMillis = array.getInteger( R.styleable.TypedTextView_typing_speed, ( int ) DEFAULT_TYPING_SPEED );
        mbShowCursor = array.getBoolean( R.styleable.TypedTextView_show_cursor, SHOW_CURSOR );
        mbSplitSentences = array.getBoolean( R.styleable.TypedTextView_split_sentences, SPLIT_SENTENCES );
        mbRandomizeTypeDelay = array.getBoolean( R.styleable.TypedTextView_random_type_speed, RANDOMIZE_TYPE_DELAY );

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

            if( mbRandomizeTypeDelay )
            {
                if( mTypingDelayMillis == 0 )
                {
                    mTypingDelayMillis = mTypingSeed;
                }
                mTypingDelayMillis = mTypingSeed + new Random().nextInt( ( int ) ( mTypingDelayMillis ) );
            }

            //set character by character
            setText( charSequence );

            if( mOnCharacterTypedListener != null )
            {
                mOnCharacterTypedListener.onCharacterTyped( mIndex );
            }

            if( mIndex < mText.length() )
            {
                mHandler.postDelayed( mTypeWriter, mTypingDelayMillis );

                if( mIndex != 0 && ( mText.charAt( mIndex - 1 ) == '.' || mText.charAt( mIndex - 1 ) == ',' ) )
                {
                    mHandler.removeCallbacks( mTypeWriter );
                    mHandler.postDelayed( mTypeWriter, mSentenceDelayMillis );
                }
                mIndex++;
            }
            else
            {
                mHandler.removeCallbacks( mTypeWriter );
                if( mbShowCursor )
                {
                    mHandler.postDelayed( mCursorProxyRunnable, mCursorBlinkDelayMillis );
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
            mHandler.postDelayed( mCursorProxyRunnable, mCursorBlinkDelayMillis );
        }
    };

    /**
     * Set text to be typed with the TypeWriter effect.
     *
     * @param text String text to be typed
     */
    public void setTypedText( final @NonNull String text )
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
        mHandler.postDelayed( mTypeWriter, mTypingDelayMillis );
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
    public void setOnCharacterTypedListener( OnCharacterTypedListener onCharacterTypedListener )
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
     * @param sentenceDelayMillis long duration in milliseconds to wait after every sentence
     */
    public void setSentenceDelay( final long sentenceDelayMillis )
    {
        mSentenceDelayMillis = sentenceDelayMillis;
    }

    /**
     * Set duration to wait after every cursor blink
     *
     * @param cursorBlinkDelayMillis long duration in milliseconds between every cursor blink
     */
    public void setCursorBlinkDelay( final long cursorBlinkDelayMillis )
    {
        mCursorBlinkDelayMillis = cursorBlinkDelayMillis;
    }

    /**
     * Set duration to wait after every character typed
     *
     * @param typingDelayMillis long duration in milliseconds to wait after every character typed
     */
    public void setTypingDelay( final long typingDelayMillis )
    {
        mTypingDelayMillis = typingDelayMillis;
    }

    /**
     * Randomize Typing delay
     *
     * @param seed long seed to randomize the default typing speed
     */
    public void randomizeTypeDelay( long seed )
    {
        this.mbRandomizeTypeDelay = true;
        mTypingSeed = seed;
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
            mHandler.postDelayed( mTypeWriter, mTypingDelayMillis );
        }
    }
}