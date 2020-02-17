plugins {
  application
}

application {
  mainClassName = "com.degrendel.reverserogue.zircon.MainKt"
}

repositories {
    maven {
      url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation("org.hexworks.zircon:zircon.core-jvm:2020.0.1-PREVIEW")
    implementation("org.hexworks.zircon:zircon.jvm.swing:2020.0.1-PREVIEW")
    implementation("info.picocli:picocli:4.1.1")
    implementation(project(":reverse-rogue-common"))
    implementation(project(":reverse-rogue-world"))
    implementation(project(":reverse-rogue-agent"))
}
