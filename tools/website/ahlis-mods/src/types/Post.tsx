export class Post {
  id!: string;
  title!: string;
  createdAt!: string;
  text?: string;
  image?: string | Array<string>;
  download?: string;
  updatedAt?: string;
}

export class Changelog extends Post {
  version!: string;
  date?: string;
  changes!: Array<string | Change>;
}

export class Change {
  text!: string;
  children: Array<string | Change>;

  constructor(text: string, children: Array<string | Change>) {
    this.text = text;
    this.children = children;
  }
}
