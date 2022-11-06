import { useState } from 'react';
import { Post } from '../../types/Post';
import { PostView } from './PostView';

export type PostListProps = {
  title: string;
  posts: Post[];
};

export const PostList = (props: PostListProps) => {
  const [postList] = useState(props);

  return (
    <>
      <h2>{postList.title}</h2>
      {postList.posts.map((post) => (
        <PostView post={post} key={post.id} />
      ))}
    </>
  );
};
