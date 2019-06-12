# TypedTextView
[![License](https://img.shields.io/badge/License%20-Apache%202-337ab7.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![MinSDK](https://img.shields.io/badge/API-15%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=15)
[![](https://jitpack.io/v/iamporus/TypedTextView.svg)](https://jitpack.io/#iamporus/TypedTextView)
[![Build Status](https://travis-ci.com/iamporus/TypedTextView.svg?branch=master)](https://travis-ci.com/iamporus/TypedTextView)

Custom implementation of Android's TextView simulating a keyboard/type-writer.

## Features
* display a **blinking cursor** after every character typed.
* characters are displayed on screen with **random speed** which simulates human behavior.
* display **sentences on new line** on sensing fullstops in passed text.
* delay on sensing comma(,) and fullstop(.) to **simulate user pauses**. 

## Preview
![](demo.gif)

## Gradle
* **Step 1.** Add the JitPack repository to your build file.

Add following in your **Project level** build.gradle at the end of repositories:
```
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```
* **Step 2.** Add the dependency
```
dependencies {
  ...
  implementation 'com.github.iamporus:TypedTextView:0.0.3'
}
```

## Usage

### XML
```
<com.prush.typedtextview.TypedTextView
        android:id="@+id/typedTextView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="start"
        android:textSize="24sp"
        app:randomize_typing_speed="true"
        app:randomize_type_seed="75"
        app:show_cursor="true"
        app:cursor_blink_speed="530"
        app:sentence_pause="1500"
        app:split_sentences="true"
        app:typed_text="Once there lived a monkey in a jamun tree by a river. The monkey was alone. He had no friends, no family, but he was happy and content."
        app:typing_speed="175"/>
```

### Java

```
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

//Set listener to invoke other actions based on status.
typedTextView.setOnCharacterTypedListener( new TypedTextView.OnCharacterTypedListener()
{
  @Override
  public void onCharacterTyped( char character, int index )
  {
    Log.d( TAG, "onCharacterTyped: " + character + " at index " + index );
  }
});

```

## License
```
Copyright 2019 Purushottam Pawar

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
