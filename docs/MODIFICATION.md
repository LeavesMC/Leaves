Leaves Modification
===========

**English** | [中文](https://github.com/LeavesMC/Leaves/blob/master/docs/MODIFICATION_cn.md)

## Fix (Makes it usable)

> All of them won't have configurations

- Gravity block duper
- Trading with the void
- Tripwire updates when it being removed and not disarmed

## Modify

> All of them will have configuration

- Player can edit sign
- Snowball and egg can knockback player
- Fakeplayer support (like carpet) (command: `/bot`, permission: `bukkit.command.bot`)
- Shears in dispenser can unlimited use
- Shears can rotate redstone equipment (like debug-stick)
- Budding Amethyst can push by piston
- Spectators don't get Advancement
- Use stick and shift to ArmorStand can modify ArmorStand's arm status
- Remove Player Chat sign (NoChatReport Mod server side)
- Instant BlockUpdater reintroduced
- Random flatten triangular distribution (like Carpet-TIS-Addition)
- Player operation limiter (can make auto break bedrock mod unusable)
- Renewable Elytra (when shulker kill phantom)
- Stackable Empty Shulker Boxes
- MC Technical Survival Mode
- Return nether portal fix
- Extra Yggdrasil support
- Whether use Vanilla random
- Update suppression crash fixed
- Bedrock break list
- No feather falling trample
- Shared villager discounts
- Redstone wire doesn't connect if on trapdoor (as 1.20-)
- Despawn enderman with block in hand
- Creative fly no clip (need carpet mod and leaves-carpet protocol)
- Enchantment mending compatibility with infinity
- Shave snow layers
- Mob spawn ignores lc

## Performance

> All of it will have configuration

> Powered by [Pufferfish](https://github.com/pufferfish-gg/Pufferfish)

- Optimize mob spawning (updating, unavailable yet)
- Multithreaded Tracker (updating, unavailable yet)
- Fix Paper#6045
- Optimize entity coordinate key
- Optimize suffocation
- Strip raytracing for entity
- Optimize Spooky Season check
- Optimize Chunk ticking
- Skip POI finding in vehicle
- Optimize entity target finding
- Use more thread Unsafe random
- Disable method profiler
- Disable inactive goal selector
- Skip clone loot parameters
- Reduce entity allocations
- Remove lambda from ticking guard
- Remove iterators from inventory contains
- Remove streams from getting nearby players
- Remove streams and iterators from range check
- Async Pathfinding (updating)
- Cache climbing check for activation
- Use aging cache for biome temperatures
- Reduce entity fluid lookups if no fluids
- Reduce chunk loading & lookups
- Simpler Vanilla ShapelessRecipes comparison
- Improve fluid direction caching

> Powered by [Purpur](https://github.com/PurpurMC/Purpur)
- Don't send useless entity packets

> Powered by [Carpet-AMS-Addition](https://github.com/Minecraft-AMS/Carpet-AMS-Addition)
- Optimized dragon respawn

## Extra Protocol Support

> All of it will have configuration

- PCA sync protocol
- BBOR protocol
- Jade protocol
- Carpet alternative block placement (carpet-extra)
- Appleskin protocol
- Xaero Map protocol
- [Syncmatica](https://github.com/End-Tech/syncmatica) protocol
- Leaves-Carpet protocol
