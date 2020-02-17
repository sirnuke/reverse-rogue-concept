buildscript {
  extra.set("jsoarVersion", "4.0.0")
}

dependencies {
  val jsoarVersion = project.extra.get("jsoarVersion")
  implementation(project(":reverse-rogue-common"))
  implementation("com.soartech:jsoar-core:$jsoarVersion")
  implementation("com.soartech:jsoar-debugger:$jsoarVersion")
  implementation("com.soartech:jsoar-tcl:$jsoarVersion")
}
