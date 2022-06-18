// import logo from './logo.svg';
import './App.css';
import { Home } from './pages/Home';
import '@fontsource/roboto';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import { Navbar } from './components/NavBar';
import { NotFound } from './components/NotFound';

export const basename = '/Galaxy-Observer-UI';

function App() {
  return (
    <div className='App'>
      <Router>
        <Navbar />
        <div className='content'>
          <Routes>
            <Route path='/' element={<Navigate to={basename} replace />} />
            <Route path={basename} element={<Home />} />
            {/* <Route path={basename + '/ahliobs/changelog/:id'} element={<PostList title={''} posts={[]} />} /> */}
            <Route path='*' element={<NotFound />} />
          </Routes>
        </div>
      </Router>
    </div>
  );
}

export default App;
