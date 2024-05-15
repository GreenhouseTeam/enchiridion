package dev.greenhouseteam.enchiridion.access;

public interface UpgradeableEnchantmentMenuAccess {
    int enchiridion$getScrollIndex();
    void enchiridion$setScrollIndex(int value);

    int enchiridion$getBookshelfCount(int index);
    int enchiridion$getRequiredBookshelves(int index);
}
