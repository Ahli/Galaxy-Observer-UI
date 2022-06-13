import { PostList } from '../components/PostList';
import { Change, Changelog, Post } from '../types/Post';
import ahliObs72 from '../assets/ahliObs/72.jpg';
import ahliObs73a from '../assets/ahliObs/73a.jpg';
import ahliObs73b from '../assets/ahliObs/73b.jpg';
import ahliObs74a from '../assets/ahliObs/74a.jpg';
import ahliObs74b from '../assets/ahliObs/74b.jpg';

export const Home = () => {
  const posts: Post[] = [
    {
      id: '74',
      title: '0.74 - fixes and polish',
      image: [ahliObs74a, ahliObs74b],
      createdAt: '2nd March 2022',
      download: 'https://www.dropbox.com/s/tv931liwl3a3mjs/AhliObs%200.74.StormInterface?dl=1',
      version: '0.73',
      date: '2nd March 2022',
      changes: [
        'fixed self healing and heal other stats in bottom panel referencing the damage taken values instead of its actual value',
        "fixed the colors during Tomb of the Spider Queen's Webweaver UI while being in red team's vision",
        "fixed appearance of kill/quest notifications and some map objective UIs when the selected unit's player vision button is held down (v shortcut)",
        'fixed the F ability button (e.g. Nuke) being visible on maps that do not use it',
        "fixed the F ability button's cooldown label being drawn next to the button instead of on top of it",
        'talent selection UI (accessible only in player vision via the usual hotkey) is now muted',
        "fix most issues with Garden of Terror's map mechanic UI elements regarding vision and replay time jumps",
        "re-implemented Murky and Thrall's hero UI to make it appear in everyone vision and only if that hero is the sole selected unit",
        "fixed Skull Golem health bars and its skull counter's appearance during red team vision on Haunted Mines",
      ],
    },
    {
      id: '73',
      title: '0.73 - charge and k',
      image: [ahliObs73a, ahliObs73b],
      createdAt: '23rd January 2022',
      download: 'https://www.dropbox.com/s/rbwt2ahfqfvgu35/AhliObs%200.73.StormInterface?dl=1',
      version: '0.73',
      date: '23rd January 2022',
      changes: [
        new Change('added a charge counter for heroics in the top panel supporting:', [
          'Brightwing, Lunara, Garrosh, Stukov, DVa, Rangaros, Zagara, Nova, Valla, Artanis, Muradin and Deckard',
        ]),
        new Change('changed bottom panel:', [
          'stats are now displayed showing values in k',
          'role icons from support to healer and from warrior to tank',
          'column title background image is now less transparent',
        ]),
        "added Keep icons to the Core's health bars in the top panel on Alterac Pass",
        "fixed blue team's heroic icons being displayed cut off in the top panel",
        'fixed ability button border images being cut off in the bottom left panel',
        "fixed activatable talent's ability button's image being drawn outside the border image in the bottom left panel",
        "fixed Garden of Terror's map mechanic UI's seed icons not always being displayed",
        'fixed a gap appearing in heto UIs for Fenix and Junkrat',
        "Leoric's ghost won't display a low health label anymore",
        new Change(
          'exposed a setting to the Settings Editor that allows hiding player names near captured camps and destroyed forts',
          ['please update the Settings Editor as this is a new feature']
        ),
      ],
    },
    {
      id: '72',
      title: '0.72 - fixes & improvements',
      image: ahliObs72,
      createdAt: 'Mar 25, 2021, 4:42 PM',
      updatedAt: 'Mar 25, 2021, 4:45 PM',
      download: 'https://www.dropbox.com/s/o8xuhpdveczmw1h/AhliObs%200.72.StormInterface?dl=1',
      version: '0.72',
      date: '25th March 2021',
      changes: [
        new Change("added a health and energy bar for DVa's Mech (big thanks to Spazzo for the research)", [
          'the portrait changes to indicate what state the health bar refers to',
          "Please note that clicking the portrait still won't select DVa's Mech",
        ]),
        'added globally visible stack counter to the status bars of Raynor, Orphea, Valeera and Alexstrasza (only Gift Of Life)',
        "added indicator on Alexstrasza's health bar for her level 1 talent Live and Let Live",
        "added Misha to Rexxar's top panel portrait similar to the Lost Vikings",
        new Change('bottom panel changes:', [
          'fixed bottom panel showing self-healing instead of damage-taken in the role column with the shield icon',
          'removed the glow of icons in the role column making it hard to read on high grapphic settings',
        ]),
        new Change(
          "kill log entries' background animation is now respecting colorblindness (it was caused by a bug in Blizz's UI code clearing all filters including the colorblind one)",
          [
            "Chen's suicide with Stagger is now correctly displayed with the animation and sword indicating the benefiting team",
            new Change('vision changes no longer causes wrong colors to be displayed in the kill log', [
              'Please note that multiple entries will still stack on vision change',
            ]),
          ]
        ),
        'altered the level number within the tooltip of talents in the bottom panel to improve performance',
        'fixed Varian level 4 heroic cooldowns appearing player colored',
        `removed the notification's default texts to avoid "[Notification Text]" to appear on screen`,
        'vision changes via shortcuts 1-0, E and O or using the pulldown are now silent',
        'removed buffs being displayed when selected unit info is hidden',
        new Change("exposed more settings to Ahli's settings editor (separate tool) to:", [
          'force hero/player names above healthbars',
          'hide textual kill notifications',
          'hide chat at game start',
          'hide unit info at the game start',
          'hide observer controls at game start in replays',
        ]),
        "the portrait and the unit details will now show Cho'Gall's name instead of the mixed player name",
        'added the Unit Type to the selected unit details panel',
        "fixed observer control's pulldown arrow facing downwards",
        'made the map objective UI of Alterac Pass more resistant to player vision changes',
      ],
    } as Changelog,
  ];
  const title = 'Changelog';

  return (
    <>
      <PostList posts={posts} title={title} />
    </>
  );
};
