import { Card } from '@mui/material';

export type ShortcutsPageProps = {
  title: string;
  contentHtml: string;
};

export const ShortcutsPage = (props: ShortcutsPageProps) => {
  return (
    <Card sx={{ borderRadius: '0px' }}>
      <h2>{props.title}</h2>
      <div dangerouslySetInnerHTML={{ __html: props.contentHtml }} />
    </Card>
  );
};
