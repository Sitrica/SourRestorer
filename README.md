# SourRestorer
The best inventory saver and restorer plugin.

SourRestorer is the absolute best inventory saver and restorer for 1.13+ on the market for FREE. Inventory savers help your administration team manage and restore inventories of players easier and faster. From restoring inventories of players that died to a hacker, to managing their physical inventories if they stole an item they aren't suppose to have + enderchest management.

Features:

    100% configurable (View configurations at https://github.com/Sitrica/SourRestorer/tree/master/src/main/resources)
        Sounds.
        Inventory items.
        Messages.
        Every feature of the plugin.

    Auto-save inventories + configurable time.
    Save inventory on death.
    Auto-delete system.
        Delete over time.
        Delete if they hit a certain number of saves.
        Delete when they logout or login.
        Delete after restored.
        Delete if banned
        And much more configurations.
    Multi threaded and asynchronous saving. Other resources on Spigot are not multi threaded and some aren't even asynchronous.
    MySQL or H2 database options for best performance.
    Search feature, searches all saves for what you're looking for.
        Certain item type.
        An item with a custom name.
        An item with a certain word in the lore.
    Sorting
        Sort by time of the inventory save.
        Sort by damage cause (what killed the player).
        Sort by distance from you to the location of save.
        Sort by amount of items in the save.
        And much more + custom API sorting.
    View staff members that restored saves in the past.
    Edit player's enderchests and inventories (soon), even when they're offline.
    All messages have placeholders in the configurations.
    Armour saving.
    Open source code.
    Active development team.
    Extensive developer API to extend the resource to your needs.
    Professionally coded and optimized for best performance.
    Issue and suggestion pages we actively read and support.

Commands/Permissions:

    /sr admin - View all admin commands
        sourrestorer.admin
    /sr enderchest (player) - View player enderchests.
        sourrestorer.admin or sourrestorer.enderchest
    /sr save (player) - Save a player's inventory.
        sourrestorer.admin or sourrestorer.save
    /sr view (player) - View a player's inventory saves.
        Also grants restore access.
        sourrestorer.admin or sourrestorer.restore
    Permission sourrestorer.grab allows to grab items from the saves.
