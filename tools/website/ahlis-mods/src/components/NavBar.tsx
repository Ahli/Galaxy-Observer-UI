import { Link } from 'react-router-dom';

export const Navbar = () => {
  return (
    <nav className='navbar'>
      <h1>Ahli&apos;s Mods</h1>
      <div className='links'>
        <Link to='/'>Home</Link>
      </div>
    </nav>
  );
};
