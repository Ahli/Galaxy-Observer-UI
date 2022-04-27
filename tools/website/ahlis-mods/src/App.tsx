// import logo from './logo.svg';
import './App.css';
import { Home } from './pages/Home';
import '@fontsource/roboto';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import { Navbar } from './components/NavBar';
import { PostList } from './components/PostList';
import { NotFound } from './components/NotFound';

function App() {
  return (
    <div className='App'>
      <Router>
        <div className='App'>
          <Navbar />
          <div className='content'>
            <Routes>
              <Route path='/' element={<Home />} />
              <Route path='/ahliobs/changelog/:id' element={<PostList title={''} posts={[]} />} />
              <Route path='*' element={<NotFound />} />
            </Routes>
          </div>
        </div>
      </Router>
    </div>
  );
}

export default App;
