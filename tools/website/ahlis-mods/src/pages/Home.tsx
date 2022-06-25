import { PostList } from '../components/PostList';
import { Post } from '../types/Post';
import { ahliObsChangelog } from '../content/ahliObs/changelog/ahliObsChangelog';
import { Card } from '@mui/material';

export const Home = () => {
  const posts: Post[] = ahliObsChangelog;
  const title = 'Changelog';

  return (
    <Card sx={{ borderRadius: '0px' }}>
      <PostList posts={posts} title={title} />
    </Card>
  );
};
