import { ReactNode } from 'react';

export abstract class NavBarItemData {
  text!: string;
  icon!: ReactNode | null;

  constructor(text: string, icon: ReactNode | null) {
    this.text = text;
    this.icon = icon;
  }
}

export class NavBarLinkData extends NavBarItemData {
  target!: string;

  constructor(text: string, icon: ReactNode | null, target: string) {
    super(text, icon);
    this.target = target;
  }
}

export class NavBarCollapsibleData extends NavBarItemData {
  children!: Array<NavBarItemData>;
  collapsed = false;

  constructor(text: string, icon: ReactNode | null, children: Array<NavBarItemData>) {
    super(text, icon);
    this.children = children;
  }
}
