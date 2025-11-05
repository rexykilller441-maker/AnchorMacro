# ⚓ Anchor Macro (Fabric Mod)

### 🧩 Overview
**Anchor Macro** is a simple **client-side Fabric mod** that automates respawn anchor usage in Minecraft.  
With a single key press, it will:

1. Place a respawn anchor  
2. Charge it with glowstone  
3. Equip your totem  
4. Detonate the anchor 💥  

Ideal for PvP, fast respawn setups, or practicing anchor strategies!

---

### ⚙️ Features
- 🚀 One-key respawn anchor automation  
- 🔑 Custom keybinding (default: `~`)  
- ⚡ Fast and lightweight (client-only mod)  
- 🧱 Compatible with any vanilla or Fabric server  
- 🧩 Built for Minecraft **1.21** using **Fabric API**  
- 📝 Fully configurable via JSON (`config/anchormacro.json`)  
- 🔄 Reloadable config without restarting Minecraft  

---

### 🧰 Requirements
| Tool | Version |
|------|---------|
| Minecraft | 1.21 |
| Fabric Loader | ≥ 0.15.0 |
| Fabric API | Latest |
| Java | 21 |

---

### ⚙️ Configuration
The mod uses a JSON configuration file:

.minecraft/config/anchormacro.json

#### Default Config

| Property                  | Default | Description |
|----------------------------|---------|-------------|
| `anchorSlot`               | 0       | Hotbar slot for anchors (0–8) |
| `glowstoneSlot`            | 1       | Hotbar slot for glowstone |
| `totemSlot`                | 8       | Hotbar slot for Totem |
| `delayPlaceAnchor`         | 4       | Delay (ticks) between placing an anchor |
| `delaySwitchToGlowstone`   | 2       | Delay (ticks) for switching to glowstone |
| `delayChargeAnchor`        | 3       | Delay (ticks) for charging the anchor |
| `delaySwitchToTotem`       | 2       | Delay (ticks) for switching to totem |
| `delayExplodeAnchor`       | 2       | Delay (ticks) for exploding the anchor |
| `safeAnchorMode`           | false   | Place glowstone in front after charging |
| `explodeOnlyIfTotemPresent`| false   | Only explode anchor if totem is present |
| `showNotifications`        | true    | Show notifications in-game |

**Reloading Config:**
```java
AnchorMacroConfig.reload(); // call this to reload the config at runtime


---

🛠️ Installation

1. Download the latest .jar from Releases.


2. Place it in your .minecraft/mods/ folder.


3. Launch Minecraft with Fabric Loader.


4. Press ~ in-game to activate the macro.




---

🧑‍💻 Development

Clone this repository and build the mod:

git clone https://github.com/<your-username>/AnchorMacro.git
cd AnchorMacro
./gradlew build

The compiled .jar will appear in build/libs/.


---

📄 License

MIT License – feel free to use, modify, and distribute.

This version adds **config details, reload instructions, and clarifies features** for better understanding.
