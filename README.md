# TypedTextView
[![License](https://img.shields.io/badge/License%20-Apache%202-337ab7.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![MinSDK](https://img.shields.io/badge/API-15%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=15)
[![Build Status](https://travis-ci.com/iamporus/TypedTextView.svg?branch=master)](https://travis-ci.com/iamporus/TypedTextView)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/iamporus/TypedTextView.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/iamporus/TypedTextView/context:java)
[![](https://jitpack.io/v/iamporus/TypedTextView.svg)](https://jitpack.io/#iamporus/TypedTextView)
[![Android Arsenal]( https://img.shields.io/badge/Android%20Arsenal-TypedTextView-green.svg?style=flat )]( https://android-arsenal.com/details/1/7711 )

Custom implementation of Android's TextView simulating a keyboard/typewriter.

<img align="right" width="350" src="https://github.com/iamporus/TypedTextView/blob/master/demo.gif">

## Features
* display a **blinking cursor** after every character typed.
* characters are displayed on the screen with **random speed** which simulates human behavior.
* emit **audio keystrokes** with typed characters.
* **Lifecycle-aware** component. Character typing and audio stops/resumes as per Activity/Fragment state.
* support to **maintain state** across Activity/Fragment lifecycle.
* delay on sensing comma(,) and full stops(.) to **simulate user pauses**.
* display **sentences on new line** on sensing full stops in passed text.

## Gradle
* **Step 1.** Add the JitPack repository to your build file.

Add following in your **Project level** build.gradle at the end of repositories:
```gradle
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```
* **Step 2.** Add the dependency
```gradle
dependencies {
  ...
  implementation 'com.github.iamporus:TypedTextView:x.y.z'
}
```
The latest version of TypedTextView is  [![](https://jitpack.io/v/iamporus/TypedTextView.svg)](https://jitpack.io/#iamporus/TypedTextView)

## Usage - XML

### Simple Usage
```xml
<com.prush.typedtextview.TypedTextView
        android:id="@+id/typedTextView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="start"
        android:textSize="24sp"
        app:typed_text="Once there lived a monkey in a jamun tree by a river. The monkey was alone. He had no friends, no family, but he was happy and content."/>

```
### Customizations

```xml
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
        app:play_keystrokes_audio="true"                                   //use default audio 
        app:play_keystrokes_audio_res="@raw/your_audio_keystroke_res_id"   //OR use custom audio
        app:typed_text="Once there lived a monkey in a jamun tree by a river. The monkey was alone. He had no friends, no family, but he was happy and content."
        app:typing_speed="175"/>
```

## Usage - Java

### Simple Usage
```java
TypedTextView typedTextView = findViewById( R.id.typedTextView );

typedTextView.setTypedText( "Once there lived a monkey in a jamun tree by a river. The monkey was alone. He had no friends, no family, but he was happy and content." );

//Attach TypedTextView's lifecycle to Activity's lifecycle.
getLifecycle().addObserver( typedTextView.getLifecycleObserver() );

```
### Customizations
```java
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

//Play default keystrokes audio
typedTextView.playKeyStrokesAudio( true );
        
//OR play custom keystrokes audio
typedTextView.playKeyStrokesAudio( R.raw.your_audio_keystroke_res_id );
        
//Set text to be typed
typedTextView.setTypedText( "Once there lived a monkey in a jamun tree by a river. The monkey was alone. He had no friends, no family, but he was happy and content." );

//Attach TypedTextView's lifecycle to Activity's lifecycle.
getLifecycle().addObserver( typedTextView.getLifecycleObserver() );
        
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
