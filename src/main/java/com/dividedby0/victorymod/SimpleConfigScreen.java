package com.dividedby0.victorymod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import com.dividedby0.victorymod.config.JSON5ConfigManager;

/**
 * Simple config screen for Victory Mod settings.
 * Displays input fields for each configuration parameter.
 */
public class SimpleConfigScreen extends Screen {
    private final Screen previousScreen;
    private final JSON5ConfigManager configManager;
    private EditBox minRadiusInput;
    private EditBox maxRadiusInput;
    private EditBox bufferDistanceInput;
    private EditBox xpCsvInput;
    
    public SimpleConfigScreen(Screen previousScreen, JSON5ConfigManager configManager) {
        super(Component.literal("Victory Mod Configuration"));
        this.previousScreen = previousScreen;
        this.configManager = configManager;
    }
    
    @Override
    protected void init() {
        int centerX = this.width / 2;
        int labelX = centerX - 190;
        int inputX = centerX + 10;
        int y = 60;

        // Main config fields side by side
        this.minRadiusInput = new EditBox(this.font, inputX, y, 120, 20, Component.literal("Min Radius"));
        this.minRadiusInput.setValue(String.valueOf(configManager.getInt("minDungeonRadius", 40)));
        this.addRenderableWidget(this.minRadiusInput);
        y += 32;

        this.maxRadiusInput = new EditBox(this.font, inputX, y, 120, 20, Component.literal("Max Radius"));
        this.maxRadiusInput.setValue(String.valueOf(configManager.getInt("maxDungeonRadius", 750)));
        this.addRenderableWidget(this.maxRadiusInput);
        y += 32;

        this.bufferDistanceInput = new EditBox(this.font, inputX, y, 120, 20, Component.literal("Buffer Distance"));
        this.bufferDistanceInput.setValue(String.valueOf(configManager.getInt("structureBufferDistance", 30)));
        this.addRenderableWidget(this.bufferDistanceInput);
        y += 40;

        // XP heart requirements as a single CSV field
        int xpInputY = y + 28;
        StringBuilder csv = new StringBuilder();
        for (int i = 1; i <= 9; i++) {
            if (i > 1) csv.append(",");
            csv.append(configManager.getInt("xpThreshold_" + i, 10 * i));
        }
        this.xpCsvInput = new EditBox(this.font, inputX, xpInputY, 300, 20, Component.literal("XP Heart Thresholds (CSV)"));
        this.xpCsvInput.setValue(csv.toString());
        this.addRenderableWidget(this.xpCsvInput);

        // Save and Back buttons always at bottom
        int buttonY = this.height - 40;
        this.addRenderableWidget(Button.builder(Component.literal("Save"), (btn) -> this.save())
            .bounds(centerX - 110, buttonY, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Back"), (btn) -> this.onClose())
            .bounds(centerX + 10, buttonY, 100, 20).build());
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        int labelX = this.width / 2 - 190;
        int inputX = this.width / 2 + 10;
        int y = 60;
        guiGraphics.drawString(this.font, "Min Dungeon Radius (10-500):", labelX, y, 0xAAAAAA);
        y += 32;
        guiGraphics.drawString(this.font, "Max Dungeon Radius (50-1000):", labelX, y, 0xAAAAAA);
        y += 32;
        guiGraphics.drawString(this.font, "Structure Buffer (5-200):", labelX, y, 0xAAAAAA);
        y += 40;
        guiGraphics.drawString(this.font, "XP Heart Requirements (CSV, 9 values):", labelX, y, 0xFFCC66);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    
    private void save() {
        try {
            int minRadius = Integer.parseInt(this.minRadiusInput.getValue());
            int maxRadius = Integer.parseInt(this.maxRadiusInput.getValue());
            int bufferDist = Integer.parseInt(this.bufferDistanceInput.getValue());
            minRadius = Math.max(10, Math.min(500, minRadius));
            maxRadius = Math.max(50, Math.min(1000, maxRadius));
            bufferDist = Math.max(5, Math.min(200, bufferDist));
            configManager.setInt("minDungeonRadius", minRadius);
            configManager.setInt("maxDungeonRadius", maxRadius);
            configManager.setInt("structureBufferDistance", bufferDist);
            // Parse CSV for XP thresholds
            String[] xpVals = this.xpCsvInput.getValue().split(",");
            for (int i = 1; i <= 9; i++) {
                int value = 10 * i;
                if (xpVals.length >= i) {
                    try {
                        value = Integer.parseInt(xpVals[i-1].trim());
                    } catch (NumberFormatException ignore) {}
                }
                configManager.setInt("xpThreshold_" + i, value);
            }
            configManager.saveConfig();
            this.onClose();
        } catch (NumberFormatException e) {
            // Show error - values will be reset on close
        }
    }
    
    @Override
    public void onClose() {
        this.minecraft.setScreen(this.previousScreen);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
