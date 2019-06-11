package com.prush.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.prush.typedtextview.TypedTextView;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        TypedTextView typedTextView = findViewById( R.id.typedTextView );
        //Set typing speed
        typedTextView.setTypingSpeed( 175 );

        //Configure sentences
        typedTextView.splitSentences( true );
        typedTextView.setSentencePause( 1500 );

        //Configure Cursor
        typedTextView.showCursor( true );
        typedTextView.setCursorBlinkSpeed( 530 );

        //Configure randomizing typing speed to simulate human behaviour
        typedTextView.randomizeTypingSpeed( true );
        typedTextView.randomizeTypeSeed( 75 );

        //Set text to be typed
        typedTextView.setTypedText( "Once there lived a monkey in a jamun tree by a river. The monkey was alone. He had no friends, no family, but he was happy and content." );
    }
}
