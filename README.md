<div align="center">
<img src="src/main/resources/assets/banner/logo.png">
  <h1>Mohist Banner 1.20.x</h1>

### The Bukkit/Spigot/Paper API implementation for Fabric
[![](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fci.codemc.io%2Fjob%2FMohistMC%2Fjob%2FBanner&label=Jenkins&logo=jenkins&logoColor=%23ffffff)](https://ci.codemc.io/job/MohistMC/job/Banner-1.20)
[![](https://img.shields.io/github/stars/MohistMC/Banner.svg?label=Stars&logo=github)](https://github.com/MohistMC/Banner/stargazers)
[![](https://img.shields.io/badge/jdk-17.0.5+8-brightgreen.svg?colorB=469C00&logo=java)](https://adoptium.net/temurin/releases/?version=17)
[![](https://img.shields.io/badge/Gradle-8.1.1-brightgreen.svg?colorB=469C00&logo=gradle)](https://docs.gradle.org/7.5.1/release-notes.html)
[![](https://img.shields.io/discord/311256119005937665.svg?color=%237289da&label=Discord&logo=discord&logoColor=%237289da)](https://discord.gg/mohistmc)

[![]()](https://bstats.org/plugin/server-implementation/Mohist/6762)
</div>

| Version | Support     | Stability | Mod compatibility   | Plugin compatibility |
|---------|-------------|-----------|---------------------|----------------------|
| 1.20.x  | WIP         | Poor      | Better than plugins | Poor                 |
| 1.19.4  | WIP         | Poor      | Better than plugins | Poor                 |

## Notice
- Fabric + Bukkit is more vanilla-like than Forge + Bukkit
- Fabric API uses mixins to change minecraft indirectly
- There's very little breaking changes
- Banner still has a lot of bugs, so do not use it in production
- Download for testing only, report bugs

## Progress
- [ ] Start patch
    * [x] Bukkit([**54e8ec7**](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/commits/54e8ec7))
    * [ ] CraftBukkit([**6962456**](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/commits/6962456))
    - [ ] Spigot ([**723951c**](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/spigot/commits/723951c))
        - [ ] Bukkit-Patches
        - [ ] CraftBukkit-Patches (93%)
### Running?
It could be compilable and running, but the compatibility with plugins is poor.

### NMS Support
We do support using Spigot's net.minecraft.server classes. Classes and Fields will automatically remap to their intermediary counterparts in runtime, but it will not change plugins at all,
so don't worried about the plugin files will be changed to unsafe

## Installation (for Linux)
- You'll need `wget` and `curl`
```bash
cd ./<your-server-folder>
wget https://github.com/8Mi-Tech/Shell/raw/main/banner-launcher
chmod +x ./banner-launcher
./banner-launcher -i
```

## Usage
- Just put it in the mods folder.
- Also, remember that Banner is still in WIP - do not use it in a production environment!

## Upstream Projects
- [**Fabric Loader**](https://github.com/FabricMC/fabric-loader.git) - Mod load support.
- [**Fabric API**](https://github.com/FabricMC/fabric-loader.git) - Mod API support.
- [**Bukkit**](https://hub.spigotmc.org/stash/scm/spigot/bukkit.git) - Plugin support.
- [**CraftBukkit**](https://hub.spigotmc.org/stash/scm/spigot/craftbukkit.git) - Plugin support.
- [**Spigot**](https://hub.spigotmc.org/stash/scm/spigot/spigot.git) - Plugin support.
- [**Paper**](https://github.com/PaperMC/Paper.git) - Plugin support.
- [**Arclight**](https://github.com/IzzelAliz/Arclight.git) - Some code.
- [**Mohist**](https://github.com/MohistMC/Mohist.git) - Some code.

## Special Thanks To:
<a href="https://ci.codemc.io/"><img src="https://i.loli.net/2020/03/11/YNicj3PLkU5BZJT.png" width="172"></a>

<a href="https://www.bisecthosting.com/mohistmc"><img src="https://www.bisecthosting.com/partners/custom-banners/118608b8-6e45-4301-b244-41934cdac6d1.png"></a>

![YourKit-Logo](https://www.yourkit.com/images/yklogo.png)

[YourKit](http://www.yourkit.com/), makers of the outstanding java profiler, support open source projects of all kinds with their full-featured [Java](https://www.yourkit.com/java/profiler/index.jsp) and [.NET](https://www.yourkit.com/.net/profiler/index.jsp) application profilers. We thank them for granting Mohist an OSS license so that we can make our software the best it can be.
