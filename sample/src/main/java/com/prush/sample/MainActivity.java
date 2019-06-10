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
        typedTextView.showCursor( true );
        typedTextView.setTypedText( "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum." );
    }
}
