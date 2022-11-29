Leaves Modification
===========

**English** | [中文](https://github.com/LeavesMC/Leaves/blob/master/docs/MODIFICATION_cn.md)

## Fix

> All of it will don't have configuration

- Gravity block duper
- Trading with the void
- Tripwire update when it being removed and not disarmed

## Modify

> All of it will have configuration

- Player can edit sign
- Snowball and egg can knockback player
- Fakeplayer support (like carpet) (command: /bot)
- Shears in dispenser can unlimited use
- Shears can rotate redstone equipment (like debug-stick)
- Budding Amethyst can push by piston
- Spectator don't get Advancement
- Use stick and shift to ArmorStand can modify ArmorStand's arm status
- Remove Player Chat sign (can instead of NoChatReport Mod server side)
- Instant BlockUpdater reintroduced
- Random flatten triangular distribution (like Carpet-TIS-Addition)

## Performance

> All of it will have configuration

> Powered by [Pufferfish](https://github.com/pufferfish-gg/Pufferfish)
- Optimize mob spawning
- Multithreaded Tracker (updating)
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

> Powered by [Purpur](https://github.com/PurpurMC/Purpur)
- Don't send useless entity packets

## Extra Protocol Support

> All of it will have configuration

- PCA sync protocol
- BBOR protocol