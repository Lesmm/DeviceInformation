

    PATH=$PATH:$ANDROID_SDK/build-tools/29.0.2/

    sh gradlew assembleDebug
    sh gradlew assembleRelease

    find -name *.jar
    dx --dex --output=$HOME/Downloads/DeviceInfo.jar deviceinfolibrary/build/intermediates/full_jar/release/createFullJarRelease/full.jar
    dx --dex --output=$HOME/Downloads/DeviceInfo.jar deviceinfolibrary/build/intermediates/runtime_library_classes_jar/release/classes.jar
    dx --dex --output=$HOME/Downloads/DeviceInfo.jar deviceinfolibrary/build/intermediates/aar_main_jar/release/classes.jar

    file $HOME/Downloads/DeviceInfo.jar
