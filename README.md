# OneWayBlocks
Assign textures to different block sides


[SpigotMC page](https://www.spigotmc.org/resources/23778/)

## How it works 

* When a block is converted, the plugin places a marker ArmorStand inside of it
    * its custom name contains the BlockFace & the configured material
    * an additional direction-marker ArmorStand is placed on the clicked block side
* When players move, the plugin checks all nearby marker ArmorStands
    * first, the block location is compared against the player's location, to see if the block face is visible to the player
    * then the plugin checks the direction-marker's line-of-sight to the player
    * if the player can see the BlockFace, a block-change packet is sent to only that player
    * (if they can't see it, a packet to reset the block is sent)
* Since it does the whole checking every time a player moves, it's currently not that resource friendly.
