package it.hurts.sskirillss.raccompat.misc;

import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootCollection;

public class RACLootCollections {
    public static final LootCollection FORLORN = LootCollection.builder()
            .entry("[\\w]+:chests\\/[\\w_\\/]*forlorn[\\w_\\/]*", 0.1F)
            .build();

    public static final LootCollection TOXIC = LootCollection.builder()
            .entry("[\\w]+:chests\\/[\\w_\\/]*toxic[\\w_\\/]*", 0.1F)
            .build();

    public static final LootCollection PRIMORDIAL = LootCollection.builder()
            .entry("[\\w]+:chests\\/[\\w_\\/]*primordial[\\w_\\/]*", 0.1F)
            .entry("alexscaves:chests/caveman_house", 0.1F)
            .build();

    public static final LootCollection MAGNETIC = LootCollection.builder()
            .entry("[\\w]+:chests\\/[\\w_\\/]*magnetic[\\w_\\/]*", 0.1F)
            .build();

    public static final LootCollection ABYSSAL = LootCollection.builder()
            .entry("[\\w]+:chests\\/[\\w_\\/]*abyssal[\\w_\\/]*", 0.1F)
            .build();
}