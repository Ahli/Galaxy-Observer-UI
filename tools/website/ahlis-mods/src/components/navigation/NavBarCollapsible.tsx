import { ExpandLess, ExpandMore } from '@mui/icons-material';
import { Collapse, List, ListItemButton, ListItemIcon, ListItemText } from '@mui/material';

import { useState } from 'react';
import { NavBarCollapsibleData } from '../../types/NavBarItemData';
import { renderNavBarItemData } from './NavBar';

export type NavBarCollapsibleProps = {
  collapsibleData: NavBarCollapsibleData;
  level: number;
};

export const NavBarCollapsible = (props: NavBarCollapsibleProps) => {
  const [collapsed, setCollapsed] = useState(props.collapsibleData.collapsed);

  const alternateCollapsedFn = () => setCollapsed(!collapsed);

  const { text, icon, children } = props.collapsibleData;
  return (
    <>
      <ListItemButton onClick={alternateCollapsedFn}>
        <ListItemIcon>{icon}</ListItemIcon>
        <ListItemText primary={text} />
        {collapsed ? <ExpandLess /> : <ExpandMore />}
      </ListItemButton>
      <Collapse in={collapsed} timeout='auto' unmountOnExit>
        <List>
          {children.map((item, index) => {
            return (
              <li key={index} style={{ paddingLeft: 16 + 16 * props.level }}>
                {renderNavBarItemData(item, 1 + props.level)}
              </li>
            );
          })}
        </List>
      </Collapse>
    </>
  );
};
