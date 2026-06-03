# Keep Room generated code
-keep class * extends androidx.room.RoomDatabase
-keepclassmembers class * { @androidx.room.* <methods>; }

# Kotlinx serialization-free: we use manual JSON, nothing extra needed.
