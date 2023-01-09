export const sc2logosMod = `
<div align="left" style="margin-left:40px">
<div>
    Logos.SC2Mod is the name of the SC2 mod file that can be placed into the game's directory attached to the user account.
</div>
<div>For example, that mod should be placed into '<font face='courier new, monospace'>C:\\Users\\YourUserName\\Documents\\StarCraft II</font>'.
</div>
<div><br></div>
<div><a href="https://www.dropbox.com/s/7l8zi8pajcnkw7i/Logos.SC2Mod?dl=1" target="_blank" rel="nofollow">Download Logos.SC2Mod</a><br></div>
<div><br></div>
<div>If it loads, you should see an additional dropdown in a game lobby where you can select a data entry per player. Your replay will be saved with the index number of that entry, so whenever you watch a replay or watch the game live, its data is loaded.</div>
<div><br></div>
<div>The mod allows you to inject text and images.</div>
<div><br></div>
<div>
    <div>To inject data, the Logos.SC2Mod contains and describes this data. If the game found the mod and can read data from it, then you can select a data set entry for each player in a game lobby. This allows tournaments to display player information within the in-game UI, if they've properly set up the Logos.SC2Mod before the tournament and to alter the tournament logo that appears on melee maps that placed the unit for that.</div>
    <div><br></div>
    <div>The following three lists are for reference, you might want to skip them.</div>
    <div><br></div>
    <div><b>Identical elements from WCS UI:</b></div>
    <div><ul><li>FrameLogoText: PlayerName - name of the player (e.g.: "dayvie")</li>
    <li>FrameLogoText: PlayerRealName - real name of the player (e.g.: "David Kim")</li>
    <li>FrameLogoText: PlayerRank - rank of the player (e.g.: "1")</li>
    <li>FrameLogoText: PlayerScore - score value of the player (e.g.: "9001")</li>
    <li>FrameLogoText: PlayerFlavorText - some information about the player (e.g.: "it's nurfin time")</li>
    <li>FrameLogoImage: PortraitCutout - photo of the player (transparent)</li>
    <li>FrameLogoImage: Trophies - image about achievements (transparent)</li>
    <li>FrameLogoImage: Flag - country's flag of the player (transparent)</li>
    <li>FrameLogoImage: LogoSmall - small logo of the team (transparent)</li>
    <li>FrameLogoImage: LogoText - logo of the team displaying its name (transparent)</li>
    <li>FrameLogoImage: Sponsors - sponsors (transparent)</li></ul>
    </div><div><br>
</div>
<div><br></div>
<div><b>New elements in GameHeart Observer UI:</b></div>
<div><ul><li>FrameLogoText: PlayerRankTitle - optional<span style="background-color:transparent;font-size:10pt"> new title of the rank (default name is "WCS STANDINGS:" which is displayed if your logos mod does not contain this entry. This would be the </span>case,<span style="background-color:transparent;font-size:10pt"> if your mod was created for WCS 2.0 UI.)</span></li>
    <li>FrameLogoText: PlayerScoreTitle - optional<span style="background-color:transparent;font-size:10pt"> new title of the score (default name is "POINTS:" which is displayed if your logos mod does not contain this entry.)</span></li></ul>
</div>
<div><br></div>
<div><br></div>
<div>Elements the GameHeart / WCS mod uses to display information on the ground in the game world:</div>
<div><ul><li>FrameLogoImage: FrameLogoImage: Logo - Logo of the team (transparent)</li>
    <li>PlayerLogoEntry: PrimaryLogo - image next to every base (transparent)</li>
    <li>PlayerLogoEntry: SecondaryLogo - second image next to the starting base at the beginning of the map (transparent)</li>
    <li>LeagueLogoEntry: PrimaryLogo - league logo placed on the map (transparent)</li>
    <li>LeagueLogoEntry: SecondaryLogo - league logo placed on the map? (transparent)</li></ul>
</div>
<div><br></div>
<div>The example Logos.SC2Mod linked above contains 3 example player data entries and a definition for the tournament logo.</div>
<div><br></div>
<div>Unfortunately, the file you need to edit is not editable within the editor directly. So, you would <font color="#ff9900">save the mod as a "SC2 Component Folder"</font> via</div>
<div><span>&nbsp;&nbsp; &nbsp;</span><span>&nbsp;&nbsp; &nbsp;</span><span>&nbsp;&nbsp; &nbsp;</span>File -&gt; Save As -&gt; file type: SC2 Component Folder.</div>
<div>(In case you encounter an error saving, use another file name.)</div>
<div>Th<span style="background-color:transparent;font-size:13.3333px">e mod is saved uncompressed as a folder instead of compressed into a single mod file. This makes it easy for you to edit the definitions file and add new images.</span></div>
<div><br></div>
<div>Within the folder, there is a text file "<b><font color="#ff9900">LogoConfig.xml</font></b>". It contains the player entries with its text entries and image references. I suggest that you copy an existing entry and edit it as you like.</div>
<div>Images that you are referencing are placed within the mod as done in that example.</div>
<div><br></div>
<div>To convert the folder back to a mod, you open up the contained "ComponentList.SC2Components" in the editor and save it as a SC2 Mod again.</div>
<div>The game will display dropdowns during a lobby, if the mod is named correctly, is located in the correct directory and if it can be loaded.</div>
<div><br></div>
<h2><a name="TOC-Usage:"></a><b>Usage:</b></h2>
<div>The Logos.SC2Mod needs to be placed at:</div>
<div>Windows: "C:\\Users\\yourUsername\\Documents\\StarCraft II"</div>
<div>For OSX, you might find it here: "~/Library/Application Support/Blizzard/StarCraft II"</div>
<div><br></div>
<div>If everything is fine (location of the mod + its content), then you will see additional dropdowns for each player in a lobby. They allow you to chose a player entry from the mod which will be used for that player. Only the observers need to do this, though. The selection is local on your computer only, so it's impossible to alter the game of the other players using this.</div>
</div>
<div><br></div>
<div>Replays generated will save the information of the selection. But I believe it was only the index number of the selection, so editing the Logos.SC2Mod might alter the player's data you will see.</div>
<div><br></div>
<h2><a name="TOC-Further-Info:"></a>Further Info:</h2>
<div>Please read Ryan T. Schutter's explanation (who received the info from the engineers at Blizzard that added this feature):&nbsp;<a href="http://www.teamliquid.net/blogs/467658-gameheart-project-update-8-wcs-gameheart" target="_blank" rel="nofollow">Further Logos.SC2Mod Information</a></div>
<div><br></div>
</div>
`;
