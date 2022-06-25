// import logo from './logo.svg';
import './App.css';
import { Home } from './pages/Home';
import '@fontsource/roboto';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import { Navbar, navBarWidth } from './components/NavBar';
import { NotFound } from './components/NotFound';
import { ThemeProvider, createTheme } from '@mui/material/styles';

export const basename = '/Galaxy-Observer-UI';

const darkTheme = createTheme({
  palette: {
    mode: 'dark',
  },
});

function App() {
  return (
    <div className='App'>
      <ThemeProvider theme={darkTheme}>
        <Router>
          <Navbar />
          <div style={{ marginLeft: navBarWidth + 'px' }}>
            <Routes>
              <Route path='/' element={<Navigate to={basename} replace />} />
              <Route path={basename} element={<Home />} />
              {/* <Route path={basename + '/ahliobs/changelog/:id'} element={<PostList title={''} posts={[]} />} /> */}
              <Route path='*' element={<NotFound />} />
            </Routes>
          </div>
        </Router>
      </ThemeProvider>
    </div>
  );
}

export default App;
