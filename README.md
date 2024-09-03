<div align="center">
<img src="src/main/resources/assets/banner/logo.png">
  <h1>Mohist Banner 1.21.1</h1>

### The Bukkit/Spigot/Paper API implementation for Fabric
### This project has nothing to do with cardboard and uses a completely different development structure
[![](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fci.codemc.io%2Fjob%2FMohistMC%2Fjob%2FBanner-1.21.1&label=Jenkins&logo=jenkins&logoColor=%23ffffff)](https://ci.codemc.io/job/MohistMC/job/Banner-1.21.1)
[![](https://img.shields.io/github/stars/MohistMC/Banner.svg?label=Stars&logo=github)](https://github.com/MohistMC/Banner/stargazers)
[![](https://img.shields.io/badge/JDK-21.0.3-brightgreen.svg?colorB=469C00&logo=java)](https://www.azul.com/downloads/?version=java-21-lts#zulu)
[![](https://img.shields.io/badge/Gradle-8.8-brightgreen.svg?colorB=469C00&logo=gradle)](https://docs.gradle.org/8.8/release-notes.html)
[![](https://img.shields.io/discord/311256119005937665.svg?color=%237289da&label=Discord&logo=discord&logoColor=%237289da)](https://discord.gg/mohistmc)

[![]()](https://bstats.org/plugin/server-implementation/Mohist/6762)
</div>

| Version | Support     | Stability | Mod compatibility | Plugin compatibility |
|---------|-------------|-----------|-------------------|----------------------| 
| 1.21.1  | Active      | Poor      | Poor              | Poor                 |
| 1.20.1  | Active      | Good      | Good              | Good                 |
| 1.19.4  | End Of Life | Poor      | Poor              | Poor                 |

## Notice
- Fabric + Bukkit is more vanilla-like than Forge + Bukkit
- Fabric API uses mixins to change minecraft indirectly
- Banner also use mixins to hook Bukkit api as a fabric mod
- There's a little breaking changes
- This version of Banner 1.20.1,supports MC version 1.21.1

## Tips
- If you want to try a different Fabric + Bukkit hybrid server, you can try CardBoard
- It is implements bukkit api by itself,and the author is a pioneer to try a new way to implements Fabric + Bukkit
- Banner is different with Cardboard,you can also try Cardboard as an alternative choice if you want

## Progress
- [ ] Start patch
    * [x] Bukkit([**69c7ce2**](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/commits/69c7ce2))
    * [ ] CraftBukkit([**3f9263b**](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/commits/3f9263b))
    - [ ] Spigot ([**723951c**](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/spigot/commits/723951c))
        - [ ] Bukkit-Patches
        - [ ] CraftBukkit-Patches (93%)
### Running?
It could be compilable and running, but the compatibility with plugins is poor.

### NMS Support
We do support using Spigot's net.minecraft.server classes. Classes and Fields will automatically remap to their intermediary counterparts in runtime, but it will not change plugins at all,
so don't worried about the plugin files will be changed to unsafe

## Usage
- Download Banner.
- Launch with command java -jar banner-launcher-version.jar nogui. The nogui argument will disable the server control panel.

## Developer Support
- Download the dev lib jar from GitHub actions.
- Use Fabric Official Template [**Fabric Example Mod**](https://github.com/FabricMC/fabric-example-mod.git).
- Using Mojang Official Mappings in your build.gradle
- Create a directory called lib in your root dir.
- Add dependencies of Banner, such as compileOnly(fileTree("lib/banner-version-dev.jar"))

## Upstream Projects
- [**Bukkit**](https://hub.spigotmc.org/stash/scm/spigot/bukkit.git) - Plugin support.
- [**CraftBukkit**](https://hub.spigotmc.org/stash/scm/spigot/craftbukkit.git) - Plugin support.
- [**Spigot**](https://hub.spigotmc.org/stash/scm/spigot/spigot.git) - Plugin support.
- [**Paper**](https://github.com/PaperMC/Paper.git) - Plugin support.
- [**Arclight**](https://github.com/IzzelAliz/Arclight.git) - Some code.
- [**Mohist**](https://github.com/MohistMC/Mohist.git) - Some code.
- [**StackDeobfuscator**](https://github.com/booky10/StackDeobfuscator) - auto deobfuscate logger crash

## Special Thanks To:
<a href="https://ci.codemc.io/"><img src="https://i.loli.net/2020/03/11/YNicj3PLkU5BZJT.png" width="172"></a>

<a href="https://www.bisecthosting.com/mohistmc"><img src="https://www.bisecthosting.com/partners/custom-banners/118608b8-6e45-4301-b244-41934cdac6d1.png"></a>

![YourKit-Logo](https://www.yourkit.com/images/yklogo.png)

[YourKit](http://www.yourkit.com/), makers of the outstanding java profiler, support open source projects of all kinds with their full-featured [Java](https://www.yourkit.com/java/profiler/index.jsp) and [.NET](https://www.yourkit.com/.net/profiler/index.jsp) application profilers. We thank them for granting Mohist an OSS license so that we can make our software the best it can be.

[<img src="https://user-images.githubusercontent.com/21148213/121807008-8ffc6700-cc52-11eb-96a7-2f6f260f8fda.png" alt="" width="100">](https://www.jetbrains.com)

[JetBrains](https://www.jetbrains.com/), creators of the IntelliJ IDEA, supports Paper with one of their [Open Source Licenses](https://www.jetbrains.com/opensource/). IntelliJ IDEA is the recommended IDE for working with Paper, and most of the Paper team uses it.