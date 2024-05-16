package dev.greenhouseteam.enchiridion.access;

public interface LevelUpEnchantmentMenuAccess {
    int enchiridion$getEnchantmentSize();
    int enchiridion$getBookshelfCount();
    int enchiridion$getRequiredBookshelves(int index);

    void enchiridion$refreshEnchantmentIndexes();

    double enchiridion$getScrollOffset();
    void enchiridion$setScrollOffset(double value);
}
