import { Card, Link } from '@mui/material';
import { useNavigate } from 'react-router-dom';

export const NotFoundPage = () => {
  const navigate = useNavigate();
  return (
    <Card sx={{ borderRadius: '0px', height: '100vh' }}>
      <h2>404 Error</h2>
      <p>We cannot find that page!</p>
      <Link onClick={() => navigate('/')} sx={{ cursor: 'pointer' }}>
        Take me back to Home
      </Link>
    </Card>
  );
};
