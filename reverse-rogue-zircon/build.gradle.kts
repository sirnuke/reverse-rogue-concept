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

buildscript {
  extra.set("zirconVersion", "2020.1.0-RELEASE")
}

dependencies {
  val zirconVersion = project.extra.get("zirconVersion")!!
  implementation("org.hexworks.zircon:zircon.core-jvm:$zirconVersion")
  implementation("org.hexworks.zircon:zircon.jvm.swing:$zirconVersion")
  implementation("info.picocli:picocli:3.9.6")
  implementation(project(":reverse-rogue-common"))
  implementation(project(":reverse-rogue-world"))
  implementation(project(":reverse-rogue-agent"))
}
