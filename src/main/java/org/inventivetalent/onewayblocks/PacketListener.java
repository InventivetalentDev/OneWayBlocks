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

import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.plugin.Plugin;
import org.inventivetalent.packetlistener.handler.PacketHandler;
import org.inventivetalent.packetlistener.handler.PacketOptions;
import org.inventivetalent.packetlistener.handler.ReceivedPacket;
import org.inventivetalent.packetlistener.handler.SentPacket;
import org.inventivetalent.reflection.resolver.ConstructorResolver;
import org.inventivetalent.reflection.resolver.MethodResolver;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;

import java.util.Set;
import java.util.logging.Level;

public class PacketListener extends PacketHandler {

	static final NMSClassResolver nmsClassResolver = new NMSClassResolver();

	static Class<?> BaseBlockPosition        = nmsClassResolver.resolveSilent("BaseBlockPosition");
	static Class<?> BlockPosition            = nmsClassResolver.resolveSilent("BlockPosition");
	static Class<?> PacketPlayOutBlockChange = nmsClassResolver.resolveSilent("PacketPlayOutBlockChange");

	static MethodResolver BaseBlockPositionMethodResolver = new MethodResolver(BaseBlockPosition);

	static ConstructorResolver BlockPositionConstructorResolver = new ConstructorResolver(BlockPosition);

	public PacketListener(Plugin plugin) {
		super(plugin);
		addHandler(this);
	}

	@PacketOptions(forcePlayer = true)
	@Override
	public void onSend(SentPacket sentPacket) {
		if ("PacketPlayOutBlockChange".equals(sentPacket.getPacketName())) {
			Object a = sentPacket.getPacketValue("a");

			int x = (int) BaseBlockPositionMethodResolver.resolveWrapper("getX").invoke(a);
			int y = (int) BaseBlockPositionMethodResolver.resolveWrapper("getY").invoke(a);
			int z = (int) BaseBlockPositionMethodResolver.resolveWrapper("getZ").invoke(a);
			if (y < 0) {// This *should* be our packet -> update Y and let it through
				try {
					Object blockPosition = BlockPositionConstructorResolver.resolve(new Class[] {
							int.class,
							int.class,
							int.class }).newInstance(x, -y, z);

					sentPacket.setPacketValue("a", blockPosition);
				} catch (Exception e) {
					getPlugin().getLogger().log(Level.SEVERE, "Failed to update BlockPosition in BlockChange-Packet", e);
				}
			} else {
				Block block = sentPacket.getPlayer().getWorld().getBlockAt(x, y, z);
				Set<ArmorStand> armorStands = ((OneWayBlocks) getPlugin()).getArmorStandsInBlock(block);
				if (!armorStands.isEmpty()) {
					sentPacket.setCancelled(true);
				}
			}
		}
	}

	@Override
	public void onReceive(ReceivedPacket receivedPacket) {
	}
}
