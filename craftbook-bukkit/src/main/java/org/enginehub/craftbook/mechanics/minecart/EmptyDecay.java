/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.mechanics.minecart;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.util.yaml.YAMLProcessor;

public class EmptyDecay extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleExit(VehicleExitEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        Vehicle vehicle = event.getVehicle();

        if (!(vehicle instanceof RideableMinecart)) return;

        CraftBookPlugin.inst().getServer().getScheduler().runTaskLater(CraftBookPlugin.inst(), new Decay((RideableMinecart) vehicle), delay);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChunkLoad(ChunkLoadEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        for (Entity ent : event.getChunk().getEntities()) {
            if (ent == null || !ent.isValid())
                continue;
            if (!(ent instanceof RideableMinecart))
                continue;
            if (!ent.isEmpty())
                continue;
            CraftBookPlugin.inst().getServer().getScheduler().runTaskLater(CraftBookPlugin.inst(), new Decay((RideableMinecart) ent), delay);
        }
    }

    private static class Decay implements Runnable {

        RideableMinecart cart;

        Decay(RideableMinecart cart) {

            this.cart = cart;
        }

        @Override
        public void run() {

            if (cart == null || !cart.isValid() || !cart.isEmpty()) return;
            cart.remove();
        }
    }

    private int delay;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("time-in-ticks", "The time in ticks that the cart will wait before decaying.");
        delay = config.getInt("time-in-ticks", 20);
    }
}