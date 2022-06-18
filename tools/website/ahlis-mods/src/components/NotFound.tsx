import { Link } from 'react-router-dom';
import { basename } from '../App';

export const NotFound = () => {
  return (
    <div className='not-found'>
      <h2>404 Error</h2>
      <p>We cannot find that page!</p>
      <Link to={basename}>Take me back to Home</Link>
    </div>
  );
};
