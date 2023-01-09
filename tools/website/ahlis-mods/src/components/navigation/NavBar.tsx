import { Drawer, List } from '@mui/material';
import HomeIcon from '@mui/icons-material/Home';
import NoteAltOutlinedIcon from '@mui/icons-material/NoteAltOutlined';
import KeyboardOutlinedIcon from '@mui/icons-material/KeyboardOutlined';
import { NavBarCollapsibleData, NavBarItemData, NavBarLinkData } from '../../types/NavBarItemData';
import { NavBarLink } from './NavBarLink';
import { NavBarCollapsible } from './NavBarCollapsible';
import sc2Img from '../../assets/common/sc2.png';
import heroesImg from '../../assets/common/heroes.png';

export const navBarWidth = 200;

const sc2icon = <img width='40' height='40' src={sc2Img} />;
const heroesIcon = <img width='40' height='40' src={heroesImg} />;

const navbarItems = [
  new NavBarLinkData('News', <HomeIcon />, '/'),
  new NavBarCollapsibleData('AhliObs', heroesIcon, [
    new NavBarLinkData('Changelog', <NoteAltOutlinedIcon />, '/ahliobs/changelog'),
    new NavBarLinkData('Shortcuts', <KeyboardOutlinedIcon />, '/ahliobs/shortcuts'),
  ]),
  new NavBarLinkData('GameHeart', sc2icon, '/gameheart'),
  new NavBarLinkData('Pro2020 AhliMod', sc2icon, '/pro2020ahlimod'),
  new NavBarCollapsibleData('Heroes of the Storm', heroesIcon, [
    new NavBarLinkData('Settings', <NoteAltOutlinedIcon />, '/heroes/settings'),
  ]),
  new NavBarCollapsibleData('StarCraft II', sc2icon, [
    new NavBarLinkData('Logos.SC2Mod', <NoteAltOutlinedIcon />, '/sc2/logosmod'),
  ]),
] as Array<NavBarItemData>;

export const Navbar = () => {
  return (
    <Drawer
      variant='permanent'
      anchor='left'
      // PaperProps={{
      //   sx: {
      //     color: '#ffffff',
      //     backgroundColor: '#40585a',
      //   },
      // }}
      sx={{
        width: navBarWidth,
        flexShrink: 0,
        '& .MuiDrawer-paper': {
          width: navBarWidth,
          boxSizing: 'border-box',
        },
      }}>
      <List>
        {navbarItems.map((item, index) => {
          return <li key={index}>{renderNavBarItemData(item, 0)}</li>;
        })}
      </List>
    </Drawer>
  );
};

export function renderNavBarItemData(item: NavBarItemData, level: number) {
  if (item instanceof NavBarLinkData) {
    return <NavBarLink linkData={item as NavBarLinkData} level={level} />;
  }

  if (item instanceof NavBarCollapsibleData) {
    return <NavBarCollapsible collapsibleData={item as NavBarCollapsibleData} level={level} />;
  }

  throw new Error('Unhandled NavBarItemData type.');
}
