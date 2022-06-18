import { Link } from 'react-router-dom';
import { basename } from '../App';

export const Navbar = () => {
  return (
    <nav className='navbar'>
      <h1>Ahli&apos;s Mods</h1>
      <div className='links'>
        <Link to={basename}>Home</Link>
      </div>
    </nav>
  );
};
