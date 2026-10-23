# TV launcher

Home screen for Android TV and Google TV. Plain Java, AndroidX RecyclerView, `minSdk` 26.
The launcher does not use the network.

This tree is the launcher and the files needed to build it. It is derived from
[CleanYourTV](https://github.com/Gathin23/cleanyourtv) by Gathin and used under the MIT license
in `LICENSE`. The Outfit typeface is under the SIL Open Font License; see
`launcher/FONT-LICENSE-OFL.txt`.

## Build

JDK 17 (or a JDK Gradle 8.14 accepts) and Android SDK platform 35 with build-tools 35.0.0.
Point `ANDROID_HOME` at the SDK. `launcher/local.properties` is created from that if it is missing;
do not commit it.

```bash
cd launcher
./gradlew assembleOnn4k2023Debug test
```

Device flavors, one package, different `res/values`: `onn4k2021`, `onn4k2023`, `onn4kpro`,
`onn4kpro2026`, `onn4kplus`, `onn4kstick2026`, `onnstickfhd`, `shield`, `shieldpro`, `shieldtube`.

```bash
./gradlew assembleShieldDebug
```

Release builds are signed with the debug keystore on the machine that builds them. No keystore
is stored in this tree.
