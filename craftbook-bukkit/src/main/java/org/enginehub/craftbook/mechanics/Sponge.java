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

package org.enginehub.craftbook.mechanics;

import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.util.BlockUtil;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.LocationUtil;
import org.enginehub.craftbook.util.events.SourcedBlockRedstoneEvent;
import com.sk89q.util.yaml.YAMLProcessor;

public class Sponge extends AbstractCraftBookMechanic {

    private boolean isValidSponge(Block block) {
        return block.getType() == Material.SPONGE || (includeWet && block.getType() == Material.WET_SPONGE);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockFromTo(BlockFromToEvent event) {

        if(event.getBlock().getType() != Material.WATER) return;

        if(!BlockUtil.isBlockReplacable(event.getToBlock().getType())) return;

        if(!EventUtil.passesFilter(event)) return;

        for (int cx = -radius; cx <= radius; cx++) {
            for (int cy = -radius; cy <= radius; cy++) {
                for (int cz = -radius; cz <= radius; cz++) {
                    Block sponge = event.getToBlock().getRelative(cx, cy, cz);
                    if(circularRadius && !LocationUtil.isWithinSphericalRadius(sponge.getLocation(), event.getToBlock().getLocation(), radius)) continue;
                    if(redstone && !sponge.isBlockIndirectlyPowered()) continue;
                    if(isValidSponge(sponge)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {

        if(!isValidSponge(event.getBlock())) return;

        if(redstone && !event.getBlock().isBlockIndirectlyPowered()) return;

        if(!EventUtil.passesFilter(event)) return;

        removeWater(event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {

        if(!isValidSponge(event.getBlock())) return;

        if(redstone && !event.getBlock().isBlockIndirectlyPowered()) return;

        if(!EventUtil.passesFilter(event)) return;

        Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), () -> addWater(event.getBlock()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRedstoneChange(SourcedBlockRedstoneEvent event) {

        if(!redstone) return;
        if(!isValidSponge(event.getBlock())) return;

        if(event.isMinor()) return;

        if(!EventUtil.passesFilter(event)) return;

        if(!event.isOn())
            addWater(event.getBlock());
        else
            removeWater(event.getBlock());
    }

    public void removeWater(Block block) {
        for (int cx = -radius; cx <= radius; cx++) {
            for (int cy = -radius; cy <= radius; cy++) {
                for (int cz = -radius; cz <= radius; cz++) {
                    Block water = block.getRelative(cx, cy, cz);
                    if(circularRadius && !LocationUtil.isWithinSphericalRadius(water.getLocation(), block.getLocation(), radius)) continue;
                    if(water.getType() == Material.WATER) {
                        water.setType(Material.AIR);
                    }
                }
            }
        }
    }

    public void addWater(Block block) {

        int cx,cy,cz;

        // The negative x edge
        cx = block.getX() - radius - 1;
        for (cy = block.getY() - radius - 1; cy <= block.getY() + radius + 1; cy++) {
            for (cz = block.getZ() - radius - 1; cz <= block.getZ() + radius + 1; cz++) {
                Block water = block.getWorld().getBlockAt(cx, cy, cz);
                if(circularRadius && !LocationUtil.isWithinSphericalRadius(water.getLocation(), block.getLocation(), radius+1.5)) continue;
                if (water.getType() == Material.WATER) {
                    if(BlockUtil.isBlockReplacable(water.getRelative(1, 0, 0).getType())) {
                        BlockFromToEvent event = new BlockFromToEvent(water, water.getRelative(1, 0, 0));
                        Bukkit.getPluginManager().callEvent(event);
                        if(!event.isCancelled())
                            water.getRelative(1, 0, 0).setType(Material.WATER);
                    }
                }
            }
        }

        // The positive x edge
        cx = block.getX() + radius + 1;
        for (cy = block.getY() - radius - 1; cy <= block.getY() + radius + 1; cy++) {
            for (cz = block.getZ() - radius - 1; cz <= block.getZ() + radius + 1; cz++) {
                Block water = block.getWorld().getBlockAt(cx, cy, cz);
                if(circularRadius && !LocationUtil.isWithinSphericalRadius(water.getLocation(), block.getLocation(), radius+1.5)) continue;
                if (water.getType() == Material.WATER) {
                    if(BlockUtil.isBlockReplacable(water.getRelative(-1, 0, 0).getType())) {
                        BlockFromToEvent event = new BlockFromToEvent(water, water.getRelative(-1, 0, 0));
                        Bukkit.getPluginManager().callEvent(event);
                        if(!event.isCancelled())
                            water.getRelative(-1, 0, 0).setType(Material.WATER);
                    }
                }
            }
        }

        // The negative y edge
        /*cy = block.getY() - radius - 1;
        for (cx = block.getX() - radius - 1; cx <= block.getX() + radius + 1; cx++) {
            for (int cz = block.getZ() - radius - 1; cz <= block.getZ() + radius + 1; cz++) {
                Block water = block.getWorld().getBlockAt(cx, cy, cz);
                if(CraftBookPlugin.inst().getConfiguration().spongeCircleRadius && !LocationUtil.isWithinSphericalRadius(water.getLocation(), block.getLocation(), radius+1.5)) continue;
                if (water.getType() == Material.WATER) {
                    if(BlockUtil.isBlockReplacable(water.getRelative(0, 1, 0).getType())) {
                        BlockFromToEvent event = new BlockFromToEvent(water, water.getRelative(0, 1, 0));
                        Bukkit.getPluginManager().callEvent(event);
                        if(!event.isCancelled())
                            water.getRelative(0, 1, 0).setType(Material.WATER);
                    }
                }
            }
        }*/

        // The positive y edge
        cy = block.getY() + radius + 1;
        for (cx = block.getX() - radius - 1; cx <= block.getX() + radius + 1; cx++) {
            for (cz = block.getZ() - radius - 1; cz <= block.getZ() + radius + 1; cz++) {
                Block water = block.getWorld().getBlockAt(cx, cy, cz);
                if(circularRadius && !LocationUtil.isWithinSphericalRadius(water.getLocation(), block.getLocation(), radius+1.5)) continue;
                if (water.getType() == Material.WATER) {
                    if(BlockUtil.isBlockReplacable(water.getRelative(0, -1, 0).getType())) {
                        BlockFromToEvent event = new BlockFromToEvent(water, water.getRelative(0, -1, 0));
                        Bukkit.getPluginManager().callEvent(event);
                        if(!event.isCancelled())
                            water.getRelative(0, -1, 0).setType(Material.WATER);
                    }
                }
            }
        }

        // The negative z edge
        cz = block.getZ() - radius - 1;
        for (cx = block.getX() - radius - 1; cx <= block.getX() + radius + 1; cx++) {
            for (cy = block.getY() - radius - 1; cy <= block.getY() + radius + 1; cy++) {
                Block water = block.getWorld().getBlockAt(cx, cy, cz);
                if(circularRadius && !LocationUtil.isWithinSphericalRadius(water.getLocation(), block.getLocation(), radius+1.5)) continue;
                if (water.getType() == Material.WATER) {
                    if(BlockUtil.isBlockReplacable(water.getRelative(0, 0, 1).getType())) {
                        BlockFromToEvent event = new BlockFromToEvent(water, water.getRelative(0, 0, 1));
                        Bukkit.getPluginManager().callEvent(event);
                        if(!event.isCancelled())
                            water.getRelative(0, 0, 1).setType(Material.WATER);
                    }
                }
            }
        }

        // The positive z edge
        cz = block.getZ() + radius + 1;
        for (cx = block.getX() - radius - 1; cx <= block.getX() + radius + 1; cx++) {
            for (cy = block.getY() - radius - 1; cy <= block.getY() + radius + 1; cy++) {
                Block water = block.getWorld().getBlockAt(cx, cy, cz);
                if(circularRadius && !LocationUtil.isWithinSphericalRadius(water.getLocation(), block.getLocation(), radius+1.5)) continue;
                if (water.getType() == Material.WATER) {
                    if(BlockUtil.isBlockReplacable(water.getRelative(0, 0, -1).getType())) {
                        BlockFromToEvent event = new BlockFromToEvent(water, water.getRelative(0, 0, -1));
                        Bukkit.getPluginManager().callEvent(event);
                        if(!event.isCancelled())
                            water.getRelative(0, 0, -1).setType(Material.WATER);
                    }
                }
            }
        }
    }

    private int radius;
    private boolean circularRadius;
    private boolean redstone;
    private boolean includeWet;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("radius", "The maximum radius of the sponge.");
        radius = config.getInt("radius", 5);

        config.setComment("circular-radius", "Whether the radius should be circular or square.");
        circularRadius = config.getBoolean("circular-radius", true);

        config.setComment("include-wet", "Whether to include wet sponges or not.");
        includeWet = config.getBoolean("include-wet", false);

        config.setComment("require-redstone", "Whether to require redstone to suck up water or not.");
        redstone = config.getBoolean("require-redstone", false);
    }
}
