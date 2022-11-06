export const ahliObsShortcuts = `
<div align="center">
    This UI adds a couple of hotkeys which control the displayed content.

    <h3>Team Information</h3>
    <div style="padding-left: 2em">
        Displays information in the bottom center panel.
    </div>
    <br>
    <table>
        <tr>
            <td>Talents</td>
            <td>Control + 1</td>
        </tr>
            <td>Deaths, Damage, Role</td>
            <td>Control + 2</td>
        <tr>
            <td>Actions per Minute</td>
            <td>Control + 3</td>
        </tr>
        <tr>
            <td>Experience</td>
            <td>Control + 4</td>
        </tr>
        <tr>
            <td>Time dead, Deaths, Self sustain</td>
            <td>Control + 5</td>
        </tr>
        <tr>
            <td>Carried Objectives</td>
            <td>Control + 6</td>
        </tr>
        <tr>
            <td>Kills, Deaths, Assists</td>
            <td>Control + 7</td>
        </tr>
        <tr>
            <td>Time CC-ed enemy Heroes</td>
            <td>Control + 8</td>
        </tr>
    </table>
    <br>
    <br>
    <br>
    <h3>E-Sport Broadcast</h3>
    <br>
    <table>
        <tr>
            <td>Use minimalistic selected unit information</td>
            <td>Control + Alt + K</td>
        </tr>
    </table>
    <br>
    <br>
    <br>
    <h3>Map Scores</h3>
    <div style="padding-left: 2em">
        The UI supports a display of map scores of both teams including team names.<br>
        Score values range from 0 to 9 and can be altered via hotkeys and mouse clicks.<br>
        The team names can be set in the game lobby via the Logos mod feature, but it is recommended to use Stream Overlays for this.<br>
    </div>
    <br>
    <table>
        <tr>
            <td>Toggle Map Scores</td>
            <td>Control + Shift + S</td>
        </tr>
        <tr>
            <td>Raise Left Team's Score</td>
            <td>Alt + S<br>
            (or click on the label)</td>
        </tr>
        <tr>
            <td>Raise Right Team's Score</td>
            <td>Alt + D<br>
            (or click on the label)</td>
        </tr>
        <tr>
            <td>Lower Left Team's Score</td>
            <td>Control + Alt + S</td>
        </tr>
        <tr>
            <td>Lower Right Team's Score</td>
            <td>Control + Alt + D</td>
        </tr>
        <tr>
            <td>Cycle through all team names</td>
            <td>Left mouse click on team name label</td>
        </tr>
    </table>
    <br>
    <br>
    <br>
    <h3>Analysis</h3>
    <br>
    <table>
        <tr>
            <td>Toggle selected unit details</td>
            <td>Control + C</td>
        </tr>
    </table>
    <br>
    <br>
    <br>
    <h3>Observer Controls</h3>
    <br>
    <table>
        <tr>
            <td>Toggle Observer Controls</td>
            <td>Control + Shift + O</td>
        </tr>
    </table>
    <br>
    <br>
    <br>
    <h3>Chat</h3>
    <br>
    <table>
        <tr>
            <td>Toggle Chat visibility</td>
            <td>Control + Shift + C</td>
        </tr>
    </table>
    <br>
    <br>
    <br>
    <h3>Cinematic</h3>
    <br>
    <table>
        <tr>
            <td>Toggle static UI</td>
            <td>Control + E</td>
        </tr>
        <tr>
            <td>Toggle entire UI</td>
            <td>Control + Alt + U</td>
        </tr>
        <tr>
            <td>Toggle Player Names</td>
            <td>Control + Alt + N</td>
        </tr>
    </table>
    <br>
    <br>
    <br>
    <br>
    <h3>Hotkeys the Game provides (editable in Game Options)</h3>
    <br>
    <table>
        <tr>
            <td>Player Slot Vision<br>
            (double-tap for Player Camera Follow)</td>
            <td>1,2,3,4,5,6,7,8,9,0</td>
        </tr>
        <tr>
            <td>Observer Slot Vision</td>
            <td>Numpad 1, 2, ..., 6</td>
        </tr>
        <tr>
            <td>Revert to Everyone Vision</td>
            <td>E</td>
        </tr>
        <tr>
            <td>Deselect currently selected units</td>
            <td>Backspace</td>
        </tr>
        <tr>
            <td>Toggle bottom UI</td>
            <td>Control + W</td>
        </tr>
        <tr>
            <td>Toggle original leaderpanel</td>
            <td>Tab</td>
        </tr>
        <tr>
            <td>Camera zoom out (2-levels)</td>
            <td>Z, Shift+Z</td>
        </tr>
        <tr>
            <td>Follow an Observer</td>
            <td>O</td>
        </tr>
        <tr>
            <td>Vision of the selected player<br>
            (permanent)</td>
            <td>W</td>
        </tr>
        <tr>
            <td>Vision of the selected player<br>
            (as long as button held down)</td>
            <td>V</td>
        </tr>
        <tr>
            <td>Follow currently selected unit</td>
            <td>C</td>
        </tr>
    </table>
    <br>
    <br>
    <br>
    <h3>Hotkeys the Game provides (editable in Game Options)</h3>
    <br>
    <div style="padding-left: 2em">
        Some hotkeys cannot be set in the in-game UI, but need to be defined in your hotkey file.<br>
        Your hotkey file can be found under:<br>
        C:\\Users\\Ahli\\Documents\\Heroes of the Storm\\Accounts\\1234567890\\Hotkeys<br>
        <br>
        Currently, only the Camera turning hotkeys are not exposed in the in-game UI.<br>
        The definition of the two hotkeys needs to be placed into the file's [Hotkeys] section as seen in the following example:
    </div>
    <br>
    <table>
        <tr>
        <td>
            [Settings]<br>
            Grid=1<br>
            <br>
            [Hotkeys]<br>
            CameraTurnLeft=Insert<br>
            CameraTurnRight=Delete<br>
            <br>
            [Commands]<br>
            <br>
            </td>
        </tr>
    </table>
    <br>
    <br>
    <br>
    <br>
    <br>
    <br>
    <br>
    <br>
    <br>
    <br>
    <br>
    <br>
    <br>
    <br>
    <br>
    <br>
    <br>
    <br>
    <br>
    <br>
    <br>
    <br>
    <br>
    <br>
    <br>
    <br>
    <br>
    <br>
</div>
`;
