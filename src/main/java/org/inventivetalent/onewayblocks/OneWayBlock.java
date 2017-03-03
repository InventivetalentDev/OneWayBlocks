package org.inventivetalent.onewayblocks;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.inventivetalent.vectors.d3.Vector3DDouble;

@Data
@EqualsAndHashCode(doNotUseGetters = true,
				   exclude = {
						   "material",
						   "data",
						   "entity" })
public class OneWayBlock {

	private final Vector3DDouble location;
	private final BlockFace      direction;
	private       Material       material;
	private       byte           data;
	private       ArmorStand     entity;
	private       ArmorStand     directionMarker;
	private       boolean        inverted;

	public boolean faceVisibleFrom(Vector3DDouble vector) {
		//		boolean result = faceVisibleFrom0(vector);
		//		if(inverted)return !result;
		//		return result;
		return faceVisibleFrom0(vector);
	}

	public boolean faceVisibleFrom0(Vector3DDouble vector) {
		Vector3DDouble check = location.add(direction.getModX() * .5, direction.getModY() * .5, direction.getModZ() * .5);
		Vector3DDouble diff = check.subtract(vector);

		if (direction.getModX() == 1) {
			return diff.getX() >= 0;
		}
		if (direction.getModX() == -1) {
			return diff.getX() <= 0;
		}

		if (direction.getModY() == 1) {
			return diff.getY() >= 0;
		}
		if (direction.getModY() == -1) {
			return diff.getY() <= 0;
		}

		if (direction.getModZ() == 1) {
			return diff.getZ() >= 0;
		}
		if (direction.getModZ() == -1) {
			return diff.getZ() <= 0;
		}

		return false;
	}

	public Block getBlock(World world) {
		return location.toBukkitLocation(world).getBlock();
	}

	public static OneWayBlock of(Vector3DDouble location, BlockFace blockFace) {
		return new OneWayBlock(location, blockFace);
	}

	public static OneWayBlock of(Location location, BlockFace blockFace) {
		return of(new Vector3DDouble(location), blockFace);
	}

	public static OneWayBlock of(Location location, String name) {
		String[] split = name.split("-");
		OneWayBlock block = of(location, BlockFace.valueOf(split[1].toUpperCase()));
		String[] materialSplit = split[2].split(":");
		block.setMaterial(Material.valueOf(materialSplit[0]));
		block.setData(Byte.parseByte(materialSplit[1]));
		block.setInverted(name.contains("inverted"));
		return block;
	}

	public static OneWayBlock of(Entity entity) {
		return of(entity.getLocation().getBlock().getLocation().add(.5, .5, .5), entity.getCustomName());
	}

}
