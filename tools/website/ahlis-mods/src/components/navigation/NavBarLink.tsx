import { ListItemButton, ListItemIcon, ListItemText } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { NavBarLinkData } from '../../types/NavBarItemData';

export type NavBarLinkProps = {
  linkData: NavBarLinkData;
  level: number;
};

export const NavBarLink = (props: NavBarLinkProps) => {
  const navigate = useNavigate();

  const { text, icon, target } = props.linkData;
  return (
    <ListItemButton onClick={() => navigate(target)}>
      <ListItemIcon>{icon}</ListItemIcon>
      <ListItemText primary={text} />
    </ListItemButton>
  );
};
