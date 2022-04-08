import { PostList } from '../components/PostList';
import { Change, Changelog, Post } from '../types/Post';
import ahliObs72 from '../assets/ahliObs/72.jpg';

export const Home = () => {
  const posts: Post[] = [
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
    {
      id: 'test-2',
      title: 'Post 2',
      text: 'test test test 2',
      createdAt: 'test',
    },
  ];
  const title = 'Changelog';

  return (
    <>
      <PostList posts={posts} title={title} />
    </>
  );
};
