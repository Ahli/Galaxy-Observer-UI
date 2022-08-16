import { Drawer, List, ListItem, ListItemIcon, ListItemText } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import HomeIcon from '@mui/icons-material/Home';
import { NavBarItem } from '../types/NavBarItem';

export const navBarWidth = 200;

export const Navbar = () => {
  const navigate = useNavigate();
  const itemslist = [
    { text: 'Home', icon: <HomeIcon />, onclick: () => navigate('/') },
    {
      text: 'AhliObs',
      icon: null,
      onClick: () => navigate('/ahliobs'),
    },
    {
      text: 'GameHeart',
      icon: null,
      onClick: () => navigate('/gameheart'),
    },
    {
      text: 'Pro2020 AhliMod',
      icon: null,
      onClick: () => navigate('/pro2020ahlimod'),
    },
  ] as Array<NavBarItem>;
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
        {itemslist.map((item /*, index*/) => {
          const { text, icon, onClick } = item;
          return (
            <ListItem button key={text} onClick={onClick}>
              <ListItemIcon>{icon}</ListItemIcon>
              <ListItemText primary={text} />
            </ListItem>
          );
        })}
      </List>
    </Drawer>
  );
};
