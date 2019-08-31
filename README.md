# SegmentedVideoPlayer
Instagram stories like video player written in Kotlin

![preview](https://i.ibb.co/BVWRLvT/Segmented-Player-Preview.jpg)

## How to use
1. Import the Library
2. Declare `SegmentedPlayerView` in your layout.xml
3. Find declared view and set `segments` and `videoUrl(HLS)`

## In XML
```xml
    <com.chilisoft.segmentedexoplayer.SegmentedPlayerView
            android:id="@+id/segmentedPlayer"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:autoPlay="true"
            app:progressBackgroundColor="#80FFFFFF"
            app:progressColor="#FFFFFF"
            app:progressCornerRadius="2dp"
            app:progressDividerPadding="10dp"
            app:progressHeight="4dp"
            app:progressPaddingBottom="0dp"
            app:progressPaddingLeft="4dp"
            app:progressPaddingRight="4dp"
            app:progressPaddingTop="12dp"
            app:scale_mode="fit">

```

## In Ativity or Fragment
```kotlin
        segmentedPlayer.videoUrl = "https://bitdash-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8"
        segmentedPlayer.segments = mutableListOf(
            TimeUnit.SECONDS.toSeconds(30).toInt(),
            TimeUnit.SECONDS.toSeconds(30).toInt(),
            TimeUnit.MINUTES.toSeconds(1).toInt(),
            TimeUnit.MINUTES.toSeconds(1).toInt(),
            TimeUnit.SECONDS.toSeconds(30).toInt()
        )
```

