package com.prush.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.prush.typedtextview.TypedTextView;

public class MainActivity extends AppCompatActivity
{

    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        TypedTextView typedTextView = findViewById( R.id.typedTextView );
        //Set typing speed

        TypedTextView.Builder builder = new TypedTextView.Builder( typedTextView )
                .setTypingSpeed( 175 )
                .splitSentences( true )
                .setSentencePause( 1500 )
                .setCursorBlinkSpeed( 530 )
                .randomizeTypingSpeed( true )
                .showCursor( false )
                .playKeyStrokesAudio( true )
                .randomizeTypeSeed( 250 );

        typedTextView = builder.build();

        typedTextView.setTypedText( "Once there lived a monkey in a jamun tree by a river. The monkey was alone. He had no friends, no family, but he was happy and content." );

        typedTextView.setOnCharacterTypedListener( new TypedTextView.OnCharacterTypedListener()
        {
            @Override
            public void onCharacterTyped( char character, int index )
            {
                Log.d( TAG, "onCharacterTyped: " + character + " at index " + index );
            }
        } );

        //Attach TypedTextView's lifecycle to Activity's lifecycle.
        getLifecycle().addObserver( typedTextView.getLifecycleObserver() );
    }
}
