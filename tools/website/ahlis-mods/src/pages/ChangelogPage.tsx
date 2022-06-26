import { PostList } from '../components/PostList';
import { Post } from '../types/Post';
import { Card } from '@mui/material';

export type ChangelogPageProps = {
  posts: Post[];
  title: string;
};

export const ChangelogPage = (props: ChangelogPageProps) => {
  return (
    <Card sx={{ borderRadius: '0px' }}>
      <PostList posts={props.posts} title={props.title} />
    </Card>
  );
};
