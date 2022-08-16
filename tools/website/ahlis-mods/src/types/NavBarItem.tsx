import { ReactNode } from "react";

export class NavBarItem {
    text!: string;
    icon!: ReactNode|null;
    onClick!: () => void;
}
