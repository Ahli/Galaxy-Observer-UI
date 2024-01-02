import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';

export default function ScrollToTop() {
  const { pathname } = useLocation();

  useEffect(() => {
    document.documentElement.scrollTo({
      left: 0,
      top: 0,
      behavior: 'instant' as ScrollBehavior,
    //   behavior: 'instant', // Optional if you want to skip the scrolling animation
    });
  }, [pathname]); 

  return null;
}
