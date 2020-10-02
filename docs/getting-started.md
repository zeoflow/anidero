<!--docs:
title: "Getting Started"
layout: landing
section: docs
path: /docs/getting-started/
-->

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

Visit [MVN Repository](https://mvnrepository.com/artifact/com.zeoflow/anidero)
to find the latest version of the library.

## Contributors

Anidero for Android welcomes contributions from the community. Check
out our [contributing guidelines](contributing.md) before getting started.
