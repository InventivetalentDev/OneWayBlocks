/*
 * Copyright 2015-2016 inventivetalent. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and contributors and should not be interpreted as representing official policies,
 *  either expressed or implied, of anybody else.
 */

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
