Scavenger - Hook to Authenticator and Debug for Authme / xAuth / AuthDB
====================

Scavenger from cnaude , modify to hook to authenticator and auth plugin

Source :

http://dev.bukkit.org/server-mods/scavenger/
Thanks to cnaude

Source for Authenticator :

http://dev.bukkit.org/server-mods/authenticator/
Thanks to ThisIsAreku


How it works :

Add this line in your config file or deleting yours and let the plugin add the new :

OfflineMode: false

When OfflineMode is set to false : Scavenger work normaly without hooks to Authenticator

When OfflineMode is set to true : Scavenger hook to Authenticator and the player recovered his inventory after /login and moving

