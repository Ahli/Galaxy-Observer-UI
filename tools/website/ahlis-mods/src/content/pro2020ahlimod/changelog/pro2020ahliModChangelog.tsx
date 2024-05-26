import { Change, Changelog, Post } from '../../../types/Post';
import pro2020_10 from '../../../assets/pro2020ahliMod/pro2020ahliMod_10.avif';
import pro2020_11 from '../../../assets/pro2020ahliMod/pro2020ahliMod_11.avif';
import pro2020_12 from '../../../assets/pro2020ahliMod/pro2020ahliMod_12.avif';

export const pro2020AhliModChangelog: Post[] = [
  {
    id: '12',
    title: '1.2',
    image: pro2020_12,
    createdAt: '5th Januaray 2023',
    download: 'https://www.dropbox.com/s/1efcfpgb7m9fk9n/Pro2020ahliMod_1.2.SC2Interface?dl=1',
    version: '1.2',
    date: '5th Januaray 2023',
    changes: [
      'added support for AhliObs extension mods (Battle Report, Production Tab)',
      'replaced advantage icons with the resource income values per player',
      'integrated the Battle Report dialog into the system that makes dialogs hide each other',
      'added configurable hotkey for BattleReport (Shift+B) and list it in the message log screen',
    ],
  } as Changelog,
  {
    id: '11',
    title: '1.1',
    image: pro2020_11,
    createdAt: '26th June 2022',
    download: 'https://www.dropbox.com/s/g47yvccechvmiqn/Pro2020ahliMod_1.1.SC2Interface?dl=1',
    version: '1.1',
    date: '26th June 2022',
    changes: [
      new Change('added support for HomeStoryCup XX extension mod:', [
        'Resources Gathered Graph on NumPad4',
        'Resources Lost Difference Graph on NumPad3',
        'Production Tab of the Leaderpanel will show a Chrono Boost indicator',
      ]),
      "reverted shortcuts of graphs back to NumPad1 and NumPad2. Use Ahli's Settings Editor to customize these",
    ],
  } as Changelog,
  {
    id: '10',
    title: '1.0',
    image: pro2020_10,
    createdAt: '18th May 2022',
    download: 'https://www.dropbox.com/s/3hwuxn9cbilr09j/Pro2020ahliMod_1.0.SC2Interface?dl=1',
    version: '1.0',
    date: '18th May 2022',
    changes: [
      new Change('modified graphs:', [
        'changed shortcuts from NumPad1,NumPad2 to Shift+I and Shift+A',
        "added graph shortcuts to the message log's manual",
        'stylized appearance',
      ]),
      'dialogs now auto-hide when other dialogs are opened',
    ],
  } as Changelog,
];
