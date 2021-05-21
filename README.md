

## 用 dx 工具制作包含 dex 的 jar

    PATH=$PATH:$ANDROID_SDK/build-tools/29.0.2/

    sh gradlew assembleDebug
    sh gradlew assembleRelease

    find -name *.jar
    dx --dex --output=$HOME/Downloads/DeviceInfoLib.jar deviceinfolibrary/build/intermediates/full_jar/release/createFullJarRelease/full.jar
    dx --dex --output=$HOME/Downloads/DeviceInfoLib.jar deviceinfolibrary/build/intermediates/runtime_library_classes_jar/release/classes.jar
    dx --dex --output=$HOME/Downloads/DeviceInfoLib.jar deviceinfolibrary/build/intermediates/aar_main_jar/release/classes.jar
    ## 后两个是不全的，build.gradle 里implementation第三方的库不在里面

    ## 下面是整个app全的，编译时注意不要带上 androidx.appcompat:appcompat
    dx --dex --output=$HOME/Downloads/DeviceInfo.jar app/build/intermediates/dex/release/mergeDexRelease/classes.dex

    file $HOME/Downloads/DeviceInfo.jar


## 全并纯粹的 java jar

    cp a.jar tmp/
    cp b.jar tmp/
    cd tmp/
    jar -xvf a.jar
    jar -xvf b.jar
    rm *.jar
    jar -cvfM out.jar .



