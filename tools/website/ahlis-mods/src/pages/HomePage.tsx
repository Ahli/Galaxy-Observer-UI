import { PostList } from '../components/PostList';
import { Post } from '../types/Post';
import { Card } from '@mui/material';
import { newsPosts } from '../content/_news/news';

export const HomePage = () => {
  const posts: Post[] = newsPosts;

  return (
    <Card sx={{ borderRadius: '0px', height: '100vh' }}>
      <PostList posts={posts} title='News' />
    </Card>
  );
};
