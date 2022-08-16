import { Change, Changelog, Post } from '../../../types/Post';
import pro2020_11 from '../../../assets/pro2020ahliMod/pro2020ahliMod_11.jpg';
import pro2020_10 from '../../../assets/pro2020ahliMod/pro2020ahliMod_10.jpg';

export const pro2020AhliModChangelog: Post[] = [
  {
    id: '11',
    title: '1.1',
    image: [pro2020_11],
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
      "reverted shortcuts of graphs back to NumPad1 and NumPad2. Use AHli's Settings Editor to customize these",
    ],
  } as Changelog,
  {
    id: '10',
    title: '1.0',
    image: [pro2020_10],
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
