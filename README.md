# TypedTextView
Custom implementation of Android's TextView simulating a keyboard/type-writer.

## Features
* display a **blinking cursor** after every character typed.
* characters are displayed on screen with **random speed** which simulates human behavior.
* display **sentences on new line** on sensing fullstops in passed text.
* delay on sensing comma(,) and fullstop(.) to **simulate user pauses**. 

## Gradle
* **Step 1.** Add the JitPack repository to your build file
Add following in your root build.gradle at the end of repositories:
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
  implementation 'com.github.iamporus:TypedTextView:0.0.2'
}
```

## Usage
```
<com.prush.typedtextview.TypedTextView
        android:id="@+id/typedTextView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="start"
        android:textSize="24sp"
        app:random_type_speed="true"
        app:show_cursor="true"
        app:split_sentences="true"
        app:typed_text="Once there lived a monkey in a jamun tree by a river. The monkey was alone. He had no friends, no family, but he was happy and content."
        app:typing_speed="175"/>
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
