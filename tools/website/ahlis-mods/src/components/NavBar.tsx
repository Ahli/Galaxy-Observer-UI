import { Drawer, List, ListItem, ListItemIcon, ListItemText } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { basename } from '../App';
import HomeIcon from '@mui/icons-material/Home';

export const navBarWidth = 200;

export const Navbar = () => {
  const navigate = useNavigate();
  const itemslist = [
    { text: 'Home', icon: <HomeIcon />, onclick: () => navigate(basename) },
    {
      text: 'AhliObs',
      icon: null,
      onclick: () => navigate(basename + '/ahliobs'),
    },
    {
      text: 'GameHeart',
      icon: null,
      onclick: () => navigate(basename + '/gameheart'),
    },
  ];
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
          const { text, icon, onclick } = item;
          return (
            <ListItem button key={text} onClick={onclick}>
              <ListItemIcon>{icon}</ListItemIcon>
              <ListItemText primary={text} />
            </ListItem>
          );
        })}
      </List>
    </Drawer>
  );
};
