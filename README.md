# ClickableLinks
 Brings clickable links to minecraft chat.  

Other pages: [Spigot](https://www.spigotmc.org/resources/clickablelinks.105786/)  

<b>Note</b>: Bukkit removed this feature in [1.19.1](https://www.minecraft.net/ru-ru/article/minecraft-1-19-1-pre-release-6). Therefore, use this plugin only on Bukkit/Spigot/Paper 1.19+ servers, but on 1.18- it can also be useful due to link underlining and supported link formats.  

Later, the original Bukkit code was found [here](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/browse/src/main/java/org/bukkit/craftbukkit/util/CraftChatMessage.java). It has some issues: no URL-escaping, only latin letters in domains, invalid IPv4, TLD of length 2-4(no .museum or .vodka). These issues probably were inherited from the [vanilla parser 1.5.x-1.6.x](https://bugs.mojang.com/browse/MC-18898).  
  
<h2>Features</h2>

Preserves format, e.g.
* prefixes, suffixes and colors in LuckyPerms x Vault x EssentialsX Chat;
* prefixes and colors in TownyChat;
* client whisper messages ("You whisper to %s: %s" in English, "Вы прошептали %s: %s" in Russian, etc).

Works on:  
* player chat;  
* player and console private messages (/w, /msg, /tell);  
* /say, /me.

Can deal with:  
* Regular links  
[https://www.youtube.com](https://github.com/festino-mc-plugins/LinkRestorer/edit/main/README.md#features)  
* No scheme (auto _https://_)  
[vk.com](https://github.com/festino-mc-plugins/LinkRestorer/edit/main/README.md#features)  
* Hieroglyphs in query  
[https://www.youtube.com/c/モエソデmoesode/videos](https://github.com/festino-mc-plugins/LinkRestorer/edit/main/README.md#features)  
* Cyrillic in domain  
[https://стопкоронавирус.рф](https://github.com/festino-mc-plugins/LinkRestorer/edit/main/README.md#features)  
* IPv4  
[1.2.3.4](https://github.com/festino-mc-plugins/LinkRestorer/edit/main/README.md#features)  
* Invalid IPv4  
1.2.3.256  
