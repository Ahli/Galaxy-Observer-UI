import './App.css';
import { HomePage } from './pages/HomePage';
import '@fontsource/roboto';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import { Navbar, navBarWidth } from './components/NavBar';
import { NotFound } from './components/NotFound';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { ChangelogPage } from './pages/ChangelogPage';
import { ahliObsChangelog } from './content/ahliObs/changelog/ahliObsChangelog';
import { gh3changelog } from './content/gameheart/v3/changelog/gh3changelog';
import { pro2020AhliModChangelog } from './content/pro2020/changelog/pro2020ahliModChangelog';

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
              <Route path={basename} element={<HomePage />} />
              <Route path={basename + '/ahliobs'} element={<Navigate to={basename + '/ahliobs/changelog'} replace />} />
              <Route
                path={basename + '/ahliobs/changelog'}
                element={<ChangelogPage posts={ahliObsChangelog} title='AhliObs - Changelog' />}
              />
              <Route
                path={basename + '/gameheart'}
                element={<Navigate to={basename + '/gameheart/v3/changelog'} replace />}
              />
              <Route
                path={basename + '/gameheart/v3/changelog'}
                element={<ChangelogPage posts={gh3changelog} title='GameHeart 3 - Changelog' />}
              />
              <Route
                path={basename + '/pro2020ahlimod'}
                element={<Navigate to={basename + '/pro2020ahlimod/changelog'} replace />}
              />
              <Route
                path={basename + '/pro2020ahlimod/changelog'}
                element={<ChangelogPage posts={pro2020AhliModChangelog} title='Pro2020 AhliMod - Changelog' />}
              />
              <Route path='*' element={<NotFound />} />
            </Routes>
          </div>
        </Router>
      </ThemeProvider>
    </div>
  );
}

export default App;
