# Shrink and optimise, but keep class and method names: the size win comes from dropping unused
# AndroidX code, not from renaming, and readable stack traces matter more in a sideloaded app
# where crash reports arrive as screenshots.
-dontobfuscate

# Manifest components. R8 already keeps these; say so for the accessibility services and dreams.
-keep class com.gathin.tvhome.MainActivity { *; }
-keep class com.gathin.tvhome.HomeSettingsActivity { *; }
-keep class com.gathin.tvhome.HomeKeyService { *; }
-keep class com.gathin.tvhome.InputKeyService { *; }
-keep class com.gathin.tvhome.ButtonGuardService { *; }
-keep class com.gathin.tvhome.ClockDream { *; }
