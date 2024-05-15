package dev.greenhouseteam.enchiridion.gui;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.DataSlot;

public class ScrollIndexDataSlot extends DataSlot {
    private int value;
    private Container container;

    public ScrollIndexDataSlot(Container container) {
        this.container = container;
    }

    @Override
    public int get() {
        return value;
    }

    @Override
    public void set(int value) {
        this.value = value;
    }

    public void setAndMarkChanged(int value) {
        this.value = value;
        container.setChanged();
    }
}
