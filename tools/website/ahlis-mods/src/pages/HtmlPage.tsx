import { Card } from '@mui/material';

export type HtmlPageProps = {
  title: string;
  contentHtml: string;
};

export const HtmlPage = (props: HtmlPageProps) => {
  return (
    <Card sx={{ borderRadius: '0px' }}>
      <h2>{props.title}</h2>
      <div dangerouslySetInnerHTML={{ __html: props.contentHtml }} />
    </Card>
  );
};
