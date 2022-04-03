import { useState } from 'react'

export type PostProps = {
  title: string;
  text: string;
  image?: string;
};

export const Post = (props: PostProps) => {
  const [post] = useState(props);

  return (
    <>
        <h1>{post.title}</h1>
        {post.image &&
          post.image}
        {post.text}
    </>
  );
}
