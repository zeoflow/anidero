# Anidero for Android

## Intro
An Android Library that renders After Effects animations
in JSON format

## Getting Started
For information on how to get started with Anidero,
take a look at our [Getting Started](docs/getting-started.md) guide.

## Submitting Bugs or Feature Requests
Bugs or feature requests should be submitted at our [GitHub Issues section](https://github.com/zeoflow/anidero/issues).

## How does it work?
### 1. Depend on our library

Anidero for Android is available through Google's Maven Repository.
To use it:

1.  Open the `build.gradle` file for your application.
2.  Make sure that the `repositories` section includes Google's Maven Repository
    `google()`. For example:

    ```groovy
      allprojects {
        repositories {
          google()
          jcenter()
        }
      }
    ```

3.  Add the library to the `dependencies` section:

    ```groovy
      dependencies {
        // ...
        implementation 'com.zeoflow:anidero:<version>'
        // ...
      }
    ```

### 2. Add a Anidero component to your app
`activity_main.xml`

```xml
    <!--
        ...
    -->
    <com.zeoflow.anidero.AnideroView
        android:id="@+id/zAnideroView"
        android:layout_width="match_parent"
        android:layout_height="200dp"
        android:layout_centerInParent="true"
        app:anidero_loop="true" />
    <!--
        ...
    -->
```

### 3. Activity/Fragment Class
`MainActivity.java`

```java
public class MainActivity extends BindAppActivity<ActivityMainBinding, MainViewBinding>
{
    //..
    private AnideroView zAnideroView;
    //..
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        //..
        zAnideroView = findViewById(R.id.zAnideroView);
        //..
        zAnideroView.setAnimation("animation.json");
        zAnideroView.playAnimation();
        zAnideroView.setSpeed(1f);
        //..
    }
    //..
}
```
###### `animation.json` is read from the \assets folder

## License
    Copyright 2020 ZeoFlow
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
      http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.