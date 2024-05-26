import Button from '@mui/material/Button';
import Card from '@mui/material/Card';
import CardActions from '@mui/material/CardActions';
import CardContent from '@mui/material/CardContent';
import CardMedia from '@mui/material/CardMedia';
import Typography from '@mui/material/Typography';
import { useState } from 'react';
import { Post, Changelog, Change } from '../../types/Post';
import EmblaCarousel from '../images/EmblaCarousel';

export type PostProps = {
  post: Post;
};

type Data = {
  post: Post;
  isChangelog: boolean;
};

export const PostView = (props: PostProps) => {
  const [data] = useState({ post: props.post, isChangelog: (props.post as Changelog) != undefined } as Data);
  return (
    <Card variant='outlined'>
      {renderImages(data.post.image)}
      <CardContent>
        <Typography gutterBottom variant='h5' component='div'>
          {data.post.title}
        </Typography>
        {data.post.createdAt && (
          <Typography variant='subtitle2' component='div'>
            {data.post.createdAt}
          </Typography>
        )}
        <div className='post-body'>
          {renderChangelog(data)}
          <Typography variant='body1' color='text.secondary'>
            {data.post.text}
          </Typography>
        </div>
      </CardContent>
      <CardActions>
        {data.post.download && (
          <Button size='small' color='primary' href={data.post.download}>
            Download
          </Button>
        )}
      </CardActions>
    </Card>
  );
};

function renderChangelog(data: Data) {
  const changes = (data.post as Changelog).changes;
  return <>{changes != undefined && renderChanges(changes)}</>;
}

function renderChanges(changes: (string | Change)[]) {
  return (
    <ul>
      {changes.map((change, index) => (
        <li key={index}>{renderChange(change)}</li>
      ))}
    </ul>
  );
}

function renderChange(change: string | Change) {
  return (
    <>
      {typeof change === 'string' ? (
        <>{change}</>
      ) : (
        <>
          {change.text}
          <ul>{renderChanges(change.children)}</ul>
        </>
      )}
    </>
  );
}

function renderImages(image?: string | Array<string>) {
  if (!image) {
    return null;
  }
  const isString = typeof image === 'string';
  if (isString || image.length == 1) {
    return <CardMedia component='img' loading='lazy' image={isString ? image : image[0]} alt='screenshot' />;
  }
  return <EmblaCarousel slides={Array.from(Array(image.length).keys())} images={image} options={{ loop: true }} />;
}
